package guiOneTimePassword;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;

/*******
 * <p> Title: ViewOneTimePassword Class. </p>
 *
 * <p> Description: The Java/FX-based One-Time Password Page.  This page implements the Admin
 * user story "set a one-time password".  The Admin selects a user, chooses the deadline after
 * which the temporary password stops working, and presses Generate.  The generated password is
 * shown once so the Admin can pass it to the user.
 *
 * The class has been written using a singleton design pattern and is the View portion of the
 * Model, View, Controller pattern.  All accesses to this page start by invoking the static
 * method displayOneTimePassword.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version
 *
 */

public class ViewOneTimePassword {

	/*-*******************************************************************************************

	Attributes

	*/

	// These are the application values required by the user interface
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

	// GUI Area 1: It informs the user about the purpose of this page and whose account is in use
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();

	// This is a separator and it is used to partition the GUI for various tasks
	private static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI Area 2: Select the user, choose the deadline, and generate the password
	protected static Label label_SelectUser = new Label("Select the user who needs a one-time password:");
	protected static ComboBox <String> combobox_SelectUser = new ComboBox <String>();
	protected static Label label_Status = new Label("");
	protected static Label label_Deadline = new Label("Expires on");
	protected static DatePicker datepicker_Deadline = new DatePicker();
	protected static Label label_DeadlineTime = new Label("at (HH:mm, 24-hour)");
	protected static TextField text_DeadlineTime = new TextField();
	protected static Button button_Generate = new Button("Generate One-Time Password");
	protected static Alert alertError = new Alert(AlertType.INFORMATION);
	protected static Alert alertGenerated = new Alert(AlertType.INFORMATION);

	// This is a separator and it is used to partition the GUI for various tasks
	private static Line line_Separator4 = new Line(20, 525, width-20, 525);

	// GUI Area 3: Return, Logout, and Quit
	protected static Button button_Return = new Button("Return");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// These attributes are used to configure the page and populate it with this user's information
	private static ViewOneTimePassword theView;	// Used to determine if instantiation is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us
	private static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User (an Admin)
	private static Scene theOneTimePasswordScene;	// The shared Scene each invocation populates
	protected static String theSelectedUser = "";	// The user chosen in the ComboBox


	/*-*******************************************************************************************

	Constructors

	*/

	/**********
	 * <p> Method: displayOneTimePassword(Stage ps, User user) </p>
	 *
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the One-Time Password page to be displayed.  It instantiates the singleton if needed,
	 * reloads the list of users, resets the deadline, and shows the Scene.</p>
	 *
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 *
	 * @param user specifies the Admin using this page
	 *
	 */
	public static void displayOneTimePassword(Stage ps, User user) {
		theStage = ps;
		theUser = user;

		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewOneTimePassword();

		// Populate the dynamic aspects of the GUI
		label_UserDetails.setText("User: " + theUser.getUserName());
		ControllerOneTimePassword.reloadUserList();

		theStage.setTitle("CSE 360 Foundation Code: One-Time Password Page");
		theStage.setScene(theOneTimePasswordScene);
		theStage.show();
	}


	/**********
	 * <p> Method: ViewOneTimePassword() </p>
	 *
	 * <p> Description: This private constructor initializes all the elements of the graphical
	 * user interface.  It is a singleton and is only performed once.</p>
	 */
	private ViewOneTimePassword() {
		theRootPane = new Pane();
		theOneTimePasswordScene = new Scene(theRootPane, width, height);

		// GUI Area 1
		label_PageTitle.setText("Set a One-Time Password");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

		// GUI Area 2
		setupLabelUI(label_SelectUser, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 120);
		setupComboBoxUI(combobox_SelectUser, "Dialog", 16, 250, 20, 160);
		combobox_SelectUser.setOnAction((_) -> {ControllerOneTimePassword.doSelectUser(); });

		setupLabelUI(label_Status, "Arial", 16, width-40, Pos.TOP_LEFT, 20, 210);
		label_Status.setWrapText(true);

		// The deadline: a date picker plus a 24-hour time of day (same widgets as invitations)
		setupLabelUI(label_Deadline, "Arial", 16, 100, Pos.BASELINE_LEFT, 20, 285);
		datepicker_Deadline.setStyle("-fx-font: 16 Dialog;");
		datepicker_Deadline.setPrefWidth(170);
		datepicker_Deadline.setLayoutX(130);
		datepicker_Deadline.setLayoutY(280);
		datepicker_Deadline.setEditable(false);				// Force use of the calendar popup
		setupLabelUI(label_DeadlineTime, "Arial", 16, 180, Pos.BASELINE_LEFT, 315, 285);
		setupTextUI(text_DeadlineTime, "Arial", 16, 80, Pos.CENTER, 490, 280, true);
		text_DeadlineTime.setPromptText("HH:mm");

		setupButtonUI(button_Generate, "Dialog", 18, 300, Pos.CENTER, 20, 340);
		button_Generate.setOnAction((_) -> {ControllerOneTimePassword.performGenerate(); });
		button_Generate.setDisable(true);					// Enabled once a user is chosen

		alertError.setTitle("One-Time Password");
		alertError.setHeaderText("The one-time password was not set");

		alertGenerated.setTitle("One-Time Password");
		alertGenerated.setHeaderText("One-time password generated");

		// GUI Area 3
		setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
		button_Return.setOnAction((_) -> {ControllerOneTimePassword.performReturn(); });

		setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
		button_Logout.setOnAction((_) -> {ControllerOneTimePassword.performLogout(); });

		setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
		button_Quit.setOnAction((_) -> {ControllerOneTimePassword.performQuit(); });

		// Place all of the widget items into the Root Pane's list of children
		theRootPane.getChildren().addAll(
				label_PageTitle, label_UserDetails, line_Separator1,
				label_SelectUser, combobox_SelectUser, label_Status,
				label_Deadline, datepicker_Deadline, label_DeadlineTime, text_DeadlineTime,
				button_Generate,
				line_Separator4, button_Return, button_Logout, button_Quit);
	}


	/**********
	 * <p> Method: setUserList(List<String> users) </p>
	 *
	 * <p> Description: Replace the ComboBox contents and select the placeholder entry.</p>
	 *
	 * @param users the list of usernames, starting with the "<Select a User>" placeholder
	 */
	protected static void setUserList(List<String> users) {
		combobox_SelectUser.setItems(FXCollections.observableArrayList(users));
		combobox_SelectUser.getSelectionModel().select(0);
		theSelectedUser = "";
		label_Status.setText("");
		button_Generate.setDisable(true);
	}


	/**********
	 * <p> Method: Database getDatabase() </p>
	 *
	 * @return the shared database reference used by this page's controller
	 */
	protected static Database getDatabase() { return theDatabase; }


	/*-*******************************************************************************************

	Helper methods used to minimizes the number of lines of code needed above

	*/

	/**********
	 * Private local method to initialize the standard fields for a label
	 *
	 * @param l		The Label object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Label
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);
	}


	/**********
	 * Private local method to initialize the standard fields for a button
	 *
	 * @param b		The Button object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);
	}


	/**********
	 * Private local method to initialize the standard fields for a text input field
	 *
	 * @param t		The TextField object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the TextField
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 * @param e		Is this TextField user editable?
	 */
	private void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, double y, boolean e){
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);
		t.setEditable(e);
	}


	/**********
	 * Private local method to initialize the standard fields for a ComboBox
	 *
	 * @param c		The ComboBox object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the ComboBox
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private void setupComboBoxUI(ComboBox <String> c, String ff, double f, double w, double x, double y){
		c.setStyle("-fx-font: " + f + " " + ff + ";");
		c.setMinWidth(w);
		c.setLayoutX(x);
		c.setLayoutY(y);
	}
}
