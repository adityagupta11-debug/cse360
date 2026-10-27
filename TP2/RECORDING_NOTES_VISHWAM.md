# Vishwam's lesson-data screencast segment (approximately five minutes)

Use the current generated Javadoc and actual source screenshots. The full lesson video is paired with Member 3's approximately 2.5-minute list segment. This is a presenter outline, not a finished recording.

| Time | Show | Explain |
|---|---|---|
| 0:00-0:35 | `Backend_Requirements.md` and entity package inventory screenshot | Contributor core management, locks/visibility, linked experiences and persistence; identify local VG traceability IDs. |
| 0:35-1:25 | `Lessons_Learned_Javadocs/entityClasses/LessonsLearned.html` and corresponding source pages | Existing four fields, stable ID/owner, Joshua's lock/curator-edit attributes, new timestamp rationale, persistent CRUD facade. Member 3 covers list CRUD/search separately. |
| 1:25-2:15 | `database/LessonStore.html` update/delete methods and source screenshots | Authenticated identity rather than mutable cache; conditional SQL checks locks at write time; unchanged locked fields may coexist with permitted edits; any lock blocks deletion. |
| 2:15-3:15 | `entityClasses/LessonExperience.html`, `experienceRecognizer/ExperienceRecognizer.html` and schema source screenshot | Each attempt links to a lesson, elapsed time differs from individual effort, shared validation limits, transaction/rollback policy and cascade deletion. |
| 3:15-4:00 | Database migration and curator metadata source screenshots | Additive upgrade, migration-time timestamps on old rows, curator-only information excluded from Contributor projections, future TP3 capacity with no claimed AI implementation. |
| 4:00-4:40 | `Contributor_Javadocs/guiLessonSupport/PersistentAdditionalAccess.html` and real experience-entry image | Default real adapter connects Kanish's already-built GUI; demonstrate saved experience without pretending fixture data is production behavior. |
| 4:40-5:00 | `ContributorTests_Javadocs/Backend_Test_Index.html` and backend output | Point to real storage/concurrency/restart/rollback evidence; test-video pair demonstrates full test execution. Hand over to list-class presenter. |

Before recording: run `python3 dev.py test`, then `python3 document.py`. Keep the final video readable, audible and below 100 MB. Record with your own narration; do not represent generated screenshots or this outline as an actual standup or personal presentation.
