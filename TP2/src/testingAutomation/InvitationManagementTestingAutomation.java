package testingAutomation;

import java.time.LocalDateTime;
import java.util.List;

import database.Database;
import entityClasses.Invitation;

/*******
 * <p> Title: InvitationManagementTestingAutomation Class. </p>
 *
 * <p> Description: A semi-automated test driver, in the style of the
 * PasswordEvaluationTestingAutomation class, that exercises the Admin "manage invitations" user
 * story: listing every invitation with its status, revoking one before it is used, and purging
 * the invitations whose deadlines have passed.  It runs against a private in-memory H2 database
 * so the production database file is never touched. </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00	2026-09-17	Initial version
 *
 */
public class InvitationManagementTestingAutomation {

	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	// A private in-memory database so these tests never alter the production database
	static Database theDatabase = new Database("jdbc:h2:mem:invManageTests;DB_CLOSE_DELAY=-1");

	/*
	 * This mainline displays a header to the console, performs a sequence of test cases, and
	 * then displays a footer with a summary of the results
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________________________________________");
		System.out.println("\nInvitation Management Testing Automation");

		theDatabase.connectToDatabase();

		/************** Start of the test cases **************/

		// Test 1: A new database has no invitations to manage
		performTestCase(1, "A new database has an empty invitation list",
				theDatabase.getInvitationList().isEmpty(), true);

		// Test 2: Three invitations are created, each with a different role and deadline
		String codeA = theDatabase.generateInvitationCode("a@asu.edu", "Contributor",
				LocalDateTime.now().plusHours(2));
		String codeB = theDatabase.generateInvitationCode("b@asu.edu", "Viewer",
				LocalDateTime.now().plusDays(3));
		String codeC = theDatabase.generateInvitationCode("c@asu.edu", "Curator",
				LocalDateTime.now().plusSeconds(1));
		performTestCase(2, "Three invitations were created and all have codes",
				!codeA.isEmpty() && !codeB.isEmpty() && !codeC.isEmpty(), true);

		// Test 3: The list contains all three invitations
		performTestCase(3, "The invitation list contains three entries",
				theDatabase.getInvitationList().size() == 3, true);

		// Test 4: The list is ordered by deadline, so the soonest expiry is shown first
		List<Invitation> list = theDatabase.getInvitationList();
		performTestCase(4, "The list is ordered by deadline (c@asu.edu expires first)",
				list.get(0).getEmailAddress().compareTo("c@asu.edu") == 0, true);

		// Test 5: Each entry carries the email address and role that were requested
		Invitation first = findByCode(codeA);
		performTestCase(5, "The entry for codeA carries a@asu.edu and the Contributor role",
				first.getEmailAddress().compareTo("a@asu.edu") == 0
				&& first.getRole().compareTo("Contributor") == 0, true);

		// Test 6: A live invitation reports the status "Active"
		performTestCase(6, "A live invitation reports the status \"Active\"",
				first.getStatus().compareTo("Active") == 0, true);

		// Test 7: The deadline is formatted for display rather than shown as a raw timestamp
		performTestCase(7, "The displayed deadline is 16 characters (yyyy-MM-dd HH:mm)",
				first.getDeadlineText().length() == 16, true);

		// Test 8: Revoking an invitation removes it from the list
		performTestCase(8, "Revoking codeB succeeds", theDatabase.removeInvitation(codeB), true);

		// Test 9: The revoked code can no longer be used to establish an account
		performTestCase(9, "The revoked code no longer has a role",
				theDatabase.getRoleGivenAnInvitationCode(codeB).isEmpty(), true);

		// Test 10: The revoked code is reported as expired, so the new account page refuses it
		performTestCase(10, "The revoked code is treated as expired",
				theDatabase.isInvitationExpired(codeB), true);

		// Test 11: Revoking the same code a second time is refused instead of crashing
		performTestCase(11, "Revoking codeB a second time is refused",
				theDatabase.removeInvitation(codeB), false);

		// Test 12: Revoking a code that never existed is refused
		performTestCase(12, "Revoking a code that does not exist is refused",
				theDatabase.removeInvitation("zzzzzz"), false);

		// Test 13: An empty code is refused rather than deleting anything
		performTestCase(13, "Revoking an empty code is refused",
				theDatabase.removeInvitation(""), false);

		// Test 14: A 10,000 character code (hacker-style long input) is refused, not a crash
		performTestCase(14, "Revoking a 10,000 character code is refused",
				theDatabase.removeInvitation("X".repeat(10000)), false);

		// Test 15: A null code is refused rather than throwing an exception
		performTestCase(15, "Revoking a null code is refused",
				theDatabase.removeInvitation(null), false);

		// Test 16: Revoking frees the email address so the Admin can invite that person again
		performTestCase(16, "b@asu.edu may be invited again after the revocation",
				theDatabase.emailaddressHasBeenUsed("b@asu.edu"), false);

		// Test 17: Once its deadline passes, an invitation reports the status "Expired"
		Thread.sleep(1500);							// Let codeC's deadline pass
		performTestCase(17, "An invitation past its deadline reports \"Expired\"",
				findByCode(codeC).isExpired(), true);

		// Test 18: Purging removes exactly the expired invitation and keeps the live one
		int removed = theDatabase.removeExpiredInvitations();
		performTestCase(18, "Purging removes 1 expired invitation and keeps the live one",
				removed == 1 && theDatabase.getInvitationList().size() == 1, true);

		// Test 19: Purging again removes nothing, since nothing has expired since
		performTestCase(19, "Purging a second time removes nothing",
				theDatabase.removeExpiredInvitations() == 0, true);

		// Test 20: The surviving invitation is the one that has not expired
		performTestCase(20, "The surviving invitation is codeA",
				theDatabase.getInvitationList().get(0).getCode().compareTo(codeA) == 0, true);

		/************** End of the test cases **************/

		theDatabase.closeConnection();

		System.out.println("______________________________________________________________________");
		System.out.println("\nNumber of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	/*
	 * A helper that locates one invitation in the list returned by the database
	 */
	private static Invitation findByCode(String code) {
		for (Invitation i : theDatabase.getInvitationList())
			if (i.getCode().compareTo(code) == 0) return i;
		return new Invitation(code, "", "", null);
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
