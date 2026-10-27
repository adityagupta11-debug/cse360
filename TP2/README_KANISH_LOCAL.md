# Kanish GUI contribution

Base: `67bd0a9381128225911936171046b123b7b17085` (Joshua's TP2 base, October 26, 2026).
Prepared for the kanish-tp2-gui branch. Experience storage and lock integration remain pending.

## Run the integrated application

On Windows x64, with JDK 21 or newer on PATH, open PowerShell in this TP2 folder:

```powershell
./run.ps1
```

Install the project dependencies with `python setup_dependencies.py` in TP2 before the first run.
The dependency files in `lib` are ignored by Git.
Eclipse imports use project-relative library paths; select a JDK 21+ and run Project > Clean.
No saved accounts or production database are included. Create an initial administrator, create
or invite a Contributor using the existing application, then sign in as that Contributor.

Contributor Home retains Joshua's own-lesson list and selection preview. Its New, View, Edit
and Delete actions now open Kanish's separate MVC windows. Create/edit preserve all four
actual fields: Title, Category, Situation, Lesson Text. Existing limits are reused: title 5-100,
situation/lesson text 20-2000, category from the existing enumeration.

## Completed locally

- Create/View/Edit/Delete GUI integrated into the real current application and real database API.
- Add Experience window and full/per-field lock behavior implemented behind a capability-aware
  interface. These are testable GUI components, but the real backend for them is absent upstream.
- Stable session-based actor lookup added to Database; new GUI calls do not trust editable
  owner input or the mutable account-details cache. Existing schema and lesson CRUD are retained.
- Own-list refresh rejects a logged-out or role-revoked session. Logout clears authentication.
- Five MVC packages, shared field/layout helpers, generated Javadocs, real GUI captures and
  source-viewer captures, requirement/test matrix, manual checks and recording notes.
- Title and Lesson Text AI-coaching contribution aligned with the actual repository fields.
- Eclipse classpath now uses project-local JARs instead of custom JavaFX/H2 user libraries.

## Checks

Run `./test-kanish.ps1` for 23 model checks, 14 real database/session/persistence checks and
18 JavaFX window checks. All 55 passed. The live CRUD/navigation checks use current Database;
full locks and experience saving are explicitly labeled fixture-only because the backend is absent.

The inherited `./test.ps1` passed: database 35, controller/view 19, login/invitation 7,
password view 6, first-user relogin 4 and TP1 regression 152. Joshua's lesson regression
also passed 79 checks. Total across the executed suites: 357 passing checks, no failed assertions.
Generated reports are copied into `ContributorTests_Javadocs/results`.

To regenerate evidence, first run `./test-kanish.ps1`, then set JAVA_HOME to a JDK with
javadoc and run `./document-kanish.ps1`. The latter generates HTML and source screenshots.

## What current GitHub actually contains

Joshua added TP2 in `49d7ed0` and renamed the Eclipse project in `67bd0a9`.
The checked remote main and remote branch history contain no new October TP2 implementation
by Vishwam. The current TP2 source is Joshua's HW2 lesson variant; older shared TP1/Vishwam
integration code is inherited. Do not mistake old author comments for completed new TP2 work.

There is no experience entity/table/write API, full-lesson or per-field lock metadata,
curator-edited lesson flag or lesson timestamps in the current lesson model/schema. There
are also no standalone My Lessons/search packages or new TP2 keyword-search methods.
The current entity names are Lesson and LessonList, not the assignment's requested names;
the entity/data owner and team should reconcile that separately.

## Remaining integration dependency

Vishwam (or the data owner) must provide a real `CurrentRepoGateway.AdditionalAccess`:

- `read(id)`: authorized complete Contributor-safe snapshot, including locks/curator flag.
- `update(id, draft)`: persistent ownership/role and atomic full/per-field lock checks.
- `delete(id, confirmed)`: explicit confirmation, no deletion with any lock, linked-record policy.
- `addExperience(lessonId, experience)`: persistent what/how/duration/member-effort rows.

Pass that implementation as the second CurrentRepoGateway constructor argument in
ControllerContributorHome.windowContext. Alternatively implement LessonGateway directly.
Until connected, the actual application visibly reports missing lock metadata and disables
Add Experience. It does not invent timestamps, create a second database, or fake experience saves.

Experience GUI assumptions awaiting the data owner: decimal hours, positive duration,
nonnegative individual effort with positive total, unique nonblank member names and 2000-character
what/how text. Confirm official constraints and member-ID mapping before the final backend merge.

## Personal and team duties

Record your real GUI screencast segment and assigned standup summary after reviewing the
actual integrated build. Use RECORDING_NOTES_KANISH.md. Historical standups, signatures,
team progress, peer evaluations and submission receipts have not been fabricated.
The official Contributor user-story PDF is not in the repository; reconcile final coverage
against it and the latest course clarifications before claiming full TP2 completion.

This folder is your current-repository contribution, not a complete graded team submission.
It does not replace Joshua's remaining list/search work or the other members' deliverables.

Latest rerun: all 55 contribution assertions passed with process exit code 0. JavaFX on this JDK printed a shutdown-thread NullPointerException after the results; desktop shutdown compatibility still needs review.
