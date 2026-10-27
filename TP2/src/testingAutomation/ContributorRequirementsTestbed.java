package testingAutomation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import database.Database;
import database.Database.LessonResult;
import entityClasses.LessonExperience;
import entityClasses.LessonsLearned;
import entityClasses.LessonsLearnedList;
import entityClasses.User;
import guiLessonDelete.ModelLessonDelete;
import guiLessonSupport.CurrentRepoGateway;
import guiLessonSupport.LessonGateway;
import guiLessonSupport.Rules;
import lessonRecognizer.LessonRecognizer;

/*******
 * <p> Title: ContributorRequirementsTestbed Class. </p>
 *
 * <p> Description: The team's requirement-driven testbed for every TP2 Contributor requirement
 * (Task 7).  Each requirement in {@link ContributorRequirement} is covered by two or more tests,
 * at least one positive (valid use succeeds) and one negative (invalid use is refused or handled
 * safely).  Each test is its own method with its own Javadoc, written to the team's test Javadoc
 * template: the requirement tested, how the code checks it, and how to read its output. </p>
 *
 * <p> The tests use the real integrated code: the session-bound {@link Database} and its
 * LessonStore, the {@link LessonsLearned} and {@link LessonsLearnedList} entity classes, the
 * experience recognizer, and the Contributor GUI's non-visual layer (the gateway, Rules, and the
 * Delete window's model).  They run against a private, file-backed H2 database in a temporary
 * folder, so they never touch the application's real data, and that file lets CR-16 close and
 * restart the database to prove persistence. </p>
 *
 * <p> <b>How to run:</b> run this class as a Java Application in Eclipse (no arguments), or run
 * <code>python3 dev.py test</code> from the TP2 folder to run this testbed together with every
 * other team suite.  No GUI is opened. </p>
 *
 * <p> <b>How to read the output:</b> every test prints a header line with its requirement ID,
 * test number, POSITIVE or NEGATIVE, and title.  Each check under it prints what was expected
 * and what was observed, then "ok" or "FAILED".  The test ends with PASS only when every check
 * succeeded.  The final summary lists, for each requirement, how many positive and negative tests
 * passed, then the totals.  A correct build ends with "Tests failed: 0" and
 * "Requirement coverage: complete", and exits with status 0; any failure exits with status 1. </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author Aditya Gupta (agupt515)
 *
 * @version 1.00	2026-10-27	Initial version for TP2
 */
public final class ContributorRequirementsTestbed {

	/*
	 * Attributes
	 */

	/** Shared password for every fixture account; these accounts exist only in the temp database */
	private static final String PASSWORD = "TestbedPass1!";

	/** A valid Situation (20-2000 characters) reused by tests that only care about other fields */
	private static final String SITUATION =
			"The team merged two branches the night before the deadline.";

	/** A valid Lesson Text (20-2000 characters) reused by tests that only care about other fields */
	private static final String LESSON_TEXT =
			"Merge every day after standup so conflicts stay small and easy to fix.";

	/** The Curator fixture account, the only account allowed to set locks and curator data */
	private static final String CURATOR = "testbedCurator";

	/** The temporary database URL; kept so CR-16 can close and reopen the same file */
	private static String url;

	/** The database under test; replaced when CR-16 restarts it */
	private static Database db;

	/** Number of checks that failed inside the test that is currently running */
	private static int failedChecksInTest;

	/** Number of checks that passed across the whole run */
	private static int checksPassed;

	/** Used to make each test's fixture account names unique */
	private static int accountNumber;

	private ContributorRequirementsTestbed() {}


	/*****
	 * <p> Method: main(String[] args) </p>
	 *
	 * <p> Description: Creates the temporary database, runs every test annotated with
	 * {@link RequirementTest} in requirement order, prints the per-requirement summary, and
	 * verifies that every requirement has at least one positive and one negative test. </p>
	 *
	 * @param args not used
	 *
	 * @throws Exception if the temporary database cannot be created
	 */
	public static void main(String[] args) throws Exception {
		Path folder = Files.createTempDirectory("tp2-contributor-testbed-");
		url = "jdbc:h2:" + folder.resolve("testbed").toAbsolutePath();
		db = new Database(url);
		db.connectToDatabase();
		register(CURATOR, false, true);

		System.out.println("TP2 Contributor Requirements Testbed");
		System.out.println("Every Contributor requirement is tested by at least one positive and one "
				+ "negative test.");
		System.out.println("Temporary database: " + folder.toAbsolutePath());
		System.out.println("=".repeat(78));

		List<Method> tests = annotatedTests();
		Map<ContributorRequirement, int[]> passedByRequirement =
				new EnumMap<>(ContributorRequirement.class);
		for (ContributorRequirement requirement : ContributorRequirement.values())
			passedByRequirement.put(requirement, new int[3]);	// positive, negative, failed
		int testsPassed = 0, testsFailed = 0;

		for (Method test : tests) {
			RequirementTest info = test.getAnnotation(RequirementTest.class);
			System.out.println();
			System.out.println(label(info) + " [" + info.kind() + "] " + info.title());
			failedChecksInTest = 0;
			try {
				test.setAccessible(true);
				test.invoke(null);
			} catch (InvocationTargetException failure) {
				// An exception the test did not expect is a failure, never a silent pass
				failedChecksInTest++;
				System.out.println("    unexpected exception: " + failure.getCause());
			}
			int[] counts = passedByRequirement.get(info.requirement());
			if (failedChecksInTest == 0) {
				testsPassed++;
				counts[info.kind() == RequirementTest.Kind.POSITIVE ? 0 : 1]++;
				System.out.println("    PASS");
			} else {
				testsFailed++;
				counts[2]++;
				System.out.println("    FAIL (" + failedChecksInTest + " check(s) failed)");
			}
		}

		db.closeConnection();
		deleteQuietly(folder);
		boolean coverageComplete = printSummary(tests, passedByRequirement, testsPassed, testsFailed);
		if (testsFailed > 0 || !coverageComplete) System.exit(1);
	}


	/*****
	 * <p> Method: List&lt;Method&gt; annotatedTests() </p>
	 *
	 * <p> Description: Finds every test method and orders it by requirement, then test number,
	 * so the output always appears in the same order as the requirements list. </p>
	 *
	 * @return the test methods in requirement order
	 */
	static List<Method> annotatedTests() {
		List<Method> tests = new ArrayList<>();
		for (Method method : ContributorRequirementsTestbed.class.getDeclaredMethods())
			if (method.isAnnotationPresent(RequirementTest.class)) tests.add(method);
		tests.sort(Comparator.comparing((Method m) -> m.getAnnotation(RequirementTest.class)
				.requirement()).thenComparingInt(m -> m.getAnnotation(RequirementTest.class).number()));
		return tests;
	}


	/*****
	 * <p> Method: String label(RequirementTest info) </p>
	 *
	 * <p> Description: Builds the test's printed label, such as "CR-04 T2". </p>
	 *
	 * @param info the test's annotation
	 *
	 * @return the label
	 */
	static String label(RequirementTest info) {
		return info.requirement().id() + " T" + info.number();
	}


	/*****
	 * <p> Method: boolean printSummary(...) </p>
	 *
	 * <p> Description: Prints one row per requirement and the totals, and reports whether every
	 * requirement has two or more passing tests including a positive and a negative one. </p>
	 */
	private static boolean printSummary(List<Method> tests,
			Map<ContributorRequirement, int[]> passed, int testsPassed, int testsFailed) {
		System.out.println();
		System.out.println("=".repeat(78));
		System.out.println("Requirement  Owner                     Positive  Negative  Failed");
		boolean complete = true;
		for (ContributorRequirement requirement : ContributorRequirement.values()) {
			int[] counts = passed.get(requirement);
			long declared = tests.stream().filter(m -> m.getAnnotation(RequirementTest.class)
					.requirement() == requirement).count();
			boolean covered = counts[0] >= 1 && counts[1] >= 1 && declared >= 2;
			if (!covered) complete = false;
			System.out.printf("%-12s %-25s %8d  %8d  %6d%s%n", requirement.id(), requirement.owner(),
					counts[0], counts[1], counts[2], covered ? "" : "  <-- needs a passing P and N test");
		}
		System.out.println("-".repeat(78));
		System.out.println("Requirements: " + ContributorRequirement.values().length);
		System.out.println("Tests passed: " + testsPassed);
		System.out.println("Tests failed: " + testsFailed);
		System.out.println("Checks passed: " + checksPassed);
		System.out.println("Requirement coverage: " + (complete ? "complete" : "INCOMPLETE"));
		System.out.println("=".repeat(78));
		return complete;
	}


	/*
	 * Check and fixture helpers
	 */

	/** Prints one check's expected and observed values and records whether they matched. */
	private static void check(String what, Object expected, Object observed, boolean ok) {
		System.out.println("    " + what);
		System.out.println("        expected: " + expected);
		System.out.println("        observed: " + observed + "   " + (ok ? "ok" : "FAILED"));
		if (ok) checksPassed++; else failedChecksInTest++;
	}

	/** Checks that an operation is refused with an exception; returns the refusal message. */
	private static String refused(String what, String expected, Action action) {
		try {
			action.run();
			check(what, expected, "no exception (the operation was allowed)", false);
			return "";
		} catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
			check(what, expected, "refused: " + e.getMessage(), true);
			return e.getMessage();
		} catch (Exception e) {
			check(what, expected, "wrong exception: " + e, false);
			return "";
		}
	}

	/** An operation that may throw; used by {@link #refused}. */
	private interface Action { void run() throws Exception; }

	/** Registers a fixture account in the temporary database (Admin, Contributor, Viewer, Curator). */
	private static void register(String name, boolean contributor, boolean curator)
			throws Exception {
		db.register(new User(name, PASSWORD, "Testbed", "", "User", "", name + "@example.test",
				false, contributor, !contributor && !curator, curator));
	}

	/** Registers a new uniquely named Contributor so each test starts from a known state. */
	private static String newContributor(String prefix) throws Exception {
		String name = prefix + (++accountNumber);
		register(name, true, false);
		return name;
	}

	/** Signs in as the named account; a failed sign-in stops the test as a failure. */
	private static void signIn(String name) {
		if (!db.authenticateSession(name, PASSWORD))
			throw new IllegalStateException("Fixture sign-in failed for " + name);
	}

	/** Adds a valid lesson owned by the named Contributor and leaves that Contributor signed in. */
	private static int addLesson(String owner, String title) {
		signIn(owner);
		LessonsLearned lesson = new LessonsLearned(owner, title, "Testing", SITUATION, LESSON_TEXT);
		LessonResult result = db.addLesson(lesson);
		if (result != LessonResult.SUCCESS)
			throw new IllegalStateException("Fixture lesson could not be added: " + result);
		return lesson.getLessonId();
	}

	/** Runs a Curator-only setup step, then signs the Contributor back in. */
	private static LessonResult asCurator(String contributor, CuratorStep step) throws Exception {
		signIn(CURATOR);
		try {
			return step.run();
		} finally {
			signIn(contributor);
		}
	}

	/** A Curator-only setup step. */
	private interface CuratorStep { LessonResult run() throws Exception; }

	/** A valid two-member experience for the given lesson. */
	private static LessonExperience experience(int lessonId) {
		return new LessonExperience(lessonId, "Reused the daily merge lesson",
				"Merged after standup in the fixed order database, entity, GUI, tests",
				new BigDecimal("2.5"), List.of(
						new LessonExperience.Effort("Ana", new BigDecimal("1.5")),
						new LessonExperience.Effort("Ben", new BigDecimal("1"))));
	}

	/** The same lesson with a different Situation, used by update tests. */
	private static LessonsLearned withSituation(LessonsLearned stored, String situation) {
		return new LessonsLearned(stored.getLessonId(), stored.getOwner(), stored.getTitle(),
				stored.getCategory(), situation, stored.getLessonText());
	}

	/** Deletes the temporary database folder; leftovers in the temp folder are harmless. */
	private static void deleteQuietly(Path folder) {
		try (var paths = Files.walk(folder)) {
			paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
		} catch (IOException ignored) {
			// The operating system cleans its temporary folder; nothing else depends on this
		}
	}


	/*
	 * CR-01: create a lesson with core information
	 */

	/*****
	 * <p> Test CR-01 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-01, a Contributor can add a new lesson by entering its
	 * core information, and the stored lesson belongs to the signed-in Contributor. </p>
	 *
	 * <p> <b>How it is checked:</b> a new Contributor signs in and calls
	 * LessonsLearned.create (which calls Database.addLesson) with four valid fields.  The lesson
	 * is then read back from the database with Database.getLesson. </p>
	 *
	 * <p> <b>How to read the output:</b> the first check expects SUCCESS with an ID greater than
	 * 0; the second expects the stored owner and title to equal what was entered.  PASS means the
	 * lesson was stored for its author. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR01, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "A valid lesson is stored for its author")
	private static void cr01ValidLessonIsStoredForAuthor() throws Exception {
		String alice = newContributor("alice");
		signIn(alice);
		LessonsLearned lesson = new LessonsLearned(alice, "Merge daily", "Process", SITUATION,
				LESSON_TEXT);
		LessonResult result = lesson.create(db);
		check("create returns SUCCESS and a new ID", "SUCCESS, ID > 0",
				result + ", ID " + lesson.getLessonId(),
				result == LessonResult.SUCCESS && lesson.getLessonId() > 0);
		LessonsLearned stored = db.getLesson(lesson.getLessonId());
		check("stored owner and title", alice + " / Merge daily",
				stored == null ? "nothing stored" : stored.getOwner() + " / " + stored.getTitle(),
				stored != null && alice.equals(stored.getOwner())
						&& "Merge daily".equals(stored.getTitle()));
	}

	/*****
	 * <p> Test CR-01 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-01, only a signed-in Contributor can add a lesson. </p>
	 *
	 * <p> <b>How it is checked:</b> the session is cleared (as after logout) and Database.addLesson
	 * is called with an otherwise valid lesson; the Contributor then signs in and counts their
	 * lessons. </p>
	 *
	 * <p> <b>How to read the output:</b> the first check expects UNAUTHORIZED; the second expects 0
	 * stored lessons.  PASS means nothing is stored without a signed-in Contributor. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR01, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "No lesson is stored without a session")
	private static void cr01NoSessionStoresNothing() throws Exception {
		String alice = newContributor("alice");
		db.clearAuthenticatedSession();
		LessonResult result = db.addLesson(
				new LessonsLearned(alice, "Merge daily", "Process", SITUATION, LESSON_TEXT));
		check("addLesson with no session", LessonResult.UNAUTHORIZED, result,
				result == LessonResult.UNAUTHORIZED);
		signIn(alice);
		int count = db.getLessonsByOwner(alice).size();
		check("lessons stored for " + alice, 0, count, count == 0);
	}

	/*****
	 * <p> Test CR-01 T3 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-01, the stored lesson belongs to the signed-in Contributor,
	 * so a Contributor cannot add a lesson in another Contributor's name. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice signs in and submits a lesson whose owner field says
	 * Bob; Bob's list is then read while Bob is signed in. </p>
	 *
	 * <p> <b>How to read the output:</b> expect NOT_OWNER and 0 lessons for Bob.  PASS means a
	 * forged owner is refused. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR01, number = 3,
			kind = RequirementTest.Kind.NEGATIVE, title = "A lesson in another author's name is refused")
	private static void cr01ForgedOwnerIsRefused() throws Exception {
		String alice = newContributor("alice"), bob = newContributor("bob");
		signIn(alice);
		LessonResult result = db.addLesson(
				new LessonsLearned(bob, "Merge daily", "Process", SITUATION, LESSON_TEXT));
		check("Alice adds a lesson owned by Bob", LessonResult.NOT_OWNER, result,
				result == LessonResult.NOT_OWNER);
		signIn(bob);
		int count = db.getLessonsByOwner(bob).size();
		check("lessons stored for Bob", 0, count, count == 0);
	}


	/*
	 * CR-02: core field validation
	 */

	/*****
	 * <p> Test CR-02 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-02, values exactly at the lower limits are valid: a
	 * 5-character Title and 20-character Situation and Lesson Text. </p>
	 *
	 * <p> <b>How it is checked:</b> LessonRecognizer.checkForValidLesson is called on the boundary
	 * lesson, and the lesson is then added through Database.addLesson. </p>
	 *
	 * <p> <b>How to read the output:</b> expect an empty recognizer message (shown as "") and
	 * SUCCESS.  PASS means the limits are inclusive. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR02, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "Boundary values at the minimum lengths are accepted")
	private static void cr02BoundaryValuesAreAccepted() throws Exception {
		String alice = newContributor("alice");
		signIn(alice);
		LessonsLearned lesson = new LessonsLearned(alice, "Plans", "Design",
				"x".repeat(20), "y".repeat(20));
		String message = LessonRecognizer.checkForValidLesson(lesson);
		check("recognizer message for 5/20/20 characters", "\"\"", "\"" + message + "\"",
				message.isEmpty());
		LessonResult result = db.addLesson(lesson);
		check("addLesson", LessonResult.SUCCESS, result, result == LessonResult.SUCCESS);
	}

	/*****
	 * <p> Test CR-02 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-02, a Title shorter than 5 characters is refused with a
	 * message that explains the fix, and nothing is stored. </p>
	 *
	 * <p> <b>How it is checked:</b> a lesson with the 4-character title "Plan" is checked by
	 * LessonRecognizer and submitted to Database.addLesson; the Contributor's list is counted. </p>
	 *
	 * <p> <b>How to read the output:</b> expect a non-empty message that mentions the title,
	 * INVALID_INPUT, and 0 stored lessons. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR02, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "A 4-character title is refused and nothing is stored")
	private static void cr02ShortTitleIsRefused() throws Exception {
		String alice = newContributor("alice");
		signIn(alice);
		LessonsLearned lesson = new LessonsLearned(alice, "Plan", "Design", SITUATION, LESSON_TEXT);
		String message = LessonRecognizer.checkForValidLesson(lesson);
		check("recognizer explains the title problem", "a message mentioning the title",
				"\"" + message.trim() + "\"", message.toLowerCase().contains("title"));
		LessonResult result = db.addLesson(lesson);
		int count = db.getLessonsByOwner(alice).size();
		check("addLesson result and stored count", "INVALID_INPUT, 0", result + ", " + count,
				result == LessonResult.INVALID_INPUT && count == 0);
	}

	/*****
	 * <p> Test CR-02 T3 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-02, the Category must come from the fixed list. </p>
	 *
	 * <p> <b>How it is checked:</b> a lesson with the category "Cooking", which is not in
	 * LessonsLearned.CATEGORIES, is submitted to Database.addLesson. </p>
	 *
	 * <p> <b>How to read the output:</b> expect INVALID_INPUT and 0 stored lessons. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR02, number = 3,
			kind = RequirementTest.Kind.NEGATIVE, title = "A category outside the fixed list is refused")
	private static void cr02UnknownCategoryIsRefused() throws Exception {
		String alice = newContributor("alice");
		signIn(alice);
		LessonResult result = db.addLesson(
				new LessonsLearned(alice, "Merge daily", "Cooking", SITUATION, LESSON_TEXT));
		int count = db.getLessonsByOwner(alice).size();
		check("addLesson result and stored count", "INVALID_INPUT, 0", result + ", " + count,
				result == LessonResult.INVALID_INPUT && count == 0);
	}

	/*****
	 * <p> Test CR-02 T4 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-02, Lesson Text longer than 2000 characters is refused.
	 * </p>
	 *
	 * <p> <b>How it is checked:</b> a lesson whose Lesson Text has 2001 characters is submitted to
	 * Database.addLesson. </p>
	 *
	 * <p> <b>How to read the output:</b> expect INVALID_INPUT and 0 stored lessons. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR02, number = 4,
			kind = RequirementTest.Kind.NEGATIVE, title = "Lesson Text over 2000 characters is refused")
	private static void cr02OverlongLessonTextIsRefused() throws Exception {
		String alice = newContributor("alice");
		signIn(alice);
		LessonResult result = db.addLesson(
				new LessonsLearned(alice, "Merge daily", "Process", SITUATION, "z".repeat(2001)));
		int count = db.getLessonsByOwner(alice).size();
		check("addLesson result and stored count", "INVALID_INPUT, 0", result + ", " + count,
				result == LessonResult.INVALID_INPUT && count == 0);
	}


	/*
	 * CR-03: update an unlocked lesson
	 */

	/*****
	 * <p> Test CR-03 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-03, a Contributor can update an unlocked field of their
	 * own lesson; the ID and owner do not change. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds a lesson, changes its Situation through
	 * LessonsLearned.update (which calls Database.updateLesson), and reads it back. </p>
	 *
	 * <p> <b>How to read the output:</b> expect SUCCESS, then the new Situation with the same ID
	 * and owner. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR03, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "An unlocked field is updated and the identity is kept")
	private static void cr03UnlockedFieldIsUpdated() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		String newSituation = "We merged once a week and spent a whole day on conflicts.";
		LessonsLearned changed = withSituation(db.getLesson(id), newSituation);
		LessonResult result = changed.update(db, alice);
		check("update result", LessonResult.SUCCESS, result, result == LessonResult.SUCCESS);
		LessonsLearned stored = db.getLesson(id);
		check("stored situation, ID and owner", newSituation + " / " + id + " / " + alice,
				stored.getSituation() + " / " + stored.getLessonId() + " / " + stored.getOwner(),
				newSituation.equals(stored.getSituation()) && stored.getLessonId() == id
						&& alice.equals(stored.getOwner()));
	}

	/*****
	 * <p> Test CR-03 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-03, an update with invalid values is refused and the stored
	 * lesson is unchanged. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice submits an update whose Situation is only 5 characters
	 * and then reads the lesson back. </p>
	 *
	 * <p> <b>How to read the output:</b> expect INVALID_INPUT and the original Situation. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR03, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "An invalid update changes nothing")
	private static void cr03InvalidUpdateChangesNothing() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		LessonResult result = db.updateLesson(withSituation(db.getLesson(id), "Short"), alice);
		check("update result", LessonResult.INVALID_INPUT, result,
				result == LessonResult.INVALID_INPUT);
		String stored = db.getLesson(id).getSituation();
		check("stored situation", SITUATION, stored, SITUATION.equals(stored));
	}


	/*
	 * CR-04: per-field locks
	 */

	/*****
	 * <p> Test CR-04 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-04, while one field is locked the Contributor can still
	 * change the other, unlocked fields. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks the Title; Alice then submits an update
	 * that keeps the Title and changes only the Situation. </p>
	 *
	 * <p> <b>How to read the output:</b> expect SUCCESS and the new Situation stored. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR04, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "Unlocked fields stay editable while Title is locked")
	private static void cr04OtherFieldsStayEditable() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setFieldLock(id, "Title", true));
		String newSituation = "Two members edited the same database file on the same day.";
		LessonResult result = db.updateLesson(withSituation(db.getLesson(id), newSituation), alice);
		check("update of Situation only", LessonResult.SUCCESS, result,
				result == LessonResult.SUCCESS);
		check("stored situation", newSituation, db.getLesson(id).getSituation(),
				newSituation.equals(db.getLesson(id).getSituation()));
	}

	/*****
	 * <p> Test CR-04 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-04, a locked field cannot be changed by the Contributor.
	 * </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks the Category; Alice submits an update that
	 * changes the Category from Testing to Design and the lesson is read back. </p>
	 *
	 * <p> <b>How to read the output:</b> expect LOCKED and the stored Category still Testing. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR04, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "A locked Category cannot be changed")
	private static void cr04LockedFieldCannotChange() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setFieldLock(id, "Category", true));
		LessonsLearned stored = db.getLesson(id);
		LessonResult result = db.updateLesson(new LessonsLearned(id, alice, stored.getTitle(),
				"Design", stored.getSituation(), stored.getLessonText()), alice);
		check("update that changes the locked Category", LessonResult.LOCKED, result,
				result == LessonResult.LOCKED);
		check("stored category", "Testing", db.getLesson(id).getCategory(),
				"Testing".equals(db.getLesson(id).getCategory()));
	}

	/*****
	 * <p> Test CR-04 T3 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-04, the Edit window's rules refuse a change to a locked
	 * field before anything is sent to the database. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks the Lesson Text; the GUI gateway reads the
	 * lesson as Alice and Rules.editable is given a draft that changes the Lesson Text. </p>
	 *
	 * <p> <b>How to read the output:</b> expect a refusal whose message names the locked field.
	 * </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR04, number = 3,
			kind = RequirementTest.Kind.NEGATIVE, title = "The Edit window refuses a changed locked field")
	private static void cr04EditRulesRefuseLockedField() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setFieldLock(id, "Lesson", true));
		LessonGateway.Lesson shown = new CurrentRepoGateway(db).read(id);
		LessonGateway.Draft draft = new LessonGateway.Draft(shown.title(), shown.category(),
				shown.situation(), "A completely different lesson text for this test.");
		String message = refused("Rules.editable with changed Lesson Text",
				"refused, naming Lesson text", () -> Rules.editable(shown, draft));
		check("message names the locked field", "contains \"Lesson text\"", message,
				message.contains("Lesson text"));
	}


	/*
	 * CR-05: whole-lesson lock
	 */

	/*****
	 * <p> Test CR-05 T1 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-05, a whole-lesson lock prevents every core-field change.
	 * </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks the whole lesson and Alice submits an update
	 * that changes only the Situation (a field with no field lock). </p>
	 *
	 * <p> <b>How to read the output:</b> expect LOCKED and the original Situation. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR05, number = 1,
			kind = RequirementTest.Kind.NEGATIVE, title = "A locked lesson refuses any core edit")
	private static void cr05LockedLessonRefusesEdits() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setLessonLock(id, true));
		LessonResult result = db.updateLesson(withSituation(db.getLesson(id),
				"An edit that a whole-lesson lock must refuse."), alice);
		check("update of a locked lesson", LessonResult.LOCKED, result,
				result == LessonResult.LOCKED);
		check("stored situation", SITUATION, db.getLesson(id).getSituation(),
				SITUATION.equals(db.getLesson(id).getSituation()));
	}

	/*****
	 * <p> Test CR-05 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-05, a whole-lesson lock also prevents adding experience
	 * information. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks the whole lesson and Alice calls
	 * Database.addExperience with a valid experience; her experiences are then counted. </p>
	 *
	 * <p> <b>How to read the output:</b> expect a refusal saying the lesson is locked, and 0
	 * experiences. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR05, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "A locked lesson refuses new experience")
	private static void cr05LockedLessonRefusesExperience() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setLessonLock(id, true));
		refused("addExperience on a locked lesson", "refused (lesson is locked)",
				() -> db.addExperience(id, experience(id)));
		int count = db.getExperiences(id).size();
		check("experiences stored", 0, count, count == 0);
	}

	/*****
	 * <p> Test CR-05 T3 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-05, the lock is the only thing preventing the edit: once
	 * the Curator unlocks the lesson, the same edit succeeds. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks and then unlocks the lesson; Alice submits
	 * the Situation edit. </p>
	 *
	 * <p> <b>How to read the output:</b> expect SUCCESS after the unlock. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR05, number = 3,
			kind = RequirementTest.Kind.POSITIVE, title = "After unlocking, the same edit succeeds")
	private static void cr05UnlockedLessonAcceptsEdit() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setLessonLock(id, true));
		asCurator(alice, () -> db.setLessonLock(id, false));
		LessonResult result = db.updateLesson(withSituation(db.getLesson(id),
				"An edit made after the curator removed the lock."), alice);
		check("update after unlock", LessonResult.SUCCESS, result,
				result == LessonResult.SUCCESS);
	}


	/*
	 * CR-06: confirmed deletion
	 */

	/*****
	 * <p> Test CR-06 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-06, a confirmed delete removes an unlocked lesson and its
	 * linked experience records. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds a lesson and an experience, then the Delete window's
	 * model (ModelLessonDelete) is called with confirmed = true (the user answered Yes). </p>
	 *
	 * <p> <b>How to read the output:</b> expect true from the model, the lesson no longer readable,
	 * and its experiences no longer available. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR06, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "Yes deletes the lesson and its experience")
	private static void cr06ConfirmedDeleteRemovesLesson() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		db.addExperience(id, experience(id));
		boolean deleted = new ModelLessonDelete(new CurrentRepoGateway(db)).remove(id, true);
		check("ModelLessonDelete.remove(id, Yes)", true, deleted, deleted);
		check("lesson after delete", "null (gone)", db.getLesson(id), db.getLesson(id) == null);
		refused("reading the deleted lesson's experiences", "refused (lesson unavailable)",
				() -> db.getExperiences(id));
	}

	/*****
	 * <p> Test CR-06 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-06, answering No (the default) deletes nothing. </p>
	 *
	 * <p> <b>How it is checked:</b> ModelLessonDelete.remove is called with confirmed = false, and
	 * Database.deleteLesson is also called directly with confirmed = false. </p>
	 *
	 * <p> <b>How to read the output:</b> expect false, CANCELLED, and the lesson still present.
	 * </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR06, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "No (the default) keeps the lesson")
	private static void cr06NoKeepsLesson() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		boolean deleted = new ModelLessonDelete(new CurrentRepoGateway(db)).remove(id, false);
		check("ModelLessonDelete.remove(id, No)", false, deleted, !deleted);
		LessonResult result = db.deleteLesson(id, alice, false);
		check("Database.deleteLesson unconfirmed", LessonResult.CANCELLED, result,
				result == LessonResult.CANCELLED);
		check("lesson still stored", "present", db.getLesson(id) == null ? "missing" : "present",
				db.getLesson(id) != null);
	}


	/*
	 * CR-07: no deletion with any lock
	 */

	/*****
	 * <p> Test CR-07 T1 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-07, a lesson with one locked field cannot be deleted, even
	 * when the Contributor confirms. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks only the Situation; Alice calls
	 * Database.deleteLesson with confirmed = true. </p>
	 *
	 * <p> <b>How to read the output:</b> expect LOCKED and the lesson still present. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR07, number = 1,
			kind = RequirementTest.Kind.NEGATIVE, title = "A field lock prevents a confirmed delete")
	private static void cr07FieldLockPreventsDelete() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setFieldLock(id, "Situation", true));
		LessonResult result = db.deleteLesson(id, alice, true);
		check("confirmed delete with a Situation lock", LessonResult.LOCKED, result,
				result == LessonResult.LOCKED);
		check("lesson still stored", "present", db.getLesson(id) == null ? "missing" : "present",
				db.getLesson(id) != null);
	}

	/*****
	 * <p> Test CR-07 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-07, the Delete window's model also refuses a locked lesson,
	 * so the GUI cannot report a deletion that the rules forbid. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks the whole lesson; the gateway's view of it is
	 * checked for deletionLocked, and ModelLessonDelete.remove is called with Yes. </p>
	 *
	 * <p> <b>How to read the output:</b> expect deletionLocked = true, then a refusal that says the
	 * lesson is locked. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR07, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "The Delete window refuses a locked lesson")
	private static void cr07DeleteModelRefusesLockedLesson() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setLessonLock(id, true));
		CurrentRepoGateway gateway = new CurrentRepoGateway(db);
		boolean locked = gateway.read(id).deletionLocked();
		check("gateway reports deletionLocked", true, locked, locked);
		refused("ModelLessonDelete.remove(id, Yes)", "refused (locked)",
				() -> new ModelLessonDelete(gateway).remove(id, true));
	}

	/*****
	 * <p> Test CR-07 T3 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-07, deletion is blocked only while a lock exists; after the
	 * Curator removes it, a confirmed delete succeeds. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks and unlocks the Title; Alice deletes with
	 * confirmed = true. </p>
	 *
	 * <p> <b>How to read the output:</b> expect SUCCESS and the lesson gone. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR07, number = 3,
			kind = RequirementTest.Kind.POSITIVE, title = "After unlocking, a confirmed delete succeeds")
	private static void cr07UnlockedLessonCanBeDeleted() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setFieldLock(id, "Title", true));
		asCurator(alice, () -> db.setFieldLock(id, "Title", false));
		LessonResult result = db.deleteLesson(id, alice, true);
		check("confirmed delete after unlock", LessonResult.SUCCESS, result,
				result == LessonResult.SUCCESS);
		check("lesson after delete", "null (gone)", db.getLesson(id), db.getLesson(id) == null);
	}


	/*
	 * CR-08: curator-only information is hidden
	 */

	/*****
	 * <p> Test CR-08 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-08, other roles can add information to a Contributor's
	 * lesson (this proves the hidden information really exists for the negative tests). </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator stores private notes and search terms on Alice's
	 * lesson with Database.setCuratorData and reads the notes back. </p>
	 *
	 * <p> <b>How to read the output:</b> expect the Curator to read back the exact notes. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR08, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "A Curator can store and read private notes")
	private static void cr08CuratorStoresPrivateNotes() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		signIn(CURATOR);
		db.setCuratorData(id, "PRIVATE-NOTE-CR08", "merge, branches");
		String notes = db.getCuratorNotes(id);
		check("Curator reads the notes", "PRIVATE-NOTE-CR08", notes,
				"PRIVATE-NOTE-CR08".equals(notes));
	}

	/*****
	 * <p> Test CR-08 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-08, the Contributor cannot see the curator's information.
	 * </p>
	 *
	 * <p> <b>How it is checked:</b> after the Curator adds notes, Alice calls getCuratorNotes, and
	 * every text field of her lesson and of the GUI gateway's view is searched for the note. </p>
	 *
	 * <p> <b>How to read the output:</b> expect a refusal, and "not found" for the note text. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR08, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "The Contributor cannot see curator notes")
	private static void cr08ContributorCannotSeeNotes() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> {
			db.setCuratorData(id, "PRIVATE-NOTE-CR08", "hidden-search-term");
			return LessonResult.SUCCESS;
		});
		refused("Contributor calls getCuratorNotes", "refused (Curator or Admin only)",
				() -> db.getCuratorNotes(id));
		LessonsLearned lesson = db.getLesson(id);
		String visible = lesson.getTitle() + lesson.getCategory() + lesson.getSituation()
				+ lesson.getLessonText() + new CurrentRepoGateway(db).read(id);
		boolean leaked = visible.contains("PRIVATE-NOTE-CR08") || visible.contains("hidden-search-term");
		check("private text in the Contributor's view", "not found",
				leaked ? "found" : "not found", !leaked);
	}

	/*****
	 * <p> Test CR-08 T3 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-08, the Contributor cannot alter the curator's
	 * information. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice calls setCuratorData on her own lesson; the Curator then
	 * reads the notes. </p>
	 *
	 * <p> <b>How to read the output:</b> expect a refusal and the Curator's original notes. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR08, number = 3,
			kind = RequirementTest.Kind.NEGATIVE, title = "The Contributor cannot change curator notes")
	private static void cr08ContributorCannotAlterNotes() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> {
			db.setCuratorData(id, "ORIGINAL-NOTE", "terms");
			return LessonResult.SUCCESS;
		});
		refused("Contributor calls setCuratorData", "refused (Curator or Admin only)",
				() -> db.setCuratorData(id, "OVERWRITTEN", "terms"));
		signIn(CURATOR);
		String notes = db.getCuratorNotes(id);
		check("notes after the attempt", "ORIGINAL-NOTE", notes, "ORIGINAL-NOTE".equals(notes));
	}


	/*
	 * CR-09: the Contributor's own list
	 */

	/*****
	 * <p> Test CR-09 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-09, the Contributor's list holds every lesson they added,
	 * including one a Curator has edited. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds two lessons and the Curator marks one as edited;
	 * LessonsLearnedList.loadForContributor is called for Alice. </p>
	 *
	 * <p> <b>How to read the output:</b> expect 2 lessons, one of them flagged curator-edited. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR09, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "My Lessons holds all own lessons, edited ones too")
	private static void cr09OwnListIncludesEditedLessons() throws Exception {
		String alice = newContributor("alice");
		addLesson(alice, "Merge daily");
		int edited = addLesson(alice, "Review early");
		asCurator(alice, () -> db.setCuratorEdited(edited, true));
		LessonsLearnedList list = LessonsLearnedList.loadForContributor(db, alice);
		long editedCount = list.getLessons().stream().filter(LessonsLearned::isCuratorEdited).count();
		check("size and curator-edited count", "2 lessons, 1 edited",
				list.size() + " lessons, " + editedCount + " edited",
				list.size() == 2 && editedCount == 1);
	}

	/*****
	 * <p> Test CR-09 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-09, the list never contains other Contributors' lessons,
	 * and asking for another Contributor's list by name returns nothing. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice and Bob each add a lesson; while Alice is signed in, her
	 * list and Bob's list (by name) are requested. </p>
	 *
	 * <p> <b>How to read the output:</b> expect Alice's list to hold only her lesson and Bob's list
	 * to be empty. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR09, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "Other Contributors' lessons never appear")
	private static void cr09OtherContributorsExcluded() throws Exception {
		String alice = newContributor("alice"), bob = newContributor("bob");
		addLesson(bob, "Bob's lesson");
		addLesson(alice, "Alice's lesson");
		LessonsLearnedList own = db.getLessonsByOwner(alice);
		check("Alice's list", "1 lesson, owner " + alice,
				own.size() + " lesson(s), owner " + (own.isEmpty() ? "-" : own.get(0).getOwner()),
				own.size() == 1 && alice.equals(own.get(0).getOwner()));
		int bobs = db.getLessonsByOwner(bob).size();
		check("Bob's list requested by Alice", 0, bobs, bobs == 0);
	}


	/*
	 * CR-10: lock state is visible when examining a lesson
	 */

	/*****
	 * <p> Test CR-10 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-10, the lesson the Contributor examines tells which field
	 * is locked and which are editable. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks the Lesson Text; Alice reads the lesson
	 * through the GUI gateway that View/Edit Lesson use. </p>
	 *
	 * <p> <b>How to read the output:</b> expect Lesson Text locked and the other three fields and
	 * the lesson itself unlocked. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR10, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "The view shows exactly which field is locked")
	private static void cr10ViewShowsLockedField() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setFieldLock(id, "Lesson", true));
		LessonGateway.Lesson shown = new CurrentRepoGateway(db).read(id);
		String observed = "lessonText=" + shown.lessonTextLocked() + " title=" + shown.titleLocked()
				+ " category=" + shown.categoryLocked() + " situation=" + shown.situationLocked()
				+ " lesson=" + shown.lessonLocked();
		check("lock flags in View Lesson's data",
				"lessonText=true title=false category=false situation=false lesson=false", observed,
				shown.lessonTextLocked() && !shown.titleLocked() && !shown.categoryLocked()
						&& !shown.situationLocked() && !shown.lessonLocked());
	}

	/*****
	 * <p> Test CR-10 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-10, only other roles set locks: the Contributor cannot
	 * remove a lock, either by calling the lock API or by submitting a lesson with the lock
	 * flags cleared. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator locks the Title; Alice calls setFieldLock(false),
	 * then submits an update whose lesson object has every lock flag false and a new Title. </p>
	 *
	 * <p> <b>How to read the output:</b> expect UNAUTHORIZED, then LOCKED, and the Title still
	 * locked. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR10, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "The Contributor cannot unlock a field")
	private static void cr10ContributorCannotUnlock() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setFieldLock(id, "Title", true));
		LessonResult unlock = db.setFieldLock(id, "Title", false);
		check("Contributor calls setFieldLock(Title, false)", LessonResult.UNAUTHORIZED, unlock,
				unlock == LessonResult.UNAUTHORIZED);
		LessonsLearned forged = db.getLesson(id);
		forged.setTitleLocked(false);
		forged.setTitle("Renamed while locked");
		LessonResult update = db.updateLesson(forged, alice);
		check("update with lock flags cleared in the request", LessonResult.LOCKED, update,
				update == LessonResult.LOCKED);
		check("Title lock after both attempts", true, db.getLesson(id).isTitleLocked(),
				db.getLesson(id).isTitleLocked());
	}


	/*
	 * CR-11: keyword search
	 */

	/*****
	 * <p> Test CR-11 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-11, a keyword search returns the Contributor's lessons that
	 * contain every keyword, ignoring upper and lower case. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds "Kanban board" and "Retrospective notes" (words
	 * that appear only in those titles); her list is searched for "KANBAN" and for
	 * "kanban retrospective". </p>
	 *
	 * <p> <b>How to read the output:</b> expect 1 match titled Kanban board, then 0 matches because
	 * no single lesson contains both words. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR11, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "Search finds own lessons containing every keyword")
	private static void cr11SearchFindsOwnMatches() throws Exception {
		String alice = newContributor("alice");
		addLesson(alice, "Kanban board");
		addLesson(alice, "Retrospective notes");
		LessonsLearnedList list = LessonsLearnedList.loadForContributor(db, alice);
		LessonsLearnedList one = list.search("KANBAN");
		check("search \"KANBAN\"", "1 match: Kanban board",
				one.size() + " match: " + (one.isEmpty() ? "-" : one.get(0).getTitle()),
				one.size() == 1 && "Kanban board".equals(one.get(0).getTitle()));
		int both = list.search("kanban retrospective").size();
		check("search \"kanban retrospective\" (both words required)", 0, both, both == 0);
	}

	/*****
	 * <p> Test CR-11 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-11, a search never returns another Contributor's lesson;
	 * a word found only in Bob's lesson gives an empty result, not null. </p>
	 *
	 * <p> <b>How it is checked:</b> Bob adds a lesson titled "Zebra stripes"; Alice's list is
	 * searched for "zebra". </p>
	 *
	 * <p> <b>How to read the output:</b> expect an empty list (size 0). </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR11, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "Search never returns another author's lesson")
	private static void cr11SearchExcludesOthers() throws Exception {
		String alice = newContributor("alice"), bob = newContributor("bob");
		addLesson(bob, "Zebra stripes");
		addLesson(alice, "Merge daily");
		LessonsLearnedList result = LessonsLearnedList.loadForContributor(db, alice).search("zebra");
		check("Alice searches for Bob's word", "empty list (size 0)",
				result == null ? "null" : "size " + result.size(), result != null && result.isEmpty());
	}


	/*
	 * CR-12: add experience information
	 */

	/*****
	 * <p> Test CR-12 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-12, experience information (what, how, duration, and each
	 * member's effort) is stored and linked to the lesson. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds an experience with duration 2.5 hours and members
	 * Ana 1.5 and Ben 1 hours, then reads the lesson's experiences. </p>
	 *
	 * <p> <b>How to read the output:</b> expect a new ID and one stored experience with exactly the
	 * entered text, duration and member hours. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR12, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "An experience with member hours is stored")
	private static void cr12ExperienceIsStored() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		long experienceId = db.addExperience(id, experience(id));
		check("addExperience returns an ID", "ID > 0", experienceId, experienceId > 0);
		List<LessonExperience> stored = db.getExperiences(id);
		LessonExperience e = stored.isEmpty() ? null : stored.get(0);
		boolean exact = e != null && e.durationHours().compareTo(new BigDecimal("2.5")) == 0
				&& e.efforts().size() == 2
				&& e.efforts().get(0).member().equals("Ana")
				&& e.efforts().get(0).hours().compareTo(new BigDecimal("1.5")) == 0
				&& e.efforts().get(1).hours().compareTo(BigDecimal.ONE) == 0
				&& e.what().equals("Reused the daily merge lesson");
		check("stored experience", "1 record: 2.5 h, Ana 1.5 h, Ben 1 h",
				e == null ? "none" : stored.size() + " record: " + e.durationHours() + " h, "
						+ e.efforts(), stored.size() == 1 && exact);
	}

	/*****
	 * <p> Test CR-12 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-12, experience can only be added to an existing lesson the
	 * Contributor owns. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds experience to a lesson ID that does not exist, then
	 * to Bob's lesson. </p>
	 *
	 * <p> <b>How to read the output:</b> expect both attempts to be refused as unavailable. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR12, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "Experience on a missing or foreign lesson is refused")
	private static void cr12ExperienceNeedsOwnExistingLesson() throws Exception {
		String alice = newContributor("alice"), bob = newContributor("bob");
		int bobs = addLesson(bob, "Bob's lesson");
		signIn(alice);
		refused("addExperience to lesson 999999", "refused (unavailable)",
				() -> db.addExperience(999999, experience(999999)));
		refused("addExperience to Bob's lesson", "refused (unavailable)",
				() -> db.addExperience(bobs, experience(bobs)));
	}

	/*****
	 * <p> Test CR-12 T3 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-12, the Add Experience window's rules turn the typed text
	 * and the member-and-hours table into the experience that is saved. </p>
	 *
	 * <p> <b>How it is checked:</b> Rules.experience is given the strings a user types: duration
	 * "0.75" and rows (Ana, "0.5") and (Ben, "0.25"). </p>
	 *
	 * <p> <b>How to read the output:</b> expect duration 0.75 and two members totaling 0.75 hours.
	 * </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR12, number = 3,
			kind = RequirementTest.Kind.POSITIVE, title = "The Add Experience form builds the record")
	private static void cr12FormBuildsExperience() {
		LessonGateway.Experience built = Rules.experience("Paired on the merge", "Shared screen",
				"0.75", List.of(new String[] {"Ana", "0.5"}, new String[] {"Ben", "0.25"}));
		BigDecimal total = built.efforts().stream().map(LessonGateway.Effort::hours)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		check("duration, members, total hours", "0.75, 2, 0.75",
				built.durationHours() + ", " + built.efforts().size() + ", " + total,
				built.durationHours().compareTo(new BigDecimal("0.75")) == 0
						&& built.efforts().size() == 2 && total.compareTo(new BigDecimal("0.75")) == 0);
	}


	/*
	 * CR-13: experience validation
	 */

	/*****
	 * <p> Test CR-13 T1 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-13, a blank "How it was done" is refused and nothing is
	 * stored. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice calls addExperience with How set to spaces, then counts
	 * the lesson's experiences. </p>
	 *
	 * <p> <b>How to read the output:</b> expect a refusal naming "How it was done" and 0 stored.
	 * </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR13, number = 1,
			kind = RequirementTest.Kind.NEGATIVE, title = "A blank How is refused and nothing is stored")
	private static void cr13BlankHowIsRefused() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		String message = refused("addExperience with a blank How", "refused naming How it was done",
				() -> db.addExperience(id, new LessonExperience(id, "Merged", "   ",
						BigDecimal.ONE, List.of(new LessonExperience.Effort("Ana", BigDecimal.ONE)))));
		check("message names the field", "contains \"How it was done\"", message,
				message.contains("How it was done"));
		check("experiences stored", 0, db.getExperiences(id).size(),
				db.getExperiences(id).isEmpty());
	}

	/*****
	 * <p> Test CR-13 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-13, the duration must be greater than zero hours. </p>
	 *
	 * <p> <b>How it is checked:</b> the Add Experience rules are given the typed duration "0", and
	 * the database is given a 0-hour duration directly. </p>
	 *
	 * <p> <b>How to read the output:</b> expect two refusals that say the duration must be greater
	 * than zero, and 0 stored. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR13, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "A zero duration is refused by form and database")
	private static void cr13ZeroDurationIsRefused() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		refused("form duration \"0\"", "refused (greater than zero)",
				() -> Rules.experience("Merged", "Daily", "0",
						List.<String[]>of(new String[] {"Ana", "1"})));
		refused("database duration 0", "refused (greater than zero)",
				() -> db.addExperience(id, new LessonExperience(id, "Merged", "Daily",
						BigDecimal.ZERO, List.of(new LessonExperience.Effort("Ana", BigDecimal.ONE)))));
		check("experiences stored", 0, db.getExperiences(id).size(),
				db.getExperiences(id).isEmpty());
	}

	/*****
	 * <p> Test CR-13 T3 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-13, each member appears once (ignoring case), and total
	 * effort must be positive. </p>
	 *
	 * <p> <b>How it is checked:</b> addExperience is called with members "Ana" and "ana", then with
	 * one member who has 0 hours. </p>
	 *
	 * <p> <b>How to read the output:</b> expect two refusals and 0 stored. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR13, number = 3,
			kind = RequirementTest.Kind.NEGATIVE, title = "Duplicate members and zero total effort are refused")
	private static void cr13BadMemberRowsAreRefused() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		refused("members Ana and ana", "refused (each member once)",
				() -> db.addExperience(id, new LessonExperience(id, "Merged", "Daily", BigDecimal.ONE,
						List.of(new LessonExperience.Effort("Ana", BigDecimal.ONE),
								new LessonExperience.Effort("ana", BigDecimal.ONE)))));
		refused("one member with 0 hours", "refused (positive total effort)",
				() -> db.addExperience(id, new LessonExperience(id, "Merged", "Daily", BigDecimal.ONE,
						List.of(new LessonExperience.Effort("Ana", BigDecimal.ZERO)))));
		check("experiences stored", 0, db.getExperiences(id).size(),
				db.getExperiences(id).isEmpty());
	}

	/*****
	 * <p> Test CR-13 T4 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-13, valid limits are accepted: a member may record 0 hours
	 * when another member's hours are positive, and What may be exactly 2000 characters. </p>
	 *
	 * <p> <b>How it is checked:</b> addExperience is called with a 2000-character What and members
	 * Ana 0 hours and Ben 3 hours. </p>
	 *
	 * <p> <b>How to read the output:</b> expect a new ID and 1 stored experience. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR13, number = 4,
			kind = RequirementTest.Kind.POSITIVE, title = "Values at the valid limits are accepted")
	private static void cr13BoundaryExperienceIsAccepted() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		long experienceId = db.addExperience(id, new LessonExperience(id, "w".repeat(2000),
				"Daily", new BigDecimal("0.001"), List.of(
						new LessonExperience.Effort("Ana", BigDecimal.ZERO),
						new LessonExperience.Effort("Ben", new BigDecimal("3")))));
		check("addExperience at the limits", "ID > 0, 1 stored",
				experienceId + ", " + db.getExperiences(id).size() + " stored",
				experienceId > 0 && db.getExperiences(id).size() == 1);
	}


	/*
	 * CR-14: own content only
	 */

	/*****
	 * <p> Test CR-14 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-14, the Contributor still sees their own lesson after a
	 * Curator has edited it. </p>
	 *
	 * <p> <b>How it is checked:</b> the Curator marks Alice's lesson as edited; Alice reads it.
	 * </p>
	 *
	 * <p> <b>How to read the output:</b> expect the lesson readable, owned by Alice, and flagged
	 * curator-edited. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR14, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "Own lesson stays visible after a curator edit")
	private static void cr14OwnEditedLessonVisible() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		asCurator(alice, () -> db.setCuratorEdited(id, true));
		LessonsLearned lesson = db.getLesson(id);
		check("Alice reads her edited lesson", "owner " + alice + ", curatorEdited true",
				lesson == null ? "not visible"
						: "owner " + lesson.getOwner() + ", curatorEdited " + lesson.isCuratorEdited(),
				lesson != null && alice.equals(lesson.getOwner()) && lesson.isCuratorEdited());
	}

	/*****
	 * <p> Test CR-14 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-14, a Contributor cannot read another Contributor's lesson
	 * or its experience. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds a lesson with an experience; Bob signs in and asks
	 * for that lesson by ID through the Database and through the GUI gateway, then asks for its
	 * experiences. </p>
	 *
	 * <p> <b>How to read the output:</b> expect null from the Database and refusals from the
	 * gateway and the experience read. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR14, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "Another Contributor's lesson cannot be read")
	private static void cr14ForeignLessonCannotBeRead() throws Exception {
		String alice = newContributor("alice"), bob = newContributor("bob");
		int id = addLesson(alice, "Alice's lesson");
		db.addExperience(id, experience(id));
		signIn(bob);
		check("Bob calls getLesson on Alice's ID", "null", db.getLesson(id), db.getLesson(id) == null);
		refused("Bob opens it through the GUI gateway", "refused (unavailable)",
				() -> new CurrentRepoGateway(db).read(id));
		refused("Bob reads its experiences", "refused (unavailable)", () -> db.getExperiences(id));
	}

	/*****
	 * <p> Test CR-14 T3 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-14, a Contributor cannot change or delete another
	 * Contributor's lesson. </p>
	 *
	 * <p> <b>How it is checked:</b> Bob submits an update and a confirmed delete for Alice's
	 * lesson; Alice then reads it. </p>
	 *
	 * <p> <b>How to read the output:</b> expect NOT_OWNER twice and Alice's original text. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR14, number = 3,
			kind = RequirementTest.Kind.NEGATIVE, title = "Another Contributor's lesson cannot be changed")
	private static void cr14ForeignLessonCannotChange() throws Exception {
		String alice = newContributor("alice"), bob = newContributor("bob");
		int id = addLesson(alice, "Alice's lesson");
		signIn(bob);
		LessonResult update = db.updateLesson(new LessonsLearned(id, bob, "Taken over", "Testing",
				SITUATION, LESSON_TEXT), bob);
		LessonResult delete = db.deleteLesson(id, bob, true);
		check("Bob's update and delete", "NOT_OWNER, NOT_OWNER", update + ", " + delete,
				update == LessonResult.NOT_OWNER && delete == LessonResult.NOT_OWNER);
		signIn(alice);
		check("Alice's title afterwards", "Alice's lesson", db.getLesson(id).getTitle(),
				"Alice's lesson".equals(db.getLesson(id).getTitle()));
	}


	/*
	 * CR-15: signed-in Contributor role required
	 */

	/*****
	 * <p> Test CR-15 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-15, a signed-in Contributor is recognized by the GUI layer
	 * and can read their own lessons. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice signs in; getAuthenticatedContributorName and her list
	 * are read. </p>
	 *
	 * <p> <b>How to read the output:</b> expect her name and 1 lesson. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR15, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "A signed-in Contributor has access")
	private static void cr15SignedInContributorHasAccess() throws Exception {
		String alice = newContributor("alice");
		addLesson(alice, "Merge daily");
		String name = db.getAuthenticatedContributorName();
		int count = db.getLessonsByOwner(alice).size();
		check("session name and lesson count", alice + ", 1", name + ", " + count,
				alice.equals(name) && count == 1);
	}

	/*****
	 * <p> Test CR-15 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-15, after logout the lessons can no longer be read or
	 * changed. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds a lesson and the session is cleared (logout); the
	 * lesson is then read, updated, and deleted. </p>
	 *
	 * <p> <b>How to read the output:</b> expect null, UNAUTHORIZED, UNAUTHORIZED. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR15, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "After logout nothing can be read or changed")
	private static void cr15LogoutEndsAccess() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		LessonsLearned stored = db.getLesson(id);
		db.clearAuthenticatedSession();
		LessonResult update = db.updateLesson(withSituation(stored,
				"A change attempted after the user logged out."), alice);
		LessonResult delete = db.deleteLesson(id, alice, true);
		check("read, update, delete after logout", "null, UNAUTHORIZED, UNAUTHORIZED",
				db.getLesson(id) + ", " + update + ", " + delete,
				db.getLesson(id) == null && update == LessonResult.UNAUTHORIZED
						&& delete == LessonResult.UNAUTHORIZED);
	}

	/*****
	 * <p> Test CR-15 T3 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-15, an account without the Contributor role (a Viewer, or a
	 * Contributor whose role was removed) cannot use lesson operations. </p>
	 *
	 * <p> <b>How it is checked:</b> a Viewer-only account signs in and tries to add a lesson; then
	 * Alice's Contributor role is removed while she is signed in and she reads her lesson. </p>
	 *
	 * <p> <b>How to read the output:</b> expect UNAUTHORIZED for the Viewer and null for Alice.
	 * </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR15, number = 3,
			kind = RequirementTest.Kind.NEGATIVE, title = "No Contributor role means no access")
	private static void cr15RoleRequired() throws Exception {
		String viewer = "viewer" + (++accountNumber);
		register(viewer, false, false);
		signIn(viewer);
		LessonResult add = db.addLesson(
				new LessonsLearned(viewer, "Viewer lesson", "Testing", SITUATION, LESSON_TEXT));
		check("Viewer adds a lesson", LessonResult.UNAUTHORIZED, add,
				add == LessonResult.UNAUTHORIZED);
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		db.updateUserRole(alice, "Contributor", "false");
		check("Alice reads her lesson after losing the role", "null", db.getLesson(id),
				db.getLesson(id) == null);
	}


	/*
	 * CR-16: persistence after restart
	 */

	/*****
	 * <p> Test CR-16 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-16, a lesson, its lock state, its curator-edited flag, and
	 * its experience with exact member hours survive closing and restarting the database. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds a lesson and an experience; the Curator locks the
	 * Title and marks it edited; the database connection is closed and a new Database object
	 * opens the same file, as when the application restarts; Alice signs in again. </p>
	 *
	 * <p> <b>How to read the output:</b> expect the same title, Title lock, edited flag, and member
	 * hours Ana 1.5 / Ben 1 after the restart. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR16, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "Lesson, locks, flag and hours survive a restart")
	private static void cr16EverythingSurvivesRestart() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		db.addExperience(id, experience(id));
		asCurator(alice, () -> db.setFieldLock(id, "Title", true));
		asCurator(alice, () -> db.setCuratorEdited(id, true));
		restart();
		signIn(alice);
		LessonsLearned lesson = db.getLesson(id);
		List<LessonExperience> experiences = db.getExperiences(id);
		boolean same = lesson != null && "Merge daily".equals(lesson.getTitle())
				&& lesson.isTitleLocked() && lesson.isCuratorEdited() && experiences.size() == 1
				&& experiences.get(0).efforts().get(0).hours().compareTo(new BigDecimal("1.5")) == 0
				&& experiences.get(0).efforts().get(1).hours().compareTo(BigDecimal.ONE) == 0;
		check("after restart", "Merge daily, titleLocked, curatorEdited, Ana 1.5 / Ben 1",
				lesson == null ? "lesson missing" : lesson.getTitle() + ", titleLocked "
						+ lesson.isTitleLocked() + ", curatorEdited " + lesson.isCuratorEdited()
						+ ", " + (experiences.isEmpty() ? "no experience" : experiences.get(0).efforts()),
				same);
	}

	/*****
	 * <p> Test CR-16 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-16, what persists is the data, not the sign-in: after a
	 * restart nothing is visible until the Contributor signs in again, and a deleted lesson
	 * stays deleted. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds two lessons and deletes one; the database restarts;
	 * the kept lesson is read before and after Alice signs in, and her list is counted. </p>
	 *
	 * <p> <b>How to read the output:</b> expect null before sign-in, then the kept lesson and a
	 * list of 1. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR16, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "Restart needs a new sign-in; deletions persist")
	private static void cr16RestartKeepsDeletionsAndNeedsSignIn() throws Exception {
		String alice = newContributor("alice");
		int kept = addLesson(alice, "Kept lesson");
		int gone = addLesson(alice, "Deleted lesson");
		db.deleteLesson(gone, alice, true);
		restart();
		check("read before signing in again", "null", db.getLesson(kept), db.getLesson(kept) == null);
		signIn(alice);
		int count = db.getLessonsByOwner(alice).size();
		check("after sign-in: kept lesson and list size", "Kept lesson, 1",
				(db.getLesson(kept) == null ? "missing" : db.getLesson(kept).getTitle()) + ", " + count,
				db.getLesson(kept) != null && db.getLesson(gone) == null && count == 1);
	}

	/** Closes the database and opens the same file again, as an application restart does. */
	private static void restart() throws Exception {
		db.closeConnection();
		db = new Database(url);
		db.connectToDatabase();
	}


	/*
	 * CR-17: list class CRUD, empty or any size
	 */

	/*****
	 * <p> Test CR-17 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-17, a new Contributor's list is empty (not null), and a
	 * lesson added through the list is in both the list and the database. </p>
	 *
	 * <p> <b>How it is checked:</b> LessonsLearnedList.loadForContributor is called for a new
	 * Contributor; then LessonsLearnedList.addLesson adds a lesson. </p>
	 *
	 * <p> <b>How to read the output:</b> expect size 0, then list size 1 and the lesson readable
	 * from the database. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR17, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "An empty list grows by one through the list class")
	private static void cr17EmptyListThenAdd() throws Exception {
		String alice = newContributor("alice");
		signIn(alice);
		LessonsLearnedList list = LessonsLearnedList.loadForContributor(db, alice);
		check("new Contributor's list", "not null, size 0",
				list == null ? "null" : "not null, size " + list.size(), list != null && list.isEmpty());
		LessonsLearned lesson = new LessonsLearned(alice, "Merge daily", "Process", SITUATION,
				LESSON_TEXT);
		LessonResult result = list.addLesson(db, lesson);
		check("add through the list", "SUCCESS, list 1, stored",
				result + ", list " + list.size() + ", "
						+ (db.getLesson(lesson.getLessonId()) == null ? "not stored" : "stored"),
				result == LessonResult.SUCCESS && list.size() == 1
						&& db.getLesson(lesson.getLessonId()) != null);
	}

	/*****
	 * <p> Test CR-17 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-17, a refused change alters neither the list nor the
	 * database. </p>
	 *
	 * <p> <b>How it is checked:</b> an invalid lesson (title "Bad") is added through the list, and
	 * a delete through the list is cancelled. </p>
	 *
	 * <p> <b>How to read the output:</b> expect INVALID_INPUT and CANCELLED with the list and the
	 * database still holding exactly 1 lesson. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR17, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "Refused list changes alter neither list nor database")
	private static void cr17RefusedChangesAlterNothing() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		LessonsLearnedList list = LessonsLearnedList.loadForContributor(db, alice);
		LessonResult add = list.addLesson(db,
				new LessonsLearned(alice, "Bad", "Process", SITUATION, LESSON_TEXT));
		LessonResult delete = list.deleteLesson(db, id, alice, false);
		int stored = db.getLessonsByOwner(alice).size();
		check("invalid add, cancelled delete, sizes", "INVALID_INPUT, CANCELLED, list 1, db 1",
				add + ", " + delete + ", list " + list.size() + ", db " + stored,
				add == LessonResult.INVALID_INPUT && delete == LessonResult.CANCELLED
						&& list.size() == 1 && stored == 1);
	}

	/*****
	 * <p> Test CR-17 T3 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-17, the list's contents are protected: code that receives
	 * the lessons cannot add to them behind the list's back. </p>
	 *
	 * <p> <b>How it is checked:</b> getLessons() is called and an add is attempted on the result.
	 * </p>
	 *
	 * <p> <b>How to read the output:</b> expect UnsupportedOperationException and the size
	 * unchanged. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR17, number = 3,
			kind = RequirementTest.Kind.NEGATIVE, title = "The returned lessons cannot be modified")
	private static void cr17ContentsAreProtected() throws Exception {
		String alice = newContributor("alice");
		addLesson(alice, "Merge daily");
		LessonsLearnedList list = LessonsLearnedList.loadForContributor(db, alice);
		String observed;
		try {
			list.getLessons().add(new LessonsLearned());
			observed = "add allowed";
		} catch (UnsupportedOperationException e) {
			observed = "UnsupportedOperationException";
		}
		check("add to getLessons()", "UnsupportedOperationException, size 1",
				observed + ", size " + list.size(),
				observed.equals("UnsupportedOperationException") && list.size() == 1);
	}


	/*
	 * CR-18: experience update and delete
	 */

	/*****
	 * <p> Test CR-18 T1 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-18, an experience can be updated and keeps its ID. </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds an experience, updates it to duration 4 hours with
	 * one member, and reads it back. </p>
	 *
	 * <p> <b>How to read the output:</b> expect the same ID, duration 4, and one member row. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR18, number = 1,
			kind = RequirementTest.Kind.POSITIVE, title = "An experience update keeps its ID")
	private static void cr18UpdateKeepsIdentity() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		long experienceId = db.addExperience(id, experience(id));
		db.updateExperience(experienceId, id, new LessonExperience(id, "Updated what",
				"Updated how", new BigDecimal("4"),
				List.of(new LessonExperience.Effort("Ana", new BigDecimal("4")))));
		LessonExperience e = db.getExperiences(id).get(0);
		check("after update", "ID " + experienceId + ", 4 h, 1 member",
				"ID " + e.id() + ", " + e.durationHours() + " h, " + e.efforts().size() + " member",
				e.id() == experienceId && e.durationHours().compareTo(new BigDecimal("4")) == 0
						&& e.efforts().size() == 1);
	}

	/*****
	 * <p> Test CR-18 T2 (negative) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-18, an unconfirmed experience delete keeps the record, and
	 * a whole-lesson lock blocks experience updates. </p>
	 *
	 * <p> <b>How it is checked:</b> deleteExperience is called with confirmed = false; then the
	 * Curator locks the lesson and Alice tries updateExperience. </p>
	 *
	 * <p> <b>How to read the output:</b> expect false with 1 record kept, then a refusal. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR18, number = 2,
			kind = RequirementTest.Kind.NEGATIVE, title = "Unconfirmed delete and locked update change nothing")
	private static void cr18UnconfirmedAndLockedChangeNothing() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		long experienceId = db.addExperience(id, experience(id));
		boolean deleted = db.deleteExperience(experienceId, id, false);
		check("unconfirmed delete", "false, 1 kept",
				deleted + ", " + db.getExperiences(id).size() + " kept",
				!deleted && db.getExperiences(id).size() == 1);
		asCurator(alice, () -> db.setLessonLock(id, true));
		refused("update while the lesson is locked", "refused (lesson is locked)",
				() -> db.updateExperience(experienceId, id, experience(id)));
	}

	/*****
	 * <p> Test CR-18 T3 (positive) </p>
	 *
	 * <p> <b>Requirement tested:</b> CR-18, a confirmed experience delete removes only that record.
	 * </p>
	 *
	 * <p> <b>How it is checked:</b> Alice adds two experiences and deletes the first with
	 * confirmed = true. </p>
	 *
	 * <p> <b>How to read the output:</b> expect true and exactly 1 remaining experience, the second
	 * one. </p>
	 */
	@RequirementTest(requirement = ContributorRequirement.CR18, number = 3,
			kind = RequirementTest.Kind.POSITIVE, title = "A confirmed delete removes only that experience")
	private static void cr18ConfirmedDeleteRemovesOne() throws Exception {
		String alice = newContributor("alice");
		int id = addLesson(alice, "Merge daily");
		long first = db.addExperience(id, experience(id));
		long second = db.addExperience(id, experience(id));
		boolean deleted = db.deleteExperience(first, id, true);
		List<LessonExperience> left = db.getExperiences(id);
		check("confirmed delete of the first", "true, 1 left (ID " + second + ")",
				deleted + ", " + left.size() + " left" + (left.isEmpty() ? "" : " (ID " + left.get(0).id() + ")"),
				deleted && left.size() == 1 && left.get(0).id() == second);
	}
}
