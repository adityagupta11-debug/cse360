package guiDeleteUser;

import java.util.List;
import java.util.Optional;
import java.sql.SQLException;
import java.util.function.Predicate;
import database.Database.DeletionCandidate;
import database.Database.DeletionResult;

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
 * @author Kanish Garg - transactional deletion integration
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
    private static DeletionCandidate selectedIdentity;
    private static final ModelDeleteUser model = new ModelDeleteUser(theDatabase);


	/**********
	 * <p> Method: reloadUserList() </p>
	 *
	 * <p> Description: Protected method that re-reads the usernames from the database and places
	 * them into the ComboBox with the placeholder entry selected.</p>
	 */
	protected static void reloadUserList() {
		selectedIdentity = null;
        try {
            List<String> users = new java.util.ArrayList<>();
            users.add("<Select a User>");
            for (DeletionCandidate candidate : model.choices()) users.add(candidate.username());
            ViewDeleteUser.setUserList(users);
        } catch (SQLException failure) {
            ViewDeleteUser.setUserList(java.util.List.of("<Select a User>"));
            ViewDeleteUser.label_SelectedDetails.setText("An active administrator session is required. Log in again.");
        }
	}


	/**********
	 * <p> Method: doSelectUser() </p>
	 *
	 * <p> Description: Protected method invoked when the Admin chooses an entry in the ComboBox.
	 * The selected user's details are shown so the Admin can confirm the right account was
	 * chosen, and the Delete button is enabled only for a real user.</p>
	 */
	protected static void doSelectUser() {
		selectedIdentity = null;
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
		try {
            selectedIdentity = model.choices().stream().filter(item -> item.username().equals(selected)).findFirst().orElse(null);
        } catch (SQLException failure) { selectedIdentity = null; }
        ViewDeleteUser.button_Delete.setDisable(selectedIdentity == null);
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
		if (target == null || target.isEmpty() || target.length() > 32 || target.startsWith("<"))
			return "Select a user first.";
		if (requester != null && target.compareTo(requester) == 0)
			return "You cannot delete the account you are currently using.";
		if (requester == null || !db.userIsAdmin(requester))
            return "An administrator is required.";
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
    /** Captures the stable selection before confirmation and delegates to the model. */
    protected static DeletionResult request(ModelDeleteUser model, DeletionCandidate selected,
            Predicate<DeletionCandidate> confirm) throws SQLException {
        if (selected == null) return DeletionResult.NO_SELECTION;
        return model.remove(selected, confirm.test(selected));
    }

    protected static void performDelete() {
        DeletionCandidate captured = selectedIdentity;
        try {
            DeletionResult result = request(model, captured, candidate -> {
                ViewDeleteUser.alertConfirmDelete.setContentText("Delete account \"" + candidate.username()
                        + "\" (ID " + candidate.id() + ")? This permanently removes access.");
                Optional<ButtonType> answer = ViewDeleteUser.alertConfirmDelete.showAndWait();
                return answer.isPresent() && answer.get() == ButtonType.YES;
            });
            if (result == DeletionResult.CANCELLED) return;
            if (result == DeletionResult.DELETED) {
                ViewDeleteUser.alertDeleteDone.setContentText(ModelDeleteUser.message(result));
                ViewDeleteUser.alertDeleteDone.showAndWait();
                reloadUserList();
            } else {
                ViewDeleteUser.alertDeleteRefused.setContentText(ModelDeleteUser.message(result));
                ViewDeleteUser.alertDeleteRefused.showAndWait();
                if (result == DeletionResult.NOT_FOUND) reloadUserList();
            }
        } catch (SQLException failure) {
            ViewDeleteUser.alertDeleteRefused.setContentText("Unable to delete the account. Reload the page and try again.");
            ViewDeleteUser.alertDeleteRefused.showAndWait();
        }
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
