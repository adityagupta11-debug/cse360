package testingAutomation;

import java.time.LocalDateTime;

import database.Database;
import entityClasses.User;
import guiOneTimePassword.ControllerOneTimePassword;
import passwordPopUpWindow.Model;

/*******
 * <p> Title: OneTimePasswordTestingAutomation Class. </p>
 *
 * <p> Description: A semi-automated test driver, in the style of the
 * PasswordEvaluationTestingAutomation class, that exercises the Admin "set a one-time password"
 * user story and the login behavior that goes with it, against a private in-memory H2 database.
 * Each test case states what is being tested, performs the action through the same methods the
 * GUI uses, and compares the observed result to the expected result.  The production database
 * file is never touched. </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00	2026-09-17	Initial version
 *
 */
public class OneTimePasswordTestingAutomation {

	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	// A private in-memory database so these tests never alter the production database
	static Database theDatabase = new Database("jdbc:h2:mem:otpTests;DB_CLOSE_DELAY=-1");

	/*
	 * This mainline displays a header to the console, performs a sequence of test cases, and
	 * then displays a footer with a summary of the results
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________________________________________");
		System.out.println("\nOne-Time Password Testing Automation");

		theDatabase.connectToDatabase();

		// Establish a known starting state: an Admin and a Contributor with known passwords
		theDatabase.register(new User("adminOne", "Aa!15678", "", "", "", "",
				"admin@asu.edu", true, false, false, false));
		theDatabase.register(new User("contribOne", "Bb!15678", "", "", "", "",
				"contributor@asu.edu", false, true, false, false));

		LocalDateTime inTwoHours = LocalDateTime.now().plusHours(2);
		LocalDateTime anHourAgo = LocalDateTime.now().minusHours(1);

		/************** Start of the test cases **************/

		// Test 1: A newly registered user has no one-time password
		performTestCase(1, "A new user has no active one-time password",
				theDatabase.hasActiveOneTimePassword("contribOne"), false);

		// Test 2: A generated one-time password satisfies every password requirement, so the
		// user is never handed a temporary password the system would itself reject
		boolean allValid = true;
		java.io.PrintStream console = System.out;	// The evaluator traces every character, so
		System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
		for (int i = 0; i < 100; i++) {				// its output is silenced for this one loop
			if (!Model.evaluatePassword(
					ControllerOneTimePassword.generateOneTimePassword()).isEmpty())
				allValid = false;
		}
		System.setOut(console);						// Restore the console for the report
		performTestCase(2, "100 generated one-time passwords all satisfy the requirements",
				allValid, true);

		// Test 3: The generated password has the documented length
		performTestCase(3, "A generated one-time password is " +
				ControllerOneTimePassword.ONE_TIME_PASSWORD_LENGTH + " characters long",
				ControllerOneTimePassword.generateOneTimePassword().length() ==
				ControllerOneTimePassword.ONE_TIME_PASSWORD_LENGTH, true);

		// Test 4: Two generated passwords are not the same (they are randomly generated)
		performTestCase(4, "Two generated one-time passwords differ",
				ControllerOneTimePassword.generateOneTimePassword().compareTo(
				ControllerOneTimePassword.generateOneTimePassword()) == 0, false);

		// Test 5: An Admin can store a one-time password with a future deadline
		String otp = "Tt!12345678";
		performTestCase(5, "Setting a one-time password for contribOne",
				theDatabase.setOneTimePassword("contribOne", otp, inTwoHours), true);

		// Test 6: The stored one-time password is reported as active
		performTestCase(6, "contribOne now has an active one-time password",
				theDatabase.hasActiveOneTimePassword("contribOne"), true);

		// Test 7: The stored deadline is the deadline that was requested
		performTestCase(7, "The stored deadline equals the requested deadline",
				theDatabase.getOneTimePasswordDeadline("contribOne")
				.withNano(0).equals(inTwoHours.withNano(0)), true);

		// Test 8: The one-time password is accepted at login before the deadline
		performTestCase(8, "Logging in with the correct one-time password",
				theDatabase.loginWithOneTimePassword("contribOne", otp), true);

		// Test 9: A different text is refused
		performTestCase(9, "Logging in with the wrong one-time password",
				theDatabase.loginWithOneTimePassword("contribOne", "Zz!99999999"), false);

		// Test 10: The user's own password still works while a one-time password is outstanding
		theDatabase.getUserAccountDetails("contribOne");
		performTestCase(10, "The user's own password is unchanged by setting a one-time password",
				theDatabase.getCurrentPassword().compareTo("Bb!15678") == 0, true);

		// Test 11: After the user sets a new password, the one-time password is cleared so it
		// can never be used a second time (this is what the login page does)
		theDatabase.updatePassword("contribOne", "Cc!15678");
		theDatabase.clearOneTimePassword("contribOne");
		performTestCase(11, "The one-time password is refused after being used and cleared",
				theDatabase.loginWithOneTimePassword("contribOne", otp), false);

		// Test 12: The new password chosen by the user is the one now stored
		theDatabase.getUserAccountDetails("contribOne");
		performTestCase(12, "The user's new password is stored",
				theDatabase.getCurrentPassword().compareTo("Cc!15678") == 0, true);

		// Test 13: The user no longer has an active one-time password
		performTestCase(13, "contribOne has no active one-time password after the reset",
				theDatabase.hasActiveOneTimePassword("contribOne"), false);

		// Test 14: A deadline in the past is refused when the one-time password is set
		performTestCase(14, "Setting a one-time password with a deadline in the past",
				theDatabase.setOneTimePassword("contribOne", otp, anHourAgo), false);

		// Test 15: A null deadline is refused
		performTestCase(15, "Setting a one-time password with no deadline",
				theDatabase.setOneTimePassword("contribOne", otp, null), false);

		// Test 16: An empty one-time password is refused
		performTestCase(16, "Setting an empty one-time password",
				theDatabase.setOneTimePassword("contribOne", "", inTwoHours), false);

		// Test 17: A one-time password cannot be set for a user who does not exist
		performTestCase(17, "Setting a one-time password for a user who does not exist",
				theDatabase.setOneTimePassword("noSuchUser", otp, inTwoHours), false);

		// Test 18: An expired one-time password is refused at login.  The deadline is written
		// directly with a short lifetime and allowed to pass.
		theDatabase.setOneTimePassword("adminOne", otp, LocalDateTime.now().plusSeconds(1));
		Thread.sleep(1500);							// Let the deadline pass
		performTestCase(18, "Logging in with an expired one-time password",
				theDatabase.loginWithOneTimePassword("adminOne", otp), false);

		// Test 19: The expired one-time password was purged as a side effect of the attempt
		performTestCase(19, "The expired one-time password is no longer active",
				theDatabase.hasActiveOneTimePassword("adminOne"), false);

		// Test 20: A 10,000 character password (hacker-style long input) is refused, not a crash
		theDatabase.setOneTimePassword("adminOne", otp, inTwoHours);
		performTestCase(20, "Logging in with a 10,000 character one-time password",
				theDatabase.loginWithOneTimePassword("adminOne", "X".repeat(10000)), false);

		// Test 21: A null one-time password at login is refused rather than throwing
		performTestCase(21, "Logging in with a null one-time password",
				theDatabase.loginWithOneTimePassword("adminOne", null), false);

		// Test 22: Setting a new one-time password replaces the earlier one
		String second = "Ss!87654321";
		theDatabase.setOneTimePassword("adminOne", second, inTwoHours);
		performTestCase(22, "The replaced one-time password no longer works",
				theDatabase.loginWithOneTimePassword("adminOne", otp), false);

		// Test 23: The replacement one-time password does work
		performTestCase(23, "The replacement one-time password works",
				theDatabase.loginWithOneTimePassword("adminOne", second), true);

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
