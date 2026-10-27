package guiLessonDetail;

import database.Database;
import database.Database.LessonResult;
import entityClasses.LessonsLearned;

/*******
 * <p> Title: ModelLessonDetail Class. </p>
 * 
 * <p> Description: The Lesson Detail Page Model.  It holds the lesson being examined, read
 * fresh from the database, and performs the update and delete requests for it.  Holding the
 * lesson here means its ID, owner, and lock state are always the stored values, never values a
 * widget could have changed. </p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * 
 * @version 1.00		2026-10-22 Initial version for TP2
 * 
 */
public class ModelLessonDetail {
	
	// The lesson on the page, as last read from the database (null if it no longer exists)
	private static LessonsLearned currentLesson = null;

	/**
	 * Default constructor is not used.
	 */
	public ModelLessonDetail() {
	}

	
	/**********
	 * <p> Method: LessonsLearned loadLesson(Database database, int lessonId) </p>
	 * 
	 * <p> Description: Reads the lesson from the database by its ID and makes it the current
	 * lesson.  Reading it again, rather than using the object the My Lessons list held, means
	 * the page shows the latest stored values and lock state. </p>
	 * 
	 * @param database is the application's Database
	 * @param lessonId is the ID of the lesson to examine
	 * 
	 * @return the lesson, or null if it no longer exists
	 * 
	 */
	protected static LessonsLearned loadLesson(Database database, int lessonId) {
		currentLesson = database.getLesson(lessonId);
		return currentLesson;
	}

	
	/**********
	 * <p> Method: LessonsLearned getCurrentLesson() </p>
	 * 
	 * <p> Description: Returns the lesson on the page. </p>
	 * 
	 * @return the current lesson, or null if there is none
	 * 
	 */
	protected static LessonsLearned getCurrentLesson() { return currentLesson; }

	
	/**********
	 * <p> Method: LessonResult saveChanges(Database database, String actingUser, String title,
	 * 		String category, String situation, String lessonText) </p>
	 * 
	 * <p> Description: Saves the edited values to the current lesson (Contributor story 1, "I can
	 * see and manage it").  The ID and owner come from the stored lesson, so an edit can never
	 * move a lesson to another owner.  The Database validates the values and checks ownership
	 * before changing anything. </p>
	 * 
	 * @param database is the application's Database
	 * @param actingUser is the logged-in contributor's username
	 * @param title is the edited title
	 * @param category is the selected category
	 * @param situation is the edited situation
	 * @param lessonText is the edited lesson text
	 * 
	 * @return SUCCESS or the reason the update was refused
	 * 
	 */
	protected static LessonResult saveChanges(Database database, String actingUser, String title,
			String category, String situation, String lessonText) {
		if (currentLesson == null) return LessonResult.NOT_FOUND;
		LessonsLearned edited = new LessonsLearned(currentLesson.getLessonId(),
				currentLesson.getOwner(), title, category, situation, lessonText);
		LessonResult result = database.updateLesson(edited, actingUser);
		// Re-read after a successful save so the page shows exactly what was stored
		if (result == LessonResult.SUCCESS) loadLesson(database, currentLesson.getLessonId());
		return result;
	}

	
	/**********
	 * <p> Method: LessonResult deleteLesson(Database database, String actingUser,
	 * 		boolean confirmed) </p>
	 * 
	 * <p> Description: Deletes the current lesson if the contributor answered "Yes" to "Are you
	 * sure?".  A "No" answer returns CANCELLED and changes nothing. </p>
	 * 
	 * @param database is the application's Database
	 * @param actingUser is the logged-in contributor's username
	 * @param confirmed is true only when the contributor answered "Yes"
	 * 
	 * @return SUCCESS, CANCELLED, or the reason the delete was refused
	 * 
	 */
	protected static LessonResult deleteLesson(Database database, String actingUser,
			boolean confirmed) {
		if (currentLesson == null) return LessonResult.NOT_FOUND;
		LessonResult result =
				database.deleteLesson(currentLesson.getLessonId(), actingUser, confirmed);
		if (result == LessonResult.SUCCESS) currentLesson = null;
		return result;
	}
}