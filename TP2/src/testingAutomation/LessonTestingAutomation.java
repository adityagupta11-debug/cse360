package testingAutomation;

import java.io.File;

import database.Database;
import database.Database.LessonResult;
import entityClasses.Lesson;
import entityClasses.LessonList;
import lessonRecognizer.LessonRecognizer;

/*******
 * <p> Title: LessonTestingAutomation Class. </p>
 *
 * <p> Description: A semi-automated test driver, in the style of the
 * InputValidationTestingAutomation class, that performs every test case in the HW2 Test
 * Cases.pdf (TC-01 through TC-23) for the lessons learned Create, Read, Update, and Delete
 * operations and their input validation.  Each test prints the test case ID from the PDF, so the
 * console output can be matched to the document line by line.
 *
 * Unlike the input validation tests, these tests need a database.  They use the
 * Database(String dbUrl) constructor with private in-memory H2 databases, so they never alter the
 * production database.  The persistence test (TC-23) uses a temporary database file instead,
 * because persistence can only be shown by closing a connection and opening a new one. </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author Joshua Luther
 *
 * @version 1.00	2026-10-08	Initial version for HW2
 *
 */
public class LessonTestingAutomation {

	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests
	static int testNumber = 0;	// The number of the next check to be reported

	// The two contributors used throughout, so the ownership rules can be tested.  Lessons store
	// the owner's username, so no user accounts need to be created for these tests.
	static final String JSMITH = "jsmith";
	static final String AKIM = "akim";

	// The "valid sample lesson" from Test Cases.pdf, based on a real CSE 360 setup question
	static final String SAMPLE_TITLE = "JavaFX app fails to launch in Eclipse";
	static final String SAMPLE_CATEGORY = "Tools and Setup";
	static final String SAMPLE_SITUATION = "After importing the project, running it gave a module "
			+ "error saying the JavaFX runtime components were missing, even though the JavaFX "
			+ "JARs were on the build path.";
	static final String SAMPLE_LESSON = "Add the JavaFX module path and --add-modules to the Run "
			+ "Configuration VM arguments. Putting the JARs on the build path alone is not enough "
			+ "for JavaFX 11 and later.";
	
	// The exact error messages promised in User Stories.pdf and Test Cases.pdf
	static final String MSG_NOT_FOUND = "That lesson could not be found. It may have been "
			+ "deleted. Please refresh your list.";
	static final String MSG_NOT_OWNER = "You can only edit or delete lessons that you added.";

	/*
	 * This mainline displays a header to the console, performs a sequence of test cases, and
	 * then displays a footer with a summary of the results
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________________________________________");
		System.out.println("\nLessons Learned CRUD and Input Validation Testing Automation");

		/************** Create (US-C1) *********************************************************/

		// TC-01: A valid lesson is stored with a new ID, the right owner, and the entered values
		Database db = freshDatabase("create");
		Lesson sample = sampleLesson(JSMITH);
		performTestCase("TC-01", "A valid lesson is added successfully",
				db.addLesson(sample) == LessonResult.SUCCESS, true);
		Lesson stored = db.getLesson(sample.getLessonId());
		performTestCase("TC-01", "The new lesson has a database ID and is owned by jsmith",
				sample.getLessonId() > 0 && stored != null && JSMITH.equals(stored.getOwner()),
				true);
		performTestCase("TC-01", "All four stored fields match what was entered",
				stored != null && SAMPLE_TITLE.equals(stored.getTitle())
				&& SAMPLE_CATEGORY.equals(stored.getCategory())
				&& SAMPLE_SITUATION.equals(stored.getSituation())
				&& SAMPLE_LESSON.equals(stored.getLessonText()), true);

		/************** Title validation (US-V1) ***********************************************/

		// TC-02: An empty title and a title of only spaces are refused, and nothing is stored
		db = freshDatabase("title");
		checkRejectedAdd("TC-02", db, "An empty title is refused",
				new Lesson(JSMITH, "", SAMPLE_CATEGORY, SAMPLE_SITUATION, SAMPLE_LESSON),
				"Title is required.");
		checkRejectedAdd("TC-02", db, "A title of only spaces is refused",
				new Lesson(JSMITH, "     ", SAMPLE_CATEGORY, SAMPLE_SITUATION, SAMPLE_LESSON),
				"Title is required.");

		// TC-03: Titles one character outside each limit are refused
		checkRejectedAdd("TC-03", db, "A 4-character title is refused",
				new Lesson(JSMITH, "abcd", SAMPLE_CATEGORY, SAMPLE_SITUATION, SAMPLE_LESSON),
				"Title must be at least 5 characters.");
		checkRejectedAdd("TC-03", db, "A 101-character title is refused",
				new Lesson(JSMITH, "t".repeat(101), SAMPLE_CATEGORY, SAMPLE_SITUATION,
						SAMPLE_LESSON),
				"Title must be 100 characters or fewer.");

		// TC-04: Titles exactly at each limit are accepted
		performTestCase("TC-04", "A 5-character title is accepted",
				db.addLesson(new Lesson(JSMITH, "abcde", SAMPLE_CATEGORY, SAMPLE_SITUATION,
						SAMPLE_LESSON)) == LessonResult.SUCCESS, true);
		performTestCase("TC-04", "A 100-character title is accepted",
				db.addLesson(new Lesson(JSMITH, "t".repeat(100), SAMPLE_CATEGORY,
						SAMPLE_SITUATION, SAMPLE_LESSON)) == LessonResult.SUCCESS, true);

		/************** Category validation (US-V2) ********************************************/

		// TC-05: No selection and a value outside the list are both refused
		db = freshDatabase("category");
		checkRejectedAdd("TC-05", db, "A lesson with no category selected is refused",
				new Lesson(JSMITH, SAMPLE_TITLE, null, SAMPLE_SITUATION, SAMPLE_LESSON),
				"Please select a category for this lesson.");
		checkRejectedAdd("TC-05", db, "The category \"Misc\" (not in the list) is refused",
				new Lesson(JSMITH, SAMPLE_TITLE, "Misc", SAMPLE_SITUATION, SAMPLE_LESSON),
				"Please select a category for this lesson.");

		// TC-06: Every allowed category is accepted and stored correctly
		boolean allAccepted = true;
		for (String category : Lesson.CATEGORIES) {
			Lesson lesson = new Lesson(JSMITH, SAMPLE_TITLE, category, SAMPLE_SITUATION,
					SAMPLE_LESSON);
			allAccepted &= db.addLesson(lesson) == LessonResult.SUCCESS
					&& category.equals(db.getLesson(lesson.getLessonId()).getCategory());
		}
		performTestCase("TC-06", "All " + Lesson.CATEGORIES.length +
				" allowed categories are accepted and stored", allAccepted, true);

		/************** Situation and lesson text validation (US-V3, US-V4) ********************/

		// TC-07: Situations that are empty, too short, or too long are refused
		db = freshDatabase("text");
		checkRejectedAdd("TC-07", db, "An empty situation is refused",
				new Lesson(JSMITH, SAMPLE_TITLE, SAMPLE_CATEGORY, "", SAMPLE_LESSON),
				"Describe the situation in at least 20 characters.");
		checkRejectedAdd("TC-07", db, "A 19-character situation is refused",
				new Lesson(JSMITH, SAMPLE_TITLE, SAMPLE_CATEGORY, "s".repeat(19), SAMPLE_LESSON),
				"Describe the situation in at least 20 characters.");
		checkRejectedAdd("TC-07", db, "A 2,001-character situation is refused",
				new Lesson(JSMITH, SAMPLE_TITLE, SAMPLE_CATEGORY, "s".repeat(2001),
						SAMPLE_LESSON),
				"Situation must be 2,000 characters or fewer.");

		// TC-08: Lesson texts that are empty, too short, or too long are refused
		checkRejectedAdd("TC-08", db, "An empty lesson text is refused",
				new Lesson(JSMITH, SAMPLE_TITLE, SAMPLE_CATEGORY, SAMPLE_SITUATION, ""),
				"Describe the lesson in at least 20 characters.");
		checkRejectedAdd("TC-08", db, "A 19-character lesson text is refused",
				new Lesson(JSMITH, SAMPLE_TITLE, SAMPLE_CATEGORY, SAMPLE_SITUATION,
						"l".repeat(19)),
				"Describe the lesson in at least 20 characters.");
		checkRejectedAdd("TC-08", db, "A 2,001-character lesson text is refused",
				new Lesson(JSMITH, SAMPLE_TITLE, SAMPLE_CATEGORY, SAMPLE_SITUATION,
						"l".repeat(2001)),
				"Lesson must be 2,000 characters or fewer.");

		// TC-09: Both text fields exactly at each limit are accepted, and long text is not cut off
		performTestCase("TC-09", "Situation and lesson text of 20 characters are accepted",
				db.addLesson(new Lesson(JSMITH, SAMPLE_TITLE, SAMPLE_CATEGORY, "s".repeat(20),
						"l".repeat(20))) == LessonResult.SUCCESS, true);
		Lesson longest = new Lesson(JSMITH, SAMPLE_TITLE, SAMPLE_CATEGORY, "s".repeat(2000),
				"l".repeat(2000));
		performTestCase("TC-09", "Situation and lesson text of 2,000 characters are accepted",
				db.addLesson(longest) == LessonResult.SUCCESS, true);
		Lesson readBack = db.getLesson(longest.getLessonId());
		performTestCase("TC-09", "The 2,000-character text is read back without being cut off",
				readBack != null && readBack.getSituation().length() == 2000
				&& readBack.getLessonText().length() == 2000, true);

		/************** Read (US-R1, US-R2) ****************************************************/

		// TC-10: Each contributor's list holds only that contributor's lessons
		db = freshDatabase("read");
		for (int i = 0; i < 3; i++) db.addLesson(sampleLesson(JSMITH));
		for (int i = 0; i < 2; i++) db.addLesson(sampleLesson(AKIM));
		LessonList jsmithList = db.getLessonsByOwner(JSMITH);
		LessonList akimList = db.getLessonsByOwner(AKIM);
		performTestCase("TC-10", "jsmith's list holds exactly 3 lessons, all owned by jsmith",
				jsmithList.size() == 3 && jsmithList.filterByOwner(JSMITH).size() == 3, true);
		performTestCase("TC-10", "akim's list holds exactly 2 lessons, all owned by akim",
				akimList.size() == 2 && akimList.filterByOwner(AKIM).size() == 2, true);
		performTestCase("TC-10", "The full list holds all 5 lessons, and its jsmith subset holds 3",
				db.getAllLessons().size() == 5
				&& db.getAllLessons().filterByOwner(JSMITH).size() == 3, true);

		// TC-11: A contributor with no lessons gets an empty list, not null or an error
		LessonList emptyList = db.getLessonsByOwner("newcontributor");
		performTestCase("TC-11", "A contributor with no lessons gets an empty list (not null)",
				emptyList != null && emptyList.isEmpty() && emptyList.size() == 0, true);
		performMessageTestCase("TC-11", "The empty-list message is the one in Test Cases.pdf",
				LessonList.EMPTY_LIST_MESSAGE, "You have not added any lessons yet.");

		// TC-12: There is no fixed upper limit on the number of lessons in a list
		Database bigDb = freshDatabase("large");
		for (int i = 0; i < 1000; i++) bigDb.addLesson(sampleLesson(JSMITH));
		performTestCase("TC-12", "All 1,000 lessons are returned in jsmith's list",
				bigDb.getLessonsByOwner(JSMITH).size() == 1000, true);
		bigDb.closeConnection();

		// TC-13: A single lesson can be retrieved and examined in detail
		Lesson toView = sampleLesson(JSMITH);
		db.addLesson(toView);
		Lesson viewed = db.getLesson(toView.getLessonId());
		performTestCase("TC-13", "Viewing a lesson returns every field and the owner",
				viewed != null && SAMPLE_TITLE.equals(viewed.getTitle())
				&& SAMPLE_CATEGORY.equals(viewed.getCategory())
				&& SAMPLE_SITUATION.equals(viewed.getSituation())
				&& SAMPLE_LESSON.equals(viewed.getLessonText())
				&& JSMITH.equals(viewed.getOwner()), true);

		// TC-14: Asking for a lesson that does not exist is reported, not a crash
		performTestCase("TC-14", "Viewing a nonexistent lesson returns no lesson",
				db.getLesson(999999) == null, true);
		performMessageTestCase("TC-14", "The not-found message is the one in Test Cases.pdf",
				LessonResult.NOT_FOUND.getMessage(), MSG_NOT_FOUND);
		db.closeConnection();

		/************** Update (US-U1) *********************************************************/

		// TC-15: A valid edit is saved, keeping the same ID and owner
		db = freshDatabase("update");
		Lesson toEdit = sampleLesson(JSMITH);
		db.addLesson(toEdit);
		int editId = toEdit.getLessonId();
		Lesson edited = new Lesson(editId, JSMITH, "JavaFX VM arguments in Eclipse",
				SAMPLE_CATEGORY, SAMPLE_SITUATION, SAMPLE_LESSON + " Uncheck -XstartOnFirstThread "
				+ "on a Mac.");
		performTestCase("TC-15", "A valid update by the owner succeeds",
				db.updateLesson(edited, JSMITH) == LessonResult.SUCCESS, true);
		Lesson afterEdit = db.getLesson(editId);
		performTestCase("TC-15", "The stored lesson has the new values, same ID and owner",
				afterEdit != null && "JavaFX VM arguments in Eclipse".equals(afterEdit.getTitle())
				&& afterEdit.getLessonText().endsWith("on a Mac.")
				&& afterEdit.getLessonId() == editId && JSMITH.equals(afterEdit.getOwner()), true);

		// TC-16: An invalid edit is refused and the stored lesson is left unchanged
		Lesson badEdit = new Lesson(editId, JSMITH, "Bug", SAMPLE_CATEGORY, SAMPLE_SITUATION,
				SAMPLE_LESSON);
		performTestCase("TC-16", "An update with the title \"Bug\" is refused",
				db.updateLesson(badEdit, JSMITH) == LessonResult.INVALID_INPUT, true);
		performMessageTestCase("TC-16", "The error message names the title rule",
				LessonRecognizer.lessonRecognizerErrorMessage,
				"Title must be at least 5 characters.");
		performTestCase("TC-16", "The stored title is unchanged",
				"JavaFX VM arguments in Eclipse".equals(db.getLesson(editId).getTitle()), true);

		// TC-17: A contributor cannot edit another contributor's lesson
		Lesson akimLesson = sampleLesson(AKIM);
		db.addLesson(akimLesson);
		Lesson hijack = new Lesson(akimLesson.getLessonId(), AKIM, "Changed by jsmith",
				SAMPLE_CATEGORY, SAMPLE_SITUATION, SAMPLE_LESSON);
		LessonResult result = db.updateLesson(hijack, JSMITH);
		performTestCase("TC-17", "jsmith's update of akim's lesson is refused",
				result == LessonResult.NOT_OWNER, true);
		performMessageTestCase("TC-17", "The ownership message is the one in Test Cases.pdf",
				result.getMessage(), MSG_NOT_OWNER);
		performTestCase("TC-17", "akim's lesson is unchanged",
				SAMPLE_TITLE.equals(db.getLesson(akimLesson.getLessonId()).getTitle()), true);

		// TC-18: Updating a lesson that does not exist is reported, and no lesson is created
		int countBefore = db.getNumberOfLessons();
		Lesson ghost = new Lesson(999999, JSMITH, SAMPLE_TITLE, SAMPLE_CATEGORY,
				SAMPLE_SITUATION, SAMPLE_LESSON);
		result = db.updateLesson(ghost, JSMITH);
		performTestCase("TC-18", "Updating a nonexistent lesson is refused",
				result == LessonResult.NOT_FOUND, true);
		performMessageTestCase("TC-18", "The not-found message is the one in Test Cases.pdf",
				result.getMessage(), MSG_NOT_FOUND);
		performTestCase("TC-18", "No new lesson was created",
				db.getNumberOfLessons() == countBefore, true);
		db.closeConnection();

		/************** Delete (US-D1) *********************************************************/

		// TC-19: Deleting after answering "Yes" removes only that lesson
		db = freshDatabase("delete");
		Lesson toDelete = sampleLesson(JSMITH);
		Lesson toKeep = sampleLesson(JSMITH);
		db.addLesson(toDelete);
		db.addLesson(toKeep);
		performTestCase("TC-19", "Deleting jsmith's lesson after \"Yes\" succeeds",
				db.deleteLesson(toDelete.getLessonId(), JSMITH, true) == LessonResult.SUCCESS,
				true);
		performTestCase("TC-19", "The deleted lesson is gone from the database and the list",
				db.getLesson(toDelete.getLessonId()) == null
				&& db.getLessonsByOwner(JSMITH).findById(toDelete.getLessonId()) == null, true);
		performTestCase("TC-19", "The other lesson is unaffected",
				db.getLesson(toKeep.getLessonId()) != null, true);

		// TC-20: Answering "No" cancels the delete and leaves the lesson in place
		performTestCase("TC-20", "Answering \"No\" cancels the delete",
				db.deleteLesson(toKeep.getLessonId(), JSMITH, false) == LessonResult.CANCELLED,
				true);
		performTestCase("TC-20", "The lesson is still in the database",
				db.getLesson(toKeep.getLessonId()) != null, true);

		// TC-21: A contributor cannot delete another contributor's lesson
		Lesson akimKeeps = sampleLesson(AKIM);
		db.addLesson(akimKeeps);
		result = db.deleteLesson(akimKeeps.getLessonId(), JSMITH, true);
		performTestCase("TC-21", "jsmith's delete of akim's lesson is refused",
				result == LessonResult.NOT_OWNER, true);
		performMessageTestCase("TC-21", "The ownership message is the one in Test Cases.pdf",
				result.getMessage(), MSG_NOT_OWNER);
		performTestCase("TC-21", "akim's lesson still exists",
				db.getLesson(akimKeeps.getLessonId()) != null, true);

		// TC-22: Deleting a lesson that does not exist is reported, and nothing else changes
		countBefore = db.getNumberOfLessons();
		result = db.deleteLesson(toDelete.getLessonId(), JSMITH, true);	// already deleted
		performTestCase("TC-22", "Deleting an already-deleted lesson is refused",
				result == LessonResult.NOT_FOUND, true);
		performMessageTestCase("TC-22", "The not-found message is the one in Test Cases.pdf",
				result.getMessage(), MSG_NOT_FOUND);
		performTestCase("TC-22", "No other lesson was affected",
				db.getNumberOfLessons() == countBefore, true);
		db.closeConnection();

		/************** Persistence (US-P1) ****************************************************/

		// TC-23: Creates, updates, and deletes survive closing and reopening the database.  A
		// temporary database file is used (not memory), and two separate Database objects stand
		// in for two separate executions of HW2.
		File dbFile = new File(System.getProperty("java.io.tmpdir"), "hw2LessonPersistenceTest");
		new File(dbFile.getPath() + ".mv.db").delete();		// Start from a clean file
		String fileUrl = "jdbc:h2:" + dbFile.getAbsolutePath();
		
		Database firstRun = new Database(fileUrl);
		firstRun.connectToDatabase();
		Lesson lessonA = new Lesson(JSMITH, "Lesson A kept as is", SAMPLE_CATEGORY,
				SAMPLE_SITUATION, SAMPLE_LESSON);
		Lesson lessonB = new Lesson(JSMITH, "Lesson B before update", SAMPLE_CATEGORY,
				SAMPLE_SITUATION, SAMPLE_LESSON);
		Lesson lessonC = new Lesson(JSMITH, "Lesson C to be deleted", SAMPLE_CATEGORY,
				SAMPLE_SITUATION, SAMPLE_LESSON);
		firstRun.addLesson(lessonA);
		firstRun.addLesson(lessonB);
		firstRun.addLesson(lessonC);
		lessonB.setTitle("Lesson B after update");
		firstRun.updateLesson(lessonB, JSMITH);
		firstRun.deleteLesson(lessonC.getLessonId(), JSMITH, true);
		firstRun.closeConnection();							// "Close HW2"
		
		Database secondRun = new Database(fileUrl);			// "Run HW2 again"
		secondRun.connectToDatabase();
		Lesson aAgain = secondRun.getLesson(lessonA.getLessonId());
		Lesson bAgain = secondRun.getLesson(lessonB.getLessonId());
		performTestCase("TC-23", "Lesson A is present and unchanged after reopening",
				aAgain != null && "Lesson A kept as is".equals(aAgain.getTitle()), true);
		performTestCase("TC-23", "Lesson B shows the updated title after reopening",
				bAgain != null && "Lesson B after update".equals(bAgain.getTitle()), true);
		performTestCase("TC-23", "Lesson C is still deleted after reopening",
				secondRun.getLesson(lessonC.getLessonId()) == null, true);
		secondRun.closeConnection();
		new File(dbFile.getPath() + ".mv.db").delete();		// Remove the temporary file

		/************** End of the test cases **************/

		System.out.println("______________________________________________________________________");
		System.out.println("\nNumber of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	/*
	 * Each group of tests gets its own private in-memory database, so the counts checked in one
	 * test case (e.g., "exactly 3 lessons") are never affected by lessons added in another.
	 */
	private static Database freshDatabase(String name) throws Exception {
		Database db = new Database("jdbc:h2:mem:lessonTest_" + name + ";DB_CLOSE_DELAY=-1");
		db.connectToDatabase();
		return db;
	}

	// Builds a new copy of the valid sample lesson from Test Cases.pdf for the given owner
	private static Lesson sampleLesson(String owner) {
		return new Lesson(owner, SAMPLE_TITLE, SAMPLE_CATEGORY, SAMPLE_SITUATION, SAMPLE_LESSON);
	}

	/*
	 * A negative create test has three parts, and all three are checked: the add is refused, the
	 * error message is exactly the one in Test Cases.pdf, and the number of stored lessons did
	 * not change.  Checking the count proves the invalid lesson never reached the database.
	 */
	private static void checkRejectedAdd(String testCaseId, Database db, String description,
			Lesson lesson, String expectedMessage) {
		int countBefore = db.getNumberOfLessons();
		performTestCase(testCaseId, description,
				db.addLesson(lesson) == LessonResult.INVALID_INPUT, true);
		performMessageTestCase(testCaseId, "The error message is the one in Test Cases.pdf",
				LessonRecognizer.lessonRecognizerErrorMessage, expectedMessage);
		performTestCase(testCaseId, "No lesson was stored",
				db.getNumberOfLessons() == countBefore, true);
	}

	/*
	 * This method performs a single test case: it compares the observed result to the expected
	 * result and reports the outcome in the same manner as the InputValidationTestingAutomation
	 * class.  The test case ID from Test Cases.pdf is shown alongside the sequential check number.
	 */
	private static void performTestCase(String testCaseId, String description, boolean observed,
			boolean expected) {
		testNumber++;
		System.out.println("______________________________________________________________________");
		System.out.println("\nTest case: " + testNumber + " (" + testCaseId + ")");
		System.out.println("Testing: " + description);
		System.out.println("Expected: " + expected + "   Observed: " + observed);
		if (observed == expected) {
			System.out.println("***Success*** The test case produced the expected result.\n");
			numPassed++;
		} else {
			System.out.println("***Failure*** The test case did NOT produce the expected result.\n");
			numFailed++;
		}
	}

	/*
	 * A variation of performTestCase for error messages.  It prints both messages in full, so the
	 * console output itself shows that the user would see the helpful, context-relevant message
	 * the Test Cases.pdf promises, not just that two strings matched.
	 */
	private static void performMessageTestCase(String testCaseId, String description,
			String observed, String expected) {
		testNumber++;
		System.out.println("______________________________________________________________________");
		System.out.println("\nTest case: " + testNumber + " (" + testCaseId + ")");
		System.out.println("Testing: " + description);
		System.out.println("Expected message: \"" + expected + "\"");
		System.out.println("Observed message: \"" + observed + "\"");
		if (expected.equals(observed)) {
			System.out.println("***Success*** The test case produced the expected result.\n");
			numPassed++;
		} else {
			System.out.println("***Failure*** The test case did NOT produce the expected result.\n");
			numFailed++;
		}
	}
}