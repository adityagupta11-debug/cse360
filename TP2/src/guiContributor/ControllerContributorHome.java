package guiContributor;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import database.Database;
import database.Database.LessonResult;
import entityClasses.Lesson;
import lessonRecognizer.LessonRecognizer;


/*******
 * <p> Title: ControllerContributorHome Class. </p>
 *
 * <p> Description: The Java/FX-based Contributor Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class, including
 * the actions a contributor uses to create, read, update, and delete lessons learned.
 *
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 * @author Joshua Luther (J.L.) - lessons learned actions
 *
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *
 * @version 1.02		2026-10-08 Lesson create, read, update, and delete actions for HW2
 * 							(Joshua Luther)
 */

public class ControllerContributorHome {

	/*-*******************************************************************************************

	User Interface Actions for this page

	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and
	the Model is often just a stub, or will be a singleton instantiated object.

	 */
	
	// Reference for the in-memory database so this package has access (the same approach the View
	// uses)
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**
	 * Default constructor is not used.
	 */
	public ControllerContributorHome() {
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 *
	 * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 *
	 */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewContributorHome.theStage, ViewContributorHome.theUser);
	}
	
	
	/**********
	 * <p> Method: refreshLessonList() </p>
	 *
	 * <p> Description: Reloads the list from the database with only the lessons owned by the
	 * logged-in contributor (US-R1).  Reading from the database each time, rather than editing the
	 * list in memory, guarantees the list always shows what is actually stored. </p>
	 *
	 */
	protected static void refreshLessonList() {
		ViewContributorHome.listView_MyLessons.getItems().setAll(
				theDatabase.getLessonsByOwner(currentContributor()).getLessons());
	}
	
	
	/**********
	 * <p> Method: performSelectLesson() </p>
	 *
	 * <p> Description: Called when the contributor selects a lesson in the list.  The lesson is
	 * read again from the database by its ID and its core information is shown in the form
	 * (US-R2).  If it no longer exists, the not-found message is shown (US-V5). </p>
	 *
	 */
	protected static void performSelectLesson() {
		Lesson selected = ViewContributorHome.listView_MyLessons.getSelectionModel().getSelectedItem();
		
		// The selection is cleared whenever the list is reloaded or the form is cleared.  Nothing
		// needs to happen then, and the message label is left alone so a confirmation such as
		// "Lesson added." is not erased by the reload that follows it.
		if (selected == null) return;
		
		Lesson current = theDatabase.getLesson(selected.getLessonId());
		if (current == null) {
			showError(LessonResult.NOT_FOUND.getMessage());
			return;
		}
		ViewContributorHome.text_Title.setText(current.getTitle());
		ViewContributorHome.comboBox_Category.setValue(current.getCategory());
		ViewContributorHome.text_Situation.setText(current.getSituation());
		ViewContributorHome.text_LessonText.setText(current.getLessonText());
		clearMessage();
	}
	
	
	/**********
	 * <p> Method: performClearForm() </p>
	 *
	 * <p> Description: Empties the form and clears the selection, so the contributor can enter a
	 * new lesson from a blank form. </p>
	 *
	 */
	protected static void performClearForm() {
		ViewContributorHome.listView_MyLessons.getSelectionModel().clearSelection();
		ViewContributorHome.text_Title.clear();
		ViewContributorHome.comboBox_Category.setValue(null);
		ViewContributorHome.text_Situation.clear();
		ViewContributorHome.text_LessonText.clear();
		clearMessage();
	}
	
	
	/**********
	 * <p> Method: performAddLesson() </p>
	 *
	 * <p> Description: Adds the lesson in the form as a new lesson owned by the logged-in
	 * contributor (US-C1).  The Database validates the input with the LessonRecognizer, so the
	 * error message shown here is exactly the one the automated tests check. </p>
	 *
	 */
	protected static void performAddLesson() {
		Lesson lesson = lessonFromForm(0, currentContributor());
		LessonResult result = theDatabase.addLesson(lesson);
		
		if (result == LessonResult.SUCCESS) {
			refreshLessonList();
			selectLesson(lesson.getLessonId());		// Show the contributor the lesson just added
			showSuccess("Lesson added.");
		}
		else if (result == LessonResult.INVALID_INPUT) showValidationError();
		else showError(result.getMessage());
	}
	
	
	/**********
	 * <p> Method: performSaveLesson() </p>
	 *
	 * <p> Description: Saves the edits in the form to the selected lesson (US-U1).  If the input
	 * is invalid, or the lesson no longer exists or is not owned by this contributor, the stored
	 * lesson is unchanged and the reason is shown. </p>
	 *
	 */
	protected static void performSaveLesson() {
		Lesson selected = ViewContributorHome.listView_MyLessons.getSelectionModel().getSelectedItem();
		if (selected == null) return;		// The Save button is disabled when nothing is selected
		
		Lesson edited = lessonFromForm(selected.getLessonId(), selected.getOwner());
		LessonResult result = theDatabase.updateLesson(edited, currentContributor());
		
		if (result == LessonResult.SUCCESS) {
			refreshLessonList();
			selectLesson(edited.getLessonId());	// Keep the edited lesson selected after the reload
			showSuccess("Lesson updated.");
		}
		else if (result == LessonResult.INVALID_INPUT) showValidationError();
		else showError(result.getMessage());
	}
	
	
	/**********
	 * <p> Method: performDeleteLesson() </p>
	 *
	 * <p> Description: Deletes the selected lesson after the contributor answers "Yes" to
	 * "Are you sure?" (US-D1).  Like the Admin delete-user page, the View asks the question and
	 * the Database is told the answer, so a "No" answer never changes the database. </p>
	 *
	 */
	protected static void performDeleteLesson() {
		Lesson selected = ViewContributorHome.listView_MyLessons.getSelectionModel().getSelectedItem();
		if (selected == null) return;		// The Delete button is disabled when nothing is selected
		
		Alert confirm = new Alert(AlertType.CONFIRMATION,
				"Delete the lesson \"" + selected.getTitle() + "\"?  This cannot be undone.",
				ButtonType.YES, ButtonType.NO);
		confirm.setTitle("Delete Lesson");
		confirm.setHeaderText("Are you sure?");
		Optional<ButtonType> answer = confirm.showAndWait();
		boolean confirmed = answer.isPresent() && answer.get() == ButtonType.YES;
		
		LessonResult result = theDatabase.deleteLesson(selected.getLessonId(),
				currentContributor(), confirmed);
		
		if (result == LessonResult.SUCCESS) {
			performClearForm();
			refreshLessonList();
			showSuccess("Lesson deleted.");
		}
		else if (result == LessonResult.CANCELLED) showSuccess("Delete cancelled. The lesson was not changed.");
		else showError(result.getMessage());
	}

	/**********
	 * <p> Method: performLogout() </p>
	 *
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 *
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewContributorHome.theStage);
	}

	/**********
	 * <p> Method: performQuit() </p>
	 *
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 *
	 */
	protected static void performQuit() {
		System.exit(0);
	}
	
	
	/*-*******************************************************************************************

	Private helper methods for the lesson actions

	 */
	
	/*
	 * The owner always comes from the user this page was displayed for, not from the Database's
	 * current-user cache.  That cache changes whenever any user's details are read (see the K.G.
	 * comments in Database), but theUser is fixed for as long as this contributor is on the page.
	 */
	private static String currentContributor() {
		return ViewContributorHome.theUser.getUserName();
	}
	
	// Builds a Lesson from the values currently in the form
	private static Lesson lessonFromForm(int lessonId, String owner) {
		return new Lesson(lessonId, owner,
				ViewContributorHome.text_Title.getText(),
				ViewContributorHome.comboBox_Category.getValue(),
				ViewContributorHome.text_Situation.getText(),
				ViewContributorHome.text_LessonText.getText());
	}
	
	// Selects the lesson with the given ID in the list, if it is there
	private static void selectLesson(int lessonId) {
		for (Lesson lesson : ViewContributorHome.listView_MyLessons.getItems()) {
			if (lesson.getLessonId() == lessonId) {
				ViewContributorHome.listView_MyLessons.getSelectionModel().select(lesson);
				return;
			}
		}
	}
	
	/*
	 * Shows the recognizer's message and moves the cursor to the field it names, so the
	 * contributor sees both what is wrong and where to fix it.
	 */
	private static void showValidationError() {
		showError(LessonRecognizer.lessonRecognizerErrorMessage);
		switch (LessonRecognizer.lessonRecognizerErrorField) {
		case "Title":		ViewContributorHome.text_Title.requestFocus(); break;
		case "Category":	ViewContributorHome.comboBox_Category.requestFocus(); break;
		case "Situation":	ViewContributorHome.text_Situation.requestFocus(); break;
		case "Lesson":		ViewContributorHome.text_LessonText.requestFocus(); break;
		default: break;
		}
	}
	
	// Errors are shown in red and confirmations in green, so the two are never confused
	private static void showError(String message) {
		ViewContributorHome.label_Message.setTextFill(Color.RED);
		ViewContributorHome.label_Message.setText(message);
	}
	
	private static void showSuccess(String message) {
		ViewContributorHome.label_Message.setTextFill(Color.GREEN);
		ViewContributorHome.label_Message.setText(message);
	}
	
	private static void clearMessage() {
		ViewContributorHome.label_Message.setText("");
	}
}