# TP2 backend requirements and evidence

Local traceability IDs VG01-VG10 map to Contributor Phase Two in the August 19, 2026 initial user stories. They are not official course story numbers. Joshua's classes/locks are preserved and Kanish's GUI is connected to real storage.

| ID | Source and required behavior | Implementation | Positive and negative execution evidence |
|---|---|---|---|
| VG01 | Contributor story 1: create validated core information, assigned to the authenticated author. | `LessonsLearned.create`, `Database.addLesson`, `LessonStore.add`, existing `LessonRecognizer`. Four core fields remain Title, Category, Situation, Lesson Text. | `VG01_CreateOwnValid`, `VG01_InvalidCreateChangesNothing`, `VG01_ForgedOwnerRejected`, `VG01_UntrustedFlagsIgnored`. |
| VG02 | Stories 1/2/4: inspect and list only own lessons; curator edits do not transfer authorship. Do not trust cached account details or supplied usernames. | Immutable owning account IDs, including account rename/deletion/name-reuse protection; session-bound `LessonStore.read/byOwner/all`; `CurrentRepoGateway`; authenticated list reload before search. | `VG02_OwnReadVisible`, foreign read/list/update/delete tests, forged actor, cache, logout and role-revocation tests; rename/deleted-name-reuse/migration-orphan regressions. |
| VG03 | Stories 1/2: locked fields cannot change; whole-lesson locks prevent any core edit. Unlocked fields may change independently. | Existing Joshua lock columns and setters; conditional SQL in `LessonStore.update`; GUI read-only controls. Setters now require an Admin/Curator session. | For each of Title/Category/Situation/Lesson: stored lock, changed-field rejection and unchanged-field acceptance; whole-lock/stale-screen tests; three real concurrent row-lock pairs. |
| VG04 | Story 2: cannot delete a lesson with any lock. Explicit confirmation follows the team's TP1 destructive-action UX. | Conditional `LessonStore.delete`, retained `Database.deleteLesson` API; foreign-key cascading removes linked private/experience/effort records atomically. | Every field-lock deletion test; whole-lock and cancellation tests; unlocked deletion, missing lesson and cascade tests; concurrent delete waits/rechecks. |
| VG05 | Story 3: add experience to an existing lesson: what, how, elapsed duration and effort per member. | Immutable `LessonExperience`, experience/effort tables, transaction in `LessonStore.saveExperience`, connected Add Experience window. | Linked create/read, exact member hours and independent duration; foreign read/write and whole-lock rejection; real GUI save in `CurrentRepoWindowTests`. |
| VG06 | TP2 input validation: invalid experience data must never partially persist. | `ExperienceRecognizer` shared by GUI `Rules` and storage; database decimal/check constraints. Limits below are team policies, not official story limits. | Thirteen invalid cases, null input, valid data and actual SQL-failure rollback tests. |
| VG07 | Team work split: experience update/delete as well as add/read. Existing identity/author/link must not move during update. | `Database.updateExperience/deleteExperience`; transactionally replace member rows. Whole-lesson lock prevents all experience mutations; field locks affect only the corresponding core fields. | Replace/identity preservation, invalid/missing update, foreign/cancelled delete, successful delete, forced insert/update failure rollback and recovery. |
| VG08 | TP2 persistence and future attribute support: data and lock state survive restart; upgrade old databases without clearing them. | `createdAt/updatedAt` lesson columns and experience columns; additive migrations. Old lessons receive migration-time timestamps, not fabricated historical dates. | Reopen checks for login/core/curator flag/locks/experiences/effort; three migration checks against a genuinely old schema. |
| VG09 | Story 1: Contributor cannot see extra curator-added information. TP3 curator epics need private notes, search terms and aggregate-summary capacity. | Separate `lessonCuratorDB` with notes, searchTerms, aggregateSummary and summaryVisible. Contributor entity/list/gateway projections contain no private columns. Authenticated Admin/Curator metadata API. | Curator read allowed; Contributor private read/write refused; edited original still visible; no private text in snapshot. |
| VG10 | Integrate both teammates' contributions using the same session, database, MVC and UX. | Default `CurrentRepoGateway` installs `PersistentAdditionalAccess`; Kanish windows plus Joshua dedicated My Lessons/search route. | Connected capabilities, gateway real experience write, cancel/missing/overflow checks; real GUI CRUD/experience/search/navigation suite. |

## Field and validation decisions

- Core lesson limits remain Joshua's existing recognizer: trimmed title 5-100, Category from `LessonsLearned.CATEGORIES`, trimmed Situation and Lesson Text 20-2000 characters.
- Experience What/How: nonblank, up to 2000 trimmed characters; ordinary Unicode/newlines allowed, malformed Unicode and unsupported controls refused.
- Duration: positive decimal hours. Member effort: nonnegative decimal hours, positive total across at least one member. Each value fits `DECIMAL(12,3)` (up to 999999999.999 with at most three meaningful fractional digits); no silent rounding.
- Member names: nonblank, up to 100 trimmed characters, unique ignoring case. They are participant labels, not user-account IDs; external/non-account teammates can have effort recorded.
- Lesson and experience ownership uses immutable user account IDs; usernames are display labels. Renames preserve original records, and deleted/reused names cannot claim them. Existing legacy records are linked to a matching account only once during the ownership-column migration; records with no owner account remain inaccessible to Contributors, even after later registrations.
- Experience author, parent identity, timestamps and generated IDs come from the authenticated storage operation. A Contributor cannot change ownership through a form.
- Experience deletion is explicitly confirmed at the backend API; only Add Experience is exposed in the assigned GUI. Full experience CRUD is a team work-split extension beyond the story's explicit add action.
- Private curator data is stored separately. TP3 AI and curator interfaces are not claimed implemented. Search terms and aggregate fields prepare the published epics without inventing completed AI behavior.

## Reproduce

From the TP2 directory, run `python3 setup_dependencies.py`, `python3 dev.py test`, and `python3 document.py` (use `python` on Windows if appropriate). Java 21+ is required. Desktop GUI tests require a graphical session.

- Actual reports: `ContributorTests_Javadocs/results/`.
- Per-assertion map: `ContributorTests_Javadocs/Backend_Requirements_Test_Evidence.csv` and `Backend_Test_Index.html`.
- Entity/storage Javadoc and source evidence: `Lessons_Learned_Javadocs/index.html` and `Evidence_Index.html`.
- GUI Javadoc/evidence: `Contributor_Javadocs/index.html` and `Evidence_Index.html`.
- Test Javadoc/evidence: `ContributorTests_Javadocs/index.html` and `Evidence_Index.html`.

## Team dependencies still requiring human input

The repository contains the published TP3 epics but no confirmed five-member epic allocation. These additions support the published locking/search/aggregation/coaching directions. Teammates must supply any additional agreed attributes or course clarifications before the team claims full future-epic coverage. No standup history, research assignments, submission receipts or recordings have been invented.
