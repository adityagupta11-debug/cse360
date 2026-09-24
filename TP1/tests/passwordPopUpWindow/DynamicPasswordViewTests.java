package passwordPopUpWindow;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Pane;

/*******
 * <p>Title: DynamicPasswordViewTests</p>
 *
 * <p>Description: JavaFX regression checks for the reusable dynamic password view.  These tests
 * verify the user-visible state transitions that pure password-policy tests cannot observe.</p>
 *
 * @author Vishwam
 * @version 1.00 2026-09-21 Initial version
 */
public class DynamicPasswordViewTests {

	private static int passed;
	private static int failed;

	// Vishwam, run all widget access on the JavaFX application thread.
	public static void main(String[] args) throws Exception {
		CountDownLatch finished = new CountDownLatch(1);
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Platform.startup(() -> {
			try {
				runChecks();
			} catch (Throwable failure) {
				problem.set(failure);
			} finally {
				finished.countDown();
			}
		});
		finished.await();
		Platform.exit();

		if (problem.get() != null) throw new RuntimeException(problem.get());
		System.out.printf("RESULT: %d dynamic-password view checks passed; %d failed.%n",
				passed, failed);
		if (failed > 0) System.exit(1);
	}

	// Vishwam, exercise valid, empty, excessive, reopened, and invalid-character transitions.
	private static void runChecks() {
		Pane root = new Pane();
		View.view(root);
		View.resetForShow();

		check("P01 password entry is masked", View.text_Password instanceof PasswordField);

		View.text_Password.setText("Aa!15678");
		check("P02 valid input enables Finish", !View.button_Finish.isDisabled());

		View.text_Password.setText("");
		check("P03 erasing valid input disables Finish",
				View.button_Finish.isDisabled() && View.validPassword.getText().isEmpty());

		View.text_Password.setText("Aa!15678");
		View.text_Password.setText("Aa!1" + "b".repeat(Model.MAX_PASSWORD_LENGTH));
		check("P04 excessive paste clears stale valid state",
				View.button_Finish.isDisabled() && !Model.foundUpperCase
				&& !Model.foundLowerCase && !Model.foundNumericDigit
				&& !Model.foundSpecialChar && !Model.foundLongEnough
				&& !Model.isShortEnough());

		View.resetForShow();
		check("P05 reopening starts empty and disabled",
				View.text_Password.getText().isEmpty() && View.button_Finish.isDisabled()
				&& View.validPassword.getText().isEmpty());

		View.text_Password.setText("Aa!\t5678");
		check("P06 error marker never echoes password text",
				View.button_Finish.isDisabled()
				&& !View.errPasswordPart1.getText().contains("Aa!"));
	}

	// Vishwam, report each GUI assertion in the same concise form as the existing repository tests.
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
