# Local TP2 integration - Vishwam

Integrated Joshua, Kanish, Vishwam and Aditya's work through fetched `main` c552860, `kanish-tp2-gui` 20744f9 and `aditya-tp2` 148f470. The October 28 submission preparation adds current evidence, tracked norms, corrected coaching research and stricter packaging checks. See Verification_Integrated.json for current test totals; member-specific earlier files are historical snapshots.

## Implemented

- Retained Joshua's `LessonsLearned` / `LessonsLearnedList`, My Lessons/search page, lock columns and lock setters.
- Reconciled Kanish's older `Lesson`/`LessonList` references and connected his MVC create/detail/edit/delete/experience windows.
- Kept the home preview/actions and added a My Lessons/Search route to Joshua's latest list page. That list opens the same integrated windows.
- Added `PersistentAdditionalAccess` as the default real adapter. Add Experience is enabled and saves real data.
- Enforced authenticated Contributor ownership, role checks, whole/per-field locks and deletion protection in persistence, not only widgets. Account ownership uses stable IDs so renames or deleted/reused usernames cannot transfer records.
- Added transactional experience/effort CRUD, timestamp migrations and separate private curator metadata. Existing databases upgrade in place without deleting lessons.
- Added backend and concurrent-write tests; adapted old regression fixtures to register/sign in rather than silently bypass authorization.
- Added portable build/run/test/document commands and generated entity/GUI/test documentation and source screenshots.

## Build and run

Requires a JDK 21+ with `java`, `javac` and `javadoc` on PATH, plus Python 3. Local validation used the installed macOS ARM64 JDK 26.0.1 and project JavaFX 21.0.8 / H2 2.3.232. Compilation targets Java 21; the full suite was not independently run on a Windows host.

```sh
python3 setup_dependencies.py
python3 dev.py build
python3 dev.py run
```

Both `python3 dev.py run` and `run.ps1` use this project's `data/Team53` database. They therefore see the same saved accounts and lessons. Direct Eclipse launches must set `-Dcse360.db.url=jdbc:h2:./data/Team53` with TP2 as the working directory to use that same database. Tests use isolated temporary databases. `lib/` is platform-specific and intentionally ignored by Git; run setup on each teammate's computer. PowerShell scripts remain available, and `dev.py` provides the same commands across platforms.

```sh
python3 dev.py test
python3 document.py
```

Verification: **658 passing checks across 14 suites**, including 116 backend checks, 13 list-snapshot checks and 37 GUI checks, plus 89 checks in the 50-test Contributor requirements testbed. Actual per-suite output is included with the test Javadoc. JavaFX on the installed JDK prints an inherited deprecation warning; no shutdown exception occurred in the current runs.

Read `Backend_Requirements.md` for the contracts, validation rules, rationale and per-requirement evidence. Generated evidence is in the three assignment-named Javadoc folders.

## Isolated manual-test demo

```sh
python3 dev.py demo
```

This explicit action creates sample Contributor accounts `jsmith`, `akim`, and `newcontributor`, plus a stored-role Curator fixture, in `data/TP2Demo`. The demo-only password is `DemoPass1!`. It seeds the ordinary, field-locked, whole-locked, and curator-edited lesson states for the manual driver. The database is separate from normal application data. The demo command seeds only when `data/TP2Demo.mv.db` does not yet exist. Later `python3 dev.py demo` launches reopen it without reseeding, preserving edits and deletions for restart tests. To explicitly restore missing seed examples, close the demo and run the `testingAutomation.LessonDemoData` class from TP2; it operates only on TP2Demo and reuses matching records. It never deletes or modifies normal application databases.

## Important compatibility changes

- `Database.addLesson/getLesson/getLessonsByOwner/updateLesson/deleteLesson` now require a real authenticated Contributor session. Caller-supplied owner/actingUser strings are consistency checks, not authorization.
- `getAllLessons` returns the full set only for Admin/Curator; a Contributor receives only their own subset.
- Lock/curator-edit setters require an authenticated Admin/Curator. `python3 dev.py demo` explicitly seeds and opens a separate `data/TP2Demo` database through real authenticated APIs. Its sample accounts use the printed demo-only password. It never seeds the normal Team53 database or the old home-directory Foundations database.
- Kanish's fallback capability messages are retained for explicitly unconnected adapters; the default application now connects the real backend.
- Field locks block changes to those core fields and all lesson deletion. Whole-lesson locks also block experience mutations. This matches the existing GUI policy and is documented as a team decision for experience behavior.

## Remaining team/human deliverables

Record the actual lesson screencast segment and assigned standup summary. `RECORDING_NOTES_VISHWAM.md` is an outline, not a recording. Member 5 must incorporate the proposed What/How coaching research into the team report after confirming field allocations. Complete the required recordings, team packaging, peer evaluation and Canvas verification. This code integration is not a claim that the entire graded submission is ready.

## Readiness audit fixes

A fresh independent audit found and corrected gaps not covered by the first 530 checks: stable owner IDs now protect account rename/reuse; both normal launchers use the same project-local database; the home Browse button no longer overlaps rows; list CRUD retains canonical stored snapshots; all delivered lesson/list/manual test sources now have generated evidence. The isolated demo is distinct from normal application data, and the old contribution matrix no longer claims that connected backend features are missing.

Complete code verification is local to this Mac. A separate Windows/Eclipse execution and the human manual checklist/recordings are still unverified. This is ready for the team's code review and recording workflow, not a completed Canvas submission.

## Submission preparation on October 28

The three Javadoc folders now document the current integrated implementation and include all current test reports. Superseded guiAddLesson/guiLessonDetail packages are marked clearly in their package documentation; normal routing uses Kanish's active windows. TP2 Team Norms.docx retains actual Word revisions and its PDF shows them. AICoaching.PDF uses the supplied team compilation's field allocations, self-hosted LanguageTool and correct person-hour semantics. The package checker requires six matching dated PDF/video pairs, handles MP4/MOV durations and records subjective review in Media_Review.json. It intentionally reports NOT READY while recordings or their required content remain incomplete.
