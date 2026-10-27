package guiUserLogin;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


/*******
 * <p> Title: ViewUserLogin Class. </p>
 * 
 * <p> Description: The Java/FX-based System Startup Page.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-04-20 Initial version
 *  
 */

public class ViewUserLogin {

	/*-********************************************************************************************

	Attributes

	 *********************************************************************************************/

	// These are the application values required by the user interface

	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

	private static Label label_ApplicationTitle = new Label("Foundation Application Startup Page");

	// This set is for all subsequent starts of the system
	private static Label label_OperationalStartTitle = new Label("Log In or Invited User Account Setup ");
	private static Label label_LogInInsrtuctions = new Label("Enter your user name and password and "+	
			"then click on the LogIn button");
	protected static Alert alertUsernamePasswordError = new Alert(AlertType.INFORMATION);


	//	private User user;
	protected static TextField text_Username = new TextField();
	protected static PasswordField text_Password = new PasswordField();
	private static Button button_Login = new Button("Log In");	

	private static Label label_AccountSetupInsrtuctions = new Label("No account? "+	
			"Enter your invitation code and click on the Account Setup button");
	private static TextField text_Invitation = new TextField();
	private static Button button_SetupAccount = new Button("Setup Account");

	private static Button button_Quit = new Button("Quit");

	private static Stage theStage;	
	private static Pane theRootPane;
	public static Scene theUserLoginScene = null;	


	private static ViewUserLogin theView = null;	//	private static guiUserLogin.ControllerUserLogin theController;


	/*-********************************************************************************************

	Constructor

	 *********************************************************************************************/

	public static void displayUserLogin(Stage ps) {
		applicationMain.FoundationsMain.database.clearAuthenticatedSession();
		
		// Establish the references to the GUI. There is no current user yet.
		theStage = ps;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewUserLogin();
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.		
		text_Username.setText("");		// Reset the username and password from the last use
		text_Password.setText("");
		text_Invitation.setText("");	// Same for the invitation code

		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("CSE 360 Foundation Code: User Login Page");		
		theStage.setScene(theUserLoginScene);
		theStage.show();
	}

	/**********
	 * <p> Method: ViewUserLoginPage() </p>
	 * 
	 * <p> Description: This method is called when the application first starts. It must handle
	 * two cases: 1) when no has been established and 2) when one or more users have been 
	 * established.
	 * 
	 * If there are no users in the database, this means that the person starting the system jmust
	 * be an administrator, so a special GUI is provided to allow this Admin to set a username and
	 * password.
	 * 
	 * If there is at least one user, then a different display is shown for existing users to login
	 * and for potential new users to provide an invitation code and if it is valid, they are taken
	 * to a page where they can specify a username and password.</p>
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param theRoot specifies the JavaFX Pane to be used for this GUI and it's methods
	 * 
	 * @param db specifies the Database to be used by this GUI and it's methods
	 * 
	 */
	private ViewUserLogin() {
		// K.G.: labeled form groups and a single primary action, inspired by GitHub settings.
		javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(14);
		content.setPadding(new javafx.geometry.Insets(28, 80, 28, 80));
		Label brand = new Label("TEAM 53  /  CSE 360");
		brand.getStyleClass().add("eyebrow");
		Label title = new Label("Welcome back");
		title.getStyleClass().add("page-title");
		Label subtitle = new Label("Sign in to manage your account.");
		subtitle.getStyleClass().add("help-label");
		Label username = new Label("Username");
		username.setLabelFor(text_Username);
		Label password = new Label("Password");
		password.setLabelFor(text_Password);
		text_Username.setPromptText("Enter your username");
		text_Password.setPromptText("Enter your password");
		button_Login.setText("Sign in");
		button_Login.getStyleClass().add("primary-button");
		button_Login.setDefaultButton(true);
		button_Login.setMaxWidth(Double.MAX_VALUE);
		button_Login.setOnAction(event -> ControllerUserLogin.doLogin(theStage));
		javafx.scene.layout.VBox signIn = new javafx.scene.layout.VBox(8,
				username, text_Username, password, text_Password, button_Login);
		signIn.getStyleClass().add("card");
		Label invitation = new Label("Invited to join?");
		Label invitationLabel = new Label("Invitation code");
		invitationLabel.setLabelFor(text_Invitation);
		text_Invitation.setPromptText("Enter your invitation code");
		button_SetupAccount.setText("Create account");
		button_SetupAccount.setOnAction(event ->
				ControllerUserLogin.doSetupAccount(theStage, text_Invitation.getText()));
		javafx.scene.layout.HBox inviteRow = new javafx.scene.layout.HBox(12, text_Invitation, button_SetupAccount);
		javafx.scene.layout.HBox.setHgrow(text_Invitation, javafx.scene.layout.Priority.ALWAYS);
		javafx.scene.layout.VBox invite = new javafx.scene.layout.VBox(8, invitation, invitationLabel, inviteRow);
		invite.getStyleClass().add("card");
		button_Quit.setOnAction(event -> ControllerUserLogin.performQuit());
		content.getChildren().addAll(brand, title, subtitle, signIn, invite, button_Quit);
		theUserLoginScene = new Scene(content, width, height);
		guiTools.TeamTheme.apply(theUserLoginScene);
		alertUsernamePasswordError.setTitle("Unable to sign in");
		alertUsernamePasswordError.setHeaderText(null);
	}


	/*-********************************************************************************************

	Helper methods to reduce code length

	 *********************************************************************************************/

	/**********
	 * Private local method to initialize the standard fields for a label
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
	 * Private local method to initialize the standard fields for a text field
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
}
