package guiNewAccount;

import java.sql.SQLException;

import database.Database;
import entityClasses.User;
import passwordPopUpWindow.Model;
import passwordPopUpWindow.PasswordPopupWindow;
import userNameRecognizer.UserNameRecognizer;

/*******
 * <p> Title: ControllerNewAccount Class. </p>
 * 
 * <p> Description: The Java/FX-based New Account Page.  This class provides the controller actions
 * to allow the user to establish a new account after responding to an invitation and the use of a
 * one time code.
 * 
 * The controller deals with the user pressing the "User Step" button widget being click.  If also
 * supports the user click on the "Quit" button widget.
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
 * @version 1.01		2026-09-02 Validate a new UserName before database use
 * @version 1.02		2026-09-17 Validate the new password with the password evaluator, and remove
 * 							the invitation actually used so a code cannot be reused (A.G., agupt545)
 * @version 1.03		2026-09-21 Add dynamic password selection and safe duplicate-account handling
 * 							(Vishwam)
 *  
 */

public class ControllerNewAccount {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/

	/**
	 * Default constructor is not used.
	 */
	public ControllerNewAccount() {
	}
	
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * Open the shared live password checker and retain only a password satisfying every rule.
	 */
	// Vishwam, invited users now receive dynamic password help before the confirmation field.
	protected static void choosePassword() {
		String chosenPassword = PasswordPopupWindow.show();
		if (chosenPassword == null || chosenPassword.isEmpty()) {
			if (ViewNewAccount.text_Password1.getText().isEmpty()) {
				// Vishwam, restore the general chooser heading after any earlier confirmation error.
				ViewNewAccount.alertPasswordError.setHeaderText(
						"The password does not satisfy the requirements.");
				ViewNewAccount.alertPasswordError.setContentText(
						"Choose a valid password before creating the account.");
				ViewNewAccount.alertPasswordError.showAndWait();
			}
			return;
		}

		ViewNewAccount.text_Password1.setText(chosenPassword);
		ViewNewAccount.text_Password2.setText("");
		ViewNewAccount.text_Password2.requestFocus();
	}
	
	/**********
	 * <p> Method: public doCreateUser() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the User Setup
	 * button.  This method checks the input fields to see that they are valid.  If so, it then
	 * creates the account by adding information to the database.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 */	
	protected static void doCreateUser() {
		
		// Fetch the username and password. (We use the first of the two here, but we will validate
		// that the two password fields are the same before we do anything with it.)
		String username = ViewNewAccount.text_Username.getText();
		String password = ViewNewAccount.text_Password1.getText();

		// A new account establishes a new UserName, so apply the complete UserName FSM before
		// using the input to construct a User or access the database.
		String userNameError = UserNameRecognizer.checkForValidUserName(username);
		if (!userNameError.isEmpty()) {
			ViewNewAccount.alertUserNameError.setHeaderText("The new UserName is not valid.");
			ViewNewAccount.alertUserNameError.setContentText(
					formatUserNameError(userNameError, username));
			ViewNewAccount.alertUserNameError.showAndWait();
			return;
		}

		// Vishwam, reject a duplicate as a validation error instead of exiting the application.
		if (theDatabase.doesUserExist(username)) {
			ViewNewAccount.alertUserNameError.setHeaderText("That UserName is already in use.");
			ViewNewAccount.alertUserNameError.setContentText(
					"Choose a different UserName and try again.");
			ViewNewAccount.alertUserNameError.showAndWait();
			return;
		}
		
		// Apply the password requirements (upper case, lower case, digit, special character, and
		// a length of 8 to 20 characters) before the password is stored
		String passwordError = Model.evaluatePassword(password);
		if (!passwordError.isEmpty()) {
			ViewNewAccount.text_Password1.setText("");
			ViewNewAccount.text_Password2.setText("");
			// Vishwam, avoid retaining a prior confirmation-specific alert heading.
			ViewNewAccount.alertPasswordError.setHeaderText(
					"The password does not satisfy the requirements.");
			ViewNewAccount.alertPasswordError.setContentText(
					"The password is not acceptable: " + passwordError);
			ViewNewAccount.alertPasswordError.showAndWait();
			return;
		}

		// Vishwam, the confirmation field receives its own explicit 20-character bound before
		// equality comparison, role selection, or any database write.
		String confirmationError = Model.checkPasswordConfirmation(
				ViewNewAccount.text_Password2.getText());
		if (!confirmationError.isEmpty()) {
			ViewNewAccount.text_Password2.setText("");
			ViewNewAccount.alertPasswordError.setHeaderText(
					"The password confirmation is too long.");
			ViewNewAccount.alertPasswordError.setContentText(confirmationError);
			ViewNewAccount.alertPasswordError.showAndWait();
			return;
		}
		
		// Display key information to the log
		System.out.println("** Account for Username: " + username + "; theInvitationCode: "+
				ViewNewAccount.theInvitationCode + "; email address: " + 
				ViewNewAccount.emailAddress + "; Role: " + ViewNewAccount.theRole);
		
		// Initialize local variables that will be created during this process
		int roleCode = 0;
		User user = null;

		// Make sure the two passwords are the same.	
		if (ViewNewAccount.text_Password1.getText().
				compareTo(ViewNewAccount.text_Password2.getText()) == 0) {
			
			// The passwords match so we will set up the role and the User object base on the 
			// information provided in the invitation
			if (ViewNewAccount.theRole.compareTo("Admin") == 0) {
				roleCode = 1;
				user = new User(username, password, "", "", "", "", "", true, false, false, false);
			} else if (ViewNewAccount.theRole.compareTo("Contributor") == 0) {
				roleCode = 2;
				user = new User(username, password, "", "", "", "", "", false, true, false, false);
			} else if (ViewNewAccount.theRole.compareTo("Viewer") == 0) {
				roleCode = 3;
				user = new User(username, password, "", "", "", "", "", false, false, true, false);
			} else if (ViewNewAccount.theRole.compareTo("Curator") == 0) {
				roleCode = 4;
				user = new User(username, password, "", "", "", "", "", false, false, false, true);
			} else {
				System.out.println(
						"**** Trying to create a New Account for a role that does not exist!");
				System.exit(0);
			}
			
			// Unlike the FirstAdmin, we know the email address, so set that into the user as well.
        	user.setEmailAddress(ViewNewAccount.emailAddress);

        	// Inform the system about which role will be played
			applicationMain.FoundationsMain.activeHomePage = roleCode;
			
			// Create the account based on user and proceed to the user account update page
			try {
				// Create a new User object with the pre-set role and register in the database
				theDatabase.register(user);
				if (!theDatabase.authenticateSession(user.getUserName(), user.getPassword())) {
					throw new SQLException("Unable to start the new account session");
				}
			} catch (SQLException e) {
				System.err.println("*** ERROR *** Database error: " + e.getMessage());
				e.printStackTrace();
				// Vishwam, keep the invitation and application available after a failed write.
				theDatabase.clearAuthenticatedSession();
				ViewNewAccount.alertUserNameError.setHeaderText(
						"The account could not be created.");
				ViewNewAccount.alertUserNameError.setContentText(
						"No account was created. Review the entries and try again.");
				ViewNewAccount.alertUserNameError.showAndWait();
				return;
			}

            // The account has been set, so remove the invitation from the system
            theDatabase.removeInvitationAfterUse(ViewNewAccount.theInvitationCode);
            
            // Set the database so it has this user and the current user
            theDatabase.getUserAccountDetails(username);

            // Navigate to the Welcome Login Page
            guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewNewAccount.theStage, user);
		}
		else {
			// The two passwords are NOT the same, so clear the passwords, explain the passwords
			// must be the same, and clear the message as soon as the first character is typed.
			ViewNewAccount.text_Password1.setText("");
			ViewNewAccount.text_Password2.setText("");
			ViewNewAccount.alertUsernamePasswordError.showAndWait();
		}
	}

	/**********
	 * <p> Method: formatUserNameError(String message, String input) </p>
	 *
	 * <p> Description: Add the input and the one-based error position to the detailed message
	 * produced by the UserName recognizer.  Reporting the position helps the user correct the
	 * input without weakening the validation rule.</p>
	 *
	 * @param message specifies the detailed recognizer error
	 * @param input specifies the rejected UserName
	 * @return the message displayed by the UserName error alert
	 */
	private static String formatUserNameError(String message, String input) {
		int errorPosition = UserNameRecognizer.userNameRecognizerIndexofError + 1;
		return message.trim() + "\n\nInput: " + input + "\nError position: " + errorPosition;
	}

	
	/**********
	 * <p> Method: public performQuit() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
	 * this terminates the execution of the application.  All important data must be stored in the
	 * database, so there is no cleanup required.  (This is important so we can minimize the impact
	 * of crashed.)
	 * 
	 */	
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	
}
