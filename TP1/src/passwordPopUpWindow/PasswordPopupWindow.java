package passwordPopUpWindow;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class PasswordPopupWindow {

    public static final double WINDOW_WIDTH = 400;
    public static final double WINDOW_HEIGHT = 450;

    protected static Stage theStage;

    // Call this from anywhere in TP1 to pop up the window and get the password back
    public static String show() {
        Pane root = new Pane();
        theStage = new Stage();
        theStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        theStage.setTitle("Enter Password");

        View.view(root);

        theStage.showAndWait();   // blocks here until Controller calls theStage.hide()
        return View.text_Password.getText();
    }
}