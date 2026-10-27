package guiListUsers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;

/*******
 * <p> Title: ViewListUsers Class. </p>
 *
 * <p> Description: The Java/FX-based List Users Page.  This page implements the Admin user story
 * "list all users" by showing a table with every user's username, name, preferred first name,
 * email address, and the roles that user plays.  The page is read-only; to change anything the
 * Admin uses the other Admin pages.
 *
 * The class has been written using a singleton design pattern and is the View portion of the
 * Model, View, Controller pattern.  All accesses to this page start by invoking the static
 * method displayListUsers.  No other method should attempt to instantiate this class.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version
 *
 */

public class ViewListUsers {

	/*-*******************************************************************************************

	Attributes

	*/

	// These are the application values required by the user interface
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

	// GUI Area 1: It informs the user about the purpose of this page and whose account is in use
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Label label_NumberOfUsers = new Label();

	// This is a separator and it is used to partition the GUI for various tasks
	private static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI Area 2: The table of users.  Each column reads one attribute of a User object.
	protected static TableView<User> table_Users = new TableView<User>();

	// This is a separator and it is used to partition the GUI for various tasks
	private static Line line_Separator4 = new Line(20, 525, width-20, 525);

	// GUI Area 3: Return, Logout, and Quit
	protected static Button button_Return = new Button("Return");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// These attributes are used to configure the page and populate it with this user's information
	private static ViewListUsers theView;		// Used to determine if instantiation is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us
	private static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	private static Scene theListUsersScene;		// The shared Scene each invocation populates


	/*-*******************************************************************************************

	Constructors

	*/

	/**********
	 * <p> Method: displayListUsers(Stage ps, User user) </p>
	 *
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the List Users page to be displayed.  It instantiates the singleton if needed, refreshes
	 * the table from the database, and shows the Scene.</p>
	 *
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 *
	 * @param user specifies the Admin using this page
	 *
	 */
	public static void displayListUsers(Stage ps, User user) {
		theStage = ps;
		theUser = user;

		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewListUsers();

		// Populate the dynamic aspects of the GUI: the current user and the table contents
		label_UserDetails.setText("User: " + theUser.getUserName());
		ControllerListUsers.refreshUserList();

		theStage.setTitle("CSE 360 Foundation Code: List Users Page");
		theStage.setScene(theListUsersScene);
		theStage.show();
	}


	/**********
	 * <p> Method: ViewListUsers() </p>
	 *
	 * <p> Description: This private constructor initializes all the elements of the graphical
	 * user interface: location, size, font, and event handlers for each GUI object.  It is a
	 * singleton and is only performed once.</p>
	 */
	private ViewListUsers() {
		theRootPane = new Pane();
		theListUsersScene = new Scene(theRootPane, width, height);

		// GUI Area 1
		label_PageTitle.setText("List of All Users");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		setupLabelUI(label_NumberOfUsers, "Arial", 16, 300, Pos.BASELINE_RIGHT, width-320, 60);

		// GUI Area 2: the table and its columns
		TableColumn<User, String> col_Username = new TableColumn<User, String>("Username");
		col_Username.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getUserName()));
		col_Username.setPrefWidth(130);

		TableColumn<User, String> col_Name = new TableColumn<User, String>("Name");
		col_Name.setCellValueFactory(
				c -> new SimpleStringProperty(ControllerListUsers.fullName(c.getValue())));
		col_Name.setPrefWidth(180);

		TableColumn<User, String> col_Preferred = new TableColumn<User, String>("Preferred");
		col_Preferred.setCellValueFactory(c -> new SimpleStringProperty(
				ControllerListUsers.blankIfNull(c.getValue().getPreferredFirstName())));
		col_Preferred.setPrefWidth(100);

		TableColumn<User, String> col_Email = new TableColumn<User, String>("Email Address");
		col_Email.setCellValueFactory(c -> new SimpleStringProperty(
				ControllerListUsers.blankIfNull(c.getValue().getEmailAddress())));
		col_Email.setPrefWidth(190);

		TableColumn<User, String> col_Roles = new TableColumn<User, String>("Roles");
		col_Roles.setCellValueFactory(
				c -> new SimpleStringProperty(ControllerListUsers.rolesAsText(c.getValue())));
		col_Roles.setPrefWidth(155);

		table_Users.getColumns().add(col_Username);
		table_Users.getColumns().add(col_Name);
		table_Users.getColumns().add(col_Preferred);
		table_Users.getColumns().add(col_Email);
		table_Users.getColumns().add(col_Roles);
		table_Users.setLayoutX(20);
		table_Users.setLayoutY(110);
		table_Users.setPrefSize(width-40, 400);
		table_Users.setPlaceholder(new Label("There are no users to display."));
		table_Users.setItems(FXCollections.observableArrayList());

		// GUI Area 3
		setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
        button_Return.setOnAction((ignoredEvent) -> {ControllerListUsers.performReturn(); });

		setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
        button_Logout.setOnAction((ignoredEvent) -> {ControllerListUsers.performLogout(); });

		setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
        button_Quit.setOnAction((ignoredEvent) -> {ControllerListUsers.performQuit(); });

		// Place all of the widget items into the Root Pane's list of children
		theRootPane.getChildren().addAll(
				label_PageTitle, label_UserDetails, label_NumberOfUsers, line_Separator1,
				table_Users, line_Separator4,
				button_Return, button_Logout, button_Quit);
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
}
