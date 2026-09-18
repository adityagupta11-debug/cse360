TP1 Design Documents
CSE 360 Team Project, Phase 1
Role: Design Documents and Architecture - integration coordination and shared infrastructure
Author: A.G. (agupt545)
Date: 2026-09-17

CONTENTS
--------
TP1 Architecture and Detailed Design.pdf
    The layered architecture, the architectural decisions made this phase and why, the detailed
    design of the classes added or changed, and the input validation design (including the
    requirement the provided password evaluator diagram does not test). Contains Figures 1-5.

TP1 Traceability Matrix.pdf
    Every user story followed through to the code and the numbered test cases that verify it.
    20 user stories, 132 automated test cases, no story without coverage.

TP1 Integration and Regression Plan.pdf
    The merge schedule, the six regression checkpoints, the rules for a merge, the integration
    risks, and the defects found and fixed during integration.

Diagrams/
    The rendered PNG of each diagram, referenced by the documents above.

UML Sources/
    The PlantUML source of each diagram, so any team member can regenerate a diagram after a
    change rather than hand-editing an image.

    01-Architecture-Package-Diagram.puml      Figure 1: layers and packages
    02-Detailed-Design-Class-Diagram.puml     Figure 2: classes added or changed in TP1
    03-Sequence-One-Time-Password.puml        Figure 3: Admin issues, user redeems
    04-Sequence-Delete-User-And-Invitations.puml  Figure 4: delete a user, revoke an invitation
    05-State-Email-Address-Recognizer.puml    Figure 5: the email address directed graph

REGENERATING A DIAGRAM
----------------------
    plantuml -tpng -o ../Diagrams "UML Sources/<name>.puml"

The feature owner who changes a class that appears in a diagram updates the .puml source and
regenerates the PNG in the same merge as the code change (rule 3 of the Integration plan).

NOTE ON ASTAH
-------------
The course requires Astah for TP2 and TP3. These diagrams were produced from PlantUML sources for
TP1, which the assignment permits, and the shapes, names, and relationships are already in the
form the Astah models will take, so recreating them is transcription rather than redesign.

RUNNING THE TESTS
-----------------
    Eclipse:  run testingAutomation.TP1RegressionSuite as a Java Application
    Terminal: java -cp "bin:<h2.jar>:<javafx>/lib/*" testingAutomation.TP1RegressionSuite

The suite runs all six test classes and prints one summary. It exits non-zero if anything fails.
It uses private in-memory H2 databases, so it never alters the ~/FoundationDatabase file the
application itself uses.

K.G. DELETION INTEGRATION (17 September 2026)
KG Account Deletion Design.pdf describes the revised deletion flow and stable authenticated identity.
Class and deletion sequence diagram sources and PNGs are updated. The earlier architecture PDF embeds the previous figures; consult the updated standalone diagrams and addendum.
Run ../test.ps1 for 61 additional feature checks plus all 132 existing team checks (193 total).
