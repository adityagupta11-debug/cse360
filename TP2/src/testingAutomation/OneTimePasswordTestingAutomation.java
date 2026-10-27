package testingAutomation;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
 * @author Vishwam
 *
 * @version 1.00	2026-09-17	Initial version
 * @version 1.01	2026-09-21	Verify immediate consumption, reset persistence, input
 * 							boundaries, and concurrent one-time redemption (Vishwam)
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

		// Vishwam, test the exact initial-story lifecycle: wrong input preserves the OTP, while the
		// first correct use consumes it before any permanent-password reset occurs.
		performTestCase(8, "A wrong one-time password is refused",
				theDatabase.loginWithOneTimePassword("contribOne", "Zz!99999999"), false);
		performTestCase(9, "A wrong attempt does not consume the valid one-time password",
				theDatabase.hasActiveOneTimePassword("contribOne"), true);
		performTestCase(10, "The correct one-time password is accepted before its deadline",
				theDatabase.loginWithOneTimePassword("contribOne", otp), true);
		performTestCase(11, "Successful one-time login consumes the credential immediately",
				theDatabase.hasActiveOneTimePassword("contribOne"), false);
		performTestCase(12, "The consumed one-time password cannot be reused",
				theDatabase.loginWithOneTimePassword("contribOne", otp), false);

		// Vishwam, consuming the temporary credential does not silently change the permanent one.
		theDatabase.getUserAccountDetails("contribOne");
		performTestCase(13, "The permanent password is unchanged until reset",
				theDatabase.getCurrentPassword().compareTo("Bb!15678") == 0, true);
		performTestCase(14, "The reset password is written successfully",
				theDatabase.updatePassword("contribOne", "Cc!15678"), true);
		theDatabase.getUserAccountDetails("contribOne");
		performTestCase(15, "The new permanent password is stored",
				theDatabase.getCurrentPassword().compareTo("Cc!15678") == 0, true);
		performTestCase(16, "A fresh session accepts the new permanent password",
				theDatabase.authenticateSession("contribOne", "Cc!15678"), true);
		performTestCase(17, "The old permanent password is no longer accepted",
				theDatabase.authenticateSession("contribOne", "Bb!15678"), false);

		performTestCase(18, "Setting a one-time password with a past deadline is refused",
				theDatabase.setOneTimePassword("contribOne", otp, anHourAgo), false);
		performTestCase(19, "Setting a one-time password with no deadline is refused",
				theDatabase.setOneTimePassword("contribOne", otp, null), false);
		performTestCase(20, "Setting an empty one-time password is refused",
				theDatabase.setOneTimePassword("contribOne", "", inTwoHours), false);
		performTestCase(21, "A one-time password cannot be set for a missing user",
				theDatabase.setOneTimePassword("noSuchUser", otp, inTwoHours), false);

		// Vishwam, expired and hostile inputs are refused without consuming a valid credential.
		theDatabase.setOneTimePassword("adminOne", otp, LocalDateTime.now().plusSeconds(1));
		Thread.sleep(1500);
		performTestCase(22, "An expired one-time password is refused",
				theDatabase.loginWithOneTimePassword("adminOne", otp), false);
		performTestCase(23, "An expired one-time password is purged",
				theDatabase.hasActiveOneTimePassword("adminOne"), false);
		theDatabase.setOneTimePassword("adminOne", otp, inTwoHours);
		performTestCase(24, "A 10,000-character one-time-password input is refused",
				theDatabase.loginWithOneTimePassword("adminOne", "X".repeat(10000)), false);
		performTestCase(25, "Oversized input does not consume the valid credential",
				theDatabase.hasActiveOneTimePassword("adminOne"), true);
		performTestCase(26, "A null one-time-password input is refused",
				theDatabase.loginWithOneTimePassword("adminOne", null), false);
		performTestCase(27, "Null input does not consume the valid credential",
				theDatabase.hasActiveOneTimePassword("adminOne"), true);

		String second = "Ss!87654321";
		theDatabase.setOneTimePassword("adminOne", second, inTwoHours);
		performTestCase(28, "A replaced one-time password no longer works",
				theDatabase.loginWithOneTimePassword("adminOne", otp), false);
		performTestCase(29, "The replacement remains active after the wrong old value",
				theDatabase.hasActiveOneTimePassword("adminOne"), true);
		performTestCase(30, "The replacement one-time password works",
				theDatabase.loginWithOneTimePassword("adminOne", second), true);
		performTestCase(31, "The replacement is consumed after its first use",
				theDatabase.hasActiveOneTimePassword("adminOne"), false);

		// Vishwam, two simultaneous database sessions must produce exactly one successful redemption.
		String concurrentOtp = "Qq!13579246";
		theDatabase.setOneTimePassword("adminOne", concurrentOtp, inTwoHours);
		Database secondConnection = new Database("jdbc:h2:mem:otpTests;DB_CLOSE_DELAY=-1");
		secondConnection.connectToDatabase();
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(2);
		AtomicInteger successfulRedemptions = new AtomicInteger();
		Runnable redeemWithFirstConnection = () -> redeemConcurrently(
				theDatabase, concurrentOtp, ready, start, done, successfulRedemptions);
		Runnable redeemWithSecondConnection = () -> redeemConcurrently(
				secondConnection, concurrentOtp, ready, start, done, successfulRedemptions);
		new Thread(redeemWithFirstConnection, "otp-redemption-1").start();
		new Thread(redeemWithSecondConnection, "otp-redemption-2").start();
		ready.await();
		start.countDown();
		done.await();
		performTestCase(32, "Two simultaneous redemptions succeed exactly once",
				successfulRedemptions.get() == 1, true);
		performTestCase(33, "The concurrently redeemed credential is no longer active",
				theDatabase.hasActiveOneTimePassword("adminOne"), false);
		secondConnection.closeConnection();

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

	// Vishwam, coordinate concurrent attempts without sharing a JDBC connection between threads.
	private static void redeemConcurrently(Database database, String otp, CountDownLatch ready,
			CountDownLatch start, CountDownLatch done, AtomicInteger successes) {
		ready.countDown();
		try {
			start.await();
			if (database.loginWithOneTimePassword("adminOne", otp)) {
				successes.incrementAndGet();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			done.countDown();
		}
	}
}
