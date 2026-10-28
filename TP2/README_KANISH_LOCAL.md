# Kanish - Contributor GUI

Integrated with Joshua's main f5cb25e and Vishwam's backend 85cff20.

## Included

- Create, view, edit and confirmed delete using the shared authenticated database.
- Add Experience: what/how, elapsed duration, and a table of members and effort hours.
- Add, select, update, remove and clear member input. Uncommitted edits block saving.
- Experience and lesson writes persist; current ownership and curator locks are enforced.
- Shared Arial typography, gray background, bordered white cards, blue primary actions,
  red destructive actions and Back/Logout/Quit match Joshua's lesson pages.
- Full lesson locks disable experience editing. Individual core-field locks permit experience
  entry, following the shared backend policy. A lock applied after opening also prevents saving.
- Javadocs, source screenshots, real application screenshots and test evidence are included.

## Run

Use a JDK 21+ and Python 3 from the TP2 folder:

```
python setup_dependencies.py
python dev.py run
```

PowerShell users may also use `./run.ps1`. Both launchers use data/Team53.
For isolated sample accounts use `python dev.py demo`; see README_VISHWAM_TP2.md.
Do not copy another platform's lib directory. Tests use separate temporary databases.

## Verification

Windows, October 28, 2026: `python dev.py test` passed all 13 suites (569 assertions).
This includes 37 JavaFX checks with real persistent experience saves, row operations, draft
preservation, lock changes, minimum-size scrolling, list/search navigation and logout.
The backend suite separately verifies experience/member-hour persistence after reopening,
transaction rollback, authorization and concurrent lock changes. Model fixtures are labeled.
JDK 25 prints JavaFX deprecation warnings; this run completed without a shutdown exception.
Use `python document.py` to regenerate Javadocs and evidence after running the tests.

## Recording

The code is ready for the GUI screencast. RECORDING_NOTES_KANISH.md gives the outline.
Actual screencasts, genuine meeting summaries, peer evaluation and Canvas receipt verification
must still be completed by the team members. This code does not create or claim those records.
