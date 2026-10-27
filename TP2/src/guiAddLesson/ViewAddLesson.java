package guiAddLesson;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import entityClasses.LessonsLearned;
import entityClasses.User;
import lessonRecognizer.LessonRecognizer;


/*******
 * <p> Title: ViewAddLesson Class. </p>
 * 
 * <p> Description: The Java/FX-based Add Lesson Page.  A contributor enters a new lesson's core
 * information (title, category, situation, and lesson text) and saves it (Contributor story 1).
 * Each field shows its limits next to its label, and an invalid entry produces the
 * LessonRecognizer's specific message in red with the cursor placed in that field.</p>
 * 
 * <p> The layout follows the team's User Experience document and the My Lessons page: a light
 * background, breadcrumb and bold title, fields grouped in a bordered card with labels above
 * them, a blue button for the main action, and a bottom row of plain buttons.</p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * 
 * @version 1.00		2026-10-22 Initial version for TP2 (replaces the placeholder)
 *  
 */

public class ViewAddLesson {
	
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
	protected static Label label_Breadcrumb = new Label("TEAM 53 / CONTRIBUTOR / MY LESSONS");
	protected static Label label_PageTitle = new Label("Add a lesson");
	protected static Label label_UserDetails = new Label();

	// GUI Area 2: The card holding the lesson's core information.  The limits shown next to each
	// label come from the LessonRecognizer constants, so they can never disagree with the
	// limits the recognizer actually enforces.
	protected static Region region_Card = new Region();
	protected static Label label_Title = new Label("Title");
	protected static Label label_TitleHelp = new Label("Required. " + LessonRecognizer.TITLE_MIN
			+ " to " + LessonRecognizer.TITLE_MAX + " characters.");
	protected static TextField text_Title = new TextField();
	protected static Label label_Category = new Label("Category");
	protected static Label label_CategoryHelp = new Label("Required. Choose one.");
	protected static ComboBox<String> comboBox_Category = new ComboBox<String>();
	protected static Label label_Situation = new Label("Situation (what happened)");
	protected static Label label_SituationHelp = new Label("Required. "
			+ LessonRecognizer.TEXT_MIN + " to " + String.format("%,d", LessonRecognizer.TEXT_MAX)
			+ " characters.");
	protected static TextArea text_Situation = new TextArea();
	protected static Label label_LessonText = new Label("Lesson (what was learned)");
	protected static Label label_LessonTextHelp = new Label("Required. "
			+ LessonRecognizer.TEXT_MIN + " to " + String.format("%,d", LessonRecognizer.TEXT_MAX)
			+ " characters.");
	protected static TextArea text_LessonText = new TextArea();
	protected static Label label_Message = new Label();
	protected static Button button_SaveLesson = new Button("Save lesson");
	protected static Button button_ClearForm = new Button("Clear form");
	
	// GUI Area 3: Leaving the page
	protected static Button button_BackToMyLessons = new Button("Back to my lessons");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// This is the end of the GUI objects for the page.
	
	private static ViewAddLesson theView;		// Used to determine if instantiation of the class
												// is needed
	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	
	private static Scene theViewAddLessonScene;	// The shared Scene each invocation populates

	/*-*******************************************************************************************

	Constructors
	
	 */


	/**********
	 * <p> Method: displayAddLesson(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Add Lesson page to be displayed.  It sets up the shared attributes, builds the page the
	 * first time, and empties the form every time, because the page is a singleton and an
	 * earlier, unsaved lesson must not reappear.</p>
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 */
	public static void displayAddLesson(Stage ps, User user) {
		theStage = ps;
		theUser = user;
		if (theView == null) theView = new ViewAddLesson();		// Instantiate singleton if needed
		
		label_UserDetails.setText("Signed in as " + theUser.getUserName());
		ControllerAddLesson.performClearForm();
		
		theStage.setTitle("CSE 360 Foundations: Add a Lesson");
		theStage.setScene(theViewAddLessonScene);
		theStage.show();
	}
	
	/**********
	 * <p> Method: ViewAddLesson() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object.  This is a singleton and is only performed once.</p>
	 * 
	 */
	private ViewAddLesson() {

		theRootPane = new Pane();
		theRootPane.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
		theViewAddLessonScene = new Scene(theRootPane, width, height);
		
		// GUI Area 1
		setupLabelUI(label_Breadcrumb, 11, FontWeight.BOLD, COLOR_MUTED, 30, 16);
		setupLabelUI(label_PageTitle, 26, FontWeight.BOLD, COLOR_TEXT, 30, 32);
		setupLabelUI(label_UserDetails, 13, FontWeight.NORMAL, COLOR_MUTED, 30, 70);
		
		// GUI Area 2
		double cardX = 30;
		double cardY = 98;
		double cardWidth = width - 60;
		double innerX = cardX + 20;
		double innerWidth = cardWidth - 40;
		
		region_Card.setLayoutX(cardX);
		region_Card.setLayoutY(cardY);
		region_Card.setPrefSize(cardWidth, 428);
		region_Card.setStyle("-fx-background-color: " + COLOR_CARD + "; -fx-border-color: "
				+ COLOR_BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;");
		
		// Each field: its label above it on the left, its limits on the same line on the right
		setupFieldLabels(label_Title, label_TitleHelp, innerX, innerWidth, cardY + 16);
		setupInputUI(text_Title, innerWidth, 30, innerX, cardY + 36);
		styleInput(text_Title);
		
		setupFieldLabels(label_Category, label_CategoryHelp, innerX, innerWidth, cardY + 80);
		setupInputUI(comboBox_Category, 300, 30, innerX, cardY + 100);
		comboBox_Category.getItems().setAll(LessonsLearned.CATEGORIES);	// The list the recognizer uses
		comboBox_Category.setPromptText("Select a category");
		styleInput(comboBox_Category);
		
		setupFieldLabels(label_Situation, label_SituationHelp, innerX, innerWidth, cardY + 144);
		setupInputUI(text_Situation, innerWidth, 70, innerX, cardY + 164);
		text_Situation.setWrapText(true);
		styleInput(text_Situation);
		
		setupFieldLabels(label_LessonText, label_LessonTextHelp, innerX, innerWidth, cardY + 248);
		setupInputUI(text_LessonText, innerWidth, 70, innerX, cardY + 268);
		text_LessonText.setWrapText(true);
		styleInput(text_LessonText);
		
		// The message wraps so the longest error message still fits inside the card
		label_Message.setFont(Font.font("Arial", 13));
		label_Message.setLayoutX(innerX);
		label_Message.setLayoutY(cardY + 346);
		label_Message.setMaxWidth(innerWidth);
		label_Message.setWrapText(true);
		
		setupButtonUI(button_SaveLesson, 140, innerX, cardY + 382, true);
		button_SaveLesson.setOnAction((ignoredEvent) -> {ControllerAddLesson.performSaveLesson(); });
		setupButtonUI(button_ClearForm, 120, innerX + 150, cardY + 382, false);
		button_ClearForm.setOnAction((ignoredEvent) -> {ControllerAddLesson.performClearForm(); });
		
		// GUI Area 3
		double bottomY = cardY + 428 + 18;
		setupButtonUI(button_BackToMyLessons, 170, 30, bottomY, false);
		button_BackToMyLessons.setOnAction((ignoredEvent) -> {
			ControllerAddLesson.performBackToMyLessons(); });
		setupButtonUI(button_Logout, 90, 210, bottomY, false);
		button_Logout.setOnAction((ignoredEvent) -> {ControllerAddLesson.performLogout(); });
		setupButtonUI(button_Quit, 90, 310, bottomY, false);
		button_Quit.setOnAction((ignoredEvent) -> {ControllerAddLesson.performQuit(); });

		// The card is added first so that it is drawn behind everything placed on it
		theRootPane.getChildren().addAll(
			label_Breadcrumb, label_PageTitle, label_UserDetails, region_Card,
			label_Title, label_TitleHelp, text_Title,
			label_Category, label_CategoryHelp, comboBox_Category,
			label_Situation, label_SituationHelp, text_Situation,
			label_LessonText, label_LessonTextHelp, text_LessonText,
			label_Message, button_SaveLesson, button_ClearForm,
			button_BackToMyLessons, button_Logout, button_Quit);
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
	 * Private local method to place a field's label on the left and its help text on the right
	 * of the same line, above the field
	 * 
	 * @param label	The field's label
	 * @param help	The field's help text (its limits)
	 * @param x		The left edge of the field
	 * @param w		The width of the field
	 * @param y		The location from the top (y axis)
	 */
	private static void setupFieldLabels(Label label, Label help, double x, double w, double y) {
		setupLabelUI(label, 13, FontWeight.BOLD, COLOR_TEXT, x, y);
		setupLabelUI(help, 12, FontWeight.NORMAL, COLOR_MUTED, x, y);
		help.setMinWidth(w);
		help.setAlignment(Pos.CENTER_RIGHT);
	}
	
	
	/**********
	 * Private local method to initialize the standard fields for a button.  Primary buttons are
	 * blue with white text; all others are plain white with a border.
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
	 * Private local method to initialize the size and location of an input widget
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
	
	
	// Gives an input the bordered look the User Experience document uses for fields
	private static void styleInput(Control c) {
		c.setStyle("-fx-background-color: white; -fx-control-inner-background: white;"
				+ " -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 6;"
				+ " -fx-background-radius: 6; -fx-font-family: Arial; -fx-font-size: 13;");
	}
}