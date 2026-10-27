package guiLessonDetail;

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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import database.Database.LessonResult;
import entityClasses.LessonsLearned;
import entityClasses.User;
import lessonRecognizer.LessonRecognizer;


/*******
 * <p> Title: ViewLessonDetail Class. </p>
 * 
 * <p> Description: The Java/FX-based Lesson Detail Page.  A contributor opens one of their
 * lessons from My Lessons to examine it more closely (Contributor story 2), edit it, or delete
 * it (Contributor story 1, "I can see and manage it").  Fields a curator has locked are shown
 * but cannot be changed, and are labeled "Locked by a curator"; a lesson with any lock cannot be
 * deleted, and the page says why (Contributor story 2).  A lesson a curator has edited shows an
 * "Edited by curator" tag (Contributor story 4).  Deleting asks "Are you sure?", naming the
 * lesson, with No as the default.</p>
 * 
 * <p> The layout matches the Add Lesson page, which follows the team's User Experience
 * document, so the two forms look the same; Delete is the red button the document reserves for
 * deleting.</p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * 
 * @version 1.00		2026-10-22 Initial version for TP2 (replaces the placeholder)
 *  
 */

public class ViewLessonDetail {
	
	/*-*******************************************************************************************

	Attributes
	
	 */
	
	// These are the application values required by the user interface
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;
	
	// The User Experience document colors, the same values the My Lessons page uses, plus the
	// red the document reserves for deleting
	private static final String COLOR_BACKGROUND = "#f6f8fa";
	private static final String COLOR_CARD = "white";
	private static final String COLOR_BORDER = "#d0d7de";
	private static final String COLOR_TEXT = "#1f2328";
	private static final String COLOR_MUTED = "#59636e";
	private static final String COLOR_PRIMARY = "#0969da";
	private static final String COLOR_DANGER = "#cf222e";
	private static final String COLOR_LOCKED_FIELD = "#f6f8fa";	// Background of a locked field
	
	// The limits shown next to unlocked fields, from the LessonRecognizer constants
	protected static final String helpTitle = "Required. " + LessonRecognizer.TITLE_MIN + " to "
			+ LessonRecognizer.TITLE_MAX + " characters.";
	protected static final String helpCategory = "Required. Choose one.";
	protected static final String helpText = "Required. " + LessonRecognizer.TEXT_MIN + " to "
			+ String.format("%,d", LessonRecognizer.TEXT_MAX) + " characters.";


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.
	
	// GUI Area 1: The breadcrumb, the page title, whose account is being used, and the
	// curator-edited tag
	protected static Label label_Breadcrumb = new Label("TEAM 53 / CONTRIBUTOR / MY LESSONS");
	protected static Label label_PageTitle = new Label("Lesson detail");
	protected static Label label_UserDetails = new Label();
	protected static Label label_EditedTag = new Label("Edited by curator");

	// GUI Area 2: The card holding the lesson.  Each field's help label shows either its limits
	// or "Locked by a curator"; the Controller decides which.
	protected static Region region_Card = new Region();
	protected static Label label_Title = new Label("Title");
	protected static Label label_TitleHelp = new Label();
	protected static TextField text_Title = new TextField();
	protected static Label label_Category = new Label("Category");
	protected static Label label_CategoryHelp = new Label();
	protected static ComboBox<String> comboBox_Category = new ComboBox<String>();
	protected static Label label_Situation = new Label("Situation (what happened)");
	protected static Label label_SituationHelp = new Label();
	protected static TextArea text_Situation = new TextArea();
	protected static Label label_LessonText = new Label("Lesson (what was learned)");
	protected static Label label_LessonTextHelp = new Label();
	protected static TextArea text_LessonText = new TextArea();
	protected static Label label_Message = new Label();
	protected static Button button_SaveChanges = new Button("Save changes");
	protected static Button button_DeleteLesson = new Button("Delete lesson");
	protected static Label label_LockNote = new Label();	// Why Delete is disabled, if it is
	
	// GUI Area 3: Leaving the page
	protected static Button button_BackToMyLessons = new Button("Back to my lessons");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// This is the end of the GUI objects for the page.
	
	private static ViewLessonDetail theView;	// Used to determine if instantiation of the class
												// is needed
	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	
	private static Scene theViewLessonDetailScene;	// The shared Scene each invocation populates

	/*-*******************************************************************************************

	Constructors
	
	 */


	/**********
	 * <p> Method: displayLessonDetail(Stage ps, User user, LessonsLearned lesson) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Lesson Detail page to be displayed for the lesson selected on My Lessons.  The lesson
	 * is read again from the database by its ID, so the page shows its latest values and lock
	 * state.  If it no longer exists, the contributor is returned to My Lessons with the
	 * not-found message instead.</p>
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 * @param lesson specifies the lesson that was selected on the My Lessons page
	 * 
	 */
	public static void displayLessonDetail(Stage ps, User user, LessonsLearned lesson) {
		theStage = ps;
		theUser = user;
		if (theView == null) theView = new ViewLessonDetail();	// Instantiate singleton if needed
		
		if (ModelLessonDetail.loadLesson(applicationMain.FoundationsMain.database,
				lesson.getLessonId()) == null) {
			guiMyLessons.ViewMyLessons.displayMyLessons(theStage, theUser,
					LessonResult.NOT_FOUND.getMessage(), true);
			return;
		}
		
		label_UserDetails.setText("Signed in as " + theUser.getUserName());
		ControllerLessonDetail.showLesson();
		
		theStage.setTitle("CSE 360 Foundations: Lesson Detail");
		theStage.setScene(theViewLessonDetailScene);
		theStage.show();
		// Without this, JavaFX focuses the first field and selects all of its text, which looks
		// like the title is about to be replaced
		theRootPane.requestFocus();
	}
	
	/**********
	 * <p> Method: ViewLessonDetail() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object.  This is a singleton and is only performed once.</p>
	 * 
	 */
	private ViewLessonDetail() {

		theRootPane = new Pane();
		theRootPane.setStyle("-fx-background-color: " + COLOR_BACKGROUND + ";");
		theViewLessonDetailScene = new Scene(theRootPane, width, height);
		
		// GUI Area 1
		setupLabelUI(label_Breadcrumb, 11, FontWeight.BOLD, COLOR_MUTED, 30, 16);
		setupLabelUI(label_PageTitle, 26, FontWeight.BOLD, COLOR_TEXT, 30, 32);
		setupLabelUI(label_UserDetails, 13, FontWeight.NORMAL, COLOR_MUTED, 30, 70);
		label_EditedTag.setFont(Font.font("Arial", FontWeight.BOLD, 11));
		label_EditedTag.setStyle("-fx-background-color: #ddf4ff; -fx-text-fill: " + COLOR_PRIMARY
				+ "; -fx-padding: 2 8 2 8; -fx-background-radius: 10;");
		// At the right end of the title line, so it never overlaps the title
		label_EditedTag.setMinWidth(120);
		label_EditedTag.setAlignment(Pos.CENTER);
		label_EditedTag.setLayoutX(width - 30 - 120);
		label_EditedTag.setLayoutY(44);
		
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
		
		setupFieldLabels(label_Title, label_TitleHelp, innerX, innerWidth, cardY + 16);
		setupInputUI(text_Title, innerWidth, 30, innerX, cardY + 36);
		
		setupFieldLabels(label_Category, label_CategoryHelp, innerX, innerWidth, cardY + 80);
		setupInputUI(comboBox_Category, 300, 30, innerX, cardY + 100);
		comboBox_Category.getItems().setAll(LessonsLearned.CATEGORIES);
		
		setupFieldLabels(label_Situation, label_SituationHelp, innerX, innerWidth, cardY + 144);
		setupInputUI(text_Situation, innerWidth, 70, innerX, cardY + 164);
		text_Situation.setWrapText(true);
		
		setupFieldLabels(label_LessonText, label_LessonTextHelp, innerX, innerWidth, cardY + 248);
		setupInputUI(text_LessonText, innerWidth, 70, innerX, cardY + 268);
		text_LessonText.setWrapText(true);
		
		label_Message.setFont(Font.font("Arial", 13));
		label_Message.setLayoutX(innerX);
		label_Message.setLayoutY(cardY + 346);
		label_Message.setMaxWidth(innerWidth);
		label_Message.setWrapText(true);
		
		setupButtonUI(button_SaveChanges, 140, innerX, cardY + 382, COLOR_PRIMARY);
		button_SaveChanges.setOnAction((ignoredEvent) -> {
			ControllerLessonDetail.performSaveChanges(); });
		setupButtonUI(button_DeleteLesson, 130, innerX + 150, cardY + 382, COLOR_DANGER);
		button_DeleteLesson.setOnAction((ignoredEvent) -> {
			ControllerLessonDetail.performDeleteLesson(); });
		
		label_LockNote.setFont(Font.font("Arial", 12));
		label_LockNote.setTextFill(Color.web("#bc4c00"));
		label_LockNote.setLayoutX(innerX + 290);
		label_LockNote.setLayoutY(cardY + 382);
		label_LockNote.setMaxWidth(innerWidth - 290);
		label_LockNote.setWrapText(true);
		
		// GUI Area 3
		double bottomY = cardY + 428 + 18;
		setupButtonUI(button_BackToMyLessons, 170, 30, bottomY, null);
		button_BackToMyLessons.setOnAction((ignoredEvent) -> {
			ControllerLessonDetail.performBackToMyLessons(); });
		setupButtonUI(button_Logout, 90, 210, bottomY, null);
		button_Logout.setOnAction((ignoredEvent) -> {ControllerLessonDetail.performLogout(); });
		setupButtonUI(button_Quit, 90, 310, bottomY, null);
		button_Quit.setOnAction((ignoredEvent) -> {ControllerLessonDetail.performQuit(); });

		// The card is added first so that it is drawn behind everything placed on it
		theRootPane.getChildren().addAll(
			label_Breadcrumb, label_PageTitle, label_UserDetails, label_EditedTag, region_Card,
			label_Title, label_TitleHelp, text_Title,
			label_Category, label_CategoryHelp, comboBox_Category,
			label_Situation, label_SituationHelp, text_Situation,
			label_LessonText, label_LessonTextHelp, text_LessonText,
			label_Message, button_SaveChanges, button_DeleteLesson, label_LockNote,
			button_BackToMyLessons, button_Logout, button_Quit);
	}
	
	
	/**********
	 * <p> Method: styleAllInputs() </p>
	 * 
	 * <p> Description: Gives every input the bordered field look, with a gray background on
	 * locked fields so the contributor can see at a glance which ones cannot be changed.  The
	 * Controller calls this after it sets each field's lock state.</p>
	 * 
	 */
	protected static void styleAllInputs() {
		styleInput(text_Title, !text_Title.isEditable());
		styleInput(comboBox_Category, comboBox_Category.isDisabled());
		styleInput(text_Situation, !text_Situation.isEditable());
		styleInput(text_LessonText, !text_LessonText.isEditable());
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
		l.setTextFill(Color.web(color));
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	
	/**********
	 * Private local method to place a field's label on the left and its help text on the right
	 * of the same line, above the field
	 * 
	 * @param label	The field's label
	 * @param help	The field's help text
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
	 * Private local method to initialize the standard fields for a button
	 * 
	 * @param b		The Button object to be initialized
	 * @param w		The width of the Button
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 * @param fill	The button color (blue for the main action, red for deleting), or null for a
	 * 				plain bordered button
	 */
	private static void setupButtonUI(Button b, double w, double x, double y, String fill){
		b.setFont(Font.font("Arial", 13));
		b.setMinWidth(w);
		b.setPrefHeight(30);
		b.setLayoutX(x);
		b.setLayoutY(y);
		if (fill != null)
			b.setStyle("-fx-background-color: " + fill + "; -fx-text-fill: white;"
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
	
	
	// Gives an input the bordered field look; a locked field gets a gray background
	private static void styleInput(Control c, boolean locked) {
		String background = locked ? COLOR_LOCKED_FIELD : "white";
		c.setStyle("-fx-background-color: " + background + "; -fx-control-inner-background: "
				+ background + "; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 6;"
				+ " -fx-background-radius: 6; -fx-font-family: Arial; -fx-font-size: 13;"
				+ " -fx-opacity: 1;");
	}
}