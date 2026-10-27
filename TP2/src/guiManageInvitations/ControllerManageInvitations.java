package guiManageInvitations;

import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import database.Database;
import entityClasses.Invitation;

/*******
 * <p> Title: ControllerManageInvitations Class. </p>
 *
 * <p> Description: The controller for the Manage Invitations page.  It loads the invitations
 * from the database, revokes the invitation the Admin selected (after confirmation), purges the
 * expired invitations, and handles the Return, Logout, and Quit buttons.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version
 *
 */

public class ControllerManageInvitations {

	/**
	 * Default constructor is not used.
	 */
	public ControllerManageInvitations() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;


	/**********
	 * <p> Method: refreshInvitationList() </p>
	 *
	 * <p> Description: Protected method that re-reads every invitation from the database and
	 * places them into the table, then updates the count shown at the top of the page.</p>
	 */
	protected static void refreshInvitationList() {
		List<Invitation> invitations = theDatabase.getInvitationList();
		ViewManageInvitations.table_Invitations.setItems(
				FXCollections.observableArrayList(invitations));
		int active = 0;
		for (Invitation i : invitations) if (!i.isExpired()) active++;
		ViewManageInvitations.label_NumberOfInvitations.setText("Outstanding: " + active +
				"   Expired: " + (invitations.size() - active));
	}


	/**********
	 * <p> Method: performRevoke() </p>
	 *
	 * <p> Description: Protected method invoked by the Revoke button.  It requires a selected
	 * row, asks "Are you sure?", and on Yes removes that invitation so the code can no longer be
	 * used.</p>
	 */
	protected static void performRevoke() {
		Invitation selected =
				ViewManageInvitations.table_Invitations.getSelectionModel().getSelectedItem();
		if (selected == null) {
			ViewManageInvitations.alertInfo.setContentText(
					"Select an invitation in the table first.");
			ViewManageInvitations.alertInfo.showAndWait();
			return;
		}

		ViewManageInvitations.alertConfirmRevoke.setContentText("Revoke the invitation for " +
				selected.getEmailAddress() + " (code " + selected.getCode() +
				")? The code will stop working immediately.");
		Optional<ButtonType> answer = ViewManageInvitations.alertConfirmRevoke.showAndWait();
		if (answer.isEmpty() || answer.get() != ButtonType.YES) return;

		if (theDatabase.removeInvitation(selected.getCode())) {
			System.out.println("** Invitation revoked: " + selected.getCode());
			ViewManageInvitations.alertInfo.setContentText("The invitation for " +
					selected.getEmailAddress() + " has been revoked.");
		} else {
			ViewManageInvitations.alertInfo.setContentText(
					"That invitation no longer exists. The list has been refreshed.");
		}
		ViewManageInvitations.alertInfo.showAndWait();
		refreshInvitationList();
	}


	/**********
	 * <p> Method: performPurge() </p>
	 *
	 * <p> Description: Protected method invoked by the Remove Expired button.  It deletes every
	 * invitation whose deadline has passed and reports how many were removed.</p>
	 */
	protected static void performPurge() {
		int removed = theDatabase.removeExpiredInvitations();
		ViewManageInvitations.alertInfo.setContentText(removed == 0 ?
				"There were no expired invitations to remove." :
				removed + " expired invitation" + (removed == 1 ? "" : "s") + " removed.");
		ViewManageInvitations.alertInfo.showAndWait();
		refreshInvitationList();
	}


	/**********
	 * <p> Method: performReturn() </p>
	 *
	 * <p> Description: Return the Admin to the Admin Home page.</p>
	 */
	protected static void performReturn() {
		guiAdminHome.ViewAdminHome.displayAdminHome(ViewManageInvitations.theStage,
				ViewManageInvitations.theUser);
	}


	/**********
	 * <p> Method: performLogout() </p>
	 *
	 * <p> Description: Log this user out of the system and return to the login page.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewManageInvitations.theStage);
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
