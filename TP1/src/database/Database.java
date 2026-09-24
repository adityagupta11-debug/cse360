package database;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import entityClasses.Invitation;
import entityClasses.User;

/*******
 * <p> Title: Database Class. </p>
 * 
 * <p> Description: This is an in-memory database built on H2.  Detailed documentation of H2 can
 * be found at https://www.h2database.com/html/main.html (Click on "PDF (2MB)" on the l3ft side
 * of the page under the heading "Reference" for a PDF of 438 pages.)  This class leverages H2
 * and provides numerous special supporting methods.
 * </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @author Kanish Garg (K.G.) - authenticated deletion policy and transactional safeguards
 * 
 * @version 2.00		2025-04-29 Updated and expanded from the version produce by Pravalika 
 * 							Mukkiri and Ishwarya Hidkimath Basavaraj
 * @version 2.01		2025-12-17 Minor updates for Spring 2026
 * @version 2.02		2026-09-16 Named roles, invitation deadlines, last-Admin guard (A.G., agupt515)
 * @version 2.03		2026-09-17 One-time passwords, delete user, list users, manage invitations
 * 							(A.G., agupt545)
 * @version 2.04		2026-09-21 Atomically consume accepted one-time passwords and report
 * 							password-update failures (Vishwam)
 * @version 2.05		2026-09-23 Deletion-block attribution documented only; no behavior changed
 * 							(Vishwam)
 */

/*
 * The Database class is responsible for establishing and managing the connection to the database,
 * and performing operations such as user registration, login validation, handling invitation 
 * codes, and numerous other database related functions.
 */
public class Database {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  
	
	/** The default number of hours an invitation remains valid when no explicit deadline is given */
	public static final int DEFAULT_INVITATION_HOURS = 24;

	// The URL actually used by this instance (the default is DB_URL; automated tests may use an
	// in-memory database so they never touch the production database file)
	private String dbUrl = System.getProperty("cse360.db.url", DB_URL);

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	//  Shared variables used within this class
	private Integer authenticatedUserId; // Independent of the account-details cache.
	private Connection connection = null;		// Singleton to access the database 
	private Statement statement = null;			// The H2 Statement is used to construct queries
	
	// These are the easily accessible attributes of the currently logged-in user
	// This is only useful for single user applications
	private String currentUsername;
	private String currentPassword;
	private String currentFirstName;
	private String currentMiddleName;
	private String currentLastName;
	private String currentPreferredFirstName;
	private String currentEmailAddress;
	private boolean currentAdminRole;
	private boolean currentContributorRole;
	private boolean currentViewerRole;
	private boolean currentCuratorRole;

	/*******
	 * <p> Method: Database </p>
	 * 
	 * <p> Description: The default constructor used to establish this singleton object.</p>
	 * 
	 */
	
	public Database () {
		
	}
	
	
/*******
 * <p> Method: Database(String dbUrl) </p>
 * 
 * <p> Description: This constructor allows the caller to specify an alternate JDBC URL.  It is
 * used by the automated testing classes to run against a private in-memory H2 database (e.g.,
 * "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1") so the tests never alter the production database.</p>
 * 
 * @param dbUrl is the JDBC URL of the H2 database to be used by this instance
 * 
 */
	public Database (String dbUrl) {
		this.dbUrl = dbUrl;
	}
	
	
/*******
 * <p> Method: connectToDatabase </p>
 * 
 * <p> Description: Used to establish the in-memory instance of the H2 database from secondary
 *		storage.</p>
 *
 * @throws SQLException when the DriverManager is unable to establish a connection
 * 
 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			connection = DriverManager.getConnection(dbUrl, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
			removeExpiredInvitations();	// Purge any invitations whose deadline has passed
		} catch (ClassNotFoundException e) {
			throw new SQLException("H2 JDBC driver is missing", e);
		}
	}

	
/*******
 * <p> Method: createTables </p>
 * 
 * <p> Description: Used to create new instances of the two database tables used by this class.</p>
 * 
 */
	private void createTables() throws SQLException {
		// Create the user database
		String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "firstName VARCHAR(255), "
				+ "middleName VARCHAR(255), "
				+ "lastName VARCHAR (255), "
				+ "preferredFirstName VARCHAR(255), "
				+ "emailAddress VARCHAR(255), "
				+ "adminRole BOOL DEFAULT FALSE, "
				+ "contributorRole BOOL DEFAULT FALSE, "
				+ "viewerRole BOOL DEFAULT FALSE, "
				+ "curatorRole BOOL DEFAULT FALSE)";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	    		+ "emailAddress VARCHAR(255), "
	            + "role VARCHAR(20), "
	            + "deadline TIMESTAMP)";
	    statement.execute(invitationCodesTable);
	    
	    // If an older database file (created before the named roles and the invitation deadline
	    // were introduced) is being reused, add the missing columns so the queries still work.
	    statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS contributorRole BOOL DEFAULT FALSE");
	    statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS viewerRole BOOL DEFAULT FALSE");
	    statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS curatorRole BOOL DEFAULT FALSE");
	    statement.execute("ALTER TABLE InvitationCodes ADD COLUMN IF NOT EXISTS deadline TIMESTAMP");
	    // Role names such as "Contributor" exceed the original VARCHAR(10) width
	    statement.execute("ALTER TABLE InvitationCodes ALTER COLUMN role SET DATA TYPE VARCHAR(20)");

	    // The Admin "one-time password" user story stores a temporary password and the deadline
	    // after which it can no longer be used.  Both are NULL when no one-time password is set.
	    statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS oneTimePassword VARCHAR(255)");
	    statement.execute("ALTER TABLE userDB ADD COLUMN IF NOT EXISTS oneTimePasswordDeadline TIMESTAMP");
	}


/*******
 * <p> Method: isDatabaseEmpty </p>
 * 
 * <p> Description: If the user database has no rows, true is returned, else false.</p>
 * 
 * @return true if the database is empty, else it returns false
 * 
 */
	public boolean isDatabaseEmpty() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count") == 0;
			}
		}  catch (SQLException e) {
	        return false;
	    }
		return true;
	}
	
	
/*******
 * <p> Method: getNumberOfUsers </p>
 * 
 * <p> Description: Returns an integer .of the number of users currently in the user database. </p>
 * 
 * @return the number of user records in the database.
 * 
 */
	public int getNumberOfUsers() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
	        return 0;
	    }
		return 0;
	}

/*******
 * <p> Method: register(User user) </p>
 * 
 * <p> Description: Creates a new row in the database using the user parameter. </p>
 * 
 * @throws SQLException when there is an issue creating the SQL command or executing it.
 * 
 * @param user specifies a user object to be added to the database.
 * 
 */
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO userDB (userName, password, firstName, middleName, "
				+ "lastName, preferredFirstName, emailAddress, adminRole, contributorRole, "
				+ "viewerRole, curatorRole) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			currentUsername = user.getUserName();
			pstmt.setString(1, currentUsername);
			
			currentPassword = user.getPassword();
			pstmt.setString(2, currentPassword);
			
			currentFirstName = user.getFirstName();
			pstmt.setString(3, currentFirstName);
			
			currentMiddleName = user.getMiddleName();			
			pstmt.setString(4, currentMiddleName);
			
			currentLastName = user.getLastName();
			pstmt.setString(5, currentLastName);
			
			currentPreferredFirstName = user.getPreferredFirstName();
			pstmt.setString(6, currentPreferredFirstName);
			
			currentEmailAddress = user.getEmailAddress();
			pstmt.setString(7, currentEmailAddress);
			
			currentAdminRole = user.getAdminRole();
			pstmt.setBoolean(8, currentAdminRole);
			
			currentContributorRole = user.getContributorRole();
			pstmt.setBoolean(9, currentContributorRole);
			
			currentViewerRole = user.getViewerRole();
			pstmt.setBoolean(10, currentViewerRole);
			
			currentCuratorRole = user.getCuratorRole();
			pstmt.setBoolean(11, currentCuratorRole);
			
			pstmt.executeUpdate();
		}
		
	}
	
/*******
 *  <p> Method: List getUserList() </p>
 *  
 *  <P> Description: Generate an List of Strings, one for each user in the database,
 *  starting with "&lt;Select a User&gt;" at the start of the list. </p>
 *  
 *  @return a list of userNames found in the database.
 */
	public List<String> getUserList () {
		List<String> userList = new ArrayList<String>();
		userList.add("<Select a User>");
		String query = "SELECT userName FROM userDB";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userList.add(rs.getString("userName"));
			}
		} catch (SQLException e) {
	        return null;
	    }
//		System.out.println(userList);
		return userList;
	}

/*******
 * <p> Method: boolean loginAdmin(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Admin role.
 * 
 * @return true if the specified user has been logged in as an Admin else false.
 * 
 */
	public boolean loginAdmin(User user){
		// Validates an admin user's login credentials so the user can login in as an Admin.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "adminRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();	// If a row is returned, rs.next() will return true		
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
/*******
 * <p> Method: boolean loginContributor(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Contributor role.
 * 
 * @return true if the specified user has been logged in as an Contributor else false.
 * 
 */
	public boolean loginContributor(User user) {
		// Validates a contributor user's login credentials.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "contributorRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: boolean loginViewer(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username, password, and role
	 * 		is the same as a row in the table for the username, password, and role. </p>
	 * 
	 * @param user specifies the specific user that should be logged in playing the Reviewer role.
	 * 
	 * @return true if the specified user has been logged in as an Contributor else false.
	 * 
	 */
	// Validates a viewer user's login credentials.
	public boolean loginViewer(User user) {
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "viewerRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean loginCurator(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username, password, and
	 * Curator role is the same as a row in the table for the username, password, and role.</p>
	 * 
	 * @param user specifies the specific user that should be logged in playing the Curator role.
	 * 
	 * @return true if the specified user has been logged in as a Curator else false.
	 * 
	 */
	// Validates a curator user's login credentials.
	public boolean loginCurator(User user) {
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "curatorRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean doesUserExist(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username is  in the table. </p>
	 * 
	 * @param userName specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return true if the specified user is in the table else false.
	 * 
	 */
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	
	/*******
	 * <p> Method: int getNumberOfRoles(User user) </p>
	 * 
	 * <p> Description: Determine the number of roles a specified user plays. </p>
	 * 
	 * @param user specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return the number of roles this user plays (0 - 5).
	 * 
	 */	
	// Get the number of roles that this user plays
	public int getNumberOfRoles (User user) {
		int numberOfRoles = 0;
		if (user.getAdminRole()) numberOfRoles++;
		if (user.getContributorRole()) numberOfRoles++;
		if (user.getViewerRole()) numberOfRoles++;
		if (user.getCuratorRole()) numberOfRoles++;
		return numberOfRoles;
	}	

	
	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role) </p>
	 * 
	 * <p> Description: Given an email address and a roles, this method establishes and invitation
	 * code and adds a record to the InvitationCodes table.  When the invitation code is used, the
	 * stored email address is used to establish the new user and the record is removed from the
	 * table.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * 
	 * @param role specified the role that this new user will play.
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account.
	 * 
	 */
	// Generates a new invitation code (valid for DEFAULT_INVITATION_HOURS) and inserts it into
	// the database.
	public String generateInvitationCode(String emailAddress, String role) {
		return generateInvitationCode(emailAddress, role, 
				LocalDateTime.now().plusHours(DEFAULT_INVITATION_HOURS));
	}
	
	
	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role, 
	 * 		LocalDateTime deadline) </p>
	 * 
	 * <p> Description: Given an email address, a role, and a deadline, this method establishes
	 * an invitation code that is only valid until the deadline.  Once the deadline has passed, the
	 * code can no longer be used to set up an account and the invitation will be purged.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * 
	 * @param role specified the role that this new user will play.
	 * 
	 * @param deadline specifies the date and time after which the invitation is no longer valid.
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account,
	 * or an empty string if the deadline is null or not in the future.
	 * 
	 */
	// Generates a new invitation code with an explicit deadline and inserts it into the database.
	public String generateInvitationCode(String emailAddress, String role, 
			LocalDateTime deadline) {
		// Do not create an invitation that is already expired (or has no deadline at all)
		if (deadline == null || !deadline.isAfter(LocalDateTime.now())) return "";
		
	    String code = UUID.randomUUID().toString().substring(0, 6); // Generate a random 6-character code
	    String query = "INSERT INTO InvitationCodes (code, emailaddress, role, deadline) "
	    		+ "VALUES (?, ?, ?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, emailAddress);
	        pstmt.setString(3, role);
	        pstmt.setTimestamp(4, Timestamp.valueOf(deadline));
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return "";
	    }
	    return code;
	}
	
	
	/*******
	 * <p> Method: LocalDateTime getInvitationDeadline(String code) </p>
	 * 
	 * <p> Description: Get the deadline associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the deadline for the code, or null if the code is not in the table.
	 * 
	 */
	// Obtain the deadline associated with an invitation code.
	public LocalDateTime getInvitationDeadline(String code) {
	    String query = "SELECT deadline FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	Timestamp ts = rs.getTimestamp("deadline");
	        	if (ts != null) return ts.toLocalDateTime();
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	
	/*******
	 * <p> Method: boolean isInvitationExpired(String code) </p>
	 * 
	 * <p> Description: Determine whether an invitation code's deadline has passed.  A code that
	 * is not in the table (or has no deadline) is reported as expired so it cannot be used.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return true if the deadline has passed (or the code is unknown), else false.
	 * 
	 */
	// Determine if an invitation code has expired.
	public boolean isInvitationExpired(String code) {
		LocalDateTime deadline = getInvitationDeadline(code);
		if (deadline == null) return true;
		return !deadline.isAfter(LocalDateTime.now());
	}
	
	
	/*******
	 * <p> Method: int removeExpiredInvitations() </p>
	 * 
	 * <p> Description: Remove every invitation whose deadline has passed so that expired codes
	 * can never be used and are not counted as outstanding.</p>
	 *  
	 * @return the number of invitations that were removed.
	 * 
	 */
	// Purge all expired invitations from the table.
	public int removeExpiredInvitations() {
		String query = "DELETE FROM InvitationCodes WHERE deadline IS NULL OR deadline <= ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	
	/*******
	 * <p> Method: int getNumberOfInvitations() </p>
	 * 
	 * <p> Description: Determine the number of outstanding invitations in the table.</p>
	 *  
	 * @return the number of invitations in the table.
	 * 
	 */
	// Number of outstanding (not yet expired) invitations in the database
	public int getNumberOfInvitations() {
		removeExpiredInvitations();		// Expired invitations are not outstanding
		String query = "SELECT COUNT(*) AS count FROM InvitationCodes";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return 0;
	}
	
	
	/*******
	 * <p> Method: boolean emailaddressHasBeenUsed(String emailAddress) </p>
	 * 
	 * <p> Description: Determine if an email address has been user to establish a user.</p>
	 * 
	 * @param emailAddress is a string that identifies a user in the table
	 *  
	 * @return true if the email address is in the table, else return false.
	 * 
	 */
	// Check to see if an email address is already in the database
	public boolean emailaddressHasBeenUsed(String emailAddress) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        ResultSet rs = pstmt.executeQuery();
	 //     System.out.println(rs);
	        if (rs.next()) {
	            // Mark the code as used
	        	return rs.getInt("count")>0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
	/*******
	 * <p> Method: String getRoleGivenAnInvitationCode(String code) </p>
	 * 
	 * <p> Description: Get the role associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the role for the code or an empty string.
	 * 
	 */
	// Obtain the roles associated with an invitation code.
	public String getRoleGivenAnInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	
	/*******
	 * <p> Method: String getEmailAddressUsingCode (String code ) </p>
	 * 
	 * <p> Description: Get the email addressed associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the email address for the code or an empty string.
	 * 
	 */
	// For a given invitation code, return the associated email address of an empty string
	public String getEmailAddressUsingCode (String code ) {
	    String query = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("emailAddress");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return "";
	}
	
	
	/*******
	 * <p> Method: void removeInvitationAfterUse(String code) </p>
	 * 
	 * <p> Description: Remove an invitation record once it is used.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 */
	// Remove an invitation using an email address once the user account has been setup
	public void removeInvitationAfterUse(String code) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	int counter = rs.getInt(1);
	            // Only do the remove if the code is still in the invitation table
	        	if (counter > 0) {
        			query = "DELETE FROM InvitationCodes WHERE code = ?";
	        		try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
	        			pstmt2.setString(1, code);
	        			pstmt2.executeUpdate();
	        		}catch (SQLException e) {
	        	        e.printStackTrace();
	        	    }
	        	}
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return;
	}
	
	/*******
	 * <p> Method: boolean updatePassword(String username, String password) </p>
	 * 
	 * <p> Description: Update the password of a user given that user's username and the new
	 * 		password.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param password is the new password for the user
	 *
	 * @return true only when exactly one user record was updated
	 *  
	 */
	// update the password: Joshua Luther
	// Vishwam, return the database result so reset screens never claim an unsaved password worked.
	public boolean updatePassword(String username, String password) {
		if (username == null || username.isBlank() || password == null || password.isEmpty()) {
			return false;
		}
	    String query = "UPDATE userDB SET password = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, password);
	        pstmt.setString(2, username);
	        boolean updated = pstmt.executeUpdate() == 1;
	        if (updated && username.equals(currentUsername)) currentPassword = password;
	        return updated;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	/*******
	 * <p> Method: boolean updateUserName(String oldUsername, String newUsername) </p>
	 * 
	 * <p> Description: Update a user's username given the user's current username and the new
	 * 		username. Since userName is unique, this checks the new username is not already taken
	 * 		before attempting the update.</p>
	 * 
	 * @param oldUsername is the current username of the user
	 *  
	 * @param newUsername is the new username for the user
	 * 
	 * @return true if the update was successful, else false (e.g., the new username is already taken)
	 *  
	 */
	// update the username: Joshua Luther
	public boolean updateUserName(String oldUsername, String newUsername) {
	    // Refuse the update if the new username is already in use by someone else
	    if (doesUserExist(newUsername)) return false;
	    
	    String query = "UPDATE userDB SET userName = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newUsername);
	        pstmt.setString(2, oldUsername);
	        pstmt.executeUpdate();
	        currentUsername = newUsername;
	        return true;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	/*******
	 * <p> Method: String getFirstName(String username) </p>
	 * 
	 * <p> Description: Get the first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the first name of a user given that user's username 
	 *  
	 */
	// Get the First Name
	public String getFirstName(String username) {
		String query = "SELECT firstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	

	/*******
	 * <p> Method: void updateFirstName(String username, String firstName) </p>
	 * 
	 * <p> Description: Update the first name of a user given that user's username and the new
	 *		first name.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @param firstName is the new first name for the user
	 *  
	 */
	// update the first name
	public void updateFirstName(String username, String firstName) {
	    String query = "UPDATE userDB SET firstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, firstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentFirstName = firstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	/*******
	 * <p> Method: String getMiddleName(String username) </p>
	 * 
	 * <p> Description: Get the middle name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the middle name of a user given that user's username 
	 *  
	 */
	// get the middle name
	public String getMiddleName(String username) {
		String query = "SELECT MiddleName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("middleName"); // Return the middle name if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}

	
	/*******
	 * <p> Method: void updateMiddleName(String username, String middleName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param middleName is the new middle name for the user
	 *  
	 */
	// update the middle name
	public void updateMiddleName(String username, String middleName) {
	    String query = "UPDATE userDB SET middleName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, middleName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentMiddleName = middleName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getLastName(String username) </p>
	 * 
	 * <p> Description: Get the last name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the last name of a user given that user's username 
	 *  
	 */
	// get he last name
	public String getLastName(String username) {
		String query = "SELECT LastName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("lastName"); // Return last name role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateLastName(String username, String lastName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param lastName is the new last name for the user
	 *  
	 */
	// update the last name
	public void updateLastName(String username, String lastName) {
	    String query = "UPDATE userDB SET lastName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, lastName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentLastName = lastName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getPreferredFirstName(String username) </p>
	 * 
	 * <p> Description: Get the preferred first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the preferred first name of a user given that user's username 
	 *  
	 */
	// get the preferred first name
	public String getPreferredFirstName(String username) {
		String query = "SELECT preferredFirstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the preferred first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updatePreferredFirstName(String username, String preferredFirstName) </p>
	 * 
	 * <p> Description: Update the preferred first name of a user given that user's username and
	 * 		the new preferred first name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param preferredFirstName is the new preferred first name for the user
	 *  
	 */
	// update the preferred first name of the user
	public void updatePreferredFirstName(String username, String preferredFirstName) {
	    String query = "UPDATE userDB SET preferredFirstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, preferredFirstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPreferredFirstName = preferredFirstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getEmailAddress(String username) </p>
	 * 
	 * <p> Description: Get the email address of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the email address of a user given that user's username 
	 *  
	 */
	// get the email address
	public String getEmailAddress(String username) {
		String query = "SELECT emailAddress FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("emailAddress"); // Return the email address if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateEmailAddress(String username, String emailAddress) </p>
	 * 
	 * <p> Description: Update the email address name of a user given that user's username and
	 * 		the new email address.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param emailAddress is the new preferred first name for the user
	 *  
	 */
	// update the email address
	public void updateEmailAddress(String username, String emailAddress) {
	    String query = "UPDATE userDB SET emailAddress = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentEmailAddress = emailAddress;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: boolean getUserAccountDetails(String username) </p>
	 * 
	 * <p> Description: Get all the attributes of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return true of the get is successful, else false
	 *  
	 */
	// get the attributes for a specified user
	public boolean getUserAccountDetails(String username) {
		String query = "SELECT * FROM userDB WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();			
	    	if (!rs.next()) return false;		// No such user
	    	currentUsername = rs.getString("userName");
	    	currentPassword = rs.getString("password");
	    	currentFirstName = rs.getString("firstName");
	    	currentMiddleName = rs.getString("middleName");
	    	currentLastName = rs.getString("lastName");
	    	currentPreferredFirstName = rs.getString("preferredFirstName");
	    	currentEmailAddress = rs.getString("emailAddress");
	    	currentAdminRole = rs.getBoolean("adminRole");
	    	currentContributorRole = rs.getBoolean("contributorRole");
	    	currentViewerRole = rs.getBoolean("viewerRole");
	    	currentCuratorRole = rs.getBoolean("curatorRole");
			return true;
	    } catch (SQLException e) {
			return false;
	    }
	}
	
	
	/*******
	 * <p> Method: boolean updateUserRole(String username, String role, String value) </p>
	 * 
	 * <p> Description: Update a specified role for a specified user's and set and update all the
	 * 		current user attributes.  The request is rejected (false is returned) when the value is
	 * 		not "true" or "false", when the role is not one of "Admin", "Contributor", "Viewer", or "Curator", or
	 * 		when the request would remove the Admin role from the only remaining Admin.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param role is string that specifies the role to update ("Admin", "Contributor", "Viewer", or "Curator")
	 * 
	 * @param value is the string "true" or "false" for the role
	 * 
	 * @return true if the update was successful, else false
	 *  
	 */
	// Update a users role
	public boolean updateUserRole(String username, String role, String value) {
		// Reject anything other than the two legal values so bad input cannot reach the database
		if (value.compareTo("true") != 0 && value.compareTo("false") != 0) return false;
		
		if (role.compareTo("Admin") == 0) {
			// The system must always keep at least one Admin, so refuse to remove the Admin role
			// from the only remaining Admin user.
			if (value.compareTo("false") == 0 && userIsAdmin(username) && getNumberOfAdmins() <= 1)
				return false;
			
			String query = "UPDATE userDB SET adminRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentAdminRole = true;
				else
					currentAdminRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Contributor") == 0) {
			String query = "UPDATE userDB SET contributorRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentContributorRole = true;
				else
					currentContributorRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Viewer") == 0) {
			String query = "UPDATE userDB SET viewerRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentViewerRole = true;
				else
					currentViewerRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Curator") == 0) {
			String query = "UPDATE userDB SET curatorRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentCuratorRole = true;
				else
					currentCuratorRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: int getNumberOfAdmins() </p>
	 * 
	 * <p> Description: Determine how many users currently play the Admin role.</p>
	 * 
	 * @return the number of users with the Admin role
	 *  
	 */
	// Count the users who play the Admin role
	public int getNumberOfAdmins() {
		String query = "SELECT COUNT(*) AS count FROM userDB WHERE adminRole = TRUE";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return 0;
	}
	
	
	/*******
	 * <p> Method: boolean userIsAdmin(String username) </p>
	 * 
	 * <p> Description: Determine whether the specified user currently plays the Admin role.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return true if the user exists and plays the Admin role, else false
	 *  
	 */
	// Determine if a specific user plays the Admin role
	public boolean userIsAdmin(String username) {
		String query = "SELECT adminRole FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getBoolean("adminRole");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean userHasRole(String username, String role) </p>
	 * 
	 * <p> Description: Determine whether the specified user currently plays the specified role,
	 * reading the answer directly from the database rather than the cached current user.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @param role is one of "Admin", "Contributor", "Viewer", or "Curator"
	 * 
	 * @return true if the user exists and plays that role, else false
	 *  
	 */
	// Determine if a specific user plays a specific role
	public boolean userHasRole(String username, String role) {
		String column;
		if (role.compareTo("Admin") == 0) column = "adminRole";
		else if (role.compareTo("Contributor") == 0) column = "contributorRole";
		else if (role.compareTo("Viewer") == 0) column = "viewerRole";
		else if (role.compareTo("Curator") == 0) column = "curatorRole";
		else return false;
		String query = "SELECT " + column + " FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) return rs.getBoolean(column);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: {@code List<User> getAllUsers()} </p>
	 *
	 * <p> Description: Build a list of every user in the database so the Admin "List Users" page
	 * can display each user's username, name, email address, and roles.  Passwords are never
	 * copied into the returned objects.</p>
	 *
	 * @return a list of User objects, one per row in userDB, in username order (empty on error)
	 *
	 */
	// Build a list of all users for the Admin
	public List<User> getAllUsers() {
		List<User> users = new ArrayList<User>();
		String query = "SELECT * FROM userDB ORDER BY userName";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				users.add(new User(rs.getString("userName"), "", rs.getString("firstName"),
						rs.getString("middleName"), rs.getString("lastName"),
						rs.getString("preferredFirstName"), rs.getString("emailAddress"),
						rs.getBoolean("adminRole"), rs.getBoolean("contributorRole"),
						rs.getBoolean("viewerRole"), rs.getBoolean("curatorRole")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}


	/*******
	 * <p> Method: boolean deleteUser(String username) </p>
	 *
	 * <p> Description: Remove a user from the database.  The request is refused (false is
	 * returned) when the user does not exist or when the user is the only remaining Admin, since
	 * the system must always keep at least one Admin.  An authenticated Admin session is also required. The caller (the GUI) is responsible for
	 * asking the Admin "Are you sure?" before invoking this method.</p>
	 *
	 * @param username is the username of the user to be deleted
	 *
	 * @return true if the user was deleted, else false
	 *
	 */
	// Delete a user, protecting the last Admin
	public boolean deleteUser(String username) {
        // Compatibility entry point for existing callers. Authorization is enforced in the
        // same transaction as the new ID-based path; callers must first authenticate.
        if (username == null || username.isBlank() || username.length() > 32) return false;
        try (PreparedStatement query = connection.prepareStatement("SELECT id FROM userDB WHERE userName = ?")) {
            query.setString(1, username);
            try (ResultSet row = query.executeQuery()) {
                return row.next() && deleteUserAccount(row.getInt(1), true) == DeletionResult.DELETED;
            }
        } catch (SQLException failure) { return false; }
    }


	/*******
	 * <p> Method: boolean setOneTimePassword(String username, String oneTimePassword,
	 * 		LocalDateTime deadline) </p>
	 *
	 * <p> Description: Store a one-time password for a user together with the deadline after
	 * which it can no longer be used.  The request is refused when the user does not exist, the
	 * password is empty, or the deadline is missing or not in the future.  Setting a new one-time
	 * password replaces any earlier one.</p>
	 *
	 * @param username is the username of the user
	 *
	 * @param oneTimePassword is the temporary password the Admin will give the user
	 *
	 * @param deadline is the date and time after which the one-time password is no longer valid
	 *
	 * @return true if the one-time password was stored, else false
	 *
	 */
	// Store a one-time password and its deadline for a user
	public boolean setOneTimePassword(String username, String oneTimePassword,
			LocalDateTime deadline) {
		if (username == null || oneTimePassword == null || oneTimePassword.isEmpty()) return false;
		if (deadline == null || !deadline.isAfter(LocalDateTime.now())) return false;
		if (!doesUserExist(username)) return false;
		String query = "UPDATE userDB SET oneTimePassword = ?, oneTimePasswordDeadline = ? "
				+ "WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, oneTimePassword);
			pstmt.setTimestamp(2, Timestamp.valueOf(deadline));
			pstmt.setString(3, username);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	/*******
	 * <p> Method: LocalDateTime getOneTimePasswordDeadline(String username) </p>
	 *
	 * <p> Description: Get the deadline of the user's one-time password.</p>
	 *
	 * @param username is the username of the user
	 *
	 * @return the deadline, or null when the user has no one-time password
	 *
	 */
	// Obtain the deadline of a user's one-time password
	public LocalDateTime getOneTimePasswordDeadline(String username) {
		String query = "SELECT oneTimePasswordDeadline FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				Timestamp ts = rs.getTimestamp("oneTimePasswordDeadline");
				if (ts != null) return ts.toLocalDateTime();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}


	/*******
	 * <p> Method: boolean hasActiveOneTimePassword(String username) </p>
	 *
	 * <p> Description: Determine whether the user currently has a one-time password whose
	 * deadline has not yet passed.</p>
	 *
	 * @param username is the username of the user
	 *
	 * @return true if an unexpired one-time password is stored for this user, else false
	 *
	 */
	// Determine whether a user has a live one-time password
	public boolean hasActiveOneTimePassword(String username) {
		String query = "SELECT oneTimePassword, oneTimePasswordDeadline FROM userDB "
				+ "WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				String otp = rs.getString("oneTimePassword");
				Timestamp ts = rs.getTimestamp("oneTimePasswordDeadline");
				if (otp == null || otp.isEmpty() || ts == null) return false;
				return ts.toLocalDateTime().isAfter(LocalDateTime.now());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	/*******
	 * <p> Method: boolean loginWithOneTimePassword(String username, String oneTimePassword) </p>
	 *
	 * <p> Description: Check whether the supplied text matches the user's stored one-time
	 * password and that the deadline has not passed.  A successful match atomically clears the
	 * password and deadline in the same database statement, so even simultaneous requests cannot
	 * redeem it twice.  An expired one-time password is removed as a side effect.</p>
	 *
	 * @param username is the username of the user
	 *
	 * @param oneTimePassword is the text the user typed into the password field
	 *
	 * @return true if the one-time password matched, was still valid, and was consumed; else false
	 *
	 */
	// Check a one-time password at login time
	// Vishwam, consume the credential inside one conditional UPDATE to guarantee true one-time use.
	public boolean loginWithOneTimePassword(String username, String oneTimePassword) {
		if (username == null || username.isBlank() || username.length() > 32
				|| oneTimePassword == null || oneTimePassword.isEmpty()
				|| oneTimePassword.length() > 64) return false;
		String query = "UPDATE userDB SET oneTimePassword = NULL, "
				+ "oneTimePasswordDeadline = NULL WHERE userName = ? AND oneTimePassword = ? "
				+ "AND oneTimePasswordDeadline > CURRENT_TIMESTAMP";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.setString(2, oneTimePassword);
			if (pstmt.executeUpdate() == 1) return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		// Vishwam, purge only an actually expired credential; never erase a concurrently reissued one.
		String purge = "UPDATE userDB SET oneTimePassword = NULL, "
				+ "oneTimePasswordDeadline = NULL WHERE userName = ? "
				+ "AND oneTimePasswordDeadline IS NOT NULL "
				+ "AND oneTimePasswordDeadline <= CURRENT_TIMESTAMP";
		try (PreparedStatement pstmt = connection.prepareStatement(purge)) {
			pstmt.setString(1, username);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	/*******
	 * <p> Method: void clearOneTimePassword(String username) </p>
	 *
	 * <p> Description: Remove the one-time password (and its deadline) from the user's record so
	 * it cannot be used again.  Successful login already consumes a one-time password atomically;
	 * this method remains available for explicit administrative cleanup.</p>
	 *
	 * @param username is the username of the user
	 *
	 */
	// Remove a user's one-time password
	public void clearOneTimePassword(String username) {
		String query = "UPDATE userDB SET oneTimePassword = NULL, oneTimePasswordDeadline = NULL "
				+ "WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/*******
	 * <p> Method: {@code List<Invitation> getInvitationList()} </p>
	 *
	 * <p> Description: Build a list of every invitation currently in the InvitationCodes table
	 * (including any that have expired but not yet been purged) so the Admin "Manage
	 * Invitations" page can display them.</p>
	 *
	 * @return a list of Invitation objects in deadline order (empty on error)
	 *
	 */
	// Build a list of all invitations for the Admin
	public List<Invitation> getInvitationList() {
		List<Invitation> invitations = new ArrayList<Invitation>();
		String query = "SELECT code, emailAddress, role, deadline FROM InvitationCodes "
				+ "ORDER BY deadline";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Timestamp ts = rs.getTimestamp("deadline");
				invitations.add(new Invitation(rs.getString("code"), rs.getString("emailAddress"),
						rs.getString("role"), ts == null ? null : ts.toLocalDateTime()));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return invitations;
	}


	/*******
	 * <p> Method: boolean removeInvitation(String code) </p>
	 *
	 * <p> Description: Revoke an invitation before it is used or expires.  Once removed, the code
	 * can no longer be used to set up an account and the email address may be invited again.</p>
	 *
	 * @param code is the six character invitation code to be revoked
	 *
	 * @return true if an invitation was removed, else false (e.g., no such code)
	 *
	 */
	// Revoke an outstanding invitation
	public boolean removeInvitation(String code) {
		if (code == null || code.isEmpty()) return false;
		String query = "DELETE FROM InvitationCodes WHERE code = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, code);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	// K.G.: authenticated-session identity, deletion policy, and transactional safeguards begin here.
	// Vishwam, attribution audit only: clarified ownership of this existing block; no logic changed.
	/** K.G. deletion outcomes; the GUI never reports success after a database error. */
	public enum DeletionResult {
		DELETED, CANCELLED, NO_SELECTION, UNAUTHORIZED, SELF_PROTECTED,
		NOT_FOUND, LAST_ADMIN_PROTECTED
	}

	/** Stable record identity prevents a stale selection from deleting a replacement user. */
	public record DeletionCandidate(int id, String username) {
		@Override public String toString() { return username; }
	}

	/**
	 * Authenticates this single-user application session. Merely fetching another
	 * user's details never changes this identity. Existing password storage is retained.
	 * @return true only for a matching, existing account
	 */
	public synchronized boolean authenticateSession(String username, String password) {
		authenticatedUserId = null;
		// Match the existing username limit and shared password limit before SQL use.
		if (username == null || password == null || username.isEmpty()
				|| username.length() > 32 || password.length() > 64) return false;
		try (PreparedStatement query = connection.prepareStatement(
				"SELECT id FROM userDB WHERE userName = ? AND password = ?")) {
			query.setString(1, username);
			query.setString(2, password);
			try (ResultSet result = query.executeQuery()) {
				if (!result.next()) return false;
				authenticatedUserId = result.getInt("id");
				return true;
			}
		} catch (SQLException failure) { return false; }
	}

	/** Clears authorization when the login screen is shown or the connection closes. */
	public synchronized void clearAuthenticatedSession() { authenticatedUserId = null; }

	/** Checks the stored role, not role flags supplied by a view or an editable User object. */
	public synchronized boolean isAuthenticatedAdmin() throws SQLException {
		if (authenticatedUserId == null) return false;
		try (PreparedStatement query = connection.prepareStatement(
				"SELECT adminRole FROM userDB WHERE id = ?")) {
			query.setInt(1, authenticatedUserId);
			try (ResultSet result = query.executeQuery()) {
				return result.next() && result.getBoolean(1);
			}
		}
	}

	/** Retrieves account choices for deletion without changing the account-details cache. */
	public synchronized List<DeletionCandidate> getDeletionCandidates() throws SQLException {
		if (!isAuthenticatedAdmin()) throw new SQLException("An administrator session is required");
		List<DeletionCandidate> users = new ArrayList<>();
		try (PreparedStatement query = connection.prepareStatement(
				"SELECT id, userName FROM userDB ORDER BY userName");
				ResultSet result = query.executeQuery()) {
			while (result.next()) users.add(new DeletionCandidate(result.getInt(1), result.getString(2)));
		}
		return users;
	}

	/**
	 * Removes an account only after explicit Yes. No/cancel performs no database work.
	 * This implementation uses hard deletion, matching the supplied schema (no active flag).
	 * All admin rows are locked in ID order before checking authorization and the target.
	 * Therefore two administrators cannot concurrently delete one another and leave no admin.
	 * The operation commits only its own transaction; failures roll back and propagate.
	 *
	 * @param targetId database identity captured when the account was selected
	 * @param confirmed true only when the confirmation dialog returned Yes
	 * @return the precise outcome for display and automated testing
	 * @throws SQLException if persistence fails; no success should be displayed
	 */
	public synchronized DeletionResult deleteUserAccount(int targetId, boolean confirmed)
			throws SQLException {
		if (!confirmed) return DeletionResult.CANCELLED;
		if (targetId <= 0) return DeletionResult.NO_SELECTION;
		if (authenticatedUserId == null) return DeletionResult.UNAUTHORIZED;
		if (!connection.getAutoCommit()) throw new SQLException("Deletion requires its own transaction");
		connection.setAutoCommit(false);
		try {
			int adminCount = 0;
			boolean actorIsAdmin = false;
			try (PreparedStatement lock = connection.prepareStatement(
					"SELECT id FROM userDB WHERE adminRole = TRUE ORDER BY id FOR UPDATE");
					ResultSet admins = lock.executeQuery()) {
				while (admins.next()) {
					adminCount++;
					if (admins.getInt(1) == authenticatedUserId) actorIsAdmin = true;
				}
			}
			DeletionResult outcome;
			if (!actorIsAdmin) outcome = DeletionResult.UNAUTHORIZED;
			else if (targetId == authenticatedUserId) outcome = DeletionResult.SELF_PROTECTED;
			else {
				try (PreparedStatement target = connection.prepareStatement(
						"SELECT adminRole FROM userDB WHERE id = ? FOR UPDATE")) {
					target.setInt(1, targetId);
					try (ResultSet row = target.executeQuery()) {
						if (!row.next()) outcome = DeletionResult.NOT_FOUND;
						else if (row.getBoolean(1) && adminCount <= 1)
							outcome = DeletionResult.LAST_ADMIN_PROTECTED;
						else {
							try (PreparedStatement remove = connection.prepareStatement(
									"DELETE FROM userDB WHERE id = ?")) {
								remove.setInt(1, targetId);
								outcome = remove.executeUpdate() == 1
										? DeletionResult.DELETED : DeletionResult.NOT_FOUND;
							}
						}
					}
				}
			}
			connection.commit();
			return outcome;
		} catch (SQLException | RuntimeException failure) {
			try { connection.rollback(); } catch (SQLException rollback) { failure.addSuppressed(rollback); }
			throw failure;
		} finally { connection.setAutoCommit(true); }
	}

	// Attribute getters for the current user
	/*******
	 * <p> Method: String getCurrentUsername() </p>
	 * 
	 * <p> Description: Get the current user's username.</p>
	 * 
	 * @return the username value is returned
	 *  
	 */
	public String getCurrentUsername() { return currentUsername;};

	
	/*******
	 * <p> Method: String getCurrentPassword() </p>
	 * 
	 * <p> Description: Get the current user's password.</p>
	 * 
	 * @return the password value is returned
	 *  
	 */
	public String getCurrentPassword() { return currentPassword;};

	
	/*******
	 * <p> Method: String getCurrentFirstName() </p>
	 * 
	 * <p> Description: Get the current user's first name.</p>
	 * 
	 * @return the first name value is returned
	 *  
	 */
	public String getCurrentFirstName() { return currentFirstName;};

	
	/*******
	 * <p> Method: String getCurrentMiddleName() </p>
	 * 
	 * <p> Description: Get the current user's middle name.</p>
	 * 
	 * @return the middle name value is returned
	 *  
	 */
	public String getCurrentMiddleName() { return currentMiddleName;};

	
	/*******
	 * <p> Method: String getCurrentLastName() </p>
	 * 
	 * <p> Description: Get the current user's last name.</p>
	 * 
	 * @return the last name value is returned
	 *  
	 */
	public String getCurrentLastName() { return currentLastName;};

	
	/*******
	 * <p> Method: String getCurrentPreferredFirstName( </p>
	 * 
	 * <p> Description: Get the current user's preferred first name.</p>
	 * 
	 * @return the preferred first name value is returned
	 *  
	 */
	public String getCurrentPreferredFirstName() { return currentPreferredFirstName;};

	
	/*******
	 * <p> Method: String getCurrentEmailAddress() </p>
	 * 
	 * <p> Description: Get the current user's email address name.</p>
	 * 
	 * @return the email address value is returned
	 *  
	 */
	public String getCurrentEmailAddress() { return currentEmailAddress;};

	
	/*******
	 * <p> Method: boolean getCurrentAdminRole() </p>
	 * 
	 * <p> Description: Get the current user's Admin role attribute.</p>
	 * 
	 * @return true if this user plays an Admin role, else false
	 *  
	 */
	public boolean getCurrentAdminRole() { return currentAdminRole;};

	
	/*******
	 * <p> Method: boolean getCurrentContributorRole() </p>
	 * 
	 * <p> Description: Get the current user's Contributor role attribute.</p>
	 * 
	 * @return true if this user plays a Contributor role, else false
	 *  
	 */
	public boolean getCurrentContributorRole() { return currentContributorRole;};

	
	/*******
	 * <p> Method: boolean getCurrentViewerRole() </p>
	 * 
	 * <p> Description: Get the current user's Viewer role attribute.</p>
	 * 
	 * @return true if this user plays a Viewer role, else false
	 *  
	 */
	public boolean getCurrentViewerRole() { return currentViewerRole;};
	
	
	/*******
	 * <p> Method: boolean getCurrentCuratorRole() </p>
	 * 
	 * <p> Description: Get the current user's Curator role attribute.</p>
	 * 
	 * @return true if this user plays a Curator role, else false
	 *  
	 */
	public boolean getCurrentCuratorRole() { return currentCuratorRole;};

	
	/*******
	 * <p> Debugging method</p>
	 * 
	 * <p> Description: Debugging method that dumps the database of the console.</p>
	 * 
	 * @throws SQLException if there is an issues accessing the database.
	 * 
	 */
	// Dumps the database.
	public void dump() throws SQLException {
		String query = "SELECT * FROM userDB";
		ResultSet resultSet = statement.executeQuery(query);
		ResultSetMetaData meta = resultSet.getMetaData();
		while (resultSet.next()) {
		for (int i = 0; i < meta.getColumnCount(); i++) {
		System.out.println(
		meta.getColumnLabel(i + 1) + ": " +
				resultSet.getString(i + 1));
		}
		System.out.println();
		}
		resultSet.close();
	}


	/*******
	 * <p> Method: void closeConnection()</p>
	 * 
	 * <p> Description: Closes the database statement and connection.</p>
	 * 
	 */
	// Closes the database statement and connection.
	public void closeConnection() {
		clearAuthenticatedSession();
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
}
