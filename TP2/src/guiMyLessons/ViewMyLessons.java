package guiMyLessons;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import entityClasses.LessonsLearned;
import entityClasses.User;


/*******
 * <p> Title: ViewMyLessons Class. </p>
 * 
 * <p> Description: The Java/FX-based My Lessons Page.  It shows the logged-in contributor a
 * list of the lessons learned that contributor has added (Contributor story 2), including any a
 * curator has since edited (Contributor story 4), and nothing added by other contributors.  Each
 * lesson shows its title and category, plus a "Locked" tag when a curator has locked the lesson
 * or any of its fields and an "Edited by curator" tag when a curator has edited it.  The
 * contributor can search their lessons by keyword, open one to examine it more closely, or go to
 * the Add Lesson page.</p>
 * 
 * <p> The layout follows the team's User Experience document: a light background, a small
 * breadcrumb above a bold page title, related widgets grouped in a bordered card with labels
 * above the fields, a blue button for the main action, and a bottom row of plain buttons for
 * leaving the page.  This page is the template the other contributor pages copy for that
 * styling.</p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * 
 * @version 1.00		2026-10-22 Initial version for TP2, structured like the Foundations
 * 							ViewContributorHome class
 * @version 1.01		2026-10-22 Status message shown after a lesson is added or deleted on
 * 							another page (Joshua Luther)
 *  
 */

public class ViewMyLessons {
	
	/*-*******************************************************************************************

	Attributes
	
	 */
	
	// These are the application values required by the user interface
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;
	
	// The User Experience document colors.  Keeping them as named constants means every widget
	// that uses a color uses exactly the same one, and the other contributor pages can copy them.
	private static final String COLOR_BACKGROUND = "#f6f8fa";	// Light page background
	private static final String COLOR_CARD = "white";			// Card background
	private static final String COLOR_BORDER = "#d0d7de";		// Card and field borders
	private static final String COLOR_TEXT = "#1f2328";			// Main text
	private static final String COLOR_MUTED = "#59636e";		// Breadcrumb and helper text
	private static final String COLOR_PRIMARY = "#0969da";		// Blue: the main action
	private static final String COLOR_SUCCESS = "#1a7f37";		// Green: an action worked
	private static final String COLOR_ERROR = "#cf222e";		// Red: something went wrong


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.
	
	// GUI Area 1: The breadcrumb, the page title, and whose account is being used
	protected static Label label_Breadcrumb = new Label("TEAM 53 / CONTRIBUTOR");
	protected static Label label_PageTitle = new Label("My lessons");
	protected static Label label_UserDetails = new Label();

	// GUI Area 2: A card holding the search field and the contributor's list of lessons.  The
	// card is a plain Region placed behind the widgets, so the widgets themselves are positioned
	// exactly like those on the Foundations pages.
	protected static Region region_Card = new Region();
	protected static Label label_Search = new Label("Search my lessons");
	protected static TextField text_Search = new TextField();
	protected static Button button_Search = new Button("Search");
	protected static Button button_ClearSearch = new Button("Clear");
	protected static Label label_Count = new Label();
	protected static Label label_Status = new Label();			// "Lesson added." and similar
	protected static ListView<LessonsLearned> listView_Lessons = new ListView<LessonsLearned>();
	protected static Label label_Placeholder = new Label();		// Shown when the list is empty
	protected static Label label_Help = new Label(
			"Select a lesson and choose Open lesson, or double-click it, to view, edit, or delete it.");
	protected static Button button_OpenLesson = new Button("Open lesson");
	protected static Button button_AddLesson = new Button("Add lesson");
	
	// GUI Area 3: Leaving the page
	protected static Button button_BackToHome = new Button("Back to contributor home");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// This is the end of the GUI objects for the page.
	
	// These attributes are used to configure the page and populate it with this user's information
	private static ViewMyLessons theView;		// Used to determine if instantiation of the class
												// is needed

	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	
	private static Scene theViewMyLessonsScene;	// The shared Scene each invocation populates

	/*-*******************************************************************************************

	Constructors
	
	 */


	/**********
	 * <p> Method: displayMyLessons(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the My Lessons page to be displayed.
	 * 
	 * It first sets up every shared attribute so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * 
	 * After the instantiation, the code clears any earlier search and loads this contributor's
	 * lessons from the database.  The page is a singleton, so this matters: a different
	 * contributor may have used the page before, and their lessons and search must not remain.
	 * It then sets the Scene onto the stage, and makes it visible to the user.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 */
	public static void displayMyLessons(Stage ps, User user) {
		displayMyLessons(ps, user, "", false);
	}
	
	
	/**********
	 * <p> Method: displayMyLessons(Stage ps, User user, String message) </p>
	 * 
	 * <p> Description: Displays the page with a confirmation message, such as "Lesson added."
	 * after the Add Lesson page saves a lesson or "Lesson deleted." after the Lesson Detail page
	 * deletes one.  The message tells the contributor the action worked, since the page that
	 * performed it is no longer showing.</p>
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 * @param message specifies the confirmation to show (shown in green)
	 * 
	 */
	public static void displayMyLessons(Stage ps, User user, String message) {
		displayMyLessons(ps, user, message, false);
	}
	
	
	/**********
	 * <p> Method: displayMyLessons(Stage ps, User user, String message, boolean isError) </p>
	 * 
	 * <p> Description: Displays the page with a message in green for a confirmation or red for
	 * an error, such as the not-found message when a selected lesson no longer exists.  The
	 * other two display methods call this one, so the page is always set up the same way.</p>
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 * @param message specifies the message to show ("" for none)
	 * 
	 * @param isError true to show the message as an error
	 * 
	 */
	public static void displayMyLessons(Stage ps, User user, String message, boolean isError) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewMyLessons();		// Instantiate singleton if needed
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system
		label_UserDetails.setText("Signed in as " + theUser.getUserName());
		ControllerMyLessons.performClearSearch();
		ControllerMyLessons.refreshList();
		// Set after the list is loaded, because clearing the search also clears the message
		label_Status.setTextFill(Color.web(isError ? COLOR_ERROR : COLOR_SUCCESS));
		label_Status.setText(message == null ? "" : message);
				
		// Set the title for the window, display the page, and wait for the user to do something
		theStage.setTitle("CSE 360 Foundations: My Lessons");
		theStage.setScene(theViewMyLessonsScene);
		theStage.show();
	}
	
	/**********
	 * <p> Method: ViewMyLessons() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object.</p>
	 * 
	 * This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayMyLessons method.</p>
	 * 
	 */
	private ViewMyLessons() {

		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theRootPane.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
		theViewMyLessonsScene = new Scene(theRootPane, width, height);	// Create the scene
		
		// GUI Area 1
		setupLabelUI(label_Breadcrumb, 11, FontWeight.BOLD, COLOR_MUTED, 30, 16);
		setupLabelUI(label_PageTitle, 26, FontWeight.BOLD, COLOR_TEXT, 30, 32);
		setupLabelUI(label_UserDetails, 13, FontWeight.NORMAL, COLOR_MUTED, 30, 70);
		
		// GUI Area 2: the card and its contents
		double cardX = 30;
		double cardY = 98;
		double cardWidth = width - 60;
		double cardHeight = 420;
		double innerX = cardX + 20;					// Widgets are inset 20 pixels inside the card
		double innerWidth = cardWidth - 40;
		
		region_Card.setLayoutX(cardX);
		region_Card.setLayoutY(cardY);
		region_Card.setPrefSize(cardWidth, cardHeight);
		region_Card.setStyle("-fx-background-color: " + COLOR_CARD + "; -fx-border-color: "
				+ COLOR_BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;");
		
		// Labels above fields, as in the User Experience document
		setupLabelUI(label_Search, 13, FontWeight.BOLD, COLOR_TEXT, innerX, cardY + 16);
		double searchButtonsWidth = 90 + 10 + 80;
		setupInputUI(text_Search, innerWidth - searchButtonsWidth - 10, 30, innerX, cardY + 38);
		text_Search.setPromptText("Keywords from the title, category, situation, or lesson");
		styleField(text_Search);
		text_Search.setOnAction((ignoredEvent) -> {ControllerMyLessons.performSearch(); });	// Enter key
		
		double searchButtonX = innerX + innerWidth - searchButtonsWidth;
		setupButtonUI(button_Search, 90, searchButtonX, cardY + 38, false);
		button_Search.setOnAction((ignoredEvent) -> {ControllerMyLessons.performSearch(); });
		setupButtonUI(button_ClearSearch, 80, searchButtonX + 100, cardY + 38, false);
		button_ClearSearch.setOnAction((ignoredEvent) -> {ControllerMyLessons.performClearSearch(); });
		
		setupLabelUI(label_Count, 13, FontWeight.BOLD, COLOR_TEXT, innerX, cardY + 84);
		
		// The status message sits at the right end of the count line
		label_Status.setFont(Font.font("Arial", FontWeight.BOLD, 13));
		label_Status.setLayoutX(innerX);
		label_Status.setLayoutY(cardY + 84);
		label_Status.setMinWidth(innerWidth);
		label_Status.setAlignment(Pos.CENTER_RIGHT);
		
		// The list shows each lesson with its category and tags (see LessonCell below).  The
		// placeholder label's text is set by the Controller, because the right message depends on
		// whether the contributor has no lessons or only has no matches for the search.
		setupInputUI(listView_Lessons, innerWidth, 228, innerX, cardY + 106);
		listView_Lessons.setStyle("-fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 6;"
				+ " -fx-background-radius: 6;");
		listView_Lessons.setCellFactory((list) -> new LessonCell());
		label_Placeholder.setFont(Font.font("Arial", 13));
		label_Placeholder.setStyle("-fx-text-fill: " + COLOR_MUTED + ";");
		listView_Lessons.setPlaceholder(label_Placeholder);
		listView_Lessons.setOnMouseClicked((event) -> {
			if (event.getClickCount() == 2) ControllerMyLessons.performOpenLesson(); });
		
		setupLabelUI(label_Help, 12, FontWeight.NORMAL, COLOR_MUTED, innerX, cardY + 342);
		
		// Open lesson is the main action on this page, so it is the blue button.  It is disabled
		// until a lesson is selected, which prevents an "open nothing" error instead of
		// reporting it afterward.
		setupButtonUI(button_OpenLesson, 140, innerX, cardY + 370, true);
		button_OpenLesson.setOnAction((ignoredEvent) -> {ControllerMyLessons.performOpenLesson(); });
		button_OpenLesson.disableProperty().bind(
				listView_Lessons.getSelectionModel().selectedItemProperty().isNull());
		
		setupButtonUI(button_AddLesson, 140, innerX + 150, cardY + 370, false);
		button_AddLesson.setOnAction((ignoredEvent) -> {ControllerMyLessons.performAddLesson(); });
		
		// GUI Area 3: the bottom row of plain buttons, as on the Delete an Account page
		double bottomY = cardY + cardHeight + 22;
		setupButtonUI(button_BackToHome, 210, 30, bottomY, false);
		button_BackToHome.setOnAction((ignoredEvent) -> {ControllerMyLessons.performBackToHome(); });
		setupButtonUI(button_Logout, 90, 250, bottomY, false);
		button_Logout.setOnAction((ignoredEvent) -> {ControllerMyLessons.performLogout(); });
		setupButtonUI(button_Quit, 90, 350, bottomY, false);
		button_Quit.setOnAction((ignoredEvent) -> {ControllerMyLessons.performQuit(); });

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children.  The card is added
		// first so that it is drawn behind everything placed on it.
		theRootPane.getChildren().addAll(
			label_Breadcrumb, label_PageTitle, label_UserDetails,
			region_Card, label_Search, text_Search, button_Search, button_ClearSearch,
			label_Count, label_Status, listView_Lessons, label_Help, button_OpenLesson, button_AddLesson,
			button_BackToHome, button_Logout, button_Quit);
	}
	
	
	/*-********************************************************************************************

	The list cell

	 */
	
	/*
	 * Shows one lesson in the list: its title in bold, its category below it, and the tags on the
	 * right.  The tags are how the list meets Contributor story 2's "which are locked" and story
	 * 4's "after a curator has edited it" at a glance, before the contributor opens the lesson.
	 * A cell is reused for different lessons as the list scrolls, so updateItem must clear
	 * everything when it is given an empty cell.
	 */
	private static class LessonCell extends ListCell<LessonsLearned> {
		@Override
		protected void updateItem(LessonsLearned lesson, boolean empty) {
			super.updateItem(lesson, empty);
			if (empty || lesson == null) {
				setText(null);
				setGraphic(null);
				return;
			}
			Label title = new Label(lesson.getTitle());
			title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
			title.setStyle("-fx-text-fill: " + COLOR_TEXT + ";");
			Label category = new Label(lesson.getCategory());
			category.setFont(Font.font("Arial", 12));
			category.setStyle("-fx-text-fill: " + COLOR_MUTED + ";");
			VBox text = new VBox(2, title, category);
			
			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);
			HBox row = new HBox(8, text, spacer);
			row.setAlignment(Pos.CENTER_LEFT);
			row.setPadding(new Insets(4, 4, 4, 4));
			
			// hasAnyLock covers both a locked lesson and a lesson with a locked field, because
			// either one means the contributor cannot delete it
			if (lesson.hasAnyLock()) row.getChildren().add(makeTag("Locked", "#fff1e5", "#bc4c00"));
			if (lesson.isCuratorEdited())
				row.getChildren().add(makeTag("Edited by curator", "#ddf4ff", COLOR_PRIMARY));
			
			setText(null);
			setGraphic(row);
		}
		
		// A small rounded label used for the list tags
		private static Label makeTag(String text, String background, String foreground) {
			Label tag = new Label(text);
			tag.setFont(Font.font("Arial", FontWeight.BOLD, 11));
			tag.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + foreground
					+ "; -fx-padding: 2 8 2 8; -fx-background-radius: 10;");
			return tag;
		}
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
	
	
	/**********
	 * Private local method to initialize the size and location of an input widget (a text field
	 * or list).  It follows the same approach as setupLabelUI and setupButtonUI, but sets a
	 * height as well because these widgets are not sized by their text.
	 * 
	 * @param c		The input widget to be initialized
	 * @param w		The width of the widget
	 * @param h		The height of the widget
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupInputUI(Control c, double w, double h, double x, double y) {
		c.setPrefWidth(w);
		c.setPrefHeight(h);
		c.setLayoutX(x);
		c.setLayoutY(y);
	}
	
	
	// Gives a text field the bordered look the User Experience document uses for inputs
	private static void styleField(TextField t) {
		t.setFont(Font.font("Arial", 13));
		t.setStyle("-fx-background-color: white; -fx-border-color: " + COLOR_BORDER + ";"
				+ " -fx-border-radius: 6; -fx-background-radius: 6;");
	}
}