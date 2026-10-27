# Integrated Contributor manual checks

Use an isolated test database and Contributor A/B accounts. These are procedures, not a claim that a human executed them. Automated equivalents and actual screenshots are in CurrentRepoWindowTests / BackendTests.

| Check | Steps | Expected |
|---|---|---|
| Own list and search | Sign in as A, open My Lessons/Search, search for a word in an A lesson, then Clear. | Only A's records appear; Clear restores A's whole list. An empty list and a no-match result have distinct messages. |
| Create and validation | Open New, submit an empty title with other fields filled, then correct it and save. | Helpful title error retains other inputs; valid save opens the stored detail with all four fields and timestamps. |
| Edit | Open an unlocked lesson, edit an allowed field and save. | Stored value is shown; owner/ID unchanged. |
| Experience | Open Add Experience, fill What/How/Duration. Try Save without member rows, then add member/hour rows and Save. | Empty table is refused without a write; valid save returns to the lesson with a real saved experience ID. |
| Effort corrections | Select a member row, change hours, Add or update; remove a selected row. | Selection loads the row; update replaces rather than duplicates; remove affects only that draft row until experience Save. |
| Locks | Use authenticated curator fixtures to set a field lock and then a whole-lesson lock; reopen the lesson. | Specific field is read-only; any lock disables Delete. Whole lock disables editing and Add Experience. Actual backend enforcement is exercised even for stale screens in BackendTests. |
| Curator edited/private | Curator fixture flags A's lesson edited and adds private notes; sign in as A. | Original lesson remains in A's list with edit state; private curator text never appears in Contributor views. |
| Delete cancellation | Open Delete and choose No or Back. | No is the default; lesson and linked data remain. |
| Confirmed deletion | Delete an unlocked lesson with experience records using Yes. | List reloads without it; backend confirms linked records were removed. |
| Session | Log out and attempt an old/foreign record through the gateway/testbed. | No other-owner or logged-out access; new login shows only the new user's lessons. |
| Restart | Save a lesson and experience, close the application and reopen/sign in. | Core fields, lock flags, curator flag, experience and exact decimal member hours persist. |

The existing Kanish manual checklist is retained as historical contribution context. Backend-dependent states that were previously fixture-only are now connected to storage.
