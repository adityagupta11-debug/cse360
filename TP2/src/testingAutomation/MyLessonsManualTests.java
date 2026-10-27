package testingAutomation;

import java.util.Scanner;

/*******
 * <p> Title: MyLessonsManualTests Class. </p>
 *
 * <p> Description: A semi-automated driver for the manual tests of the contributor lesson pages
 * (My Lessons, Add Lesson, and Lesson Detail).  These behaviors can only be seen by using the
 * GUI, so this class does not test them itself.  Instead it prints each test's setup, steps, and
 * expected result; the tester performs the steps in the running application and answers whether
 * the result matched; and the class tallies the results the same way the automated drivers do.
 * </p>
 *
 * <p> <b>Requirements tested</b> (from the Contributor user stories):</p>
 * <ul>
 * <li>GUI-1: My Lessons shows only the contributor's own lessons, with "Locked" and "Edited by
 *     curator" tags (Contributor stories 2 and 4).</li>
 * <li>GUI-2: My Lessons search narrows the list, reports the count, and explains an empty
 *     result.</li>
 * <li>GUI-3: Add Lesson stores a valid lesson and refuses invalid input with a field-specific
 *     message (Contributor story 1).</li>
 * <li>GUI-4: Lesson Detail saves valid edits; a refused edit changes nothing (Contributor story
 *     1, "I can see and manage it").</li>
 * <li>GUI-5: Lesson Detail deletes only after "Yes", with No as the default, and blocks editing
 *     locked fields and deleting a lesson with any lock (Contributor story 2).</li>
 * <li>GUI-6: Lessons added, edited, and deleted in the GUI persist after a restart.</li>
 * </ul>
 *
 * <p> <b>Setup:</b> create two Contributor accounts (for example, jsmith and akim), then run
 * LessonDemoData with the application closed to add the sample lessons.  Run this class in its
 * own console, run the application, and follow the prompts.</p>
 *
 * <p> <b>How to read the output:</b> each test prints its ID, requirement, setup, steps, and
 * expected result, then asks "Did the result match the expected result? (y/n)".  Answering y
 * records the test as passed and n as failed.  The summary at the end gives the totals; every
 * test should pass.</p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author Joshua Luther
 *
 * @version 1.00	2026-10-22	Initial version for TP2
 *
 */
public class MyLessonsManualTests {

	static int numPassed = 0;	// Tests the tester confirmed
	static int numFailed = 0;	// Tests the tester did not confirm
	static Scanner keyboard = new Scanner(System.in);

	/**
	 * Default constructor is not used.
	 */
	public MyLessonsManualTests() {
	}

	/**
	 * Runs every manual test in order and prints the summary.
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		System.out.println("______________________________________________________________________");
		System.out.println("\nMy Lessons, Add Lesson, and Lesson Detail Manual Tests");
		System.out.println("Run LessonDemoData first (with the application closed), then start the");
		System.out.println("application and follow each test.  Answer y or n after each one.");

		testOwnLessonsWithTags();					// GUI-1
		testOtherContributorsHidden();
		testSearchNarrowsList();					// GUI-2
		testSearchNoMatchAndEmptyList();
		testAddValidLesson();						// GUI-3
		testAddInvalidLesson();
		testEditValidLesson();						// GUI-4
		testEditInvalidLesson();
		testDeleteAfterYes();						// GUI-5
		testDeleteDefaultsToNo();
		testLockedLessonProtected();
		testChangesSurviveRestart();				// GUI-6
		testDeletedLessonStaysDeleted();

		System.out.println("______________________________________________________________________");
		System.out.println("\nNumber of tests passed: " + numPassed);
		System.out.println("Number of tests failed: " + numFailed);
	}

	
	/**
	 * <p> Test MT-01 (GUI-1, positive): My Lessons shows the contributor's lessons and tags.</p>
	 * <p> How: the tester logs in as the first demo contributor and opens My Lessons; the page
	 * must show that contributor's three demo lessons with the correct tags.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testOwnLessonsWithTags() {
		performManualTest("MT-01", "GUI-1", "positive",
				"Demo data loaded.",
				new String[] {"Log in as jsmith (choose Contributor if asked).",
						"Click My lessons."},
				"\"3 lessons\" with: \"JavaFX app fails to launch in Eclipse\" (no tags); "
				+ "\"Merge conflicts right before the deadline\" (Locked and Edited by curator); "
				+ "\"Record the screencast after the tests pass\" (Locked).");
	}

	/**
	 * <p> Test MT-02 (GUI-1, negative): other contributors' lessons are never shown.</p>
	 * <p> How: the tester logs in as the second demo contributor, then as a contributor with no
	 * lessons; neither may see the first contributor's lessons.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testOtherContributorsHidden() {
		performManualTest("MT-02", "GUI-1", "negative",
				"Demo data loaded; a third Contributor account with no lessons exists.",
				new String[] {"Log out, log in as akim, and click My lessons.",
						"Log out, log in as the contributor with no lessons, and click My lessons."},
				"akim sees only \"JavaFX scene does not update after a change\". The contributor "
				+ "with no lessons sees \"You have not added any lessons yet.\" and \"0 lessons\".");
	}

	/**
	 * <p> Test MT-03 (GUI-2, positive): search narrows the list and reports the count.</p>
	 * <p> How: the tester searches the first contributor's lessons and clears the search.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testSearchNarrowsList() {
		performManualTest("MT-03", "GUI-2", "positive",
				"Logged in as jsmith, on My Lessons.",
				new String[] {"Type merge in the search field and press Enter.",
						"Click Clear."},
				"After the search: only \"Merge conflicts right before the deadline\" and the text "
				+ "\"1 of 3 lessons match \"merge\"\". After Clear: all 3 lessons and \"3 lessons\". "
				+ "akim's JavaFX lesson never appears.");
	}

	/**
	 * <p> Test MT-04 (GUI-2, negative): a search with no matches, and opening with nothing
	 * selected.</p>
	 * <p> How: the tester searches for a word no lesson contains and checks Open lesson cannot be
	 * used without a selection.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testSearchNoMatchAndEmptyList() {
		performManualTest("MT-04", "GUI-2", "negative",
				"Logged in as jsmith, on My Lessons.",
				new String[] {"Search for kubernetes.",
						"Look at the Open lesson button.",
						"Click Clear."},
				"The list says \"No lessons match your search.\" with \"0 of 3 lessons match "
				+ "\"kubernetes\"\". Open lesson is grayed out and does nothing until a lesson "
				+ "is selected.");
	}

	/**
	 * <p> Test MT-05 (GUI-3, positive): a valid lesson is added.</p>
	 * <p> How: the tester fills every field with valid values and saves.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testAddValidLesson() {
		performManualTest("MT-05", "GUI-3", "positive",
				"Logged in as jsmith, on My Lessons.",
				new String[] {"Click Add lesson.",
						"Title: Test lesson from MT-05.  Category: Process.",
						"Situation and Lesson: any text of at least 20 characters.",
						"Click Save lesson."},
				"My Lessons opens with \"Lesson added.\" in green, \"4 lessons\", and the new "
				+ "lesson in the list.");
	}

	/**
	 * <p> Test MT-06 (GUI-3, negative): invalid input is refused with a specific message.</p>
	 * <p> How: the tester saves an empty form, then a form with a short title.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testAddInvalidLesson() {
		performManualTest("MT-06", "GUI-3", "negative",
				"Logged in as jsmith, on Add Lesson.",
				new String[] {"Click Save lesson with every field empty.",
						"Type Bug as the title and click Save lesson.",
						"Click Back to my lessons."},
				"First: \"Title is required.\" in red. Second: \"Title must be at least 5 "
				+ "characters.\" in red. Each time the cursor is in the Title field. The lesson "
				+ "count on My Lessons has not changed.");
	}

	/**
	 * <p> Test MT-07 (GUI-4, positive): a valid edit is saved.</p>
	 * <p> How: the tester opens an unlocked lesson, changes its title, and saves.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testEditValidLesson() {
		performManualTest("MT-07", "GUI-4", "positive",
				"Logged in as jsmith, on My Lessons.",
				new String[] {"Double-click \"Test lesson from MT-05\".",
						"Change the title to Edited lesson from MT-07 and click Save changes.",
						"Click Back to my lessons."},
				"\"Changes saved.\" in green on Lesson Detail. My Lessons lists the lesson with "
				+ "its new title.");
	}

	/**
	 * <p> Test MT-08 (GUI-4, negative): a refused edit changes nothing.</p>
	 * <p> How: the tester enters an invalid title, then leaves without saving and reopens the
	 * lesson.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testEditInvalidLesson() {
		performManualTest("MT-08", "GUI-4", "negative",
				"Logged in as jsmith, on My Lessons.",
				new String[] {"Open \"Edited lesson from MT-07\".",
						"Change the title to Bug and click Save changes.",
						"Click Back to my lessons, then open the lesson again."},
				"\"Title must be at least 5 characters.\" in red with the cursor in Title. After "
				+ "reopening, the title is still \"Edited lesson from MT-07\".");
	}

	/**
	 * <p> Test MT-09 (GUI-5, positive): Delete removes the lesson after "Yes".</p>
	 * <p> How: the tester deletes the test lesson and answers Yes.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testDeleteAfterYes() {
		performManualTest("MT-09", "GUI-5", "positive",
				"Logged in as jsmith, viewing \"Edited lesson from MT-07\".",
				new String[] {"Click Delete lesson.",
						"Click Yes."},
				"The dialog says \"Are you sure?\" and names the lesson. After Yes, My Lessons opens "
				+ "with \"Lesson deleted.\" in green, \"3 lessons\", and the lesson is gone.");
	}

	/**
	 * <p> Test MT-10 (GUI-5, negative): No is the default, and No or closing the dialog keeps the
	 * lesson.</p>
	 * <p> How: the tester opens Delete and presses Enter, then opens it again and closes it.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testDeleteDefaultsToNo() {
		performManualTest("MT-10", "GUI-5", "negative",
				"Logged in as jsmith, viewing \"JavaFX app fails to launch in Eclipse\".",
				new String[] {"Click Delete lesson and press Enter without clicking a button.",
						"Click Delete lesson again and close the dialog with its close button."},
				"No is highlighted as the default. Both times: \"Delete cancelled. The lesson was "
				+ "not changed.\" and the lesson is still there.");
	}

	/**
	 * <p> Test MT-11 (GUI-5, negative): locked fields cannot be edited and a locked lesson cannot
	 * be deleted.</p>
	 * <p> How: the tester opens the two locked demo lessons.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testLockedLessonProtected() {
		performManualTest("MT-11", "GUI-5", "negative",
				"Logged in as jsmith, on My Lessons.",
				new String[] {"Open \"Merge conflicts right before the deadline\".",
						"Try to type in each field.",
						"Back to my lessons; open \"Record the screencast after the tests pass\"."},
				"First lesson: Title and Lesson are gray, read-only, and say \"Locked by a curator\"; "
				+ "Category and Situation can be edited; \"Edited by curator\" shows; Delete is "
				+ "grayed out with a note explaining why. Second lesson: every field is locked, "
				+ "and Save changes and Delete lesson are both grayed out.");
	}

	/**
	 * <p> Test MT-12 (GUI-6, positive): lessons added and edited in the GUI survive a restart.</p>
	 * <p> How: the tester adds and edits a lesson, quits, restarts, and checks My Lessons.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testChangesSurviveRestart() {
		performManualTest("MT-12", "GUI-6", "positive",
				"Logged in as jsmith.",
				new String[] {"Add a lesson titled Persistence check lesson.",
						"Open it and change the title to Persistence check after edit; save.",
						"Click Quit. Start the application again and log in as jsmith.",
						"Click My lessons."},
				"\"Persistence check after edit\" is listed, with the edited title.");
	}

	/**
	 * <p> Test MT-13 (GUI-6, negative): a deleted lesson does not come back after a restart.</p>
	 * <p> How: the tester deletes a lesson, quits, restarts, and checks My Lessons.</p>
	 * <p> Output: the steps and expected result, then the tester's y/n answer.</p>
	 */
	static void testDeletedLessonStaysDeleted() {
		performManualTest("MT-13", "GUI-6", "negative",
				"Logged in as jsmith.",
				new String[] {"Open \"Persistence check after edit\", delete it, and answer Yes.",
						"Click Quit. Start the application again and log in as jsmith.",
						"Click My lessons."},
				"\"Persistence check after edit\" is not listed.");
	}

	
	/*
	 * Prints one test's details and records the tester's answer, in the same pass/fail format as
	 * the automated testing classes.  Only y or n is accepted, so a typo cannot be recorded as a
	 * result.
	 */
	private static void performManualTest(String id, String requirement, String kind,
			String setup, String[] steps, String expected) {
		System.out.println("______________________________________________________________________");
		System.out.println("\nTest " + id + "  [" + requirement + ", " + kind + "]");
		System.out.println("Setup: " + setup);
		for (int i = 0; i < steps.length; i++)
			System.out.println("  Step " + (i + 1) + ": " + steps[i]);
		System.out.println("Expected: " + expected);
		
		String answer = "";
		while (!answer.equals("y") && !answer.equals("n")) {
			System.out.print("Did the result match the expected result? (y/n): ");
			answer = keyboard.hasNextLine() ? keyboard.nextLine().trim().toLowerCase() : "n";
		}
		if (answer.equals("y")) {
			System.out.println("***Success*** The tester confirmed the expected result.");
			numPassed++;
		} else {
			System.out.println("***Failure*** The tester did NOT see the expected result.");
			numFailed++;
		}
	}
}