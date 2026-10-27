package guiAddLesson;

import javafx.scene.paint.Color;
import database.Database;
import database.Database.LessonResult;
import lessonRecognizer.LessonRecognizer;

/*******
 * <p> Title: ControllerAddLesson Class. </p>
 *
 * <p> Description: The Java/FX-based Add Lesson Page.  This class provides the controller
 * actions based on the contributor's use of the widgets defined by the View: saving the new
 * lesson, clearing the form, and leaving the page.
 *
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 *
 * <p> Copyright: Joshua Luther © 2026 </p>
 *
 * @author Joshua Luther
 *
 * @version 1.00		2026-10-22 Initial version for TP2
 */
public class ControllerAddLesson {
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**
	 * Default constructor is not used.
	 */
	public ControllerAddLesson() {
	}

	
	/**********
	 * <p> Method: performSaveLesson() </p>
	 *
	 * <p> Description: Stores the lesson in the form as a new lesson owned by the logged-in
	 * contributor (Contributor story 1).  On success the contributor is taken to My Lessons,
	 * where the new lesson appears with the message "Lesson added."  On a validation failure the
	 * form is kept, the specific message is shown in red, and the cursor moves to the field that
	 * needs fixing, so nothing the contributor typed is lost. </p>
	 *
	 */
	protected static void performSaveLesson() {
		LessonResult result = ModelAddLesson.saveNewLesson(theDatabase,
				ViewAddLesson.theUser.getUserName(),
				ViewAddLesson.text_Title.getText(),
				ViewAddLesson.comboBox_Category.getValue(),
				ViewAddLesson.text_Situation.getText(),
				ViewAddLesson.text_LessonText.getText());
		
		if (result == LessonResult.SUCCESS) {
			guiMyLessons.ViewMyLessons.displayMyLessons(ViewAddLesson.theStage,
					ViewAddLesson.theUser, "Lesson added.");
		}
		else if (result == LessonResult.INVALID_INPUT) {
			showError(LessonRecognizer.lessonRecognizerErrorMessage);
			focusField(LessonRecognizer.lessonRecognizerErrorField);
		}
		else showError(result.getMessage());
	}

	
	/**********
	 * <p> Method: performClearForm() </p>
	 *
	 * <p> Description: Empties every field and the message, so the contributor can start the
	 * lesson over.  It is also called each time the page is displayed. </p>
	 *
	 */
	protected static void performClearForm() {
		ViewAddLesson.text_Title.clear();
		ViewAddLesson.comboBox_Category.setValue(null);
		ViewAddLesson.text_Situation.clear();
		ViewAddLesson.text_LessonText.clear();
		ViewAddLesson.label_Message.setText("");
	}

	
	/**********
	 * <p> Method: performBackToMyLessons() </p>
	 *
	 * <p> Description: Returns to My Lessons without saving. </p>
	 *
	 */
	protected static void performBackToMyLessons() {
		guiMyLessons.ViewMyLessons.displayMyLessons(ViewAddLesson.theStage, ViewAddLesson.theUser);
	}

	
	/**********
	 * <p> Method: performLogout() </p>
	 *
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page. </p>
	 *
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAddLesson.theStage);
	}

	
	/**********
	 * <p> Method: performQuit() </p>
	 *
	 * <p> Description: This method terminates the execution of the program. </p>
	 *
	 */
	protected static void performQuit() {
		System.exit(0);
	}
	
	
	// Shows an error in red, as the User Experience document requires for messages that explain
	// what needs to be fixed
	private static void showError(String message) {
		ViewAddLesson.label_Message.setTextFill(Color.web("#cf222e"));
		ViewAddLesson.label_Message.setText(message);
	}
	
	// Moves the cursor to the field the recognizer named, so the contributor sees both what is
	// wrong and where to fix it.  The names are the ones LessonRecognizer reports.
	private static void focusField(String fieldName) {
		switch (fieldName) {
		case "Title":		ViewAddLesson.text_Title.requestFocus(); break;
		case "Category":	ViewAddLesson.comboBox_Category.requestFocus(); break;
		case "Situation":	ViewAddLesson.text_Situation.requestFocus(); break;
		case "Lesson":		ViewAddLesson.text_LessonText.requestFocus(); break;
		default: break;
		}
	}
}