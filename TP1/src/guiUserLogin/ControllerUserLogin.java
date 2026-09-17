package guiUserLogin;

import database.Database;
import entityClasses.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import passwordPopUpWindow.PasswordPopupWindow;

/*******
 * <p> Title: ControllerUserLogin Class. </p>
 * 
 * <p> Description: The Java/FX-based User Login Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This controller determines if the log in is valid.  If so set up the link to the database, 
 * determines how many roles this user is authorized to play, and the calls one the of the array of
 * role home pages if there is only one role.  If there are more than one role, it setup up and
 * calls the multiple roles dispatch page for the user to determine which role the user wants to
 * play.
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
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 * @version 1.02		2026-09-02 Bound login UserName input before database use
 * @version 1.03		2026-09-17 Accept an Admin-issued one-time password once and require the
 * 							user to choose a new password before continuing (A.G., agupt545)
 */

public class ControllerUserLogin {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/

	/**
	 * Default constructor is not used.
	 */
	public ControllerUserLogin() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	private static Stage theStage;	
	
	// The longest password the login page will examine.  Anything longer cannot match a stored
	// password, so it is rejected before the database is used.
	private static final int MAX_PASSWORD_INPUT = 64;
	
	// Alerts used by the one-time password flow
	private static Alert alertOneTimePassword = new Alert(AlertType.INFORMATION);
	
	
	/**********
	 * <p> Method: public doLogin() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Login button. This
	 * method checks the username and password to see if they are valid.  If so, it then logs that
	 * user in my determining which role to use.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 * @param ts specifies the JavaFX Stage on which the next page will be displayed
	 * 
	 */	
	protected static void doLogin(Stage ts) {
		theStage = ts;
		String username = ViewUserLogin.text_Username.getText();
		String password = ViewUserLogin.text_Password.getText();
    	boolean loginResult = false;

		// The login page references an existing UserName rather than creating a new one.  Bound
		// both inputs before database use, but retain the generic credential error so the page
		// does not reveal whether a specific UserName exists.
		if (username.length() > 32 || password.length() > MAX_PASSWORD_INPUT) {
			ViewUserLogin.alertUsernamePasswordError.setContentText(
					"Incorrect username/password. Try again!");
			ViewUserLogin.alertUsernamePasswordError.showAndWait();
			return;
		}
    	
		// Fetch the user and verify the username
     	if (theDatabase.getUserAccountDetails(username) == false) {
     		// Don't provide too much information.  Don't say the username is invalid or the
     		// password is invalid.  Just say the pair is invalid.
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		// System.out.println("*** Username is valid");
		
		// Check to see that the login password matches the account password.  If it does not, the
		// user may be logging in with a one-time password issued by an Admin, so that is checked
		// before the credentials are rejected.
    	String actualPassword = theDatabase.getCurrentPassword();
    	
    	if (password.compareTo(actualPassword) != 0) {
    		if (!theDatabase.loginWithOneTimePassword(username, password)) {
    			ViewUserLogin.alertUsernamePasswordError.setContentText(
    					"Incorrect username/password. Try again!");
    			ViewUserLogin.alertUsernamePasswordError.showAndWait();
    			return;
    		}
    		
    		// The one-time password was accepted.  The user must replace it with a password of
    		// their own before they are allowed any further, and the one-time password is then
    		// removed so it can never be used a second time.
    		if (!forcePasswordReset(username)) return;
    		password = theDatabase.getCurrentPassword();
    	}
		// System.out.println("*** Password is valid for this user");
		
		// Establish this user's details
    	User user = new User(username, password, theDatabase.getCurrentFirstName(), 
    			theDatabase.getCurrentMiddleName(), theDatabase.getCurrentLastName(), 
    			theDatabase.getCurrentPreferredFirstName(), theDatabase.getCurrentEmailAddress(), 
    			theDatabase.getCurrentAdminRole(), 
    			theDatabase.getCurrentContributorRole(), theDatabase.getCurrentViewerRole(),
    			theDatabase.getCurrentCuratorRole());
    	
    	// See which home page dispatch to use
		int numberOfRoles = theDatabase.getNumberOfRoles(user);		
		// System.out.println("*** The number of roles: "+ numberOfRoles);
		if (numberOfRoles == 1) {
			// Single Account Home Page - The user has no choice here
			
			// Admin role
			if (user.getAdminRole()) {
				loginResult = theDatabase.loginAdmin(user);
				if (loginResult) {
					guiAdminHome.ViewAdminHome.displayAdminHome(theStage, user);
				}
			} else if (user.getContributorRole()) {
				loginResult = theDatabase.loginContributor(user);
				if (loginResult) {
					guiContributor.ViewContributorHome.displayContributorHome(theStage, user);
				}
			} else if (user.getViewerRole()) {
				loginResult = theDatabase.loginViewer(user);
				if (loginResult) {
					guiViewer.ViewViewerHome.displayViewerHome(theStage, user);
				}
			} else if (user.getCuratorRole()) {
				loginResult = theDatabase.loginCurator(user);
				if (loginResult) {
					guiCurator.ViewCuratorHome.displayCuratorHome(theStage, user);
				}
				// Other roles
			} else {
				System.out.println("***** UserLogin goToUserHome request has an invalid role");
			}
		} else if (numberOfRoles > 1) {
			// Multiple Account Home Page - The user chooses which role to play
			// System.out.println("*** Going to displayMultipleRoleDispatch");
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.
				displayMultipleRoleDispatch(theStage, user);
		}
	}
	
	
	/**********
	 * <p> Method: boolean forcePasswordReset(String username) </p>
	 * 
	 * <p> Description: This method is called when a user has logged in with a one-time password
	 * issued by an Admin.  It explains what is required, opens the dynamic password evaluator so
	 * the user can choose a password that satisfies every requirement, saves that password, and
	 * clears the one-time password so it can never be used again.
	 * 
	 * If the user closes the password window without choosing a password, nothing is changed,
	 * the login does not proceed, and the one-time password remains usable until its deadline.
	 * </p>
	 * 
	 * @param username is the username of the user who used the one-time password
	 * 
	 * @return true when a new password was saved and the login may continue, else false
	 * 
	 */	
	private static boolean forcePasswordReset(String username) {
		alertOneTimePassword.setTitle("One-Time Password Accepted");
		alertOneTimePassword.setHeaderText("Choose a new password");
		alertOneTimePassword.setContentText("This password can only be used once. "
				+ "Please set a new password now.");
		alertOneTimePassword.showAndWait();
		
		String newPassword = PasswordPopupWindow.show();
		if (newPassword == null || newPassword.isEmpty()) {
			// The user closed the window without choosing a password, so nothing changes
			alertOneTimePassword.setTitle("Password Not Changed");
			alertOneTimePassword.setHeaderText("You must set a new password");
			alertOneTimePassword.setContentText("Log in with the one-time password again and "
					+ "choose a new password to continue.");
			alertOneTimePassword.showAndWait();
			return false;
		}
		
		theDatabase.updatePassword(username, newPassword);
		theDatabase.clearOneTimePassword(username);		// It can never be used again
		theDatabase.getUserAccountDetails(username);	// Refresh the cached current user
		System.out.println("** A new password was set for " + username + 
				" after a one-time password login.");
		
		alertOneTimePassword.setTitle("Password Changed");
		alertOneTimePassword.setHeaderText("Your new password has been saved");
		alertOneTimePassword.setContentText("Use it the next time you log in.");
		alertOneTimePassword.showAndWait();
		return true;
	}
	
		
	/**********
	 * <p> Method: setup() </p>
	 * 
	 * <p> Description: This method is called to reset the page and then populate it with new
	 * content for the new user.</p>
	 * 
	 * @param theStage specifies the JavaFX Stage on which the New Account page will be displayed
	 * 
	 * @param invitationCode specifies the invitation code the potential user typed
	 * 
	 */
	protected static void doSetupAccount(Stage theStage, String invitationCode) {
		guiNewAccount.ViewNewAccount.displayNewAccount(theStage, invitationCode);
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
