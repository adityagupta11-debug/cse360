package guiLessonDetail;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import database.Database;
import database.Database.LessonResult;
import entityClasses.LessonsLearned;
import lessonRecognizer.LessonRecognizer;

/*******
 * <p> Title: ControllerLessonDetail Class. </p>
 *
 * <p> Description: The Java/FX-based Lesson Detail Page.  This class provides the controller
 * actions for examining one lesson: showing its fields with their lock state, saving edits,
 * deleting it after confirmation, and leaving the page.
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
public class ControllerLessonDetail {
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	private static final String COLOR_ERROR = "#cf222e";
	private static final String COLOR_SUCCESS = "#1a7f37";
	private static final String COLOR_LOCKED = "#bc4c00";
	private static final String COLOR_MUTED = "#59636e";

	/**
	 * Default constructor is not used.
	 */
	public ControllerLessonDetail() {
	}

	
	/**********
	 * <p> Method: showLesson() </p>
	 *
	 * <p> Description: Puts the current lesson into the page and applies its lock state
	 * (Contributor story 2: "I can see the fields I can alter, and which are locked and
	 * unavailable for me to edit").  A locked field cannot be changed and is labeled "Locked by
	 * a curator".  Delete is disabled when the lesson or any field is locked, with a note saying
	 * why, because the story says such a lesson cannot be deleted. </p>
	 *
	 */
	protected static void showLesson() {
		LessonsLearned lesson = ModelLessonDetail.getCurrentLesson();
		
		ViewLessonDetail.text_Title.setText(lesson.getTitle());
		ViewLessonDetail.comboBox_Category.setValue(lesson.getCategory());
		ViewLessonDetail.text_Situation.setText(lesson.getSituation());
		ViewLessonDetail.text_LessonText.setText(lesson.getLessonText());
		
		// Each field's lock state.  isFieldLocked is also true when the whole lesson is locked.
		applyLock(lesson.isFieldLocked("Title"), ViewLessonDetail.label_TitleHelp,
				ViewLessonDetail.helpTitle);
		ViewLessonDetail.text_Title.setEditable(!lesson.isFieldLocked("Title"));
		applyLock(lesson.isFieldLocked("Category"), ViewLessonDetail.label_CategoryHelp,
				ViewLessonDetail.helpCategory);
		ViewLessonDetail.comboBox_Category.setDisable(lesson.isFieldLocked("Category"));
		applyLock(lesson.isFieldLocked("Situation"), ViewLessonDetail.label_SituationHelp,
				ViewLessonDetail.helpText);
		ViewLessonDetail.text_Situation.setEditable(!lesson.isFieldLocked("Situation"));
		applyLock(lesson.isFieldLocked("Lesson"), ViewLessonDetail.label_LessonTextHelp,
				ViewLessonDetail.helpText);
		ViewLessonDetail.text_LessonText.setEditable(!lesson.isFieldLocked("Lesson"));
		ViewLessonDetail.styleAllInputs();
		
		// There is nothing to save when every field is locked
		boolean allLocked = lesson.isFieldLocked("Title") && lesson.isFieldLocked("Category")
				&& lesson.isFieldLocked("Situation") && lesson.isFieldLocked("Lesson");
		ViewLessonDetail.button_SaveChanges.setDisable(allLocked);
		
		// Contributor story 2: "When a lesson I created is locked or has a locked field, I am
		// unable to delete that lesson."
		ViewLessonDetail.button_DeleteLesson.setDisable(lesson.hasAnyLock());
		ViewLessonDetail.label_LockNote.setText(lesson.hasAnyLock()
				? "This lesson can't be deleted because a curator locked it or one of its fields."
				: "");
		
		// Contributor story 4: the contributor sees their lesson after a curator has edited it
		ViewLessonDetail.label_EditedTag.setVisible(lesson.isCuratorEdited());
		
		ViewLessonDetail.label_Message.setText("");
	}

	
	/**********
	 * <p> Method: performSaveChanges() </p>
	 *
	 * <p> Description: Saves the edited values.  On success the page shows the stored lesson and
	 * "Changes saved." in green.  On a validation failure the edits stay on the page, the
	 * specific message is shown in red, and the cursor moves to the field to fix.  Any other
	 * refusal (not found, not the owner) shows that reason, and nothing is changed. </p>
	 *
	 */
	protected static void performSaveChanges() {
		LessonResult result = ModelLessonDetail.saveChanges(theDatabase,
				ViewLessonDetail.theUser.getUserName(),
				ViewLessonDetail.text_Title.getText(),
				ViewLessonDetail.comboBox_Category.getValue(),
				ViewLessonDetail.text_Situation.getText(),
				ViewLessonDetail.text_LessonText.getText());
		
		if (result == LessonResult.SUCCESS) {
			showLesson();
			showMessage("Changes saved.", COLOR_SUCCESS);
		}
		else if (result == LessonResult.INVALID_INPUT) {
			showMessage(LessonRecognizer.lessonRecognizerErrorMessage, COLOR_ERROR);
			focusField(LessonRecognizer.lessonRecognizerErrorField);
		}
		else showMessage(result.getMessage(), COLOR_ERROR);
	}

	
	/**********
	 * <p> Method: performDeleteLesson() </p>
	 *
	 * <p> Description: Asks "Are you sure?", naming the lesson, and deletes it only on "Yes".
	 * As in the team's User Experience document, "No" is the default choice, so pressing Enter
	 * or closing the dialog never deletes anything.  On success the contributor returns to My
	 * Lessons, which shows "Lesson deleted." </p>
	 *
	 */
	protected static void performDeleteLesson() {
		LessonsLearned lesson = ModelLessonDetail.getCurrentLesson();
		if (lesson == null || lesson.hasAnyLock()) return;	// The button is disabled in both cases
		
		Alert confirm = new Alert(AlertType.CONFIRMATION,
				"Delete the lesson \"" + lesson.getTitle() + "\"?  This cannot be undone.",
				ButtonType.YES, ButtonType.NO);
		confirm.setTitle("Delete lesson");
		confirm.setHeaderText("Are you sure?");
		confirm.initOwner(ViewLessonDetail.theStage);
		// JavaFX makes Yes the default; the User Experience document says No must be
		((Button) confirm.getDialogPane().lookupButton(ButtonType.YES)).setDefaultButton(false);
		((Button) confirm.getDialogPane().lookupButton(ButtonType.NO)).setDefaultButton(true);
		Optional<ButtonType> answer = confirm.showAndWait();
		boolean confirmed = answer.isPresent() && answer.get() == ButtonType.YES;
		
		LessonResult result = ModelLessonDetail.deleteLesson(theDatabase,
				ViewLessonDetail.theUser.getUserName(), confirmed);
		
		if (result == LessonResult.SUCCESS)
			guiMyLessons.ViewMyLessons.displayMyLessons(ViewLessonDetail.theStage,
					ViewLessonDetail.theUser, "Lesson deleted.");
		else if (result == LessonResult.CANCELLED)
			showMessage("Delete cancelled. The lesson was not changed.", COLOR_MUTED);
		else showMessage(result.getMessage(), COLOR_ERROR);
	}

	
	/**********
	 * <p> Method: performBackToMyLessons() </p>
	 *
	 * <p> Description: Returns to My Lessons without saving unsaved edits. </p>
	 *
	 */
	protected static void performBackToMyLessons() {
		guiMyLessons.ViewMyLessons.displayMyLessons(ViewLessonDetail.theStage,
				ViewLessonDetail.theUser);
	}

	
	/**********
	 * <p> Method: performLogout() </p>
	 *
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page. </p>
	 *
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewLessonDetail.theStage);
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
	
	
	// Shows a field's limits normally, or "Locked by a curator" in orange when it is locked
	private static void applyLock(boolean locked, javafx.scene.control.Label help, String limits) {
		help.setText(locked ? "Locked by a curator" : limits);
		help.setTextFill(Color.web(locked ? COLOR_LOCKED : COLOR_MUTED));
	}
	
	private static void showMessage(String message, String color) {
		ViewLessonDetail.label_Message.setTextFill(Color.web(color));
		ViewLessonDetail.label_Message.setText(message);
	}
	
	// Moves the cursor to the field the recognizer named (the names LessonRecognizer reports)
	private static void focusField(String fieldName) {
		switch (fieldName) {
		case "Title":		ViewLessonDetail.text_Title.requestFocus(); break;
		case "Category":	ViewLessonDetail.comboBox_Category.requestFocus(); break;
		case "Situation":	ViewLessonDetail.text_Situation.requestFocus(); break;
		case "Lesson":		ViewLessonDetail.text_LessonText.requestFocus(); break;
		default: break;
		}
	}
}