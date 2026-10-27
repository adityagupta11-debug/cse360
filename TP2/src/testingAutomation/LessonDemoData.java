package testingAutomation;

import database.Database;
import entityClasses.LessonsLearned;
import entityClasses.LessonsLearnedList;

/*******
 * <p> Title: LessonDemoData Class. </p>
 *
 * <p> Description: Adds a small set of sample lessons to the application's real database so the
 * manual tests (MyLessonsManualTests) and the screencasts can show every state of the
 * contributor pages, including a lesson with locked fields, a fully locked lesson, and a
 * curator-edited lesson.  Locks and curator edits cannot be made from the GUI until the Curator
 * pages are built in TP3, so this is the only way to demonstrate them now.</p>
 *
 * <p> Usage: run this class as a Java Application while HW2/TP2 is closed (H2 allows only one
 * program to open the database file at a time).  By default it adds lessons for the
 * contributors "jsmith" and "akim"; to use other accounts, put two usernames in the run
 * configuration's Program arguments.  Lessons a contributor already has (same title) are not
 * added twice, so it is safe to run more than once.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author Joshua Luther
 *
 * @version 1.00	2026-10-22	Initial version for TP2
 *
 */
public class LessonDemoData {

	/**
	 * Default constructor is not used.
	 */
	public LessonDemoData() {
	}

	/**
	 * Adds the sample lessons and prints what was added.
	 * 
	 * @param args optional: the first and second contributor usernames
	 * @throws Exception if the database cannot be opened (for example, the application is open)
	 */
	public static void main(String[] args) throws Exception {
		String first = (args.length > 0) ? args[0] : "jsmith";
		String second = (args.length > 1) ? args[1] : "akim";
		
		Database db = new Database();			// The real database, not a test database
		db.connectToDatabase();
		
		// An ordinary lesson: no locks, not edited
		add(db, first, "JavaFX app fails to launch in Eclipse", "Tools and Setup",
				"After importing the project, running it gave a module error saying the JavaFX "
				+ "runtime components were missing, even though the JARs were on the build path.",
				"Add the JavaFX module path and --add-modules to the Run Configuration VM "
				+ "arguments. Putting the JARs on the build path alone is not enough.",
				false, false, false);
		
		// Two fields locked and edited by a curator: shows "Locked" and "Edited by curator"
		add(db, first, "Merge conflicts right before the deadline", "Teamwork",
				"Two teammates edited the same Database class on separate branches and merged an "
				+ "hour before the TP1 deadline, which took most of that hour to fix.",
				"Pull and merge shared files at least once a day, and agree on who owns which "
				+ "methods in shared classes before starting work.",
				true, false, true);
		
		// Wholly locked: every field read-only and Delete disabled
		add(db, first, "Record the screencast after the tests pass", "Testing",
				"We found a failing test while recording a screencast and had to record it again "
				+ "after fixing the code.",
				"Run the full test driver and fix every failure before recording any video.",
				false, true, false);
		
		// Another contributor's lesson: must never appear in the first contributor's list
		add(db, second, "JavaFX scene does not update after a change", "Coding",
				"A label kept showing old text because the code changed the value on a "
				+ "background thread instead of the JavaFX thread.",
				"Update JavaFX widgets only on the JavaFX Application Thread, using "
				+ "Platform.runLater when the work happens elsewhere.",
				false, false, false);
		
		System.out.println("\n" + first + " now has "
				+ LessonsLearnedList.loadForContributor(db, first).size() + " lessons; " + second
				+ " has " + LessonsLearnedList.loadForContributor(db, second).size() + ".");
		db.closeConnection();
	}
	
	/*
	 * Adds one lesson unless the owner already has one with that title, then applies the
	 * requested locks and curator-edited flag
	 */
	private static void add(Database db, String owner, String title, String category,
			String situation, String lessonText, boolean lockTwoFields, boolean lockLesson,
			boolean curatorEdited) {
		for (LessonsLearned existing : LessonsLearnedList.loadForContributor(db, owner).getLessons())
			if (existing.getTitle().equals(title)) {
				System.out.println("Already present for " + owner + ": " + title);
				return;
			}
		LessonsLearned lesson = new LessonsLearned(owner, title, category, situation, lessonText);
		db.addLesson(lesson);
		if (lockTwoFields) {
			db.setFieldLock(lesson.getLessonId(), "Title", true);
			db.setFieldLock(lesson.getLessonId(), "Lesson", true);
		}
		if (lockLesson) db.setLessonLock(lesson.getLessonId(), true);
		if (curatorEdited) db.setCuratorEdited(lesson.getLessonId(), true);
		System.out.println("Added for " + owner + ": " + title
				+ (lockTwoFields ? "  [Title and Lesson locked]" : "")
				+ (lockLesson ? "  [whole lesson locked]" : "")
				+ (curatorEdited ? "  [edited by curator]" : ""));
	}
}