# Recording outlines: Aditya Gupta

These are outlines, not recordings. Run `python3 setup_dependencies.py` once beforehand.

## TestingScreencast (Task 9.3), agupt515 part, about 6 minutes, running every test

1. **0:00-0:45 Requirements list.** Open `ContributorTests_Javadocs/Contributor_Requirements_and_Tests.html`. Explain that it covers 18 Contributor requirements from stories C1-C4, each with an owner and two or more tests, at least one positive and one negative. Point out the Javadoc link and screenshot name on one row.
2. **0:45-1:30 Template and Javadoc.** Open `Test_Javadoc_Template.html`, then the Javadoc for `cr04LockedFieldCannotChange()`: requirement tested, how it is checked, how to read the output. Then open its screenshot `CR-04_7.4_T2_...png`.
3. **1:30-3:00 Run the testbed in Eclipse.** Run `testingAutomation.ContributorRequirementsTestbed`. Scroll through one positive test (CR-01 T1) and one negative test (CR-07 T1, a field lock blocks a confirmed delete). Read the expected and observed lines. End on the summary table: 50 passed, 0 failed, "Requirement coverage: complete".
4. **3:00-5:15 Run every team test.** In a terminal in `TP2`, run `python3 dev.py test`. Narrate while the 14 suites run: the requirements testbed, Vishwam's backend and list-snapshot tests, Kanish's model, integration and real-window tests (windows open briefly), Joshua's lesson and list tests, and the TP1 regression suites. Show the final "All 14 suites passed."
5. **5:15-6:00 Manual tests and restart.** Open `Manual_Tests.html` and show M-16 (restart) and its automated equivalent CR-16 T1, which closes and reopens the database. Close by mentioning the defect log in `README_ADITYA_TP2.md`.

## PhaseTwoGUIScreencast (Task 9.2), agupt545 part, about 4 minutes

Run `python3 dev.py demo`, which creates a separate demo database. The demo-only password is printed on the first run and is also listed in `README_VISHWAM_TP2.md`. Kanish presents the windows; this part covers the flows below:

1. **0:00-1:00 Experience.** Sign in as `akim` and open his lesson. Choose Add experience and enter What and How, Duration `2.5`, and members Ana `1.5` and Ben `1`, pressing Add or update member for each. Press Save experience to show "Experience #N saved for this lesson." Then show one validation error: Duration `0`.
2. **1:00-2:00 Locks.** Sign in as `jsmith`. Open "Merge conflicts right before the deadline": the Title and Lesson Text fields are labeled "- Locked", and Delete is disabled with its explanation. Open "Record the screencast after the tests pass" to show the whole-lesson lock: Edit, Add experience and Delete are all disabled.
3. **2:00-3:00 List and search.** In My Lessons / Search, point out the Locked and Edited by curator tags. Search `JavaFX` to show one match, then `runLater` to show "No lessons match your search." Only akim's lesson contains that word.
4. **3:00-4:00 Delete and logout.** As `akim`, press Delete lesson on an unlocked lesson and answer "No, keep lesson". Then delete it with "Yes, delete lesson" and show it gone from My Lessons. Log out, and show that signing in as another Contributor shows only that person's lessons.
