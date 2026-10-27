package testingAutomation;

import java.io.File;

import database.Database;
import database.Database.LessonResult;
import entityClasses.LessonsLearned;
import entityClasses.LessonsLearnedList;
import lessonRecognizer.LessonRecognizer;

/*******
 * <p> Title: LessonListTestingAutomation Class. </p>
 *
 * <p> Description: A semi-automated test driver, in the style of the team's
 * InputValidationTestingAutomation and LessonTestingAutomation classes, for the
 * LessonsLearnedList class: the persistent list of all lessons, the my-lessons subset each
 * contributor sees, Create, Update, and Delete through the list, keyword search, and the lock
 * and curator-edited state the My Lessons page shows as tags.</p>
 *
 * <p> Each test is its own method, so each has its own Javadoc stating the requirement it
 * tests, how the code checks it, and how to read its output.  Every requirement has at least
 * two tests, at least one positive (valid use succeeds) and one negative (invalid use is
 * refused or handled safely).</p>
 *
 * <p> <b>Requirements tested</b> (from the Contributor user stories and the TP2 assignment):</p>
 * <ul>
 * <li>LIST-1: There is a persistent list of all lessons learned in the system.</li>
 * <li>LIST-2: A contributor's list holds only the lessons that contributor added, including
 *     any a curator has edited (Contributor stories 2 and 4).</li>
 * <li>LIST-3: A list may be empty or arbitrarily large, and its contents are protected.</li>
 * <li>LIST-4: A lesson added through the list is stored in the database and the list; an
 *     invalid one is stored in neither.</li>
 * <li>LIST-5: A lesson updated through the list is changed in both; a refused update changes
 *     neither.</li>
 * <li>LIST-6: A lesson deleted through the list after confirmation is removed from both; a
 *     cancelled or refused delete changes neither.</li>
 * <li>LIST-7: A keyword search returns the subset of lessons that contain every keyword.</li>
 * <li>LIST-8: The list reports each lesson's lock and curator-edited state (for the tags).</li>
 * <li>LIST-9: Changes made through the list persist after the application restarts.</li>
 * </ul>
 *
 * <p> <b>How to read the output:</b> each test prints a header with its ID, requirement, and
 * whether it is positive or negative, followed by one or more checks.  Each check prints the
 * expected and observed values (or messages) and "***Success***" when they match.  A test
 * passes only if every one of its checks succeeds.  The summary at the end gives the number of
 * tests and checks that passed and failed; a correct implementation shows 0 failures.</p>
 *
 * <p> The tests use private in-memory databases created with the Database(String dbUrl)
 * constructor, so they never alter the production database.  LT-25 and LT-26 use a temporary
 * database file instead, because persistence can only be shown by closing and reopening.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author Joshua Luther
 *
 * @version 1.00	2026-10-22	Initial version for TP2
 *
 */
public class LessonListTestingAutomation {

	static int testsPassed = 0;		// Tests whose checks all succeeded
	static int testsFailed = 0;		// Tests with at least one failed check
	static int checksPassed = 0;	// Individual checks that succeeded
	static int checksFailed = 0;	// Individual checks that failed
	static boolean currentTestOk;	// Whether every check in the current test has succeeded
	static int databaseNumber = 0;	// Gives each test its own private database

	static final String JSMITH = "jsmith";
	static final String AKIM = "akim";
	static final String SITUATION = "After importing the project, running it gave a module "
			+ "error saying the JavaFX runtime components were missing.";
	static final String LESSON = "Add the JavaFX module path and --add-modules to the Run "
			+ "Configuration VM arguments in Eclipse.";

	/**
	 * Default constructor is not used.
	 */
	public LessonListTestingAutomation() {
	}

	/*
	 * This mainline displays a header to the console, performs every test in order, and then
	 * displays a footer with a summary of the results
	 */
	/**
	 * Runs every test and prints the summary.
	 * 
	 * @param args not used
	 * @throws Exception if a test database cannot be created
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("______________________________________________________________________");
		System.out.println("\nLessonsLearnedList Testing Automation");

		testLoadAllReturnsEveryLesson();				// LIST-1
		testLoadAllOnEmptyDatabase();
		testLoadAllWithNoDatabase();
		testContributorSeesOnlyOwnLessons();			// LIST-2
		testOtherContributorsExcluded();
		testCuratorEditedLessonStaysInList();
		testLargeListHasNoLimit();						// LIST-3
		testEmptyContributorList();
		testListContentsAreProtected();
		testAddValidLessonThroughList();				// LIST-4
		testAddInvalidLessonThroughList();
		testUpdateValidLessonThroughList();				// LIST-5
		testUpdateInvalidLessonThroughList();
		testUpdateOtherContributorsLesson();
		testDeleteConfirmedThroughList();				// LIST-6
		testDeleteCancelledThroughList();
		testDeleteMissingLessonThroughList();
		testSearchMatchesEveryField();					// LIST-7
		testSearchIgnoresCase();
		testSearchRequiresAllKeywords();
		testSearchWithNoMatches();
		testSearchBlankReturnsWholeList();
		testSearchLeavesSourceUnchanged();
		testLockAndEditedStateReported();				// LIST-8
		testNewLessonHasNoLocks();
		testChangesPersistAfterRestart();				// LIST-9
		testRefusedChangesDoNotPersist();

		System.out.println("______________________________________________________________________");
		System.out.println("\nTests passed: " + testsPassed + "    Tests failed: " + testsFailed);
		System.out.println("Checks passed: " + checksPassed + "   Checks failed: " + checksFailed);
	}

	
	/*-*******************************************************************************************

	LIST-1: The persistent list of all lessons learned

	 */

	/**
	 * <p> Test LT-01 (LIST-1, positive): loadAll returns every stored lesson.</p>
	 * <p> How: stores 3 lessons from 2 contributors, then calls LessonsLearnedList.loadAll and
	 * checks the list holds all 3 and includes both contributors' lessons.</p>
	 * <p> Output: two checks; each shows Expected: true and Observed: true with Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testLoadAllReturnsEveryLesson() throws Exception {
		beginTest("LT-01", "LIST-1", "positive", "loadAll returns every lesson from every contributor");
		Database db = freshDatabase();
		LessonTestFixtures.add(db, lesson(JSMITH, "First jsmith lesson"));
		LessonTestFixtures.add(db, lesson(JSMITH, "Second jsmith lesson"));
		LessonTestFixtures.add(db, lesson(AKIM, "Only akim lesson"));
		LessonsLearnedList all = LessonTestFixtures.all(db);
		check("The list holds all 3 stored lessons", all.size() == 3, true);
		check("It includes lessons from both jsmith and akim",
				all.filterByOwner(JSMITH).size() == 2 && all.filterByOwner(AKIM).size() == 1, true);
		endTest();
	}

	/**
	 * <p> Test LT-02 (LIST-1, negative): loadAll on a database with no lessons.</p>
	 * <p> How: calls loadAll on a new, empty database and checks that the result is an empty
	 * list rather than null, so callers can always use it safely.</p>
	 * <p> Output: one check; Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testLoadAllOnEmptyDatabase() throws Exception {
		beginTest("LT-02", "LIST-1", "negative", "loadAll on an empty database gives an empty list, not null");
		LessonsLearnedList all = LessonsLearnedList.loadAll(freshDatabase());
		check("The result is a real list with no lessons", all != null && all.isEmpty(), true);
		endTest();
	}

	/**
	 * <p> Test LT-03 (LIST-1, negative): loadAll without a database.</p>
	 * <p> How: passes null as the database and checks that an empty list is returned instead of
	 * the program failing with an exception.</p>
	 * <p> Output: one check; Expected: true, Observed: true, Success.</p>
	 */
	static void testLoadAllWithNoDatabase() {
		beginTest("LT-03", "LIST-1", "negative", "loadAll with no database fails safely");
		LessonsLearnedList all = LessonsLearnedList.loadAll(null);
		check("An empty list is returned, with no exception", all != null && all.isEmpty(), true);
		endTest();
	}

	
	/*-*******************************************************************************************

	LIST-2: A contributor sees only the lessons that contributor added

	 */

	/**
	 * <p> Test LT-04 (LIST-2, positive): a contributor's list holds exactly their lessons.</p>
	 * <p> How: stores 2 lessons for jsmith and 1 for akim, loads jsmith's list with
	 * loadForContributor, and checks it holds 2 lessons, all owned by jsmith.</p>
	 * <p> Output: two checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testContributorSeesOnlyOwnLessons() throws Exception {
		beginTest("LT-04", "LIST-2", "positive", "A contributor's list holds exactly their own lessons");
		Database db = freshDatabase();
		LessonTestFixtures.add(db, lesson(JSMITH, "First jsmith lesson"));
		LessonTestFixtures.add(db, lesson(JSMITH, "Second jsmith lesson"));
		LessonTestFixtures.add(db, lesson(AKIM, "Only akim lesson"));
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		check("jsmith's list holds 2 lessons", mine.size() == 2, true);
		check("Every lesson in it is owned by jsmith", mine.filterByOwner(JSMITH).size() == 2, true);
		endTest();
	}

	/**
	 * <p> Test LT-05 (LIST-2, negative): other contributors' lessons are never included.</p>
	 * <p> How: checks that akim's lesson is not in jsmith's list, and that a contributor who
	 * has added nothing sees no lessons at all, even though the database holds some
	 * (Contributor story 4: "I am unable to see submissions from other contributors").</p>
	 * <p> Output: two checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testOtherContributorsExcluded() throws Exception {
		beginTest("LT-05", "LIST-2", "negative", "Other contributors' lessons are never in a contributor's list");
		Database db = freshDatabase();
		LessonTestFixtures.add(db, lesson(JSMITH, "A jsmith lesson"));
		LessonsLearned akims = lesson(AKIM, "An akim lesson");
		LessonTestFixtures.add(db, akims);
		check("akim's lesson is not in jsmith's list",
				LessonsLearnedList.loadForContributor(db, JSMITH).findById(akims.getLessonId()) == null,
				true);
		check("A contributor with no lessons sees none of the 2 stored",
				LessonsLearnedList.loadForContributor(db, "newcontributor").isEmpty(), true);
		endTest();
	}

	/**
	 * <p> Test LT-06 (LIST-2, positive): a curator-edited lesson stays in its contributor's
	 * list.</p>
	 * <p> How: marks one of jsmith's lessons as curator-edited, reloads jsmith's list, and checks
	 * the lesson is still there and still owned by jsmith (Contributor story 4: "the content
	 * that I have added after a curator has edited it").</p>
	 * <p> Output: two checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testCuratorEditedLessonStaysInList() throws Exception {
		beginTest("LT-06", "LIST-2", "positive", "A curator-edited lesson stays in its contributor's list");
		Database db = freshDatabase();
		LessonsLearned edited = lesson(JSMITH, "Lesson a curator edited");
		LessonTestFixtures.add(db, edited);
		LessonTestFixtures.edited(db, edited.getLessonId(), true);
		LessonsLearned found =
				LessonsLearnedList.loadForContributor(db, JSMITH).findById(edited.getLessonId());
		check("The edited lesson is still in jsmith's list", found != null, true);
		check("It is still owned by jsmith", found != null && JSMITH.equals(found.getOwner()), true);
		endTest();
	}

	
	/*-*******************************************************************************************

	LIST-3: Lists may be empty or arbitrarily large, and their contents are protected

	 */

	/**
	 * <p> Test LT-07 (LIST-3, positive): there is no upper limit on a list's size.</p>
	 * <p> How: stores 1,000 lessons for one contributor and checks that loadForContributor and
	 * loadAll each return all 1,000.</p>
	 * <p> Output: two checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testLargeListHasNoLimit() throws Exception {
		beginTest("LT-07", "LIST-3", "positive", "A list of 1,000 lessons is loaded completely");
		Database db = freshDatabase();
		for (int i = 1; i <= 1000; i++) LessonTestFixtures.add(db, lesson(JSMITH, "Lesson number " + i));
		check("jsmith's list holds all 1,000",
				LessonsLearnedList.loadForContributor(db, JSMITH).size() == 1000, true);
		check("The all-lessons list holds all 1,000", LessonTestFixtures.all(db).size() == 1000, true);
		endTest();
	}

	/**
	 * <p> Test LT-08 (LIST-3, negative): a contributor with no lessons gets an empty list and
	 * the empty-list message the My Lessons page shows.</p>
	 * <p> How: loads the list for a contributor with no lessons, checks it is empty but not null,
	 * and checks the message text exactly.</p>
	 * <p> Output: one check (Expected/Observed true), then a message check printing the expected
	 * and observed message, which must be identical.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testEmptyContributorList() throws Exception {
		beginTest("LT-08", "LIST-3", "negative", "A contributor with no lessons gets an empty list");
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(freshDatabase(), JSMITH);
		check("The list is empty but not null", mine != null && mine.size() == 0, true);
		checkMessage("The empty-list message", LessonsLearnedList.EMPTY_LIST_MESSAGE,
				"You have not added any lessons yet.");
		endTest();
	}

	/**
	 * <p> Test LT-09 (LIST-3, negative): a list's contents cannot be changed from outside.</p>
	 * <p> How: tries to clear the read-only view returned by getLessons, and tries to add null;
	 * both must be refused and the list must keep its lesson.</p>
	 * <p> Output: three checks, each Expected: true, Observed: true, Success.</p>
	 */
	static void testListContentsAreProtected() {
		beginTest("LT-09", "LIST-3", "negative", "A list's contents cannot be changed from outside");
		LessonsLearnedList list = new LessonsLearnedList();
		list.add(lesson(JSMITH, "Protected lesson"));
		boolean refused = false;
		try { list.getLessons().clear(); } catch (UnsupportedOperationException e) { refused = true; }
		check("Clearing the read-only view is refused", refused, true);
		check("Adding null is refused", list.add(null), false);
		check("The list still holds its one lesson", list.size() == 1, true);
		endTest();
	}

	
	/*-*******************************************************************************************

	LIST-4: Create through the list

	 */

	/**
	 * <p> Test LT-10 (LIST-4, positive): a valid lesson added through the list is stored.</p>
	 * <p> How: calls addLesson on the list with a valid lesson and checks the result is
	 * SUCCESS, the lesson received a database ID, it is in the list, and it is in the
	 * database.</p>
	 * <p> Output: three checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testAddValidLessonThroughList() throws Exception {
		beginTest("LT-10", "LIST-4", "positive", "A valid lesson added through the list is stored in both");
		Database db = freshDatabase();
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		LessonsLearned added = lesson(JSMITH, "JavaFX app fails to launch");
		check("addLesson returns SUCCESS", mine.addLesson(db, added) == LessonResult.SUCCESS, true);
		check("The lesson is in the list with a database ID",
				added.getLessonId() > 0 && mine.findById(added.getLessonId()) != null, true);
		check("The lesson is in the database", LessonTestFixtures.read(db, added.getLessonId()) != null, true);
		endTest();
	}

	/**
	 * <p> Test LT-11 (LIST-4, negative): an invalid lesson is stored in neither place.</p>
	 * <p> How: calls addLesson with a 3-character title and checks the result is INVALID_INPUT,
	 * the recognizer's message is exactly the documented one, and neither the list nor the
	 * database gained a lesson.</p>
	 * <p> Output: a check, a message check showing both messages, and a final check.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testAddInvalidLessonThroughList() throws Exception {
		beginTest("LT-11", "LIST-4", "negative", "An invalid lesson is stored in neither the list nor the database");
		Database db = freshDatabase();
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		LessonResult result = mine.addLesson(db, lesson(JSMITH, "Bug"));
		check("addLesson returns INVALID_INPUT", result == LessonResult.INVALID_INPUT, true);
		checkMessage("The error message", LessonRecognizer.lessonRecognizerErrorMessage,
				"Title must be at least 5 characters.");
		check("Neither the list nor the database gained a lesson",
				mine.isEmpty() && db.getNumberOfLessons() == 0, true);
		endTest();
	}

	
	/*-*******************************************************************************************

	LIST-5: Update through the list

	 */

	/**
	 * <p> Test LT-12 (LIST-5, positive): a valid update through the list changes both.</p>
	 * <p> How: updates a lesson's title through the list, with extra spaces around it, and
	 * checks SUCCESS, that the list now shows the trimmed title the database stored, and that
	 * the database holds the same title.</p>
	 * <p> Output: three checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testUpdateValidLessonThroughList() throws Exception {
		beginTest("LT-12", "LIST-5", "positive", "A valid update through the list changes both");
		Database db = freshDatabase();
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		LessonsLearned original = lesson(JSMITH, "Original title here");
		mine.addLesson(db, original);
		LessonsLearned edited = new LessonsLearned(original.getLessonId(), JSMITH,
				"  New title here  ", "Coding", SITUATION, LESSON);
		check("updateLesson returns SUCCESS",
				mine.updateLesson(db, edited, JSMITH) == LessonResult.SUCCESS, true);
		check("The list shows the trimmed, stored title",
				"New title here".equals(mine.findById(original.getLessonId()).getTitle()), true);
		check("The database holds the same title",
				"New title here".equals(LessonTestFixtures.read(db, original.getLessonId()).getTitle()), true);
		endTest();
	}

	/**
	 * <p> Test LT-13 (LIST-5, negative): an invalid update changes neither place.</p>
	 * <p> How: tries to change a title to "Bug" and checks INVALID_INPUT, the documented
	 * message, and that the list and database both keep the original title.</p>
	 * <p> Output: a check, a message check, and a final check.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testUpdateInvalidLessonThroughList() throws Exception {
		beginTest("LT-13", "LIST-5", "negative", "An invalid update changes neither the list nor the database");
		Database db = freshDatabase();
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		LessonsLearned original = lesson(JSMITH, "Original title here");
		mine.addLesson(db, original);
		LessonsLearned edited = new LessonsLearned(original.getLessonId(), JSMITH, "Bug",
				"Coding", SITUATION, LESSON);
		check("updateLesson returns INVALID_INPUT",
				mine.updateLesson(db, edited, JSMITH) == LessonResult.INVALID_INPUT, true);
		checkMessage("The error message", LessonRecognizer.lessonRecognizerErrorMessage,
				"Title must be at least 5 characters.");
		check("The list and database keep the original title",
				"Original title here".equals(mine.findById(original.getLessonId()).getTitle())
				&& "Original title here".equals(LessonTestFixtures.read(db, original.getLessonId()).getTitle()),
				true);
		endTest();
	}

	/**
	 * <p> Test LT-14 (LIST-5, negative): a contributor cannot update another contributor's
	 * lesson through the list.</p>
	 * <p> How: jsmith tries to update akim's lesson; checks NOT_OWNER, the documented message,
	 * and that akim's lesson is unchanged.</p>
	 * <p> Output: a check, a message check, and a final check.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testUpdateOtherContributorsLesson() throws Exception {
		beginTest("LT-14", "LIST-5", "negative", "Updating another contributor's lesson is refused");
		Database db = freshDatabase();
		LessonsLearned akims = lesson(AKIM, "Akim's own lesson");
		LessonTestFixtures.add(db, akims);
		LessonsLearnedList all = LessonTestFixtures.all(db);
		LessonsLearned edited = new LessonsLearned(akims.getLessonId(), AKIM,
				"Changed by jsmith", "Coding", SITUATION, LESSON);
		LessonResult result = all.updateLesson(db, edited, JSMITH);
		check("updateLesson returns NOT_OWNER", result == LessonResult.NOT_OWNER, true);
		checkMessage("The ownership message", result.getMessage(),
				"You can only edit or delete lessons that you added.");
		check("akim's lesson is unchanged",
				"Akim's own lesson".equals(LessonTestFixtures.read(db, akims.getLessonId()).getTitle()), true);
		endTest();
	}

	
	/*-*******************************************************************************************

	LIST-6: Delete through the list

	 */

	/**
	 * <p> Test LT-15 (LIST-6, positive): a confirmed delete removes the lesson from both.</p>
	 * <p> How: deletes one of two lessons with confirmed = true (the user answered "Yes") and
	 * checks SUCCESS, that it is gone from the list and the database, and that the other lesson
	 * remains.</p>
	 * <p> Output: three checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testDeleteConfirmedThroughList() throws Exception {
		beginTest("LT-15", "LIST-6", "positive", "A confirmed delete removes the lesson from both");
		Database db = freshDatabase();
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		LessonsLearned gone = lesson(JSMITH, "Lesson to delete");
		LessonsLearned kept = lesson(JSMITH, "Lesson to keep");
		mine.addLesson(db, gone);
		mine.addLesson(db, kept);
		check("deleteLesson returns SUCCESS",
				mine.deleteLesson(db, gone.getLessonId(), JSMITH, true) == LessonResult.SUCCESS, true);
		check("It is gone from the list and the database",
				mine.findById(gone.getLessonId()) == null && LessonTestFixtures.read(db, gone.getLessonId()) == null,
				true);
		check("The other lesson remains in both",
				mine.findById(kept.getLessonId()) != null && LessonTestFixtures.read(db, kept.getLessonId()) != null,
				true);
		endTest();
	}

	/**
	 * <p> Test LT-16 (LIST-6, negative): answering "No" cancels the delete.</p>
	 * <p> How: calls deleteLesson with confirmed = false and checks CANCELLED and that the lesson
	 * is still in the list and the database.</p>
	 * <p> Output: two checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testDeleteCancelledThroughList() throws Exception {
		beginTest("LT-16", "LIST-6", "negative", "Answering No cancels the delete");
		Database db = freshDatabase();
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		LessonsLearned kept = lesson(JSMITH, "Lesson to keep");
		mine.addLesson(db, kept);
		check("deleteLesson returns CANCELLED",
				mine.deleteLesson(db, kept.getLessonId(), JSMITH, false) == LessonResult.CANCELLED,
				true);
		check("The lesson is still in the list and the database",
				mine.findById(kept.getLessonId()) != null && LessonTestFixtures.read(db, kept.getLessonId()) != null,
				true);
		endTest();
	}

	/**
	 * <p> Test LT-17 (LIST-6, negative): deleting a lesson that no longer exists.</p>
	 * <p> How: deletes a lesson, then deletes it again; checks NOT_FOUND, the documented message,
	 * and that the other lesson is unaffected.</p>
	 * <p> Output: a check, a message check, and a final check.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testDeleteMissingLessonThroughList() throws Exception {
		beginTest("LT-17", "LIST-6", "negative", "Deleting a lesson that no longer exists is refused");
		Database db = freshDatabase();
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		LessonsLearned gone = lesson(JSMITH, "Lesson to delete");
		LessonsLearned kept = lesson(JSMITH, "Lesson to keep");
		mine.addLesson(db, gone);
		mine.addLesson(db, kept);
		mine.deleteLesson(db, gone.getLessonId(), JSMITH, true);
		LessonResult result = mine.deleteLesson(db, gone.getLessonId(), JSMITH, true);
		check("The second delete returns NOT_FOUND", result == LessonResult.NOT_FOUND, true);
		checkMessage("The not-found message", result.getMessage(),
				"That lesson could not be found. It may have been deleted. Please refresh your list.");
		check("The other lesson is unaffected", mine.size() == 1 && db.getNumberOfLessons() == 1, true);
		endTest();
	}

	
	/*-*******************************************************************************************

	LIST-7: Keyword search

	 */

	/**
	 * <p> Test LT-18 (LIST-7, positive): a keyword is found in any of the four fields.</p>
	 * <p> How: builds four lessons, each with a unique word in a different field (title,
	 * category, situation, lesson text), and checks a search for each word finds exactly that
	 * one lesson.</p>
	 * <p> Output: four checks, each Expected: true, Observed: true, Success.</p>
	 */
	static void testSearchMatchesEveryField() {
		beginTest("LT-18", "LIST-7", "positive", "A keyword is found in the title, category, situation, or lesson");
		LessonsLearnedList list = new LessonsLearnedList();
		list.add(new LessonsLearned(1, JSMITH, "Zebra in the title", "Coding", SITUATION, LESSON));
		list.add(new LessonsLearned(2, JSMITH, "Plain title two", "Teamwork", SITUATION, LESSON));
		list.add(new LessonsLearned(3, JSMITH, "Plain title three", "Coding",
				"The situation mentions a walrus here.", LESSON));
		list.add(new LessonsLearned(4, JSMITH, "Plain title four", "Coding", SITUATION,
				"The lesson text mentions a giraffe here."));
		check("\"zebra\" (title) finds only lesson 1", onlyId(list.search("zebra"), 1), true);
		check("\"teamwork\" (category) finds only lesson 2", onlyId(list.search("teamwork"), 2), true);
		check("\"walrus\" (situation) finds only lesson 3", onlyId(list.search("walrus"), 3), true);
		check("\"giraffe\" (lesson text) finds only lesson 4", onlyId(list.search("giraffe"), 4), true);
		endTest();
	}

	/**
	 * <p> Test LT-19 (LIST-7, positive): search ignores upper and lower case.</p>
	 * <p> How: searches for "JAVAFX" and "javafx" in a lesson titled with "JavaFX" and checks
	 * both find it.</p>
	 * <p> Output: two checks, each Expected: true, Observed: true, Success.</p>
	 */
	static void testSearchIgnoresCase() {
		beginTest("LT-19", "LIST-7", "positive", "Search ignores upper and lower case");
		LessonsLearnedList list = new LessonsLearnedList();
		list.add(new LessonsLearned(1, JSMITH, "JavaFX app fails to launch", "Coding",
				"Nothing relevant in this situation text.", "Nothing relevant in this lesson text."));
		check("\"JAVAFX\" finds the lesson", list.search("JAVAFX").size() == 1, true);
		check("\"javafx\" finds the lesson", list.search("javafx").size() == 1, true);
		endTest();
	}

	/**
	 * <p> Test LT-20 (LIST-7, positive): every keyword must match.</p>
	 * <p> How: with one lesson containing "eclipse" and another without it, both containing
	 * "javafx", checks "javafx" finds both but "javafx eclipse" finds only the first, because
	 * adding keywords narrows a search.</p>
	 * <p> Output: two checks, each Expected: true, Observed: true, Success.</p>
	 */
	static void testSearchRequiresAllKeywords() {
		beginTest("LT-20", "LIST-7", "positive", "Every keyword must match");
		LessonsLearnedList list = new LessonsLearnedList();
		list.add(new LessonsLearned(1, JSMITH, "JavaFX fails in Eclipse", "Coding",
				"Situation without the other word.", "Lesson without the other word."));
		list.add(new LessonsLearned(2, JSMITH, "JavaFX fails in VS Code", "Coding",
				"Situation without the other word.", "Lesson without the other word."));
		check("\"javafx\" finds both lessons", list.search("javafx").size() == 2, true);
		check("\"javafx eclipse\" finds only the Eclipse lesson",
				onlyId(list.search("javafx eclipse"), 1), true);
		endTest();
	}

	/**
	 * <p> Test LT-21 (LIST-7, negative): a search with no matches.</p>
	 * <p> How: searches for a word no lesson contains and checks the result is an empty list
	 * (not null), and checks the exact no-matches message the My Lessons page shows.</p>
	 * <p> Output: one check, then a message check showing both messages.</p>
	 */
	static void testSearchWithNoMatches() {
		beginTest("LT-21", "LIST-7", "negative", "A search with no matches gives an empty list");
		LessonsLearnedList list = new LessonsLearnedList();
		list.add(lesson(JSMITH, "JavaFX app fails to launch"));
		LessonsLearnedList result = list.search("kubernetes");
		check("The result is empty but not null", result != null && result.isEmpty(), true);
		checkMessage("The no-matches message", LessonsLearnedList.NO_MATCHES_MESSAGE,
				"No lessons match your search.");
		endTest();
	}

	/**
	 * <p> Test LT-22 (LIST-7, negative): a blank or missing search returns every lesson.</p>
	 * <p> How: searches with "", "   ", and null, and checks each returns all 3 lessons rather
	 * than none or an exception.</p>
	 * <p> Output: three checks, each Expected: true, Observed: true, Success.</p>
	 */
	static void testSearchBlankReturnsWholeList() {
		beginTest("LT-22", "LIST-7", "negative", "A blank or missing search returns every lesson");
		LessonsLearnedList list = new LessonsLearnedList();
		for (int i = 1; i <= 3; i++) list.add(lesson(JSMITH, "Lesson number " + i));
		check("\"\" returns all 3", list.search("").size() == 3, true);
		check("\"   \" returns all 3", list.search("   ").size() == 3, true);
		check("null returns all 3", list.search(null).size() == 3, true);
		endTest();
	}

	/**
	 * <p> Test LT-23 (LIST-7, positive): a search never changes the list it searches, and a
	 * search of a contributor's list never includes other contributors' lessons.</p>
	 * <p> How: searches jsmith's list for "kotlin", a word that also appears in akim's lesson
	 * title, then checks
	 * the result holds only jsmith's lesson and the original list still holds its lessons.</p>
	 * <p> Output: two checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testSearchLeavesSourceUnchanged() throws Exception {
		beginTest("LT-23", "LIST-7", "positive", "Search leaves the source list unchanged and stays within it");
		Database db = freshDatabase();
		// "Kotlin" appears only in these titles, not in the shared situation and lesson text
		LessonTestFixtures.add(db, lesson(JSMITH, "Kotlin for jsmith"));
		LessonTestFixtures.add(db, lesson(JSMITH, "Merge conflicts for jsmith"));
		LessonTestFixtures.add(db, lesson(AKIM, "Kotlin for akim"));
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		LessonsLearnedList found = mine.search("kotlin");
		check("The search finds only jsmith's Kotlin lesson",
				found.size() == 1 && JSMITH.equals(found.get(0).getOwner()), true);
		check("jsmith's list still holds both lessons", mine.size() == 2, true);
		endTest();
	}

	
	/*-*******************************************************************************************

	LIST-8: Lock and curator-edited state

	 */

	/**
	 * <p> Test LT-24 (LIST-8, positive): the list reports locks and curator edits.</p>
	 * <p> How: locks one field of one lesson, locks a second lesson entirely, marks the second
	 * as curator-edited, reloads the list, and checks each lesson reports the right state.  This
	 * is the information the My Lessons page uses for its "Locked" and "Edited by curator" tags,
	 * and the Lesson Detail page uses to make fields read-only and disable Delete.</p>
	 * <p> Output: three checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testLockAndEditedStateReported() throws Exception {
		beginTest("LT-24", "LIST-8", "positive", "The list reports locks and curator edits");
		Database db = freshDatabase();
		LessonsLearned fieldLocked = lesson(JSMITH, "One field locked");
		LessonsLearned wholeLocked = lesson(JSMITH, "Whole lesson locked");
		LessonTestFixtures.add(db, fieldLocked);
		LessonTestFixtures.add(db, wholeLocked);
		LessonTestFixtures.fieldLock(db, fieldLocked.getLessonId(), "Title", true);
		LessonTestFixtures.lock(db, wholeLocked.getLessonId(), true);
		LessonTestFixtures.edited(db, wholeLocked.getLessonId(), true);
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(db, JSMITH);
		LessonsLearned a = mine.findById(fieldLocked.getLessonId());
		LessonsLearned b = mine.findById(wholeLocked.getLessonId());
		check("A lesson with one locked field reports a lock on that field only",
				a.hasAnyLock() && a.isFieldLocked("Title") && !a.isFieldLocked("Situation"), true);
		check("A wholly locked lesson reports every field locked",
				b.hasAnyLock() && b.isFieldLocked("Title") && b.isFieldLocked("Lesson"), true);
		check("The curator-edited lesson reports it", b.isCuratorEdited(), true);
		endTest();
	}

	/**
	 * <p> Test LT-25 (LIST-8, negative): a new lesson has no locks and no curator edit, and an
	 * unlocked field reports unlocked.</p>
	 * <p> How: adds a lesson and checks it reports no lock and no edit; then locks and unlocks a
	 * field and checks it reports no lock again; and checks an unknown field name is refused.</p>
	 * <p> Output: three checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testNewLessonHasNoLocks() throws Exception {
		beginTest("LT-25", "LIST-8", "negative", "New and unlocked lessons report no lock");
		Database db = freshDatabase();
		LessonsLearned fresh = lesson(JSMITH, "Brand new lesson");
		LessonTestFixtures.add(db, fresh);
		LessonsLearned loaded =
				LessonsLearnedList.loadForContributor(db, JSMITH).findById(fresh.getLessonId());
		check("A new lesson has no lock and no curator edit",
				!loaded.hasAnyLock() && !loaded.isCuratorEdited(), true);
		LessonTestFixtures.fieldLock(db, fresh.getLessonId(), "Title", true);
		LessonTestFixtures.fieldLock(db, fresh.getLessonId(), "Title", false);
		check("After unlocking, the lesson reports no lock",
				!LessonsLearnedList.loadForContributor(db, JSMITH).findById(fresh.getLessonId())
				.hasAnyLock(), true);
		check("Locking an unknown field is refused",
				LessonTestFixtures.fieldLock(db, fresh.getLessonId(), "Owner", true) == LessonResult.INVALID_INPUT, true);
		endTest();
	}

	
	/*-*******************************************************************************************

	LIST-9: Persistence after a restart

	 */

	/**
	 * <p> Test LT-26 (LIST-9, positive): changes made through the list survive a restart.</p>
	 * <p> How: using a temporary database file, adds two lessons, updates one, and deletes
	 * another through the list; closes the database; opens it with a new Database object, which
	 * stands in for restarting the application; and checks the reloaded list.</p>
	 * <p> Output: two checks, each Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testChangesPersistAfterRestart() throws Exception {
		beginTest("LT-26", "LIST-9", "positive", "Changes made through the list survive a restart");
		String url = temporaryFileUrl();
		Database first = new Database(url);
		first.connectToDatabase();
        LessonTestFixtures.prepare(first);
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(first, JSMITH);
		LessonsLearned kept = lesson(JSMITH, "Lesson before update");
		LessonsLearned gone = lesson(JSMITH, "Lesson to delete");
		mine.addLesson(first, kept);
		mine.addLesson(first, gone);
		mine.updateLesson(first, new LessonsLearned(kept.getLessonId(), JSMITH,
				"Lesson after update", "Coding", SITUATION, LESSON), JSMITH);
		mine.deleteLesson(first, gone.getLessonId(), JSMITH, true);
		first.closeConnection();									// "Close the application"
		
		Database second = new Database(url);						// "Start it again"
		second.connectToDatabase();
        LessonTestFixtures.prepare(second);
		LessonsLearnedList reloaded = LessonsLearnedList.loadForContributor(second, JSMITH);
		check("The updated lesson is there with its new title",
				reloaded.size() == 1 && "Lesson after update".equals(reloaded.get(0).getTitle()),
				true);
		check("The deleted lesson is still gone", reloaded.findById(gone.getLessonId()) == null, true);
		second.closeConnection();
		deleteTemporaryFile();
		endTest();
	}

	/**
	 * <p> Test LT-27 (LIST-9, negative): refused changes leave nothing behind after a restart.</p>
	 * <p> How: using a temporary database file, tries an invalid add and a cancelled delete;
	 * restarts; and checks the database holds exactly the one valid lesson, unchanged.</p>
	 * <p> Output: one check, Expected: true, Observed: true, Success.</p>
	 * @throws Exception if the test database cannot be created
	 */
	static void testRefusedChangesDoNotPersist() throws Exception {
		beginTest("LT-27", "LIST-9", "negative", "Refused changes leave nothing behind after a restart");
		String url = temporaryFileUrl();
		Database first = new Database(url);
		first.connectToDatabase();
        LessonTestFixtures.prepare(first);
		LessonsLearnedList mine = LessonsLearnedList.loadForContributor(first, JSMITH);
		LessonsLearned kept = lesson(JSMITH, "The only valid lesson");
		mine.addLesson(first, kept);
		mine.addLesson(first, lesson(JSMITH, "Bug"));					// Refused: invalid
		mine.deleteLesson(first, kept.getLessonId(), JSMITH, false);	// Refused: cancelled
		first.closeConnection();
		
		Database second = new Database(url);
		second.connectToDatabase();
        LessonTestFixtures.prepare(second);
		LessonsLearnedList reloaded = LessonsLearnedList.loadForContributor(second, JSMITH);
		check("Exactly the one valid lesson is stored, unchanged",
				reloaded.size() == 1 && "The only valid lesson".equals(reloaded.get(0).getTitle()),
				true);
		second.closeConnection();
		deleteTemporaryFile();
		endTest();
	}

	
	/*-*******************************************************************************************

	Helpers

	 */
	
	// Each test gets its own private in-memory database, so no test can affect another's counts
	private static Database freshDatabase() throws Exception {
		Database db = new Database("jdbc:h2:mem:lessonListTest" + (++databaseNumber)
				+ ";DB_CLOSE_DELAY=-1");
		db.connectToDatabase();
        LessonTestFixtures.prepare(db);
		return db;
	}

	// A valid lesson with the given owner and title (the title decides validity in the tests)
	private static LessonsLearned lesson(String owner, String title) {
		return new LessonsLearned(owner, title, "Coding", SITUATION, LESSON);
	}

	// True when the search result holds exactly one lesson, with the given ID
	private static boolean onlyId(LessonsLearnedList result, int id) {
		return result.size() == 1 && result.get(0).getLessonId() == id;
	}
	
	// The persistence tests need a real file; it is deleted before and after each use
	private static final File TEMP = new File(System.getProperty("java.io.tmpdir"),
			"lessonListPersistenceTest");
	private static String temporaryFileUrl() {
		deleteTemporaryFile();
		return "jdbc:h2:" + TEMP.getAbsolutePath();
	}
	private static void deleteTemporaryFile() { new File(TEMP.getPath() + ".mv.db").delete(); }

	/*
	 * Starts a test: prints its header so the output can be matched to the Javadoc and the
	 * requirement it covers
	 */
	private static void beginTest(String id, String requirement, String kind, String description) {
		currentTestOk = true;
		System.out.println("______________________________________________________________________");
		System.out.println("\nTest " + id + "  [" + requirement + ", " + kind + "]");
		System.out.println("Testing: " + description);
	}

	// Ends a test: the test passes only if every one of its checks succeeded
	private static void endTest() {
		if (currentTestOk) testsPassed++; else testsFailed++;
		System.out.println(currentTestOk ? "TEST PASSED" : "TEST FAILED");
	}

	/*
	 * One check, reported in the same manner as the team's other testing automation classes:
	 * the expected and observed results, then Success or Failure
	 */
	private static void check(String description, boolean observed, boolean expected) {
		System.out.println("\n  Check: " + description);
		System.out.println("  Expected: " + expected + "   Observed: " + observed);
		if (observed == expected) {
			System.out.println("  ***Success*** The check produced the expected result.");
			checksPassed++;
		} else {
			System.out.println("  ***Failure*** The check did NOT produce the expected result.");
			checksFailed++;
			currentTestOk = false;
		}
	}

	/*
	 * A message check prints both messages in full, so the output itself shows the user would
	 * see the helpful message the requirement promises, not only that two strings matched
	 */
	private static void checkMessage(String description, String observed, String expected) {
		System.out.println("\n  Check: " + description);
		System.out.println("  Expected message: \"" + expected + "\"");
		System.out.println("  Observed message: \"" + observed + "\"");
		if (expected.equals(observed)) {
			System.out.println("  ***Success*** The check produced the expected result.");
			checksPassed++;
		} else {
			System.out.println("  ***Failure*** The check did NOT produce the expected result.");
			checksFailed++;
			currentTestOk = false;
		}
	}
}