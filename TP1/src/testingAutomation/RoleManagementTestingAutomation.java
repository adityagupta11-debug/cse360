package testingAutomation;

import database.Database;
import entityClasses.User;

/*******
 * <p> Title: RoleManagementTestingAutomation Class. </p>
 * 
 * <p> Description: A semi-automated test driver, in the style of the
 * PasswordEvaluationTestingAutomation class, that exercises the Admin "add/remove roles" user
 * stories against a private in-memory H2 database.  Each test case states what is being tested,
 * performs the action through the same Database methods the GUI uses, and compares the observed
 * result to the expected result.  The production database file is never touched. </p>
 * 
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 * 
 * @author A.G. (agupt515)
 * 
 * @version 1.00	2026-09-16	Initial version covering add, remove, and the last-Admin guard
 * @version 1.01	2026-09-16	Roles renamed to Contributor/Viewer and Curator added
 * 
 */
public class RoleManagementTestingAutomation {
	
	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests
	
	// A private in-memory database so these tests never alter the production database
	static Database theDatabase = new Database("jdbc:h2:mem:roleTests;DB_CLOSE_DELAY=-1");

	/*
	 * This mainline displays a header to the console, performs a sequence of test cases, and
	 * then displays a footer with a summary of the results
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________________________________________");
		System.out.println("\nRole Management Testing Automation");
		
		theDatabase.connectToDatabase();
		
		// Establish a known starting state: one Admin and one Contributor
		theDatabase.register(new User("adminOne", "Aa!15678", "", "", "", "", 
				"admin@asu.edu", true, false, false, false));
		theDatabase.register(new User("contribOne", "Aa!15678", "", "", "", "", 
				"contributor@asu.edu", false, true, false, false));
		
		/************** Start of the test cases **************/
		
		// Test 1: Adding the Viewer role to a Contributor succeeds and is visible in the database
		performTestCase(1, "Add Viewer role to contribOne", 
				theDatabase.updateUserRole("contribOne", "Viewer", "true") 
				&& theDatabase.userHasRole("contribOne", "Viewer"), true);
		
		// Test 2: The Contributor role is still present after adding Viewer (roles are independent)
		performTestCase(2, "contribOne still has the Contributor role", 
				theDatabase.userHasRole("contribOne", "Contributor"), true);
		
		// Test 3: Removing the Viewer role from contribOne succeeds and is visible in the database
		performTestCase(3, "Remove Viewer role from contribOne", 
				theDatabase.updateUserRole("contribOne", "Viewer", "false") 
				&& !theDatabase.userHasRole("contribOne", "Viewer"), true);
		
		// Test 4: Adding the Admin role to a Contributor succeeds and the Admin count becomes two
		performTestCase(4, "Add Admin role to contribOne (Admin count becomes 2)", 
				theDatabase.updateUserRole("contribOne", "Admin", "true") 
				&& theDatabase.getNumberOfAdmins() == 2, true);
		
		// Test 5: With two Admins, removing Admin from adminOne is allowed
		performTestCase(5, "Remove Admin from adminOne while contribOne is also Admin", 
				theDatabase.updateUserRole("adminOne", "Admin", "false") 
				&& !theDatabase.userIsAdmin("adminOne"), true);
		
		// Test 6: contribOne is now the only Admin, so removing that Admin role must be refused
		performTestCase(6, "Remove Admin from the last remaining Admin (must be refused)", 
				theDatabase.updateUserRole("contribOne", "Admin", "false"), false);
		
		// Test 7: After the refused removal the user must still be an Admin
		performTestCase(7, "Last Admin still has the Admin role after the refusal", 
				theDatabase.userIsAdmin("contribOne") && theDatabase.getNumberOfAdmins() == 1, 
				true);
		
		// Test 8: A role name that does not exist must be refused
		performTestCase(8, "Update a role that does not exist (Role1)", 
				theDatabase.updateUserRole("contribOne", "Role1", "true"), false);
		
		// Test 9: A value other than "true"/"false" must be refused
		performTestCase(9, "Update a role with an illegal value (\"yes\")", 
				theDatabase.updateUserRole("contribOne", "Viewer", "yes"), false);
		
		// Test 10: An oversized role string (hacker-style long input) must be refused, not crash
		performTestCase(10, "Update a role with a 10,000 character role name", 
				theDatabase.updateUserRole("contribOne", "X".repeat(10000), "true"), false);
		
		// Test 11: Adding a role to a user who does not exist changes nothing
		performTestCase(11, "Add Viewer to a user who does not exist", 
				theDatabase.userHasRole("noSuchUser", "Viewer"), false);
		
		// Test 12: getUserAccountDetails reports false for an unknown user instead of crashing
		performTestCase(12, "getUserAccountDetails for an unknown user returns false", 
				theDatabase.getUserAccountDetails("noSuchUser"), false);
		
		// Test 13: The Curator role follows the same pattern: it can be added and read back
		performTestCase(13, "Add Curator role to contribOne", 
				theDatabase.updateUserRole("contribOne", "Curator", "true") 
				&& theDatabase.userHasRole("contribOne", "Curator"), true);
		
		// Test 14: A user holding Admin, Contributor, and Curator reports three roles
		theDatabase.getUserAccountDetails("contribOne");
		User contribOne = new User("contribOne", "Aa!15678", "", "", "", "", "",
				theDatabase.getCurrentAdminRole(), theDatabase.getCurrentContributorRole(),
				theDatabase.getCurrentViewerRole(), theDatabase.getCurrentCuratorRole());
		performTestCase(14, "contribOne now plays exactly three roles (Admin, Contributor, Curator)", 
				theDatabase.getNumberOfRoles(contribOne) == 3 && contribOne.getNumRoles() == 3, 
				true);
		
		// Test 15: Removing the Curator role works and leaves the other roles untouched
		performTestCase(15, "Remove Curator role from contribOne, keep Admin and Contributor", 
				theDatabase.updateUserRole("contribOne", "Curator", "false") 
				&& !theDatabase.userHasRole("contribOne", "Curator")
				&& theDatabase.userHasRole("contribOne", "Contributor")
				&& theDatabase.userIsAdmin("contribOne"), true);
		
		// Test 16: The role-specific login check honors the Curator column
		theDatabase.updateUserRole("adminOne", "Curator", "true");
		performTestCase(16, "loginCurator succeeds for adminOne after Curator is added", 
				theDatabase.loginCurator(new User("adminOne", "Aa!15678", "", "", "", "", "",
						false, false, false, true)), true);
		
		/************** End of the test cases **************/
		
		theDatabase.closeConnection();
		
		System.out.println("______________________________________________________________________");
		System.out.println("\nNumber of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
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
