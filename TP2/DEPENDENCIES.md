# Build dependencies

Dependency JARs are excluded from Git. From the TP1 folder, run `python setup_dependencies.py` (or `python3 setup_dependencies.py` on macOS/Linux). Python 3 and an internet connection are needed for this setup step. The script downloads JavaFX for the current operating system and architecture, plus H2, into `lib`. It checks the Maven Central checksums before installing each file. Do not copy another operating system's lib folder.

## Eclipse setup after pulling these changes

1. Install/select JDK 21 or newer in Eclipse's Installed JREs settings. Use an Eclipse version that supports Java 21.
2. Run the dependency setup command above from TP1.
3. Refresh the TP1 project (F5), then use Project > Clean.
4. Check that the project's JRE System Library points to the installed JDK. Compiler compliance is now Java 21.
5. Run `applicationMain.FoundationsMain` as a Java Application.

The project now uses relative `lib` paths instead of custom Eclipse user libraries named JavaFX25.0.4 and H2. The unused Xtext builder/nature has been removed. No Xtext plugin is needed. The H2 module is explicitly required so JDBC is available in a modular Eclipse launch. Existing launch configurations with old absolute JavaFX paths should be recreated.

Keep dependency license/notices when redistributing. Manual URLs below are for Windows x64; use the setup script for macOS/Linux.

- OpenJFX 21.0.8: javafx-base, javafx-graphics, javafx-controls; Maven coordinates `org.openjfx:javafx-<component>:21.0.8:win`.
- H2 2.3.232: Maven coordinates `com.h2database:h2:2.3.232`.
- Java JDK is not bundled. The compiler target is Java 21; JDK 21+ is required. Verification used JDK 25 with `--release 21` on Windows.

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
