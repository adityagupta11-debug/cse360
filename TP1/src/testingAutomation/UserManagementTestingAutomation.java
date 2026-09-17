package testingAutomation;

import java.util.List;

import database.Database;
import entityClasses.User;
import guiDeleteUser.ControllerDeleteUser;
import guiListUsers.ControllerListUsers;

/*******
 * <p> Title: UserManagementTestingAutomation Class. </p>
 *
 * <p> Description: A semi-automated test driver, in the style of the
 * PasswordEvaluationTestingAutomation class, that exercises the Admin "list all users" and
 * "delete a user" user stories against a private in-memory H2 database.  Each test case states
 * what is being tested, performs the action through the same methods the GUI uses, and compares
 * the observed result to the expected result.  The production database file is never touched.
 * </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00	2026-09-17	Initial version covering listing, formatting, and deletion
 *
 */
public class UserManagementTestingAutomation {

	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	// A private in-memory database so these tests never alter the production database
	static Database theDatabase = new Database("jdbc:h2:mem:userTests;DB_CLOSE_DELAY=-1");

	/*
	 * This mainline displays a header to the console, performs a sequence of test cases, and
	 * then displays a footer with a summary of the results
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________________________________________");
		System.out.println("\nUser Management Testing Automation");

		theDatabase.connectToDatabase();

		// Establish a known starting state: two Admins and two other users
		theDatabase.register(new User("adminOne", "Aa!15678", "Ada", "B", "Lovelace", "Ada",
				"admin1@asu.edu", true, false, false, false));
		theDatabase.register(new User("adminTwo", "Aa!15678", "", "", "", "",
				"admin2@asu.edu", true, false, false, false));
		theDatabase.register(new User("contribOne", "Aa!15678", "Grace", "", "Hopper", "",
				"contributor@asu.edu", false, true, false, false));
		theDatabase.register(new User("viewerOne", "Aa!15678", "", "", "", "",
				"viewer@asu.edu", false, false, true, false));

		/************** Start of the test cases **************/

		// Test 1: The list contains every user that was registered
		List<User> users = theDatabase.getAllUsers();
		performTestCase(1, "getAllUsers returns all four registered users",
				users.size() == 4, true);

		// Test 2: The list is returned in username order so the table is predictable
		performTestCase(2, "The list is sorted by username (adminOne is first)",
				users.get(0).getUserName().compareTo("adminOne") == 0, true);

		// Test 3: Passwords are never copied out of the database into the list
		boolean noPasswords = true;
		for (User u : users) if (u.getPassword() != null && !u.getPassword().isEmpty())
			noPasswords = false;
		performTestCase(3, "No password is included in the listed users", noPasswords, true);

		// Test 4: The name column joins the name parts that are present
		performTestCase(4, "fullName builds \"Ada B Lovelace\"",
				ControllerListUsers.fullName(users.get(0)).compareTo("Ada B Lovelace") == 0, true);

		// Test 5: A user with no name parts produces an empty name rather than spaces or null
		performTestCase(5, "fullName of a user with no names is empty",
				ControllerListUsers.fullName(users.get(1)).isEmpty(), true);

		// Test 6: The roles column lists the roles a user plays
		performTestCase(6, "rolesAsText reports \"Admin\" for adminOne",
				ControllerListUsers.rolesAsText(users.get(0)).compareTo("Admin") == 0, true);

		// Test 7: A user playing more than one role has both listed, in the fixed order
		theDatabase.updateUserRole("contribOne", "Viewer", "true");
		User contribOne = findUser("contribOne");
		performTestCase(7, "rolesAsText reports \"Contributor, Viewer\" for contribOne",
				ControllerListUsers.rolesAsText(contribOne).compareTo("Contributor, Viewer") == 0,
				true);

		// Test 8: A user playing no role is reported as "(none)" rather than an empty column
		theDatabase.register(new User("noRoles", "Aa!15678", "", "", "", "", "none@asu.edu",
				false, false, false, false));
		performTestCase(8, "rolesAsText reports \"(none)\" for a user with no roles",
				ControllerListUsers.rolesAsText(findUser("noRoles")).compareTo("(none)") == 0,
				true);

		// Test 9: A null attribute is displayed as an empty string, not the text "null"
		performTestCase(9, "blankIfNull turns null into an empty string",
				ControllerListUsers.blankIfNull(null).isEmpty(), true);

		// Test 10: The policy refuses a deletion when no user has been selected
		performTestCase(10, "Delete policy refuses the \"<Select a User>\" placeholder",
				ControllerDeleteUser.checkDeletePolicy(theDatabase, "adminOne",
						"<Select a User>").isEmpty(), false);

		// Test 11: The policy refuses an Admin deleting the account they are using
		performTestCase(11, "Delete policy refuses deleting your own account",
				ControllerDeleteUser.checkDeletePolicy(theDatabase, "adminOne",
						"adminOne").isEmpty(), false);

		// Test 12: The policy refuses a username that is not in the database
		performTestCase(12, "Delete policy refuses a user who does not exist",
				ControllerDeleteUser.checkDeletePolicy(theDatabase, "adminOne",
						"noSuchUser").isEmpty(), false);

		// Test 13: The policy accepts deleting an ordinary user
		performTestCase(13, "Delete policy accepts deleting viewerOne",
				ControllerDeleteUser.checkDeletePolicy(theDatabase, "adminOne",
						"viewerOne").isEmpty(), true);

		// Test 14: Deleting an ordinary user removes exactly that user
		int before = theDatabase.getNumberOfUsers();
		performTestCase(14, "Deleting viewerOne succeeds and removes one row",
				theDatabase.deleteUser("viewerOne")
				&& theDatabase.getNumberOfUsers() == before - 1
				&& !theDatabase.doesUserExist("viewerOne"), true);

		// Test 15: Deleting a user who is already gone is refused instead of crashing
		performTestCase(15, "Deleting viewerOne a second time is refused",
				theDatabase.deleteUser("viewerOne"), false);

		// Test 16: With two Admins, one of them may be deleted
		performTestCase(16, "Deleting adminTwo while adminOne is also an Admin",
				theDatabase.deleteUser("adminTwo") && theDatabase.getNumberOfAdmins() == 1, true);

		// Test 17: The last remaining Admin may never be deleted
		performTestCase(17, "Deleting the last remaining Admin (must be refused)",
				theDatabase.deleteUser("adminOne"), false);

		// Test 18: After the refusal, that Admin is still in the database
		performTestCase(18, "The last Admin still exists after the refusal",
				theDatabase.doesUserExist("adminOne") && theDatabase.getNumberOfAdmins() == 1,
				true);

		// Test 19: The policy also refuses the last Admin, so the GUI explains it before asking
		performTestCase(19, "Delete policy refuses the last remaining Admin",
				ControllerDeleteUser.checkDeletePolicy(theDatabase, "contribOne",
						"adminOne").isEmpty(), false);

		// Test 20: A 10,000 character username (hacker-style long input) is refused, not a crash
		performTestCase(20, "Deleting a user named by 10,000 characters",
				theDatabase.deleteUser("X".repeat(10000)), false);

		// Test 21: A null username is refused rather than throwing an exception
		performTestCase(21, "Deleting a null username", theDatabase.deleteUser(null), false);

		/************** End of the test cases **************/

		theDatabase.closeConnection();

		System.out.println("______________________________________________________________________");
		System.out.println("\nNumber of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	/*
	 * A helper that locates one user in the list returned by the database
	 */
	private static User findUser(String username) {
		for (User u : theDatabase.getAllUsers())
			if (u.getUserName().compareTo(username) == 0) return u;
		return new User(username, "", "", "", "", "", "", false, false, false, false);
	}

	/*
	 * This method performs a single test case: it compares the observed result to the expected
	 * result and reports the outcome in the same manner as the password evaluation testbed
	 */
	private static void performTestCase(int testCase, String description, boolean observed,
			boolean expected) {
		System.out.println("______________________________________________________________________");
		System.out.println("\nTest case: " + testCase);
		System.out.println("Testing: " + description);
		System.out.println("Expected: " + expected + "   Observed: " + observed);
		if (observed == expected) {
			System.out.println("***Success*** The test case produced the expected result.\n");
			numPassed++;
		} else {
			System.out.println("***Failure*** The test case did NOT produce the expected result.\n");
			numFailed++;
		}
	}
}
