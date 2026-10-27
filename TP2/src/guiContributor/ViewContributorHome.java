package guiContributor;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;


/*******
 * <p> Title: ViewContributorHome Class. </p>
 * 
 * <p> Description: The Java/FX-based Contributor Home Page.  It is the page a contributor sees
 * after logging in, and it leads to the contributor's lessons learned: My Lessons (the list of
 * lessons the contributor has added, Contributor stories 2 and 4) and Add Lesson (Contributor
 * story 1).  It also keeps the Foundations Account Update, Logout, and Quit actions.</p>
 * 
 * <p> In HW2 this page held the lesson list and form itself.  For TP2 each of those became its
 * own page in its own package, as the Foundations code does for each window, so this page now
 * only navigates.  The layout follows the team's User Experience document.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @author Joshua Luther (J.L.) - lessons learned navigation and User Experience layout
 * 
 * @version 1.00		2025-08-20 Initial version
 * @version 1.01		2026-10-08 GUI Area 2 filled in with the lesson list and lesson form for
 * 							HW2 (Joshua Luther)
 * @version 1.02		2026-10-22 Lesson list and form moved to their own pages for TP2; this page
 * 							now navigates to them, in the User Experience layout (Joshua Luther)
 *  
 */

public class ViewContributorHome {
	
	/*-*******************************************************************************************

	Attributes
	
	 */
	
	// These are the application values required by the user interface
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;
	
	// The User Experience document colors, the same values the My Lessons page uses
	private static final String COLOR_BACKGROUND = "#f6f8fa";
	private static final String COLOR_CARD = "white";
	private static final String COLOR_BORDER = "#d0d7de";
	private static final String COLOR_TEXT = "#1f2328";
	private static final String COLOR_MUTED = "#59636e";
	private static final String COLOR_PRIMARY = "#0969da";


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.
	
	// GUI Area 1: The breadcrumb, the page title, and whose account is being used
	protected static Label label_Breadcrumb = new Label("TEAM 53 / CONTRIBUTOR");
	protected static Label label_PageTitle = new Label("Contributor home");
	protected static Label label_UserDetails = new Label();

	// GUI Area 2: A card leading to the contributor's lessons learned
	protected static Region region_Card = new Region();
	protected static Label label_CardTitle = new Label("Lessons learned");
	protected static Label label_CardText = new Label(
			"Add a new lesson, or open the lessons you have already added to view, edit, or "
			+ "delete them.");
	protected static Button button_MyLessons = new Button("My lessons");
	protected static Button button_AddLesson = new Button("Add lesson");
	
	// GUI Area 3: Account settings and leaving the application
	protected static Button button_UpdateThisUser = new Button("Account update");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// This is the end of the GUI objects for the page.
	
	// These attributes are used to configure the page and populate it with this user's information
	private static ViewContributorHome theView;		// Used to determine if instantiation of the class
												// is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	

	private static Scene theViewContributorHomeScene;	// The shared Scene each invocation populates
	protected static final int theRole = 2;		// Admin: 1; Contributor: 2; Viewer: 3; Curator: 4

	/*-*******************************************************************************************

	Constructors
	
	 */


	/**********
	 * <p> Method: displayContributorHome(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Contributor Home page to be displayed.
	 * 
	 * It first sets up every shared attributes so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GIUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * 
	 * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 */
	public static void displayContributorHome(Stage ps, User user) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewContributorHome();		// Instantiate singleton if needed
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;
		
		label_UserDetails.setText("Signed in as " + theUser.getUserName());
				
		// Set the title for the window, display the page, and wait for the user to do something
		theStage.setTitle("CSE 360 Foundations: Contributor Home Page");
		theStage.setScene(theViewContributorHomeScene);
		theStage.show();
	}
	
	/**********
	 * <p> Method: ViewContributorHome() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object.</p>
	 * 
	 * This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayContributorHome method.</p>
	 * 
	 */
	private ViewContributorHome() {

		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theRootPane.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
		theViewContributorHomeScene = new Scene(theRootPane, width, height);	// Create the scene
		
		// GUI Area 1
		setupLabelUI(label_Breadcrumb, 11, FontWeight.BOLD, COLOR_MUTED, 30, 16);
		setupLabelUI(label_PageTitle, 26, FontWeight.BOLD, COLOR_TEXT, 30, 32);
		setupLabelUI(label_UserDetails, 13, FontWeight.NORMAL, COLOR_MUTED, 30, 70);
		
		// GUI Area 2
		double cardX = 30;
		double cardY = 98;
		double cardWidth = width - 60;
		double innerX = cardX + 20;
		
		region_Card.setLayoutX(cardX);
		region_Card.setLayoutY(cardY);
		region_Card.setPrefSize(cardWidth, 150);
		region_Card.setStyle("-fx-background-color: " + COLOR_CARD + "; -fx-border-color: "
				+ COLOR_BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;");
		
		setupLabelUI(label_CardTitle, 16, FontWeight.BOLD, COLOR_TEXT, innerX, cardY + 18);
		setupLabelUI(label_CardText, 13, FontWeight.NORMAL, COLOR_MUTED, innerX, cardY + 46);
		label_CardText.setMaxWidth(cardWidth - 40);
		label_CardText.setWrapText(true);
		
		// My lessons is the main action, so it is the blue button
		setupButtonUI(button_MyLessons, 140, innerX, cardY + 92, true);
		button_MyLessons.setOnAction((ignoredEvent) -> {ControllerContributorHome.performMyLessons(); });
		setupButtonUI(button_AddLesson, 140, innerX + 150, cardY + 92, false);
		button_AddLesson.setOnAction((ignoredEvent) -> {ControllerContributorHome.performAddLesson(); });
		
		// GUI Area 3: the bottom row of plain buttons
		double bottomY = height - 60;
		setupButtonUI(button_UpdateThisUser, 150, 30, bottomY, false);
        button_UpdateThisUser.setOnAction((ignoredEvent) -> {ControllerContributorHome.performUpdate(); });
		setupButtonUI(button_Logout, 90, 190, bottomY, false);
        button_Logout.setOnAction((ignoredEvent) -> {ControllerContributorHome.performLogout(); });
		setupButtonUI(button_Quit, 90, 290, bottomY, false);
        button_Quit.setOnAction((ignoredEvent) -> {ControllerContributorHome.performQuit(); });

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children.  The card is added
		// first so that it is drawn behind everything placed on it.
         theRootPane.getChildren().addAll(
			label_Breadcrumb, label_PageTitle, label_UserDetails,
			region_Card, label_CardTitle, label_CardText, button_MyLessons, button_AddLesson,
			button_UpdateThisUser, button_Logout, button_Quit);
	}
	
	
	/*-********************************************************************************************

	Helper methods to reduce code length

	 */
	
	/**********
	 * Private local method to initialize the standard fields for a label
	 * 
	 * @param l		The Label object to be initialized
	 * @param f		The size of the font to be used
	 * @param weight	Bold or normal
	 * @param color	The text color
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupLabelUI(Label l, double f, FontWeight weight, String color, double x,
			double y){
		l.setFont(Font.font("Arial", weight, f));
		l.setStyle("-fx-text-fill: " + color + ";");
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	
	/**********
	 * Private local method to initialize the standard fields for a button.  Primary buttons are
	 * blue with white text; all others are plain white with a border, following the User
	 * Experience document.
	 * 
	 * @param b		The Button object to be initialized
	 * @param w		The width of the Button
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 * @param primary	true for the page's main action
	 */
	private static void setupButtonUI(Button b, double w, double x, double y, boolean primary){
		b.setFont(Font.font("Arial", 13));
		b.setMinWidth(w);
		b.setPrefHeight(30);
		b.setLayoutX(x);
		b.setLayoutY(y);
		if (primary)
			b.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white;"
					+ " -fx-background-radius: 6; -fx-font-weight: bold;");
		else
			b.setStyle("-fx-background-color: white; -fx-text-fill: " + COLOR_TEXT + ";"
					+ " -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 6;"
					+ " -fx-background-radius: 6;");
	}
}