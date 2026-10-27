package guiContributor;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.LessonsLearned;
import entityClasses.LessonsLearnedList;
import entityClasses.User;


/*******
 * <p> Title: ViewContributorHome Class. </p>
 *
 * <p> Description: The Java/FX-based Contributor Home Page.  GUI Area 2 holds the widgets a
 * contributor uses to create, read, update, and delete the lessons learned that the contributor
 * has added: a list of the contributor's lessons on the left and a form for one lesson's core
 * information on the right.</p>
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 * @author Joshua Luther (J.L.) - lessons learned widgets in GUI Area 2
 *
 * @version 1.00        2025-08-20 Initial version
 * @version 1.01        2026-10-08 GUI Area 2 filled in with the lesson list and lesson form for
 *                          HW2 (Joshua Luther)
 *
 */

public class ViewContributorHome {

    /*-*******************************************************************************************

    Attributes

     */

    // These are the application values required by the user interface

    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;


    // These are the widget attributes for the GUI. There are 3 areas for this GUI.

    // GUI Area 1: It informs the user about the purpose of this page, whose account is being used,
    // and a button to allow this user to update the account settings
    protected static Label label_PageTitle = new Label();
    protected static Label label_UserDetails = new Label();
    protected static Button button_UpdateThisUser = new Button("Account Update");

    // This is a separator and it is used to partition the GUI for various tasks
    protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

    // GUI Area 2: The contributor's lessons learned.  The list on the left shows only this
    // contributor's lessons (US-R1).  Selecting one loads it into the form on the right (US-R2),
    // where it can be edited (US-U1) or deleted (US-D1).  The form is also used to add a new
    // lesson (US-C1).  The message label reports confirmations and validation errors.
    protected static Label label_MyLessons = new Label("My Lessons");
    protected static ListView<LessonsLearned> listView_MyLessons = new ListView<LessonsLearned>();

    protected static Label label_Title = new Label("Title");
    protected static TextField text_Title = new TextField();
    protected static Label label_Category = new Label("Category");
    protected static ComboBox<String> comboBox_Category = new ComboBox<String>();
    protected static Label label_Situation = new Label("Situation (what happened)");
    protected static TextArea text_Situation = new TextArea();
    protected static Label label_LessonText = new Label("Lesson (what was learned)");
    protected static TextArea text_LessonText = new TextArea();

    protected static Label label_Message = new Label();

    protected static Button button_NewLesson = new Button("New");
    protected static Button button_AddLesson = new Button("View");
    protected static Button button_SaveLesson = new Button("Edit");
    protected static Button button_DeleteLesson = new Button("Delete");

    // This is a separator and it is used to partition the GUI for various tasks
    protected static Line line_Separator4 = new Line(20, 525, width-20,525);

    // GUI Area 3: This is last of the GUI areas.  It is used for quitting the application and for
    // logging out.
    protected static Button button_Browse = new Button("My Lessons / Search");
    protected static Button button_Logout = new Button("Logout");
    protected static Button button_Quit = new Button("Quit");

    // This is the end of the GUI objects for the page.

    // These attributes are used to configure the page and populate it with this user's information
    private static ViewContributorHome theView;     // Used to determine if instantiation of the class
                                                // is needed

    // Reference for the in-memory database so this package has access
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    protected static Stage theStage;            // The Stage that JavaFX has established for us
    protected static Pane theRootPane;          // The Pane that holds all the GUI widgets
    protected static User theUser;              // The current logged in User


    private static Scene theViewContributorHomeScene;   // The shared Scene each invocation populates
    protected static final int theRole = 2;     // Admin: 1; Contributor: 2; Viewer: 3; Curator: 4

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
     * and the system's current state, including this contributor's list of lessons.  It then sets
     * the Scene onto the stage, and makes it visible to the user.
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
        if (theView == null) theView = new ViewContributorHome();       // Instantiate singleton if needed

        // Populate the dynamic aspects of the GUI with the data from the user and the current
        // state of the system.
        theDatabase.getUserAccountDetails(user.getUserName());
        applicationMain.FoundationsMain.activeHomePage = theRole;

        label_UserDetails.setText("User: " + theUser.getUserName());

        // The page is a singleton, so a different contributor may have used it before.  Clear the
        // form and reload the list so only this contributor's lessons are shown.
        ControllerContributorHome.performClearForm();
        ControllerContributorHome.refreshLessonList();

        // Set the title for the window, display the page, and wait for the Admin to do something
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
        theViewContributorHomeScene = new Scene(theRootPane, width, height);    // Create the scene

        // Set the title for the window

        // Populate the window with the title and other common widgets and set their static state

        // GUI Area 1
        label_PageTitle.setText("Contributor Home Page");
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        label_UserDetails.setText("User: " + theUser.getUserName());
        setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

        setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
        button_UpdateThisUser.setOnAction((ignoredEvent) -> {ControllerContributorHome.performUpdate(); });

        // GUI Area 2

        // The left column: this contributor's lessons.  The ListView displays each LessonsLearned using
        // its toString() method, which returns the title.  The placeholder is shown whenever the
        // list is empty, using the same message the automated tests check (TC-11).
        setupLabelUI(label_MyLessons, "Arial", 16, 260, Pos.BASELINE_LEFT, 20, 100);
        // Leave space below the list for the Browse button; no rows are covered.
        setupInputUI(listView_MyLessons, 260, 340, 20, 125);
        listView_MyLessons.setPlaceholder(new Label(LessonsLearnedList.EMPTY_LIST_MESSAGE));
        listView_MyLessons.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldLesson, newLesson) -> {ControllerContributorHome.performSelectLesson(); });

        // The right column: the form for one lesson's core information
        double formX = 300;
        double formWidth = width - formX - 20;

        setupLabelUI(label_Title, "Arial", 14, formWidth, Pos.BASELINE_LEFT, formX, 100);
        setupInputUI(text_Title, formWidth, 28, formX, 120);
        text_Title.setPromptText("5 to 100 characters");

        setupLabelUI(label_Category, "Arial", 14, formWidth, Pos.BASELINE_LEFT, formX, 163);
        setupInputUI(comboBox_Category, formWidth, 28, formX, 183);
        comboBox_Category.getItems().setAll(LessonsLearned.CATEGORIES); // The same list the recognizer uses
        comboBox_Category.setPromptText("Select a category");

        setupLabelUI(label_Situation, "Arial", 14, formWidth, Pos.BASELINE_LEFT, formX, 226);
        setupInputUI(text_Situation, formWidth, 75, formX, 246);
        text_Situation.setWrapText(true);
        text_Situation.setPromptText("20 to 2,000 characters");

        setupLabelUI(label_LessonText, "Arial", 14, formWidth, Pos.BASELINE_LEFT, formX, 336);
        setupInputUI(text_LessonText, formWidth, 75, formX, 356);
        text_LessonText.setWrapText(true);
        text_LessonText.setPromptText("20 to 2,000 characters");

        // The message label is wide enough for the longest error message and wraps if needed
        setupLabelUI(label_Message, "Arial", 14, formWidth, Pos.BASELINE_LEFT, formX, 440);
        label_Message.setMaxWidth(formWidth);
        label_Message.setWrapText(true);

        double buttonWidth = (formWidth - 3 * 10) / 4;      // Four buttons with 10-pixel gaps
        setupButtonUI(button_NewLesson, "Dialog", 14, buttonWidth, Pos.CENTER, formX, 480);
        button_NewLesson.setOnAction((ignoredEvent) -> {ControllerContributorHome.performAddLesson(); });

        setupButtonUI(button_AddLesson, "Dialog", 14, buttonWidth, Pos.CENTER,
                formX + (buttonWidth + 10), 480);
        button_AddLesson.setOnAction((ignoredEvent) -> {ControllerContributorHome.performOpenViewLesson(); });

        setupButtonUI(button_SaveLesson, "Dialog", 14, buttonWidth, Pos.CENTER,
                formX + 2 * (buttonWidth + 10), 480);
        button_SaveLesson.setOnAction((ignoredEvent) -> {ControllerContributorHome.performSaveLesson(); });

        setupButtonUI(button_DeleteLesson, "Dialog", 14, buttonWidth, Pos.CENTER,
                formX + 3 * (buttonWidth + 10), 480);
        button_DeleteLesson.setOnAction((ignoredEvent) -> {ControllerContributorHome.performDeleteLesson(); });

        // K.G. integrates the current list with dedicated MVC CRUD windows. The old form is
        // a read-only selection preview, so no alternate editable route bypasses window rules.
        text_Title.setEditable(false); comboBox_Category.setDisable(true);
        text_Situation.setEditable(false); text_LessonText.setEditable(false);
        button_NewLesson.setId("home-new"); button_AddLesson.setId("home-view");
        button_SaveLesson.setId("home-edit"); button_DeleteLesson.setId("home-delete");
        listView_MyLessons.setId("home-lessons");
        button_SaveLesson.setText("Edit");
        button_NewLesson.setStyle("-fx-background-color: #175ab8; -fx-text-fill: white;");
        button_DeleteLesson.setStyle("-fx-background-color: #b42335; -fx-text-fill: white;");
        button_AddLesson.disableProperty().bind(listView_MyLessons.getSelectionModel().selectedItemProperty().isNull());

        // Save and Delete only make sense for an existing lesson, so they are disabled until one
        // is selected.  This prevents an "edit nothing" error rather than reporting it afterward.
        button_SaveLesson.disableProperty().bind(
                listView_MyLessons.getSelectionModel().selectedItemProperty().isNull());
        button_DeleteLesson.disableProperty().bind(
                listView_MyLessons.getSelectionModel().selectedItemProperty().isNull());

        // VG10: preserve Joshua's latest dedicated My Lessons/search route.
        setupButtonUI(button_Browse, "Dialog", 13, 190, Pos.CENTER, 20, 480);
        button_Browse.setId("home-browse");
        button_Browse.setOnAction(e -> guiMyLessons.ViewMyLessons.displayMyLessons(theStage,theUser));
        // GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((ignoredEvent) -> {ControllerContributorHome.performLogout(); });

        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((ignoredEvent) -> {ControllerContributorHome.performQuit(); });

        // This is the end of the GUI initialization code

        // Place all of the widget items into the Root Pane's list of children
         theRootPane.getChildren().addAll(
            label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
            label_MyLessons, listView_MyLessons,
            label_Title, text_Title, label_Category, comboBox_Category,
            label_Situation, text_Situation, label_LessonText, text_LessonText,
            label_Message, button_NewLesson, button_AddLesson, button_SaveLesson,
            button_DeleteLesson,
            line_Separator4, button_Browse, button_Logout, button_Quit);
}


    /*-********************************************************************************************

    Helper methods to reduce code length

     */

    /**********
     * Private local method to initialize the standard fields for a label
     *
     * @param l     The Label object to be initialized
     * @param ff    The font to be used
     * @param f     The size of the font to be used
     * @param w     The width of the Button
     * @param p     The alignment (e.g. left, centered, or right)
     * @param x     The location from the left edge (x axis)
     * @param y     The location from the top (y axis)
     */
    private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x,
            double y){
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }


    /**********
     * Private local method to initialize the standard fields for a button
     *
     * @param b     The Button object to be initialized
     * @param ff    The font to be used
     * @param f     The size of the font to be used
     * @param w     The width of the Button
     * @param p     The alignment (e.g. left, centered, or right)
     * @param x     The location from the left edge (x axis)
     * @param y     The location from the top (y axis)
     */
    private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x,
            double y){
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }


    /**********
     * Private local method to initialize the size and location of an input widget (a text field,
     * text area, combo box, or list).  It follows the same approach as setupLabelUI and
     * setupButtonUI, but sets a height as well because these widgets are not sized by their text.
     *
     * @param c     The input widget to be initialized
     * @param w     The width of the widget
     * @param h     The height of the widget
     * @param x     The location from the left edge (x axis)
     * @param y     The location from the top (y axis)
     */
    private static void setupInputUI(Control c, double w, double h, double x, double y) {
        c.setPrefWidth(w);
        c.setPrefHeight(h);
        c.setLayoutX(x);
        c.setLayoutY(y);
    }
}
