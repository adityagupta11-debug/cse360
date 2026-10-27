> **Historical pre-integration contribution notes.** The missing-backend statements and suggested progress below describe Kanish's earlier branch, not the integrated TP2 build. Use `MANUAL_GUI_CHECKS_INTEGRATED.md`, `README_VISHWAM_TP2.md`, current generated evidence, and actual test reports for current behavior. Real locks and experience persistence are now connected. Do not read the historical standup wording as a current progress report or a record of a meeting.

# Manual checks for the current-repository GUI

Local KG identifiers refer to the supplied work split, not official story numbers.
Actual automated overlap is described below; unexecuted keyboard/resizing checks remain pending.

1. KG01/KG05: From Contributor Home choose New. Leave Title blank, then try a four-character
   title, no category, a short Situation and a short Lesson Text. Each rejection identifies the
   correction and keeps the other input. Choose a known category and valid text to create.
   Automated: model bounds plus real JavaFX blank-title/preserved-draft path passed.
2. KG01: View the created lesson. Title, Category, Situation and Lesson Text are all present
   and read-only. Open Edit, change all four values, save and return to Home. Reopen and verify.
   Automated: actual Home routing, database writes and widgets passed; real reopen passed.
3. KG02: Use a different Contributor and attempt an old ID. It must not expose or edit the
   original owner's data. Load another account's details into the database cache and verify
   the signed-in identity still controls the new windows. Automated: real integration tests passed.
4. KG07: Open Delete and verify the actual lesson title/ID in Are you sure. No is default.
   Choose No; data remains. Choose Yes on an unlocked baseline lesson; it disappears from Home.
   Automated: properties and real No/Yes flows passed. Actual Enter/Escape keyboard check pending.
5. KG06/KG02: Back returns to the real list or details; Logout returns to the existing login
   and clears session authorization. Automated: real Home/Edit/View/Back/Logout path passed.
   Manually confirm Quit closes the application and database normally.
6. KG03: With the future complete backend, test each of four field locks and a full lesson
   lock. Lock labels and read-only controls must agree; any lock disables deletion, and the
   backend must reject a lock applied after the screen loaded. Model/GUI fixture checks passed;
   actual lock persistence is not available in current main and must be rerun after integration.
7. KG04: In current main, Add Experience is disabled with an accurate missing-backend note.
   After real backend integration, enter what/how, duration 2.5 and member/effort rows; save and
   restart to verify persistence. Current evidence proves only GUI/model delegation with a fixture.
8. KG04/KG05: Try empty effort rows, duplicate names, invalid hours, zero duration and missing
   what/how. Correct a selected member row and remove a row. Add or update pending member input
   before Save. Automated: required text/hours/member validation and successful delegation passed;
   final production row-update/remove workflow remains a manual integration check.
9. KG08: Force a storage error and verify no false success and no lost draft. Fixture model
   failures passed. Repeat with controlled errors from the actual future backend before release.
10. KG06: Resize each window down to 740x600. Scroll to every field and action. Confirm labels,
    focus navigation, readable dropdown values and no unreachable Save button. Captured views
    were visually reviewed at the default size; final keyboard/minimum-size inspection is pending.
