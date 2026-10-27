package guiListUsers;

import java.util.List;

import javafx.collections.FXCollections;
import database.Database;
import entityClasses.User;

/*******
 * <p> Title: ControllerListUsers Class. </p>
 *
 * <p> Description: The controller for the List Users page.  It fetches the users from the
 * database, formats the values shown in the table, and handles the Return, Logout, and Quit
 * buttons.  The formatting helpers are public and static so the automated tests can exercise
 * them without a GUI.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version
 *
 */

public class ControllerListUsers {

	/**
	 * Default constructor is not used.
	 */
	public ControllerListUsers() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;


	/**********
	 * <p> Method: refreshUserList() </p>
	 *
	 * <p> Description: Protected method that re-reads every user from the database and places
	 * them into the table, then updates the count shown at the top of the page.</p>
	 */
	protected static void refreshUserList() {
		List<User> users = theDatabase.getAllUsers();
		ViewListUsers.table_Users.setItems(FXCollections.observableArrayList(users));
		ViewListUsers.label_NumberOfUsers.setText("Number of users: " + users.size());
	}


	/**********
	 * <p> Method: String blankIfNull(String s) </p>
	 *
	 * <p> Description: Attributes that have never been set are stored as null (or as empty
	 * strings).  The table shows both as blank.</p>
	 *
	 * @param s	the attribute value
	 *
	 * @return the value, or an empty string if it was null
	 */
	public static String blankIfNull(String s) {
		return (s == null) ? "" : s;
	}


	/**********
	 * <p> Method: String fullName(User u) </p>
	 *
	 * <p> Description: Build "First Middle Last" from whichever name parts the user has set,
	 * with single spaces between the parts that are present.</p>
	 *
	 * @param u	the user
	 *
	 * @return the user's full name, or an empty string when no name parts have been set
	 */
	public static String fullName(User u) {
		StringBuilder sb = new StringBuilder();
		String[] parts = { u.getFirstName(), u.getMiddleName(), u.getLastName() };
		for (String p : parts) {
			if (p != null && !p.isEmpty()) {
				if (sb.length() > 0) sb.append(' ');
				sb.append(p);
			}
		}
		return sb.toString();
	}


	/**********
	 * <p> Method: String rolesAsText(User u) </p>
	 *
	 * <p> Description: Build a comma-separated list of the roles the user plays, in the fixed
	 * order Admin, Contributor, Viewer, Curator.</p>
	 *
	 * @param u	the user
	 *
	 * @return the list of roles, or "(none)" when the user plays no role
	 */
	public static String rolesAsText(User u) {
		StringBuilder sb = new StringBuilder();
		if (u.getAdminRole()) sb.append("Admin");
		if (u.getContributorRole()) sb.append(sb.length() > 0 ? ", " : "").append("Contributor");
		if (u.getViewerRole()) sb.append(sb.length() > 0 ? ", " : "").append("Viewer");
		if (u.getCuratorRole()) sb.append(sb.length() > 0 ? ", " : "").append("Curator");
		return sb.length() == 0 ? "(none)" : sb.toString();
	}


	/**********
	 * <p> Method: performReturn() </p>
	 *
	 * <p> Description: Return the Admin to the Admin Home page.</p>
	 */
	protected static void performReturn() {
		guiAdminHome.ViewAdminHome.displayAdminHome(ViewListUsers.theStage, ViewListUsers.theUser);
	}


	/**********
	 * <p> Method: performLogout() </p>
	 *
	 * <p> Description: Log this user out of the system and return to the login page.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewListUsers.theStage);
	}


	/**********
	 * <p> Method: performQuit() </p>
	 *
	 * <p> Description: Gracefully terminate the execution of the program.</p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
