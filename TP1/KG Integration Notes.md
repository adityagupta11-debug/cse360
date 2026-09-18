# K.G. integration notes - Team 53

Integrated against repository commit 74f8456 on 17 September 2026.

## Contribution

Confirmed account deletion uses a stable account ID and the authenticated administrator identity. The database rechecks authority, prevents self-deletion and rolls back failed deletes. The existing teammate account-details display and OTP, invitation, role and list-user features are retained. Deleting an account removes its OTP credentials with its user row.

The shared stylesheet applies across scenes. Login and deletion have updated layouts. Existing teammate credits remain in the source. User Experience.pdf describes the reference application and proposed shared conventions; team agreement is still required.

## Verification

Run `./test.ps1` from TP1 after installing dependencies described in DEPENDENCIES.md. On 17 September 2026: 35 database, 19 controller/view, 7 invitation-boundary and 132 existing team checks passed (193 total, zero failures). Tests use isolated H2 databases and offscreen JavaFX rendering. Build and test reports are regenerated under build/reports and are excluded from Git.

The feature test catalog is in KG Handoff/KG Feature Test Cases.pdf. J.L. should incorporate it into the team-wide test-case document. The deletion design addendum supersedes the earlier deletion flow; updated editable class and sequence diagrams accompany it.

## Before final submission

Record the team's UI decision and confirm hard deletion is the intended interpretation. Complete the manual desktop checks, actual standups and screencasts required by the assignment. Integrate the feature tests into the final team catalog and package the complete team submission. Repository publication does not submit anything to Canvas.

The agreed team-norms PDF is included unchanged. Existing teammate design PDFs are retained; read the deletion addendum for the revised authentication and transaction behavior.
