package guiManageInvitations;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.Invitation;
import entityClasses.User;

/*******
 * <p> Title: ViewManageInvitations Class. </p>
 *
 * <p> Description: The Java/FX-based Manage Invitations Page.  This page implements the Admin
 * user story "manage invitations".  It lists every outstanding invitation (code, email address,
 * role, deadline, and status) and lets the Admin revoke a selected invitation or purge all the
 * expired ones.
 *
 * The class has been written using a singleton design pattern and is the View portion of the
 * Model, View, Controller pattern.  All accesses to this page start by invoking the static
 * method displayManageInvitations.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version
 *
 */

public class ViewManageInvitations {

	/*-*******************************************************************************************

	Attributes

	*/

	// These are the application values required by the user interface
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

	// GUI Area 1: It informs the user about the purpose of this page and whose account is in use
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Label label_NumberOfInvitations = new Label();

	// This is a separator and it is used to partition the GUI for various tasks
	private static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI Area 2: The table of invitations and the actions that can be taken on them
	protected static TableView<Invitation> table_Invitations = new TableView<Invitation>();
	protected static Button button_Revoke = new Button("Revoke Selected Invitation");
	protected static Button button_PurgeExpired = new Button("Remove Expired Invitations");
	protected static Alert alertConfirmRevoke = new Alert(AlertType.CONFIRMATION);
	protected static Alert alertInfo = new Alert(AlertType.INFORMATION);

	// This is a separator and it is used to partition the GUI for various tasks
	private static Line line_Separator4 = new Line(20, 525, width-20, 525);

	// GUI Area 3: Return, Logout, and Quit
	protected static Button button_Return = new Button("Return");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// These attributes are used to configure the page and populate it with this user's information
	private static ViewManageInvitations theView;	// Used to determine if instantiation is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us
	private static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User (an Admin)
	private static Scene theManageInvitationsScene;	// The shared Scene each invocation populates


	/*-*******************************************************************************************

	Constructors

	*/

	/**********
	 * <p> Method: displayManageInvitations(Stage ps, User user) </p>
	 *
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Manage Invitations page to be displayed.  It instantiates the singleton if needed,
	 * refreshes the table from the database, and shows the Scene.</p>
	 *
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 *
	 * @param user specifies the Admin using this page
	 *
	 */
	public static void displayManageInvitations(Stage ps, User user) {
		theStage = ps;
		theUser = user;

		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewManageInvitations();

		// Populate the dynamic aspects of the GUI
		label_UserDetails.setText("User: " + theUser.getUserName());
		ControllerManageInvitations.refreshInvitationList();

		theStage.setTitle("CSE 360 Foundation Code: Manage Invitations Page");
		theStage.setScene(theManageInvitationsScene);
		theStage.show();
	}


	/**********
	 * <p> Method: ViewManageInvitations() </p>
	 *
	 * <p> Description: This private constructor initializes all the elements of the graphical
	 * user interface.  It is a singleton and is only performed once.</p>
	 */
	private ViewManageInvitations() {
		theRootPane = new Pane();
		theManageInvitationsScene = new Scene(theRootPane, width, height);

		// GUI Area 1
		label_PageTitle.setText("Manage Invitations");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		setupLabelUI(label_NumberOfInvitations, "Arial", 16, 340, Pos.BASELINE_RIGHT,
				width-360, 60);

		// GUI Area 2: the table and its columns
		TableColumn<Invitation, String> col_Code = new TableColumn<Invitation, String>("Code");
		col_Code.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCode()));
		col_Code.setPrefWidth(90);

		TableColumn<Invitation, String> col_Email =
				new TableColumn<Invitation, String>("Email Address");
		col_Email.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getEmailAddress()));
		col_Email.setPrefWidth(260);

		TableColumn<Invitation, String> col_Role = new TableColumn<Invitation, String>("Role");
		col_Role.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
		col_Role.setPrefWidth(110);

		TableColumn<Invitation, String> col_Deadline =
				new TableColumn<Invitation, String>("Expires");
		col_Deadline.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getDeadlineText()));
		col_Deadline.setPrefWidth(170);

		TableColumn<Invitation, String> col_Status =
				new TableColumn<Invitation, String>("Status");
		col_Status.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
		col_Status.setPrefWidth(90);

		table_Invitations.getColumns().add(col_Code);
		table_Invitations.getColumns().add(col_Email);
		table_Invitations.getColumns().add(col_Role);
		table_Invitations.getColumns().add(col_Deadline);
		table_Invitations.getColumns().add(col_Status);
		table_Invitations.setLayoutX(20);
		table_Invitations.setLayoutY(110);
		table_Invitations.setPrefSize(width-40, 340);
		table_Invitations.setPlaceholder(new Label("There are no outstanding invitations."));
		table_Invitations.setItems(FXCollections.observableArrayList());

		setupButtonUI(button_Revoke, "Dialog", 16, 300, Pos.CENTER, 20, 470);
        button_Revoke.setOnAction((ignoredEvent) -> {ControllerManageInvitations.performRevoke(); });

		setupButtonUI(button_PurgeExpired, "Dialog", 16, 300, Pos.CENTER, 480, 470);
        button_PurgeExpired.setOnAction((ignoredEvent) -> {ControllerManageInvitations.performPurge(); });

		alertConfirmRevoke.setTitle("Confirm Revocation");
		alertConfirmRevoke.setHeaderText("Are you sure?");
		alertConfirmRevoke.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

		alertInfo.setTitle("Manage Invitations");
		alertInfo.setHeaderText(null);

		// GUI Area 3
		setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
        button_Return.setOnAction((ignoredEvent) -> {ControllerManageInvitations.performReturn(); });

		setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
        button_Logout.setOnAction((ignoredEvent) -> {ControllerManageInvitations.performLogout(); });

		setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
        button_Quit.setOnAction((ignoredEvent) -> {ControllerManageInvitations.performQuit(); });

		// Place all of the widget items into the Root Pane's list of children
		theRootPane.getChildren().addAll(
				label_PageTitle, label_UserDetails, label_NumberOfInvitations, line_Separator1,
				table_Invitations, button_Revoke, button_PurgeExpired,
				line_Separator4, button_Return, button_Logout, button_Quit);
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
