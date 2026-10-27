package testingAutomation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import database.Database;
import guiAdminHome.ControllerAdminHome;

/*******
 * <p> Title: InvitationExpirationTestingAutomation Class. </p>
 * 
 * <p> Description: A semi-automated test driver, in the style of the
 * PasswordEvaluationTestingAutomation class, that exercises the invitation deadline user story:
 * an Admin sets a deadline when generating an invitation, the code is usable until the deadline,
 * and once the deadline passes the code is rejected and purged.  The deadline input validation
 * used by the Admin Home page is exercised as well.  A private in-memory H2 database is used so
 * the production database file is never touched. </p>
 * 
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 * 
 * @author A.G. (agupt515)
 * 
 * @version 1.00	2026-09-16	Initial version
 * 
 */
public class InvitationExpirationTestingAutomation {
	
	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests
	
	// A private in-memory database so these tests never alter the production database
	static Database theDatabase = new Database("jdbc:h2:mem:inviteTests;DB_CLOSE_DELAY=-1");

	/*
	 * This mainline displays a header to the console, performs a sequence of test cases, and
	 * then displays a footer with a summary of the results
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________________________________________");
		System.out.println("\nInvitation Expiration Testing Automation");
		
		theDatabase.connectToDatabase();
		
		/************** Part 1: deadline input validation (no database needed) **************/
		
		LocalDate today = LocalDate.now();
		
		// Test 1: A well-formed date and time is accepted by the parser
		performTestCase(1, "Parse a valid date with time \"14:30\"", 
				ControllerAdminHome.parseInvitationDeadline(today, "14:30") != null, true);
		
		// Test 2: A malformed time is rejected by the parser
		performTestCase(2, "Parse time \"2:30 PM\" (wrong format)", 
				ControllerAdminHome.parseInvitationDeadline(today, "2:30 PM") != null, false);
		
		// Test 3: A missing date is rejected by the parser
		performTestCase(3, "Parse with no date chosen", 
				ControllerAdminHome.parseInvitationDeadline(null, "14:30") != null, false);
		
		// Test 4: Oversized time input is rejected before parsing (hacker-style long input)
		performTestCase(4, "Parse a 10,000 character time string", 
				ControllerAdminHome.parseInvitationDeadline(today, "1".repeat(10000)) != null, 
				false);
		
		// Test 5: A deadline in the past fails validation
		performTestCase(5, "Validate a deadline one hour in the past", 
				ControllerAdminHome.validateInvitationDeadline(
						LocalDateTime.now().minusHours(1)).isEmpty(), false);
		
		// Test 6: A deadline one hour in the future passes validation
		performTestCase(6, "Validate a deadline one hour in the future", 
				ControllerAdminHome.validateInvitationDeadline(
						LocalDateTime.now().plusHours(1)).isEmpty(), true);
		
		// Test 7: A deadline beyond the maximum window fails validation
		performTestCase(7, "Validate a deadline " + (ControllerAdminHome.MAX_INVITATION_DAYS + 1) 
				+ " days in the future", 
				ControllerAdminHome.validateInvitationDeadline(LocalDateTime.now().plusDays(
						ControllerAdminHome.MAX_INVITATION_DAYS + 1)).isEmpty(), false);
		
		/************** Part 2: database behavior **************/
		
		// Test 8: An invitation with a future deadline is created and reported as outstanding
		String liveCode = theDatabase.generateInvitationCode("live@asu.edu", "Contributor", 
				LocalDateTime.now().plusHours(2));
		performTestCase(8, "Generate an invitation that expires in 2 hours", 
				liveCode.length() == 6 && theDatabase.getNumberOfInvitations() == 1, true);
		
		// Test 9: The stored deadline matches (to the second) what the Admin specified
		LocalDateTime wanted = LocalDateTime.now().plusHours(3).withNano(0);
		String code3h = theDatabase.generateInvitationCode("three@asu.edu", "Viewer", wanted);
		performTestCase(9, "Stored deadline equals the requested deadline", 
				wanted.equals(theDatabase.getInvitationDeadline(code3h)), true);
		
		// Test 10: A live invitation is not reported as expired and still yields its role
		performTestCase(10, "Live invitation is usable (not expired, role is Contributor)", 
				!theDatabase.isInvitationExpired(liveCode) 
				&& theDatabase.getRoleGivenAnInvitationCode(liveCode).equals("Contributor"), true);
		
		// Test 11: An invitation whose deadline is already in the past is refused at creation
		performTestCase(11, "Generate an invitation with a deadline in the past", 
				theDatabase.generateInvitationCode("past@asu.edu", "Contributor", 
						LocalDateTime.now().minusMinutes(1)).length() == 6, false);
		
		// Test 12: An invitation with a null deadline is refused at creation
		performTestCase(12, "Generate an invitation with a null deadline", 
				theDatabase.generateInvitationCode("null@asu.edu", "Contributor", null).length() == 6, 
				false);
		
		// Test 13: An invitation that expires in one second becomes expired after the deadline
		String shortCode = theDatabase.generateInvitationCode("short@asu.edu", "Viewer", 
				LocalDateTime.now().plusSeconds(1));
		boolean usableBefore = !theDatabase.isInvitationExpired(shortCode);
		Thread.sleep(1500);		// Let the deadline pass
		performTestCase(13, "Invitation usable before its deadline and expired after it", 
				usableBefore && theDatabase.isInvitationExpired(shortCode), true);
		
		// Test 14: Purging removes exactly the expired invitation and leaves the live ones
		int removed = theDatabase.removeExpiredInvitations();
		performTestCase(14, "Purge removes 1 expired invitation and keeps 2 live ones", 
				removed == 1 && theDatabase.getNumberOfInvitations() == 2 
				&& theDatabase.getRoleGivenAnInvitationCode(shortCode).isEmpty(), true);
		
		// Test 15: An unknown code is treated as expired (cannot be used)
		performTestCase(15, "Unknown invitation code is reported as expired", 
				theDatabase.isInvitationExpired("ZZZZZZ"), true);
		
		// Test 16: The two-argument form applies the default validity window
		String dfltCode = theDatabase.generateInvitationCode("dflt@asu.edu", "Admin");
		LocalDateTime dfltDeadline = theDatabase.getInvitationDeadline(dfltCode);
		performTestCase(16, "Default invitation expires about " 
				+ Database.DEFAULT_INVITATION_HOURS + " hours from now", 
				dfltDeadline != null 
				&& dfltDeadline.isAfter(LocalDateTime.now().plusHours(
						Database.DEFAULT_INVITATION_HOURS).minusMinutes(1))
				&& dfltDeadline.isBefore(LocalDateTime.now().plusHours(
						Database.DEFAULT_INVITATION_HOURS).plusMinutes(1)), true);
		
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
