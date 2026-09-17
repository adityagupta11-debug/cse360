package guiDeleteUser;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;

/*******
 * <p> Title: ViewDeleteUser Class. </p>
 *
 * <p> Description: The Java/FX-based Delete User Page.  This page implements the Admin user
 * story "delete a user".  The Admin selects a user from a ComboBox, reviews that user's details,
 * and presses Delete.  A confirmation dialog ("Are you sure?") must be accepted before the user
 * is removed.  The Admin cannot delete their own account from this page, and the database
 * refuses to delete the last remaining Admin.
 *
 * The class has been written using a singleton design pattern and is the View portion of the
 * Model, View, Controller pattern.  All accesses to this page start by invoking the static
 * method displayDeleteUser.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version
 *
 */

public class ViewDeleteUser {

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

	// GUI Area 2: Select the user to be deleted and review that user's details
	protected static Label label_SelectUser = new Label("Select the user to be deleted:");
	protected static ComboBox <String> combobox_SelectUser = new ComboBox <String>();
	protected static Label label_SelectedDetails = new Label("");
	protected static Button button_Delete = new Button("Delete This User");
	protected static Alert alertConfirmDelete = new Alert(AlertType.CONFIRMATION);
	protected static Alert alertDeleteRefused = new Alert(AlertType.INFORMATION);
	protected static Alert alertDeleteDone = new Alert(AlertType.INFORMATION);

	// This is a separator and it is used to partition the GUI for various tasks
	private static Line line_Separator4 = new Line(20, 525, width-20, 525);

	// GUI Area 3: Return, Logout, and Quit
	protected static Button button_Return = new Button("Return");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// These attributes are used to configure the page and populate it with this user's information
	private static ViewDeleteUser theView;		// Used to determine if instantiation is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us
	private static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User (an Admin)
	private static Scene theDeleteUserScene;	// The shared Scene each invocation populates
	protected static String theSelectedUser = "";	// The user chosen in the ComboBox


	/*-*******************************************************************************************

	Constructors

	*/

	/**********
	 * <p> Method: displayDeleteUser(Stage ps, User user) </p>
	 *
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Delete User page to be displayed.  It instantiates the singleton if needed, reloads the
	 * list of users, and shows the Scene.</p>
	 *
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 *
	 * @param user specifies the Admin using this page
	 *
	 */
	public static void displayDeleteUser(Stage ps, User user) {
		theStage = ps;
		theUser = user;

		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewDeleteUser();

		// Populate the dynamic aspects of the GUI
		label_UserDetails.setText("User: " + theUser.getUserName());
		ControllerDeleteUser.reloadUserList();

		theStage.setTitle("CSE 360 Foundation Code: Delete User Page");
		theStage.setScene(theDeleteUserScene);
		theStage.show();
	}


	/**********
	 * <p> Method: ViewDeleteUser() </p>
	 *
	 * <p> Description: This private constructor initializes all the elements of the graphical
	 * user interface.  It is a singleton and is only performed once.</p>
	 */
	private ViewDeleteUser() {
		theRootPane = new Pane();
		theDeleteUserScene = new Scene(theRootPane, width, height);

		// GUI Area 1
		label_PageTitle.setText("Delete a User");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

		// GUI Area 2
		setupLabelUI(label_SelectUser, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 120);
		setupComboBoxUI(combobox_SelectUser, "Dialog", 16, 250, 20, 160);
		combobox_SelectUser.setOnAction((_) -> {ControllerDeleteUser.doSelectUser(); });

		setupLabelUI(label_SelectedDetails, "Arial", 16, width-40, Pos.TOP_LEFT, 20, 215);
		label_SelectedDetails.setWrapText(true);

		setupButtonUI(button_Delete, "Dialog", 18, 250, Pos.CENTER, 20, 380);
		button_Delete.setStyle("-fx-text-fill: #b00020;");	// Red text warns this is destructive
		button_Delete.setOnAction((_) -> {ControllerDeleteUser.performDelete(); });
		button_Delete.setDisable(true);						// Enabled once a user is chosen

		alertConfirmDelete.setTitle("Confirm Deletion");
		alertConfirmDelete.setHeaderText("Are you sure?");
		alertConfirmDelete.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

		alertDeleteRefused.setTitle("Deletion Refused");
		alertDeleteRefused.setHeaderText("The user was not deleted");

		alertDeleteDone.setTitle("User Deleted");
		alertDeleteDone.setHeaderText("The user has been deleted");

		// GUI Area 3
		setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
		button_Return.setOnAction((_) -> {ControllerDeleteUser.performReturn(); });

		setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
		button_Logout.setOnAction((_) -> {ControllerDeleteUser.performLogout(); });

		setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
		button_Quit.setOnAction((_) -> {ControllerDeleteUser.performQuit(); });

		// Place all of the widget items into the Root Pane's list of children
		theRootPane.getChildren().addAll(
				label_PageTitle, label_UserDetails, line_Separator1,
				label_SelectUser, combobox_SelectUser, label_SelectedDetails, button_Delete,
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
		label_SelectedDetails.setText("");
		button_Delete.setDisable(true);
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
