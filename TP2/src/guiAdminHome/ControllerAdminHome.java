package guiAdminHome;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import database.Database;
import emailAddressRecognizer.EmailAddressRecognizer;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * Every button on this page is now implemented.  The invitation is created on this page; the
 * other Admin functions (Manage Invitations, One-Time Password, Delete User, List Users, and
 * Add/Remove Roles) each dispatch to a dedicated page in its own MVC package.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation
 * @version 1.02		2026-09-16 Invitation deadline and named roles (A.G., agupt515)
 * @version 1.03		2026-09-17 Manage Invitations, One-Time Password, Delete User, and List
 * 							Users now dispatch to their own pages; email addresses are validated
 * 							syntactically (A.G., agupt545)
 */

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerAdminHome() {
	}
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	// The format used for the time-of-day portion of an invitation deadline
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
	
	/** Upper bound on how far into the future an invitation deadline may be set, in days */
	public static final int MAX_INVITATION_DAYS = 30;

	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role.  The invitation carries a deadline (date and time) chosen
	 * by the Admin; after the deadline the code can no longer be used. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		// Verify that the deadline is valid - If not alert the user and return
		LocalDateTime deadline = parseInvitationDeadline(
				ViewAdminHome.datepicker_InvitationDeadline.getValue(),
				ViewAdminHome.text_InvitationDeadlineTime.getText());
		String deadlineError = validateInvitationDeadline(deadline);
		if (!deadlineError.isEmpty()) {
			ViewAdminHome.alertEmailError.setContentText(deadlineError);
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole, deadline);
		if (invitationCode.isEmpty()) {		// The database refused the invitation
			ViewAdminHome.alertEmailError.setContentText(
					"The invitation could not be created. Check the deadline and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress + "\nIt expires on " + 
				deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm")) + ".";
		System.out.println(msg);
		ViewAdminHome.alertEmailSent.setContentText(msg);
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home pages status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		resetInvitationDeadline();
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: resetInvitationDeadline () Method. </p>
	 * 
	 * <p> Description: Protected method that sets the invitation deadline widgets back to the
	 * default deadline, which is Database.DEFAULT_INVITATION_HOURS from now. </p>
	 */
	protected static void resetInvitationDeadline () {
		LocalDateTime dflt = LocalDateTime.now().plusHours(Database.DEFAULT_INVITATION_HOURS);
		ViewAdminHome.datepicker_InvitationDeadline.setValue(dflt.toLocalDate());
		ViewAdminHome.text_InvitationDeadlineTime.setText(dflt.format(TIME_FORMAT));
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: parseInvitationDeadline (LocalDate date, String time) Method. </p>
	 * 
	 * <p> Description: Protected method that combines the date chosen in the DatePicker with the
	 * text typed into the time field.  This method performs no policy checks; it only converts
	 * the input.  It is separated from the GUI so it can be exercised by the automated tests. </p>
	 * 
	 * @param date	The date chosen by the Admin (may be null if nothing was chosen)
	 * @param time	The text entered for the time of day, expected as HH:mm (24-hour clock)
	 * 
	 * @return the combined date and time, or null if either part is missing or malformed
	 */
	public static LocalDateTime parseInvitationDeadline(LocalDate date, String time) {
		if (date == null || time == null) return null;
		String t = time.trim();
		// Guard against oversized input before attempting to parse it
		if (t.length() == 0 || t.length() > 5) return null;
		try {
			return LocalDateTime.of(date, LocalTime.parse(t, TIME_FORMAT));
		} catch (DateTimeParseException e) {
			return null;
		}
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: validateInvitationDeadline (LocalDateTime deadline) Method. </p>
	 * 
	 * <p> Description: Protected method that applies the invitation deadline policy: the deadline
	 * must be present, must be in the future, and must be no more than MAX_INVITATION_DAYS from
	 * now.  It is separated from the GUI so it can be exercised by the automated tests. </p>
	 * 
	 * @param deadline	The proposed deadline (null if the input could not be parsed)
	 * 
	 * @return an empty string if the deadline is acceptable, else a message for the Admin
	 */
	public static String validateInvitationDeadline(LocalDateTime deadline) {
		if (deadline == null)
			return "Enter a valid expiration date and a time in the form HH:mm (24-hour clock).";
		LocalDateTime now = LocalDateTime.now();
		if (!deadline.isAfter(now))
			return "The expiration date and time must be in the future.";
		if (deadline.isAfter(now.plusDays(MAX_INVITATION_DAYS)))
			return "The expiration date and time must be within " + MAX_INVITATION_DAYS + 
					" days from now.";
		return "";
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: manageInvitations () Method. </p>
	 * 
	 * <p> Description: Protected method that displays the Manage Invitations page, where the Admin
	 * can review every outstanding invitation, revoke one, or purge those that have expired. </p>
	 */
	protected static void manageInvitations () {
		guiManageInvitations.ViewManageInvitations.displayManageInvitations(
				ViewAdminHome.theStage, ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: setOnetimePassword () Method. </p>
	 * 
	 * <p> Description: Protected method that displays the One-Time Password page, where the Admin
	 * selects a user and a deadline and the system generates a temporary password that the
	 * user must replace at their next login. </p>
	 */
	protected static void setOnetimePassword () {
		guiOneTimePassword.ViewOneTimePassword.displayOneTimePassword(
				ViewAdminHome.theStage, ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: deleteUser () Method. </p>
	 * 
	 * <p> Description: Protected method that displays the Delete User page, where the Admin selects
	 * a user, confirms "Are you sure?", and the user is removed (the last Admin is protected). </p>
	 */
	protected static void deleteUser () {
		guiDeleteUser.ViewDeleteUser.displayDeleteUser(ViewAdminHome.theStage,
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: listUsers () Method. </p>
	 * 
	 * <p> Description: Protected method that displays the List Users page, a table of every user's
	 * username, name, email address, and roles. </p>
	 */
	protected static void listUsers () {
		guiListUsers.ViewListUsers.displayListUsers(ViewAdminHome.theStage,
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system.  This is done by invoking the AddRemoveRoles Page. There
	 * is no need to specify the home page for the return as this can only be initiated by and
	 * Admin.</p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that checks an email address before it is used.  The
	 * input is first bounded in size and then checked syntactically by the EmailAddressRecognizer
	 * (exactly one "@", a well-formed mailbox name, and a domain with a top-level domain).  When
	 * the check fails, an alert explains the problem and true is returned so the caller stops.
	 * Whether the mailbox actually exists cannot be determined here.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 * 
	 * @return true if the email address is NOT acceptable, else false
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		String error = EmailAddressRecognizer.checkForValidEmailAddress(emailAddress);
		if (!error.isEmpty()) {
			ViewAdminHome.alertEmailError.setContentText(error + 
					"\nCorrect the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		return false;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
