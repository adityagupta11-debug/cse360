package passwordPopUpWindow;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/*******
 * <p> Title: PasswordPopupWindow Class. </p>
 *
 * <p> Description: The entry point used by the rest of the application to obtain a validated
 * password from the user.  It opens a modal window containing the dynamic password evaluator
 * (the View, Model, and Controller in this package), blocks until the user presses "Finish and
 * Save The Password" or closes the window, and returns the password.
 *
 * The Finish button is only enabled once every password requirement is satisfied, so the
 * returned password is valid whenever the user pressed Finish.  If the user closes the window
 * instead, an empty string is returned so the caller can tell that no password was chosen.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @version 1.00		2026-09-16 Initial version (Joshua Luther)
 * @version 1.01		2026-09-17 Reset the field on each use and return "" when the window is
 * 							closed without finishing (A.G., agupt545)
 */
public class PasswordPopupWindow {

    public static final double WINDOW_WIDTH = 400;
    public static final double WINDOW_HEIGHT = 450;

    protected static Stage theStage;

    // Set by the Controller when the Finish button is pressed; false if the window was closed
    protected static boolean finished = false;

    /**********
     * <p> Method: String show() </p>
     *
     * <p> Description: Pop up the password window and wait for the user.  Call this from
     * anywhere in TP1 to obtain a validated password.</p>
     *
     * @return the validated password, or an empty string if the window was closed without
     * pressing Finish
     */
    public static String show() {
        Pane root = new Pane();
        theStage = new Stage();
        theStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        theStage.setTitle("Enter Password");

        View.view(root);
        View.text_Password.setText("");		// Start empty; never show a previous password
        finished = false;

        theStage.showAndWait();   // blocks here until Controller calls theStage.hide()
        if (!finished) return "";
        return View.text_Password.getText();
    }
}
