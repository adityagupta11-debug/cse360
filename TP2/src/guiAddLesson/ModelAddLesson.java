package guiAddLesson;

import database.Database;
import database.Database.LessonResult;
import entityClasses.LessonsLearned;

/*******
 * <p> Title: ModelAddLesson Class. </p>
 * 
 * <p> Description: The Add Lesson Page Model.  It turns the values the contributor entered into
 * a LessonsLearned object and asks the Database to store it.  Keeping this here, rather than in
 * the Controller, means the Controller only moves values between the View and the Model, as in
 * the other MVC packages. </p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * 
 * @version 1.00		2026-10-22 Initial version for TP2
 * 
 */
public class ModelAddLesson {

	/**
	 * Default constructor is not used.
	 */
	public ModelAddLesson() {
	}

	
	/**********
	 * <p> Method: LessonResult saveNewLesson(Database database, String owner, String title,
	 * 		String category, String situation, String lessonText) </p>
	 * 
	 * <p> Description: Builds a new lesson owned by the contributor and stores it (Contributor
	 * story 1).  The Database validates every field with the LessonRecognizer before storing,
	 * so an invalid lesson is never saved; when that happens the specific message and the name
	 * of the field are in LessonRecognizer.lessonRecognizerErrorMessage and
	 * lessonRecognizerErrorField. </p>
	 * 
	 * @param database is the application's Database
	 * @param owner is the logged-in contributor's username
	 * @param title is the entered title
	 * @param category is the selected category (null if none was selected)
	 * @param situation is the entered situation
	 * @param lessonText is the entered lesson text
	 * 
	 * @return SUCCESS, INVALID_INPUT, or DATABASE_ERROR
	 * 
	 */
	protected static LessonResult saveNewLesson(Database database, String owner, String title,
			String category, String situation, String lessonText) {
		LessonsLearned lesson = new LessonsLearned(owner, title, category, situation, lessonText);
		return database.addLesson(lesson);
	}
}