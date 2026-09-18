# Build dependencies

Dependency JARs are excluded from Git. Create a lib folder inside TP1 and download the following unmodified Maven Central artifacts with the filenames below. Keep their embedded license/notices when redistributing. The OpenJFX files here are for Windows.

- OpenJFX 21.0.8: javafx-base, javafx-graphics, javafx-controls; Maven coordinates `org.openjfx:javafx-<component>:21.0.8:win`.
- H2 2.3.232: Maven coordinates `com.h2database:h2:2.3.232`.
- Java JDK is not bundled. Build scripts require JDK 22+; this run used JDK 25.

Sources:

- https://repo.maven.apache.org/maven2/org/openjfx/
- https://repo.maven.apache.org/maven2/com/h2database/h2/2.3.232/
- https://openjfx.io/
- https://www.h2database.com/

The existing foundation stores passwords directly; changing password storage was not part of this account-deletion implementation. The account-details view now masks password output, but that is not a password-storage migration.

Required local filenames and download URLs:

- `lib/javafx-base.jar`: https://repo.maven.apache.org/maven2/org/openjfx/javafx-base/21.0.8/javafx-base-21.0.8-win.jar
- `lib/javafx-graphics.jar`: https://repo.maven.apache.org/maven2/org/openjfx/javafx-graphics/21.0.8/javafx-graphics-21.0.8-win.jar
- `lib/javafx-controls.jar`: https://repo.maven.apache.org/maven2/org/openjfx/javafx-controls/21.0.8/javafx-controls-21.0.8-win.jar
- `lib/h2.jar`: https://repo.maven.apache.org/maven2/com/h2database/h2/2.3.232/h2-2.3.232.jar

Run `./build.ps1`, `./run.ps1` or `./test.ps1` in PowerShell from TP1. The run script stores its database under TP1/data.
