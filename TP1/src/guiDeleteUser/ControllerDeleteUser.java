package guiDeleteUser;

import java.util.List;
import java.util.Optional;

import javafx.scene.control.ButtonType;
import database.Database;
import entityClasses.User;
import guiListUsers.ControllerListUsers;

/*******
 * <p> Title: ControllerDeleteUser Class. </p>
 *
 * <p> Description: The controller for the Delete User page.  It loads the list of users, shows
 * the details of the selected user, applies the policy checks, asks the Admin to confirm, and
 * asks the database to delete the user.  The policy check is a public static method so the
 * automated tests can exercise it without a GUI.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version
 *
 */

public class ControllerDeleteUser {

	/**
	 * Default constructor is not used.
	 */
	public ControllerDeleteUser() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;


	/**********
	 * <p> Method: reloadUserList() </p>
	 *
	 * <p> Description: Protected method that re-reads the usernames from the database and places
	 * them into the ComboBox with the placeholder entry selected.</p>
	 */
	protected static void reloadUserList() {
		List<String> users = theDatabase.getUserList();
		ViewDeleteUser.setUserList(users);
	}


	/**********
	 * <p> Method: doSelectUser() </p>
	 *
	 * <p> Description: Protected method invoked when the Admin chooses an entry in the ComboBox.
	 * The selected user's details are shown so the Admin can confirm the right account was
	 * chosen, and the Delete button is enabled only for a real user.</p>
	 */
	protected static void doSelectUser() {
		String selected = ViewDeleteUser.combobox_SelectUser.getValue();
		if (selected == null || selected.startsWith("<")) {
			ViewDeleteUser.theSelectedUser = "";
			ViewDeleteUser.label_SelectedDetails.setText("");
			ViewDeleteUser.button_Delete.setDisable(true);
			return;
		}
		ViewDeleteUser.theSelectedUser = selected;

		// Look the user up and display the details without disturbing the logged in user's data
		User found = null;
		for (User u : theDatabase.getAllUsers()) {
			if (u.getUserName().compareTo(selected) == 0) { found = u; break; }
		}
		if (found == null) {
			ViewDeleteUser.label_SelectedDetails.setText("That user no longer exists.");
			ViewDeleteUser.button_Delete.setDisable(true);
			return;
		}
		String name = ControllerListUsers.fullName(found);
		ViewDeleteUser.label_SelectedDetails.setText(
				"Username: " + found.getUserName() + "\n" +
				"Name: " + (name.isEmpty() ? "(not set)" : name) + "\n" +
				"Email address: " + ControllerListUsers.blankIfNull(found.getEmailAddress()) + "\n" +
				"Roles: " + ControllerListUsers.rolesAsText(found));
		ViewDeleteUser.button_Delete.setDisable(false);
	}


	/**********
	 * <p> Method: String checkDeletePolicy(Database db, String requester, String target) </p>
	 *
	 * <p> Description: Apply the rules that decide whether a deletion may proceed: a user must be
	 * selected, the Admin may not delete their own account, the user must exist, and the last
	 * remaining Admin may not be deleted.  It is separated from the GUI so the automated tests
	 * can exercise it.</p>
	 *
	 * @param db		the database to consult
	 * @param requester	the username of the Admin asking for the deletion
	 * @param target	the username of the account to be deleted
	 *
	 * @return an empty string if the deletion may proceed, else a message for the Admin
	 */
	public static String checkDeletePolicy(Database db, String requester, String target) {
		if (target == null || target.isEmpty() || target.startsWith("<"))
			return "Select a user first.";
		if (requester != null && target.compareTo(requester) == 0)
			return "You cannot delete the account you are currently using.";
		if (!db.doesUserExist(target))
			return "There is no user with that username.";
		if (db.userIsAdmin(target) && db.getNumberOfAdmins() <= 1)
			return "This user is the only Admin. Give another user the Admin role first.";
		return "";
	}


	/**********
	 * <p> Method: performDelete() </p>
	 *
	 * <p> Description: Protected method invoked by the Delete button.  It applies the policy,
	 * asks "Are you sure?", and on Yes removes the user and refreshes the page.</p>
	 */
	protected static void performDelete() {
		String target = ViewDeleteUser.theSelectedUser;
		String problem = checkDeletePolicy(theDatabase, ViewDeleteUser.theUser.getUserName(),
				target);
		if (!problem.isEmpty()) {
			ViewDeleteUser.alertDeleteRefused.setContentText(problem);
			ViewDeleteUser.alertDeleteRefused.showAndWait();
			return;
		}

		// Ask the Admin to confirm before doing anything irreversible
		ViewDeleteUser.alertConfirmDelete.setContentText("Delete the user \"" + target +
				"\"? This cannot be undone.");
		Optional<ButtonType> answer = ViewDeleteUser.alertConfirmDelete.showAndWait();
		if (answer.isEmpty() || answer.get() != ButtonType.YES) return;

		if (theDatabase.deleteUser(target)) {
			System.out.println("** User deleted: " + target);
			ViewDeleteUser.alertDeleteDone.setContentText("The user \"" + target +
					"\" has been removed from the system.");
			ViewDeleteUser.alertDeleteDone.showAndWait();
		} else {
			ViewDeleteUser.alertDeleteRefused.setContentText(
					"The database refused to delete \"" + target + "\".");
			ViewDeleteUser.alertDeleteRefused.showAndWait();
		}
		reloadUserList();
	}


	/**********
	 * <p> Method: performReturn() </p>
	 *
	 * <p> Description: Return the Admin to the Admin Home page.</p>
	 */
	protected static void performReturn() {
		guiAdminHome.ViewAdminHome.displayAdminHome(ViewDeleteUser.theStage,
				ViewDeleteUser.theUser);
	}


	/**********
	 * <p> Method: performLogout() </p>
	 *
	 * <p> Description: Log this user out of the system and return to the login page.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewDeleteUser.theStage);
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
