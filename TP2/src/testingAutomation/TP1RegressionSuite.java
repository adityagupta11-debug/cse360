package testingAutomation;

/*******
 * <p> Title: TP1RegressionSuite Class. </p>
 *
 * <p> Description: The regression checkpoint for TP1.  It runs every testing automation class in
 * this package in turn and prints one summary at the end giving the number of test cases passed
 * and failed for each class and for the phase as a whole.  The suite exits with a non-zero status
 * when any test case fails, so it can also be run from a script before a merge.
 *
 * Run this class before every merge and at each scheduled regression checkpoint.  A merge should
 * not be accepted while any test case in this suite is failing. </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00	2026-09-17	Initial version
 *
 */
public class TP1RegressionSuite {

	/*
	 * The mainline runs each testing automation class and then reports the totals
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("======================================================================");
		System.out.println("TP1 Regression Suite");
		System.out.println("======================================================================");

		// Each entry is the name of a test class and the two counters it maintains
		InputValidationTestingAutomation.main(args);
		RoleManagementTestingAutomation.main(args);
		InvitationExpirationTestingAutomation.main(args);
		InvitationManagementTestingAutomation.main(args);
		UserManagementTestingAutomation.main(args);
		OneTimePasswordTestingAutomation.main(args);

		// Collect the results from each class
		int totalPassed = 0;
		int totalFailed = 0;

		System.out.println("\n======================================================================");
		System.out.println("TP1 Regression Suite Summary");
		System.out.println("======================================================================");

		totalPassed += report("Input Validation",
				InputValidationTestingAutomation.numPassed,
				InputValidationTestingAutomation.numFailed);
		totalFailed += InputValidationTestingAutomation.numFailed;

		totalPassed += report("Role Management",
				RoleManagementTestingAutomation.numPassed,
				RoleManagementTestingAutomation.numFailed);
		totalFailed += RoleManagementTestingAutomation.numFailed;

		totalPassed += report("Invitation Expiration",
				InvitationExpirationTestingAutomation.numPassed,
				InvitationExpirationTestingAutomation.numFailed);
		totalFailed += InvitationExpirationTestingAutomation.numFailed;

		totalPassed += report("Invitation Management",
				InvitationManagementTestingAutomation.numPassed,
				InvitationManagementTestingAutomation.numFailed);
		totalFailed += InvitationManagementTestingAutomation.numFailed;

		totalPassed += report("User Management",
				UserManagementTestingAutomation.numPassed,
				UserManagementTestingAutomation.numFailed);
		totalFailed += UserManagementTestingAutomation.numFailed;

		totalPassed += report("One-Time Password",
				OneTimePasswordTestingAutomation.numPassed,
				OneTimePasswordTestingAutomation.numFailed);
		totalFailed += OneTimePasswordTestingAutomation.numFailed;

		System.out.println("----------------------------------------------------------------------");
		System.out.printf("%-26s passed: %3d   failed: %3d%n", "TOTAL", totalPassed, totalFailed);
		System.out.println("======================================================================");
		if (totalFailed == 0) {
			System.out.println("All TP1 test cases passed. The regression checkpoint is clean.");
		} else {
			System.out.println("*** " + totalFailed + " test case(s) failed. Do not merge until "
					+ "they are resolved. ***");
			System.exit(1);
		}
	}

	/*
	 * Print one line of the summary table and return the number of test cases that passed
	 */
	private static int report(String name, int passed, int failed) {
		System.out.printf("%-26s passed: %3d   failed: %3d%n", name, passed, failed);
		return passed;
	}
}
