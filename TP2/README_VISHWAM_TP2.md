# Local TP2 integration - Vishwam

Integrated locally from Joshua's `main` commit `f5cb25e` and Kanish's `kanish-tp2-gui` commit `7bf7283`. No push is part of this work.

## Implemented

- Retained Joshua's `LessonsLearned` / `LessonsLearnedList`, My Lessons/search page, lock columns and lock setters.
- Reconciled Kanish's older `Lesson`/`LessonList` references and connected his MVC create/detail/edit/delete/experience windows.
- Kept the home preview/actions and added a My Lessons/Search route to Joshua's latest list page. That list opens the same integrated windows.
- Added `PersistentAdditionalAccess` as the default real adapter. Add Experience is enabled and saves real data.
- Enforced authenticated Contributor ownership, role checks, whole/per-field locks and deletion protection in persistence, not only widgets.
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

The default application uses the existing Foundations database location. Tests use isolated temporary databases. `lib/` is platform-specific and intentionally ignored by Git; run setup on each teammate's computer. PowerShell scripts remain available, and `dev.py` provides the same commands across platforms.

```sh
python3 dev.py test
python3 document.py
```

Verification: **530 passing assertions across 12 suites**, including 100 backend checks and 27 GUI checks. Actual per-suite output is included with the test Javadoc. JavaFX on the installed JDK prints an inherited deprecation warning; no shutdown exception occurred in the current runs.

Read `Backend_Requirements.md` for the contracts, validation rules, rationale and per-requirement evidence. Generated evidence is in the three assignment-named Javadoc folders.

## Important compatibility changes

- `Database.addLesson/getLesson/getLessonsByOwner/updateLesson/deleteLesson` now require a real authenticated Contributor session. Caller-supplied owner/actingUser strings are consistency checks, not authorization.
- `getAllLessons` returns the full set only for Admin/Curator; a Contributor receives only their own subset.
- Lock/curator-edit setters require an authenticated Admin/Curator. Old no-login demo seed utilities must not be used as proof of successful setup. Use isolated test fixtures or sign in with the appropriate role.
- Kanish's fallback capability messages are retained for explicitly unconnected adapters; the default application now connects the real backend.
- Field locks block changes to those core fields and all lesson deletion. Whole-lesson locks also block experience mutations. This matches the existing GUI policy and is documented as a team decision for experience behavior.

## Remaining team/human deliverables

Record the actual lesson screencast segment and assigned standup summary. `RECORDING_NOTES_VISHWAM.md` is an outline, not a recording. Member 5 must incorporate the proposed What/How coaching research into the team report after confirming field allocations. Confirm other members' TP3 epic-specific attributes and complete team packaging, peer evaluation and Canvas verification. This code integration is not a claim that the entire graded submission is ready.
