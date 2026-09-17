package guiUserUpdate;

import emailAddressRecognizer.EmailAddressRecognizer;
import entityClasses.User;
import javafx.stage.Stage;
import userNameRecognizer.UserNameRecognizer;

/*******
 * <p> Title: ControllerUserUpdate Class. </p>
 *
 * <p> Description: This static class supports the actions initiated by the ViewUserUpdate class.
 * Besides moving the user on to the correct home page, it holds the input validation used by
 * every field on the account update page.  Each validation method is public and static so the
 * automated tests can exercise it without a GUI.</p>
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 *
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2026-09-17 Input validation for every account update field, and the
 * 							password is no longer displayed in clear text (A.G., agupt545)
 *
 */
public class ControllerUserUpdate {
	
	/**
	 * Default constructor is not used; every method of this class is static.
	 */
	public ControllerUserUpdate() {
	}
	
	/*-********************************************************************************************

	The Controller for ViewUserUpdate

	**********************************************************************************************/

	/**
	 * The longest value accepted in any of the name fields.  Every textual input field is checked
	 * against a reasonable size limit before the value is used.
	 */
	public static final int MAX_NAME_LENGTH = 50;


	/*-********************************************************************************************

	Input validation used by the account update fields

	**********************************************************************************************/

	/**********
	 * <p> Method: String checkName(String value) </p>
	 *
	 * <p> Description: Validate a first, middle, last, or preferred first name.  A name may be
	 * cleared (an empty value is accepted, since only the last name is required by the product
	 * vision), must not exceed MAX_NAME_LENGTH characters, and may contain only letters, spaces,
	 * hyphens, and apostrophes.</p>
	 *
	 * @param value	the text the user typed
	 *
	 * @return an empty string when the value is acceptable, else a message for the user
	 */
	public static String checkName(String value) {
		if (value == null) return "";				// Nothing was entered, so nothing changes
		if (value.length() > MAX_NAME_LENGTH)
			return "A name may not exceed " + MAX_NAME_LENGTH + " characters.";
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			boolean letter = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
			if (!letter && c != ' ' && c != '-' && c != '\'')
				return "A name may contain only letters, spaces, hyphens, and apostrophes. "
						+ "The character \"" + c + "\" is not allowed.";
		}
		return "";
	}


	/**********
	 * <p> Method: String checkUserName(String value) </p>
	 *
	 * <p> Description: Validate a new UserName using the same finite state machine used when an
	 * account is first established, so the rules cannot drift apart.</p>
	 *
	 * @param value	the text the user typed
	 *
	 * @return an empty string when the value is acceptable, else a message for the user
	 */
	public static String checkUserName(String value) {
		if (value == null) return "Enter a UserName.";
		return UserNameRecognizer.checkForValidUserName(value);
	}


	/**********
	 * <p> Method: String checkEmailAddress(String value) </p>
	 *
	 * <p> Description: Validate a new email address using the same recognizer the Admin page uses
	 * when sending an invitation.</p>
	 *
	 * @param value	the text the user typed
	 *
	 * @return an empty string when the value is acceptable, else a message for the user
	 */
	public static String checkEmailAddress(String value) {
		if (value == null) return "Enter an email address.";
		return EmailAddressRecognizer.checkForValidEmailAddress(value);
	}


	/*-********************************************************************************************

	The User Interface Actions for this page

	**********************************************************************************************/


	/**********
	 * <p> Method: public goToUserHomePage(Stage theStage, User theUser) </p>
	 *
	 * <p> Description: This method is called when the user has clicked on the button to
	 * proceed to the user's home page.
	 *
	 * @param theStage specifies the JavaFX Stage for next next GUI page and it's methods
	 *
	 * @param theUser specifies the user so we go to the right page and so the right information
	 */
	protected static void goToUserHomePage(Stage theStage, User theUser) {

		// Get the roles the user selected during login
		int theRole = applicationMain.FoundationsMain.activeHomePage;

		// Use that role to proceed to that role's home page
		switch (theRole) {
		case 1:
			guiAdminHome.ViewAdminHome.displayAdminHome(theStage, theUser);
			break;
		case 2:
			guiContributor.ViewContributorHome.displayContributorHome(theStage, theUser);
			break;
		case 3:
			guiViewer.ViewViewerHome.displayViewerHome(theStage, theUser);
			break;
		case 4:
			guiCurator.ViewCuratorHome.displayCuratorHome(theStage, theUser);
			break;
		default:
			System.out.println("*** ERROR *** UserUpdate goToUserHome has an invalid role: " +
					theRole);
			System.exit(0);
		}
 	}
}
