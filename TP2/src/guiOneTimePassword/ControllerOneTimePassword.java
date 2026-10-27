package guiOneTimePassword;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import database.Database;
import guiAdminHome.ControllerAdminHome;

/*******
 * <p> Title: ControllerOneTimePassword Class. </p>
 *
 * <p> Description: The controller for the One-Time Password page.  It implements the Admin user
 * story "set a one-time password for a user".  The Admin selects a user and a deadline, and the
 * system generates a temporary password that satisfies the password rules.  The Admin gives that
 * password to the user out of band.  When the user logs in with it (before the deadline), the
 * login page forces the user to choose a new permanent password and the one-time password is
 * cleared so it can never be used again (see guiUserLogin.ControllerUserLogin).
 *
 * The generation and deadline helpers are public and static so the automated tests can exercise
 * them without a GUI.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version
 *
 */

public class ControllerOneTimePassword {

	/**
	 * Default constructor is not used.
	 */
	public ControllerOneTimePassword() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	// The format used for the time-of-day portion of a deadline
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	/** 
	 * The length of the generated one-time password.  It must be within the 8 to 20 character
	 * range enforced by the password evaluator (passwordPopUpWindow.Model).
	 */
	public static final int ONE_TIME_PASSWORD_LENGTH = 12;

	// The character classes the password evaluator requires.  Ambiguous characters (0/O, 1/l/I)
	// are left out so the Admin can read the password to the user without confusion.
	private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
	private static final String LOWER = "abcdefghjkmnpqrstuvwxyz";
	private static final String DIGIT = "23456789";
	private static final String SPECIAL = "!@#$%^&*+=?";

	private static final SecureRandom random = new SecureRandom();


	/**********
	 * <p> Method: reloadUserList() </p>
	 *
	 * <p> Description: Protected method that re-reads the usernames from the database, places
	 * them into the ComboBox, and resets the deadline widgets to the default.</p>
	 */
	protected static void reloadUserList() {
		List<String> users = theDatabase.getUserList();
		ViewOneTimePassword.setUserList(users);
		resetDeadline();
	}


	/**********
	 * <p> Method: resetDeadline() </p>
	 *
	 * <p> Description: Protected method that sets the deadline widgets back to the default,
	 * which is Database.DEFAULT_INVITATION_HOURS from now (the same default used for
	 * invitations).</p>
	 */
	protected static void resetDeadline() {
		LocalDateTime dflt = LocalDateTime.now().plusHours(Database.DEFAULT_INVITATION_HOURS);
		ViewOneTimePassword.datepicker_Deadline.setValue(dflt.toLocalDate());
		ViewOneTimePassword.text_DeadlineTime.setText(dflt.format(TIME_FORMAT));
	}


	/**********
	 * <p> Method: doSelectUser() </p>
	 *
	 * <p> Description: Protected method invoked when the Admin chooses an entry in the ComboBox.
	 * It records the selection, reports whether the user already has a live one-time password,
	 * and enables the Generate button only for a real user.</p>
	 */
	protected static void doSelectUser() {
		String selected = ViewOneTimePassword.combobox_SelectUser.getValue();
		if (selected == null || selected.startsWith("<")) {
			ViewOneTimePassword.theSelectedUser = "";
			ViewOneTimePassword.label_Status.setText("");
			ViewOneTimePassword.button_Generate.setDisable(true);
			return;
		}
		ViewOneTimePassword.theSelectedUser = selected;
		if (theDatabase.hasActiveOneTimePassword(selected)) {
			ViewOneTimePassword.label_Status.setText("This user already has a one-time password "
					+ "that expires " + theDatabase.getOneTimePasswordDeadline(selected)
					.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm"))
					+ ". Generating a new one replaces it.");
		} else {
			ViewOneTimePassword.label_Status.setText("This user has no one-time password.");
		}
		ViewOneTimePassword.button_Generate.setDisable(false);
	}


	/**********
	 * <p> Method: String generateOneTimePassword() </p>
	 *
	 * <p> Description: Build a random password of ONE_TIME_PASSWORD_LENGTH characters that is
	 * guaranteed to contain at least one upper case letter, one lower case letter, one digit,
	 * and one special character, so it satisfies the password evaluator.  It is separated from
	 * the GUI so the automated tests can exercise it.</p>
	 *
	 * @return the generated one-time password
	 */
	public static String generateOneTimePassword() {
		char[] pw = new char[ONE_TIME_PASSWORD_LENGTH];
		String all = UPPER + LOWER + DIGIT + SPECIAL;

		// Guarantee one character from each required class in the first four positions
		pw[0] = UPPER.charAt(random.nextInt(UPPER.length()));
		pw[1] = LOWER.charAt(random.nextInt(LOWER.length()));
		pw[2] = DIGIT.charAt(random.nextInt(DIGIT.length()));
		pw[3] = SPECIAL.charAt(random.nextInt(SPECIAL.length()));
		for (int i = 4; i < pw.length; i++) {
			pw[i] = all.charAt(random.nextInt(all.length()));
		}

		// Shuffle so the required characters are not always in the same positions
		for (int i = pw.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			char t = pw[i]; pw[i] = pw[j]; pw[j] = t;
		}
		return new String(pw);
	}


	/**********
	 * <p> Method: performGenerate() </p>
	 *
	 * <p> Description: Protected method invoked by the Generate button.  It validates the
	 * deadline (reusing the invitation deadline rules), generates the password, stores it with
	 * the deadline, and displays it to the Admin exactly once.</p>
	 */
	protected static void performGenerate() {
		String target = ViewOneTimePassword.theSelectedUser;
		if (target.isEmpty()) {
			ViewOneTimePassword.alertError.setContentText("Select a user first.");
			ViewOneTimePassword.alertError.showAndWait();
			return;
		}

		// The deadline rules are the same as for invitations: present, in the future, and within
		// ControllerAdminHome.MAX_INVITATION_DAYS
		LocalDateTime deadline = ControllerAdminHome.parseInvitationDeadline(
				ViewOneTimePassword.datepicker_Deadline.getValue(),
				ViewOneTimePassword.text_DeadlineTime.getText());
		String deadlineError = ControllerAdminHome.validateInvitationDeadline(deadline);
		if (!deadlineError.isEmpty()) {
			ViewOneTimePassword.alertError.setContentText(deadlineError);
			ViewOneTimePassword.alertError.showAndWait();
			return;
		}

		String otp = generateOneTimePassword();
		if (!theDatabase.setOneTimePassword(target, otp, deadline)) {
			ViewOneTimePassword.alertError.setContentText(
					"The one-time password could not be stored. Check the user and try again.");
			ViewOneTimePassword.alertError.showAndWait();
			return;
		}

		String msg = "One-time password for " + target + ":\n\n" + otp +
				"\n\nIt can be used once, until " +
				deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm")) +
				".\nThe user must choose a new password when they log in with it.";
		System.out.println("** One-time password set for " + target + " until " + deadline);
		ViewOneTimePassword.label_Status.setText("One-time password set for " + target +
				". Give it to the user now; it will not be shown again.");
		ViewOneTimePassword.alertGenerated.setContentText(msg);
		ViewOneTimePassword.alertGenerated.showAndWait();
		resetDeadline();
	}


	/**********
	 * <p> Method: performReturn() </p>
	 *
	 * <p> Description: Return the Admin to the Admin Home page.</p>
	 */
	protected static void performReturn() {
		guiAdminHome.ViewAdminHome.displayAdminHome(ViewOneTimePassword.theStage,
				ViewOneTimePassword.theUser);
	}


	/**********
	 * <p> Method: performLogout() </p>
	 *
	 * <p> Description: Log this user out of the system and return to the login page.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewOneTimePassword.theStage);
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
