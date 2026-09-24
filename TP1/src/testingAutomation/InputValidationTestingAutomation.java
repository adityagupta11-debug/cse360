package testingAutomation;

import emailAddressRecognizer.EmailAddressRecognizer;
import guiUserUpdate.ControllerUserUpdate;
import passwordPopUpWindow.Model;
import userNameRecognizer.UserNameRecognizer;

/*******
 * <p> Title: InputValidationTestingAutomation Class. </p>
 *
 * <p> Description: A semi-automated test driver, in the style of the
 * PasswordEvaluationTestingAutomation class, that exercises every textual input validator used
 * by TP1: the UserName recognizer, the password evaluator, the email address recognizer, and
 * the name checks used on the account update page.  Every validator is also given a 10,000
 * character string, because dropping a long string into each input field is a standard tactic
 * used to find an exploitable weakness; the application must refuse such input rather than
 * fail.
 *
 * These validators are pure functions of their input, so no database is required. </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 * @author Vishwam
 *
 * @version 1.00	2026-09-17	Initial version
	 * @version 1.01	2026-09-21	Add null, invalid-character, and stale dynamic-state
	 * 							regression cases (Vishwam)
	 * @version 1.02	2026-09-21	Add explicit confirmation-field boundary cases (Vishwam)
 *
 */
public class InputValidationTestingAutomation {

	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	// A string far longer than any field should accept, used for the oversized-input tests
	static final String LONG_INPUT = "X".repeat(10000);

	/*
	 * This mainline displays a header to the console, performs a sequence of test cases, and
	 * then displays a footer with a summary of the results
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________________________________________");
		System.out.println("\nInput Validation Testing Automation");

		/************** UserName field (login page, new account page, account update) **********/

		// Test 1: A well-formed UserName is accepted
		performTestCase(1, "UserName \"agupt545\" is accepted",
				UserNameRecognizer.checkForValidUserName("agupt545").isEmpty(), true);

		// Test 2: An empty UserName is refused
		performTestCase(2, "An empty UserName is refused",
				UserNameRecognizer.checkForValidUserName("").isEmpty(), false);

		// Test 3: A UserName that starts with a digit is refused
		performTestCase(3, "UserName \"1abc\" (starts with a digit) is refused",
				UserNameRecognizer.checkForValidUserName("1abc").isEmpty(), false);

		// Test 4: A 10,000 character UserName is refused rather than crashing the recognizer
		performTestCase(4, "A 10,000 character UserName is refused",
				UserNameRecognizer.checkForValidUserName(LONG_INPUT).isEmpty(), false);

		/************** Password field (first admin, new account, password popup) *************/

		// Test 5: A password satisfying every requirement is accepted
		performTestCase(5, "Password \"Aa!15678\" is accepted",
				quietEvaluatePassword("Aa!15678").isEmpty(), true);

		// Test 6: A password with no upper case letter is refused
		performTestCase(6, "Password \"aa!15678\" (no upper case) is refused",
				quietEvaluatePassword("aa!15678").isEmpty(), false);

		// Test 7: A password with no special character is refused
		performTestCase(7, "Password \"Aa115678\" (no special character) is refused",
				quietEvaluatePassword("Aa115678").isEmpty(), false);

		// Test 8: A password that is too short is refused
		performTestCase(8, "Password \"Aa!1567\" (7 characters) is refused",
				quietEvaluatePassword("Aa!1567").isEmpty(), false);

		// Test 9: A password at the maximum length is accepted (the boundary case)
		performTestCase(9, "Password of exactly " + Model.MAX_PASSWORD_LENGTH +
				" characters is accepted",
				quietEvaluatePassword("Aa!1" + "b".repeat(Model.MAX_PASSWORD_LENGTH - 4))
				.isEmpty(), true);

		// Test 10: A password one character over the maximum is refused
		performTestCase(10, "Password of " + (Model.MAX_PASSWORD_LENGTH + 1) +
				" characters is refused",
				quietEvaluatePassword("Aa!1" + "b".repeat(Model.MAX_PASSWORD_LENGTH - 3))
				.isEmpty(), false);

		// Test 11: A 10,000 character password is refused rather than being walked character
		// by character
		performTestCase(11, "A 10,000 character password is refused",
				quietEvaluatePassword(LONG_INPUT).isEmpty(), false);

		/************** Email address field (Admin invitation, account update) ****************/

		// Test 12: A normal address is accepted
		performTestCase(12, "Email \"agupt545@asu.edu\" is accepted",
				EmailAddressRecognizer.checkForValidEmailAddress("agupt545@asu.edu").isEmpty(),
				true);

		// Test 13: An address using the allowed local-part characters is accepted
		performTestCase(13, "Email \"first.last+tag_1%x-y@sub.example.co.uk\" is accepted",
				EmailAddressRecognizer.checkForValidEmailAddress(
						"first.last+tag_1%x-y@sub.example.co.uk").isEmpty(), true);

		// Test 14: An empty address is refused
		performTestCase(14, "An empty email address is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("").isEmpty(), false);

		// Test 15: An address with no "@" is refused
		performTestCase(15, "Email \"agupt545.asu.edu\" (no @) is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("agupt545.asu.edu").isEmpty(),
				false);

		// Test 16: An address with two "@" characters is refused
		performTestCase(16, "Email \"a@b@asu.edu\" (two @) is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("a@b@asu.edu").isEmpty(), false);

		// Test 17: An address with nothing before the "@" is refused
		performTestCase(17, "Email \"@asu.edu\" (no mailbox name) is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("@asu.edu").isEmpty(), false);

		// Test 18: An address with no domain is refused
		performTestCase(18, "Email \"agupt545@\" (no domain) is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("agupt545@").isEmpty(), false);

		// Test 19: A domain with no period is refused
		performTestCase(19, "Email \"agupt545@asu\" (no top-level domain) is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("agupt545@asu").isEmpty(), false);

		// Test 20: A one-letter top-level domain is refused
		performTestCase(20, "Email \"agupt545@asu.e\" (one letter TLD) is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("agupt545@asu.e").isEmpty(),
				false);

		// Test 21: Two periods in a row in the mailbox name are refused
		performTestCase(21, "Email \"first..last@asu.edu\" is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("first..last@asu.edu").isEmpty(),
				false);

		// Test 22: A mailbox name ending in a period is refused
		performTestCase(22, "Email \"agupt545.@asu.edu\" is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("agupt545.@asu.edu").isEmpty(),
				false);

		// Test 23: A domain part starting with a hyphen is refused
		performTestCase(23, "Email \"agupt545@-asu.edu\" is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("agupt545@-asu.edu").isEmpty(),
				false);

		// Test 24: A space inside the address is refused
		performTestCase(24, "Email \"agupt 545@asu.edu\" is refused",
				EmailAddressRecognizer.checkForValidEmailAddress("agupt 545@asu.edu").isEmpty(),
				false);

		// Test 25: An address at the maximum length is refused when it goes one character over
		performTestCase(25, "An email address longer than " + EmailAddressRecognizer.MAX_LENGTH +
				" characters is refused",
				EmailAddressRecognizer.checkForValidEmailAddress(
						"a".repeat(EmailAddressRecognizer.MAX_LENGTH) + "@asu.edu").isEmpty(),
				false);

		// Test 26: A 10,000 character email address is refused
		performTestCase(26, "A 10,000 character email address is refused",
				EmailAddressRecognizer.checkForValidEmailAddress(LONG_INPUT).isEmpty(), false);

		// Test 27: The recognizer reports where the error was found, so the GUI can point at it
		EmailAddressRecognizer.checkForValidEmailAddress("agupt545.asu.edu");
		performTestCase(27, "The recognizer records the index of the error",
				EmailAddressRecognizer.emailAddressIndexofError >= 0, true);

		/************** Name fields (account update page) *************************************/

		// Test 28: An ordinary name is accepted
		performTestCase(28, "Name \"Aditya\" is accepted",
				ControllerUserUpdate.checkName("Aditya").isEmpty(), true);

		// Test 29: A hyphenated and apostrophized name is accepted
		performTestCase(29, "Name \"Anne-Marie O'Brien\" is accepted",
				ControllerUserUpdate.checkName("Anne-Marie O'Brien").isEmpty(), true);

		// Test 30: An empty name is accepted, since these fields may be cleared
		performTestCase(30, "An empty name is accepted (the field may be cleared)",
				ControllerUserUpdate.checkName("").isEmpty(), true);

		// Test 31: A name containing a digit is refused
		performTestCase(31, "Name \"Aditya2\" is refused",
				ControllerUserUpdate.checkName("Aditya2").isEmpty(), false);

		// Test 32: A name at the maximum length is accepted (the boundary case)
		performTestCase(32, "A name of exactly " + ControllerUserUpdate.MAX_NAME_LENGTH +
				" characters is accepted",
				ControllerUserUpdate.checkName("a".repeat(ControllerUserUpdate.MAX_NAME_LENGTH))
				.isEmpty(), true);

		// Test 33: A name one character over the maximum is refused
		performTestCase(33, "A name of " + (ControllerUserUpdate.MAX_NAME_LENGTH + 1) +
				" characters is refused",
				ControllerUserUpdate.checkName(
						"a".repeat(ControllerUserUpdate.MAX_NAME_LENGTH + 1)).isEmpty(), false);

		// Test 34: A 10,000 character name is refused
		performTestCase(34, "A 10,000 character name is refused",
				ControllerUserUpdate.checkName(LONG_INPUT).isEmpty(), false);

		// Test 35: The account update page reuses the same UserName rules as account creation
		performTestCase(35, "The account update page refuses UserName \"1abc\" as well",
				ControllerUserUpdate.checkUserName("1abc").isEmpty(), false);

		// Test 36: The account update page reuses the same email rules as the Admin page
		performTestCase(36, "The account update page refuses email \"agupt545@asu\" as well",
				ControllerUserUpdate.checkEmailAddress("agupt545@asu").isEmpty(), false);

		/************** Dynamic password state regression cases ******************************/

		// Vishwam, direct callers and changing GUI input must never reuse an earlier valid state.
		performTestCase(37, "A null password is refused without an exception",
				quietEvaluatePassword(null).isEmpty(), false);
		performTestCase(38, "A tab character is refused as an invalid password character",
				quietEvaluatePassword("Aa!1567\t").isEmpty(), false);

		quietEvaluatePassword("Aa!15678");
		performTestCase(39, "A valid password sets every observable requirement flag",
				allPasswordRequirementFlags(), true);
		quietEvaluatePassword("Aa!1" + "b".repeat(Model.MAX_PASSWORD_LENGTH));
		performTestCase(40, "Overlong input clears all prior requirement flags",
				noPasswordRequirementFlags(), true);

		quietEvaluatePassword("Aa!15678");
		quietEvaluatePassword("");
		performTestCase(41, "Empty input clears all prior requirement flags",
				noPasswordRequirementFlags(), true);

		quietEvaluatePassword("X".repeat(10000));
		performTestCase(42, "A valid password still succeeds after an excessive paste",
				quietEvaluatePassword("Aa!15678").isEmpty(), true);

		/************** Password confirmation size boundaries *********************************/

		// Vishwam, both confirmation fields share this bound before equality comparison.
		performTestCase(43, "A 20-character password confirmation is within the limit",
				Model.checkPasswordConfirmation("x".repeat(Model.MAX_PASSWORD_LENGTH)).isEmpty(),
				true);
		performTestCase(44, "A 21-character password confirmation is refused",
				Model.checkPasswordConfirmation(
						"x".repeat(Model.MAX_PASSWORD_LENGTH + 1)).isEmpty(), false);
		performTestCase(45, "A 10,000-character password confirmation is refused",
				Model.checkPasswordConfirmation(LONG_INPUT).isEmpty(), false);
		performTestCase(46, "A null password confirmation is refused safely",
				Model.checkPasswordConfirmation(null).isEmpty(), false);

		/************** End of the test cases **************/

		System.out.println("______________________________________________________________________");
		System.out.println("\nNumber of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	/*
	 * The password evaluator reports generic requirement progress to the console. This helper keeps
	 * those status lines out of the automated report and then restores the console; no password text
	 * is emitted by the production evaluator.
	 */
	// Vishwam, keep validation-test output concise without weakening or bypassing the real evaluator.
	private static String quietEvaluatePassword(String input) {
		java.io.PrintStream console = System.out;
		System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
		String result = Model.evaluatePassword(input);
		System.setOut(console);
		return result;
	}

	// Vishwam, focused helpers make stale dynamic state visible without launching JavaFX.
	private static boolean allPasswordRequirementFlags() {
		return Model.foundUpperCase && Model.foundLowerCase && Model.foundNumericDigit
				&& Model.foundSpecialChar && Model.foundLongEnough && Model.isShortEnough();
	}

	private static boolean noPasswordRequirementFlags() {
		return !Model.foundUpperCase && !Model.foundLowerCase && !Model.foundNumericDigit
				&& !Model.foundSpecialChar && !Model.foundLongEnough && !Model.isShortEnough();
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
