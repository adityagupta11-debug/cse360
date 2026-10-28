package testingAutomation;

/*******
 * <p> Title: ContributorRequirement Enumeration. </p>
 *
 * <p> Description: The single list of every TP2 Contributor (Phase Two) requirement, with its
 * owner, its source in the "Lessons Learned System V1 2026-08-19" user stories (C1 to C4 are the
 * four Contributor Phase Two stories in order), the teammates' tests that also cover it, and a
 * manual test procedure.  This is the Task 6 requirements-to-tests list in code form: each
 * constant is mapped to two or more tests in {@link ContributorRequirementsTestbed}, at least
 * one positive and one negative, through the {@link RequirementTest} annotation. </p>
 *
 * <p> Owners follow the TP2 Work Split: Vishwam Gupta (lesson entity, experience data,
 * persistence, validation), Kanish Garg (Contributor lesson and experience GUI), Joshua Luther
 * (lesson list class, search, list GUI), and Aditya Gupta agupt515 (requirements coverage,
 * integration, testing). </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author Aditya Gupta (agupt515)
 *
 * @version 1.00	2026-10-27	Initial version for TP2
 */
public enum ContributorRequirement {

	/** C1: a Contributor adds a lesson by entering its core information. */
	CR01("CR-01", "C1",
			"A Contributor can add a new lesson by entering its core information (Title, Category, "
			+ "Situation, Lesson Text). The stored lesson belongs to the signed-in Contributor.",
			"Vishwam Gupta",
			"VG01 (BackendTests); KG01 (ModelTests, CurrentRepoWindowTests); LT-10 (LIST-4)",
			"Sign in as newcontributor, open My Lessons / Search, press Add lesson, fill all "
			+ "four fields with valid values, and press Create lesson.",
			"View Lesson opens with \"Lesson created successfully.\" and the entered values; "
			+ "the lesson is listed in My Lessons."),

	/** C1 and TP2 input validation: invalid core information is refused. */
	CR02("CR-02", "C1",
			"Core information is validated: Title 5-100 characters, Category from the fixed list, "
			+ "Situation and Lesson Text 20-2000 characters. Invalid input is refused with a "
			+ "message that explains the fix, and nothing is stored.",
			"Vishwam Gupta",
			"VG01_InvalidCreateChangesNothing; KG05 (ModelTests); LT-11 (LIST-4); "
			+ "LessonTestingAutomation",
			"As newcontributor press Add lesson, enter the title \"Plan\" (4 characters) with "
			+ "the other fields valid, and press Create lesson; then change the title to "
			+ "\"Plans\" and press Create lesson again.",
			"The first save shows a title error, keeps the other inputs, and stores nothing; "
			+ "the second save succeeds."),

	/** C1: a Contributor can see and manage (update) a lesson they added. */
	CR03("CR-03", "C1",
			"A Contributor can update the unlocked core fields of a lesson they added. The change "
			+ "is stored; the lesson ID and owner do not change.",
			"Vishwam Gupta",
			"testKG01_UpdateValid; VG03 unlocked-field acceptance; LT-12 (LIST-5)",
			"Sign in as akim, open \"JavaFX scene does not update after a change\", choose "
			+ "Edit lesson, change the Situation, and press Save changes.",
			"View Lesson shows the new Situation with the same Lesson # and Author; the other "
			+ "fields are unchanged."),

	/** C1/C2: other roles may lock fields so the Contributor cannot alter them. */
	CR04("CR-04", "C1, C2",
			"A field locked by a Curator cannot be changed by the Contributor, while the lesson's "
			+ "other unlocked fields can still be changed.",
			"Vishwam Gupta",
			"VG03 per-field tests; KG03_TitleLock/CategoryLock/SituationLock/TextLock",
			"Sign in as jsmith, open \"Merge conflicts right before the deadline\" (Title and "
			+ "Lesson Text locked), choose Edit lesson, change only the Situation, and press "
			+ "Save changes.",
			"Title and Lesson Text are labeled \"- Locked\" and cannot be changed; the "
			+ "Situation change saves."),

	/** C1/C2: a whole-lesson lock prevents all Contributor changes. */
	CR05("CR-05", "C1, C2",
			"When a Curator locks the whole lesson, the Contributor cannot change any core field "
			+ "and cannot add experience information to it.",
			"Vishwam Gupta",
			"VG03 whole-lock tests; VG05 whole-lock experience rejection; KG03_FullLock",
			"Sign in as jsmith and open \"Record the screencast after the tests pass\" (whole "
			+ "lesson locked).",
			"The note reads \"Lesson locked. Editing and deletion are unavailable.\"; Edit "
			+ "lesson, Add experience and Delete lesson are disabled."),

	/** C1 and the TP1 destructive-action UX rule: deletion needs an explicit Yes. */
	CR06("CR-06", "C1",
			"A Contributor can delete an unlocked lesson they added only after answering Yes to an "
			+ "\"Are you sure?\" confirmation that names the lesson (No is the default). Deleting "
			+ "removes the lesson and its linked experience records.",
			"Kanish Garg",
			"KG07_DeleteNo/DeleteYes; VG04 cascade tests; LT-15, LT-16 (LIST-6)",
			"Sign in as akim, open his lesson, press Delete lesson and answer No, keep lesson; "
			+ "then press Delete lesson again and answer Yes, delete lesson. (Run after M-03 and "
			+ "M-12.)",
			"The confirmation names the lesson and No is the default; No, keep lesson returns to "
			+ "View Lesson and keeps the lesson; Yes, delete lesson returns to My Lessons, which "
			+ "no longer lists it."),

	/** C2: a lesson with any lock cannot be deleted. */
	CR07("CR-07", "C2",
			"A lesson that is locked, or that has any locked field, cannot be deleted by its "
			+ "Contributor, even after confirmation.",
			"Vishwam Gupta",
			"VG04 every field-lock deletion test; KG07_AnyLock",
			"Sign in as jsmith and open \"Merge conflicts right before the deadline\" (two "
			+ "locked fields).",
			"Delete lesson is disabled and the note reads \"Locked fields are read-only. Delete "
			+ "is unavailable while any field is locked.\""),

	/** C1: the Contributor cannot see or alter information added by other roles. */
	CR08("CR-08", "C1",
			"A Contributor cannot see or alter the additional information Curators add to a lesson "
			+ "(private notes and search terms).",
			"Vishwam Gupta",
			"VG09 (BackendTests); ListSnapshotTests private-text checks",
			"Sign in as jsmith and open \"Merge conflicts right before the deadline\" (edited "
			+ "by a curator); look through View Lesson and Edit lesson.",
			"Only the four core fields, lock labels, timestamps and \"Edited by curator\" "
			+ "appear; there is no curator notes or search-terms field. (Hidden stored notes are "
			+ "checked by testbed CR-08 T2.)"),

	/** C2/C4: the Contributor's list holds exactly their own lessons. */
	CR09("CR-09", "C2, C4",
			"A Contributor can see a list of all the lessons they have added, including lessons a "
			+ "Curator has since edited, and no lessons from other Contributors.",
			"Joshua Luther",
			"LT-04, LT-05, LT-06 (LIST-2); VG02 list tests",
			"Sign in as jsmith and open My Lessons.",
			"Exactly jsmith's three lessons are listed; \"Merge conflicts right before the "
			+ "deadline\" is tagged Locked and Edited by curator; akim's lesson is not listed."),

	/** C2: examining a lesson shows which fields are locked. */
	CR10("CR-10", "C2",
			"When a Contributor examines a lesson, they can see which fields they can alter and "
			+ "which are locked (the lock state is part of what they receive).",
			"Kanish Garg",
			"KG03 (ModelTests); CurrentRepoWindowTests.testRealStorageLocks; LT-24, LT-25 (LIST-8)",
			"Sign in as jsmith, open \"Merge conflicts right before the deadline\", and choose "
			+ "Edit lesson.",
			"View Lesson says locked fields are read-only; in Edit, Title and Lesson Text end "
			+ "with \"- Locked\" and cannot be changed, while Category and Situation can."),

	/** C2 list use and the work split's keyword-search subset. */
	CR11("CR-11", "C2",
			"A Contributor can search their own list by keywords; the result is the subset of their "
			+ "lessons containing every keyword (it may be empty), never another Contributor's.",
			"Joshua Luther",
			"LT-18 to LT-23 (LIST-7); CurrentRepoWindowTests search checks",
			"Sign in as jsmith, search My Lessons for \"JavaFX\", then search for \"runLater\" "
			+ "(a word only in akim's lesson).",
			"The first search lists only \"JavaFX app fails to launch in Eclipse\" (not akim's "
			+ "JavaFX lesson); the second shows \"No lessons match your search.\""),

	/** C3: experience information is added to an existing lesson. */
	CR12("CR-12", "C3",
			"A Contributor can add experience information to an existing lesson they own: what was "
			+ "done, how it was done, how long it took (hours), and the effort (hours) of each "
			+ "team member.",
			"Vishwam Gupta",
			"VG05 (BackendTests); KG04 (ModelTests); CurrentRepoWindowTests experience save",
			"Sign in as akim, open his lesson, choose Add experience, fill What and How, "
			+ "Duration 2.5, add members Ana 1.5 and Ben 1 with Add or update member, and press "
			+ "Save experience.",
			"View Lesson opens with \"Experience #N saved for this lesson.\" (View Lesson does "
			+ "not list experiences; the stored hours are checked by testbed CR-12 T1)."),

	/** C3 and TP2 input validation: invalid experience data is refused. */
	CR13("CR-13", "C3",
			"Experience input is validated: What and How are required (up to 2000 characters), "
			+ "duration must be a positive number of hours, each member appears once with "
			+ "non-negative hours, and total effort is positive. Invalid input stores nothing.",
			"Vishwam Gupta",
			"VG06 (thirteen invalid cases); KG04_MissingText/Hours/Members",
			"In Add experience, press Save experience with How blank; then enter Duration 0; "
			+ "then add the member Ana twice.",
			"Each attempt shows an error that names the problem, keeps the typed draft, and "
			+ "saves no experience."),

	/** C4: the Contributor only sees their own content. */
	CR14("CR-14", "C4",
			"A Contributor can only see the content they added (including after a Curator edits "
			+ "it); they cannot read, change, or delete other Contributors' lessons or experience.",
			"Vishwam Gupta",
			"VG02 foreign read/list/update/delete tests; KG02_Denied; LT-14 (LIST-5)",
			"Sign in as akim, open My Lessons, and search for \"Merge\".",
			"Only akim's own lesson is ever listed and the search shows \"No lessons match your "
			+ "search.\"; none of jsmith's lessons appear. (Opening another user's lesson by ID "
			+ "is checked by testbed CR-14 T2.)"),

	/** C1-C4 with the Foundations session rules. */
	CR15("CR-15", "C1-C4",
			"Lesson operations require a signed-in account with the Contributor role; after logout "
			+ "or removal of the role, lessons can no longer be read or changed.",
			"Vishwam Gupta",
			"VG01_NoSessionCreateRejected; VG02 logout and role-revocation tests; "
			+ "CurrentRepoIntegrationTests",
			"Sign in as jsmith, open My Lessons, press Logout, then sign in as akim and open My "
			+ "Lessons.",
			"Logout returns to the login screen; akim's My Lessons shows only akim's lessons, "
			+ "none of jsmith's."),

	/** TP2 persistence requirement checked at integration. */
	CR16("CR-16", "C1-C3",
			"Lessons, lock state, the curator-edited flag, and experience records (with exact member "
			+ "hours) persist after the application is closed and restarted.",
			"Aditya Gupta (agupt515)",
			"VG08 reopen tests; LT-26, LT-27 (LIST-9); TC-23 (LessonTestingAutomation)",
			"After M-01, press Quit, start the demo again with python3 dev.py demo (it reopens "
			+ "without reseeding), and sign in as newcontributor, then as jsmith.",
			"newcontributor's new lesson is still listed; jsmith's lessons keep their Locked "
			+ "and Edited by curator tags. (Stored experience hours after a restart are checked "
			+ "by testbed CR-16 T1.)"),

	/** Work split: LessonsLearnedList CRUD, empty or any size. */
	CR17("CR-17", "C2",
			"The lesson list class keeps the list and the database in step for add, update, and "
			+ "delete; a list can be empty or of any size, and a refused change alters neither.",
			"Joshua Luther",
			"LT-07 to LT-17 (LIST-3 to LIST-6)",
			"On a fresh demo database, before M-01, sign in as newcontributor and open My "
			+ "Lessons; then do M-01.",
			"First the list shows \"You have not added any lessons yet.\" and \"0 lessons\"; "
			+ "after M-01 the count reads \"1 lesson\"."),

	/** Work split extension: experience update and delete. */
	CR18("CR-18", "C3",
			"Experience records can be updated and deleted by their Contributor (team work-split "
			+ "extension of C3); deletion requires confirmation and a whole-lesson lock blocks both.",
			"Vishwam Gupta",
			"VG07 (BackendTests)",
			"No GUI procedure: the assigned GUI exposes only Add experience. Run the testbed "
			+ "and read the CR-18 lines.",
			"CR-18 T1 to T3 print PASS.");

	private final String id;
	private final String source;
	private final String statement;
	private final String owner;
	private final String teammateTests;
	private final String manualSteps;
	private final String manualExpected;

	ContributorRequirement(String id, String source, String statement, String owner,
			String teammateTests, String manualSteps, String manualExpected) {
		this.id = id;
		this.source = source;
		this.statement = statement;
		this.owner = owner;
		this.teammateTests = teammateTests;
		this.manualSteps = manualSteps;
		this.manualExpected = manualExpected;
	}

	/** @return the requirement ID, such as CR-01 */
	public String id() { return id; }

	/** @return the Contributor story or stories (C1-C4) this requirement comes from */
	public String source() { return source; }

	/** @return the requirement statement */
	public String statement() { return statement; }

	/** @return the team member who owns the requirement */
	public String owner() { return owner; }

	/** @return teammates' tests that also exercise this requirement */
	public String teammateTests() { return teammateTests; }

	/** @return the manual test steps for this requirement */
	public String manualSteps() { return manualSteps; }

	/** @return the expected result of the manual test */
	public String manualExpected() { return manualExpected; }
}
