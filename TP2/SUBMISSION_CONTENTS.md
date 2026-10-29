# Team 53 Phase Two submission contents

This folder contains the integrated Contributor application, current generated Javadoc, source screenshots, test output, updated team norms, coaching research and six supplied planning PDFs. The existing summary videos are retained as candidates; several still need additional real team-status content.

## Run the application and tests

Install JDK 21+ and Python 3, then run `python3 setup_dependencies.py`, `python3 dev.py test` and `python3 dev.py run` from TP2. Eclipse: import this folder as an existing project, run setup first, Refresh, then Project > Clean. To share the normal launch database, set working directory TP2 and VM argument `-Dcse360.db.url=jdbc:h2:./data/Team53`. Use `python3 dev.py demo` for isolated recording fixtures.

## Documentation

- [Lesson/entity evidence](Lessons_Learned_Javadocs/Evidence_Index.html) and [generated Javadoc](Lessons_Learned_Javadocs/index.html).
- [GUI evidence](Contributor_Javadocs/Evidence_Index.html) and [generated Javadoc](Contributor_Javadocs/index.html).
- [Test evidence](ContributorTests_Javadocs/Evidence_Index.html), [requirements and tests](ContributorTests_Javadocs/Contributor_Requirements_and_Tests.html) and [manual procedures](ContributorTests_Javadocs/Manual_Tests.html).
- `TP2 Team Norms.pdf` shows tracked changes; `TP2 Team Norms.docx` retains editable Word revisions.
- `AICoaching.PDF` is the corrected final report; field assignments follow the supplied October 28 team compilation.
- `Verification_Integrated.json` records 658 checks across 14 Java suites. Member-specific earlier verification files are historical snapshots.

## Finish recordings before final submission

The three root-level final files must be `LessonsLearnedScreencast.mp4` or `.mov` (5–10 minutes), `PhaseTwoGUIScreencast.mp4` or `.mov` (5–10 minutes), and `TestingScreencast.mp4` or `.mov` (8–15 minutes). Each needs two presenters; all five members must present with roughly equal total times. Actual execution of every documented test is required in the testing recording. Each file must be under 100 MB.

The separate Recording_Work folder beside TP2 holds the original parts, edited Joshua/Kanish parts, a fresh actual test-output recording and edit notes. These are source parts, not the required final combined videos. Do not put them into the ZIP as substitutes for the final recordings.

For each dated status PDF, supply a correctly named matching summary recording lasting 3–5 minutes and showing every member’s progress, next work and blockers. Use actual meeting dates/times. `Media_Review.json` deliberately leaves unverified content and meeting-time fields unresolved.

Run `python3 planning/package_tp2.py` after updating the final files. It writes the ZIP under `build/package`, tests its extracted application and reports remaining failures. A nonzero exit is expected while recordings are incomplete. Complete each member’s evaluation and personally verify Canvas receipt separately.
