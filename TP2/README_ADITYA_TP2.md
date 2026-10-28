# TP2: Aditya Gupta (agupt515 and agupt545)

## agupt515: requirements coverage, integration, testing

| Output | Where |
|---|---|
| Test Javadoc template (shared on day 1) | `ContributorTests_Javadocs/Test_Javadoc_Template.html` |
| Task 6 / 7.1: every Contributor requirement with an owner, mapped to 2+ positive and negative tests, with Javadoc HTML and screenshot names | `ContributorTests_Javadocs/Contributor_Requirements_and_Tests.html` (and `.csv`) |
| Task 7.2 / 7.3: requirement-driven testbed, one Javadoc per test | `src/testingAutomation/ContributorRequirementsTestbed.java`, `ContributorRequirement.java`, `RequirementTest.java` |
| Task 7.4: screenshot of each test's code and comments | `ContributorTests_Javadocs/source-screenshots/CR-*_7.4_T*_*.png` (50 files) |
| Manual tests | `ContributorTests_Javadocs/Manual_Tests.html` |
| Actual test output | `ContributorTests_Javadocs/results/` |

The testbed covers 18 requirements (CR-01 to CR-18, from Contributor stories C1-C4) with 50 tests and 89 checks against the real Database, entity classes, and the GUI's non-visual layer (gateway, Rules, delete model). It uses a temporary database, so it never touches real data. A refusal only counts as a pass when its message gives the expected reason, such as "locked" or "unavailable". It fails, and exits with status 1, if any test fails or if any requirement lacks a passing positive and a passing negative test. As a check that the testbed catches real bugs, temporarily removing the field-lock condition from `LessonStore.delete` makes CR-07 T1 fail.

Run it:

- Eclipse: run `testingAutomation.ContributorRequirementsTestbed` as a Java Application.
- Everything (this testbed plus all 13 other team suites): from `TP2`, `python3 setup_dependencies.py` once, then `python3 dev.py test`.
- Regenerate Javadoc, screenshots, and lists: `python3 document.py`.

### Integration log (merge order: database, entity, GUI, tests)

| Date | Merged | Result |
|---|---|---|
| 2026-10-27 | Joshua's `main` (f5cb25e: list class, My Lessons, list tests), Kanish's `kanish-tp2-gui` (7bf7283: Contributor windows), and Vishwam's `vishwam-tp2-backend` (85cff20: database, entity, experience, integration of both). Vishwam's branch already contains the other two, so branch `aditya-tp2` starts from it and adds the testbed. | Build is clean. All 14 suites pass: the 13 existing suites plus the new testbed with 50 tests. Persistence after a restart is checked by CR-16 T1/T2, VG08, and LT-26/27, TC-23. The packaged zip, unzipped into a fresh folder, also passes all 14 suites. |
| 2026-10-28 | Kanish's commit 20744f9, pushed straight to `main` (completes Add Experience GUI). Merged into `aditya-tp2`; the only conflicts were in generated test evidence, which was regenerated. | The merged code had one failure, and it was already on `main`: `CurrentRepoWindowTests` failed `KG04_UI_LockedExperienceIsReadOnly` on macOS (DEF-03). After the fix, all 14 suites pass, Eclipse's own compiler (ECJ 3.46) builds `src` with no errors, and the testbed passes when run like Eclipse runs it. |

### Defect log

| ID | Found | Defect | Owner | Status |
|---|---|---|---|---|
| DEF-01 | 2026-10-27 | `guiAddLesson` and `guiLessonDetail` (Joshua's first Add/Detail windows) are never opened. My Lessons now opens Kanish's `guiLessonCreate` and `guiLessonDetails` windows. Both sets appear in `Contributor_Javadocs`, so graders see two View Lesson implementations. | Joshua Luther (decide with Kanish) | Open: remove the unused packages, or document them as superseded before packaging |
| DEF-02 | 2026-10-27 | View Lesson never lists the experience records a Contributor added, so the only GUI evidence of a save is the "Experience #N saved" message. Story C3 asks only to add experience, so this is a usability gap, not a failed requirement. | Kanish Garg | Open, for team decision |
| DEF-03 | 2026-10-28 | Add Experience did not lock its form on a whole-locked lesson on macOS. `ViewExperienceAdd.populate` found the member buttons with `Scene.lookup`, which returns null inside a `ScrollPane` before its skin exists. The NullPointerException skipped the code that locks the form. Fixed by keeping direct references to the three buttons. | Kanish Garg | Fixed by agupt515 (integration) on 2026-10-28; Kanish to review |

On 2026-10-27, no test failures were found in the integrated code. My only failure (CR-11 T1) was a fixture mistake in my own test: the shared lesson text contained the search word. I fixed it before committing.

## agupt545: AI coaching, planning evidence, final packaging

| Output | Where | Regenerate |
|---|---|---|
| Task 5: AICoaching.PDF (10 fields, two per member, prioritized, with free tools and checked citation URLs) | `AICoaching.PDF` | `python3 planning/make_ai_coaching_pdf.py` |
| Task board (owner, status, planned Done, done / next / blockers) | `TP2 Planning and Tracking/TP2 Task Board.csv` | edit after each standup |
| Task 8.1: Plan and Status PDF per standup | `TP2 Planning and Tracking/Plan and Status 2026-10-27.pdf` | `python3 planning/make_plan_and_status.py YYYY-MM-DD` |
| Summary-video rotation | `TP2 Planning and Tracking/Summary Video Rotation.csv` | edit |
| Task 1: final zip with all checks | `build/package/TP2-Team53.zip` | `python3 planning/package_tp2.py` |

The PDF scripts need `pip install reportlab`.

Still needed from people, as reported by `package_tp2.py` today: `TP2 Team Norms.pdf`, the three screencasts, and five `Status Summary YYYY-MM-DD.mp4` videos. Joshua should also confirm or replace the two AICoaching sections drafted for him (situation, category). Planned dates on the board and in the rotation are proposals; change them to match the real due date.
