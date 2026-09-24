package guiUserUpdate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.User;
import guiUserLogin.ViewUserLogin;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/*******
 * <p>Title: FirstUserReloginTests</p>
 *
 * <p>Description: JavaFX regression checks for the Initial User Story requiring the first Admin
 * to return to a fresh login after completing account information.</p>
 *
 * @author Vishwam
 * @version 1.00 2026-09-21 Initial version
 */
public class FirstUserReloginTests {

	private static int passed;
	private static int failed;

	// Vishwam, use a private in-memory database so the onboarding test never touches user data.
	public static void main(String[] args) throws Exception {
		Database database = new Database(
				"jdbc:h2:mem:first_user_relogin;DB_CLOSE_DELAY=-1");
		database.connectToDatabase();
		User firstAdmin = new User("firstAdmin", "Aa!15678", "", "", "", "", "",
				true, false, false, false);
		database.register(firstAdmin);
		if (!database.authenticateSession(firstAdmin.getUserName(), firstAdmin.getPassword()))
			throw new IllegalStateException("Test setup could not authenticate the first Admin");
		FoundationsMain.database = database;

		CountDownLatch finished = new CountDownLatch(1);
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Platform.startup(() -> {
			try {
				runChecks(database, firstAdmin);
			} catch (Throwable failure) {
				problem.set(failure);
			} finally {
				finished.countDown();
			}
		});
		finished.await();
		Platform.exit();
		database.closeConnection();

		if (problem.get() != null) throw new RuntimeException(problem.get());
		System.out.printf("RESULT: %d first-user relogin checks passed; %d failed.%n",
				passed, failed);
		if (failed > 0) System.exit(1);
	}

	// Vishwam, verify both the secure first-user exit and singleton reset for ordinary updates.
	private static void runChecks(Database database, User firstAdmin) throws Exception {
		Stage stage = new Stage();
		ViewUserUpdate.displayFirstUserUpdate(stage, firstAdmin);
		Button completionButton = findButton(stage.getScene().getRoot(), "Log In Again");
		check("R01 first-user completion clearly requires another login",
				completionButton != null);

		if (completionButton != null) completionButton.fire();
		check("R02 first-user completion returns to the login scene",
				stage.getScene() == ViewUserLogin.theUserLoginScene);
		check("R03 first-user completion clears the authenticated session",
				!database.isAuthenticatedAdmin());

		database.authenticateSession(firstAdmin.getUserName(), firstAdmin.getPassword());
		ViewUserUpdate.displayUserUpdate(stage, firstAdmin);
		check("R04 ordinary display resets the singleton to the role-home action",
				findButton(stage.getScene().getRoot(), "Proceed to the User Home Page") != null);
		stage.hide();
	}

	// Vishwam, locate the action by its user-visible text without exposing production controls.
	private static Button findButton(Node node, String textFragment) {
		if (node instanceof Button button && button.getText().contains(textFragment)) return button;
		if (node instanceof Parent parent) {
			for (Node child : parent.getChildrenUnmodifiable()) {
				Button found = findButton(child, textFragment);
				if (found != null) return found;
			}
		}
		return null;
	}

	// Vishwam, keep reporting consistent with the repository's other standalone feature tests.
	private static void check(String name, boolean condition) {
		if (condition) {
			System.out.println("PASS " + name);
			passed++;
		} else {
			System.out.println("FAIL " + name);
			failed++;
		}
	}
}
