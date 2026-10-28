# Contributor GUI checks

Automated Windows checks passed on October 28, 2026. Reports are under
ContributorTests_Javadocs/results. Use these steps for the screencast rehearsal.

1. Sign in as Contributor. Open My Lessons; search, clear search, select a lesson and open it.
2. Open Add Experience. Enter what/how, duration and one member's effort. Save without a row
   first to see the validation message; confirm that the draft remains.
3. Add another member, select the row and change hours, then remove it. Try a duplicate name
   and more than three decimal places. Both must be rejected without adding a row.
4. Enter a member without adding the row and select Save. Commit or clear that input first.
5. Save a valid experience. Confirm the success message; reopen the same database to verify
   stored data using the backend tests. Experience browsing/editing is not part of this form.
6. With a curator fixture, lock a core field: that field is read-only and deletion is disabled.
   Experience entry remains permitted. A whole-lesson lock disables experience entry.
7. Apply a whole-lesson lock after opening Add Experience. Saving must fail and retain input.
8. Resize to 740x600; scroll down to the member actions and Save, and use Back/Logout.
9. During rehearsal, inspect keyboard Tab order and Enter/Escape behavior on the delete
   confirmation. The automated tests check default No and cancellation, not physical key presses.

Do not mark unperformed personal rehearsal steps as completed meeting or recording evidence.
