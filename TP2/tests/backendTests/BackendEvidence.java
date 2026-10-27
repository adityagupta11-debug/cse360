package backendTests;

import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * Captures readable source-viewer screenshots from the delivered backend files. Original line
 * numbers and wrapping remain explicit; this is evidence capture, not a fabricated editor image or
 * an application test. Task 3.1 inventory comes from the actual entityClasses directory.
 *
 * @author Vishwam
 */
public final class BackendEvidence {
  private static int images;

  /**
   * Capture entity, storage, validation, adapter and test source into their deliverable folders.
   */
  public static void main(String[] args) throws Exception {
    CompletableFuture<Void> done = new CompletableFuture<>();
    Platform.startup(
        () -> {
          Stage stage = new Stage();
          try {
            Path entities = Path.of("Lessons_Learned_Javadocs/source-screenshots");
            StringBuilder tree =
                new StringBuilder("Task 3.1 - actual entityClasses package inventory\n\n");
            try (var paths = Files.list(Path.of("src/entityClasses"))) {
              paths
                  .filter(p -> p.toString().endsWith(".java"))
                  .sorted()
                  .forEach(p -> tree.append(p).append("\n"));
            }
            viewer(
                stage,
                "Entity classes in the delivered project",
                tree.toString(),
                entities.resolve("3.1_entityClasses_inventory.png"));
            for (String file :
                List.of(
                    "src/entityClasses/LessonsLearned.java",
                    "src/entityClasses/LessonsLearnedList.java",
                    "src/entityClasses/LessonExperience.java",
                    "src/database/LessonStore.java",
                    "src/experienceRecognizer/ExperienceRecognizer.java"))
              capture(stage, Path.of(file), entities, 1, Integer.MAX_VALUE, "VG01-VG09_3.4-3.5");
            List<String> database = Files.readAllLines(Path.of("src/database/Database.java"));
            int begin = 0, end = database.size();
            for (int i = 0; i < database.size(); i++) {
              if (database.get(i).contains("// J.L.: lessons learned CRUD")) begin = i + 1;
              if (database.get(i).contains("// Attribute getters for the current user")) end = i;
            }
            capture(
                stage,
                Path.of("src/database/Database.java"),
                entities,
                150,
                245,
                "VG05-VG09_3.4_schema");
            capture(
                stage,
                Path.of("src/database/Database.java"),
                entities,
                begin,
                end,
                "VG01-VG09_3.5_API");
            capture(
                stage,
                Path.of("src/guiLessonSupport/PersistentAdditionalAccess.java"),
                Path.of("Contributor_Javadocs/source-screenshots"),
                1,
                Integer.MAX_VALUE,
                "VG10_4.2_integration");
            capture(
                stage,
                Path.of("tests/backendTests/BackendTests.java"),
                Path.of("ContributorTests_Javadocs/source-screenshots"),
                1,
                Integer.MAX_VALUE,
                "VG01-VG10_7.4_tests");
            capture(
                stage,
                Path.of("tests/backendTests/ListSnapshotTests.java"),
                Path.of("ContributorTests_Javadocs/source-screenshots"),
                1,
                Integer.MAX_VALUE,
                "LIST_snapshot_7.4_tests");
            // Task 7.4 applies to inherited regression and manual procedures as well as new tests.
            for (String file :
                List.of(
                    "src/testingAutomation/LessonTestingAutomation.java",
                    "src/testingAutomation/LessonListTestingAutomation.java",
                    "src/testingAutomation/MyLessonsManualTests.java",
                    "src/testingAutomation/LessonTestFixtures.java"))
              capture(
                  stage,
                  Path.of(file),
                  Path.of("ContributorTests_Javadocs/source-screenshots"),
                  1,
                  Integer.MAX_VALUE,
                  "Contributor_7.4_regression_manual");
            stage.close();
            done.complete(null);
          } catch (Throwable failure) {
            stage.close();
            done.completeExceptionally(failure);
          }
        });
    try {
      done.get(120, TimeUnit.SECONDS);
    } finally {
      Platform.exit();
    }
    System.out.println("Captured " + images + " backend source/package screenshots.");
  }

  private static void capture(
      Stage stage, Path file, Path folder, int first, int last, String prefix) throws Exception {
    List<String> source = Files.readAllLines(file), rows = new ArrayList<>();
    for (int i = Math.max(0, first - 1); i < Math.min(last, source.size()); i++) {
      String line = source.get(i).replace("\t", "    "), number = String.format("%4d  ", i + 1);
      if (line.isEmpty()) rows.add(number);
      else {
        while (line.length() > 126) {
          rows.add(number + line.substring(0, 126));
          line = line.substring(126);
          number = "      ";
        }
        rows.add(number + line);
      }
    }
    for (int offset = 0; offset < rows.size(); offset += 43) {
      viewer(
          stage,
          file + " | page " + (offset / 43 + 1) + " | original line numbers; long lines wrap",
          String.join("\n", rows.subList(offset, Math.min(offset + 43, rows.size()))),
          folder.resolve(
              prefix
                  + "_"
                  + file.getFileName().toString().replace(".java", "")
                  + "_"
                  + String.format("%02d", offset / 43 + 1)
                  + ".png"));
    }
  }

  private static void viewer(Stage stage, String title, String text, Path output) throws Exception {
    Label heading = new Label(title);
    heading.setStyle("-fx-font: bold 15px Arial;");
    TextArea code = new TextArea(text);
    code.setEditable(false);
    code.setWrapText(false);
    code.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 14px;");
    VBox root = new VBox(10, heading, code);
    root.setPadding(new Insets(16));
    VBox.setVgrow(code, Priority.ALWAYS);
    stage.setScene(new Scene(root, 1500, 1030));
    stage.show();
    root.applyCss();
    root.layout();
    WritableImage image = root.snapshot(null, null);
    BufferedImage bitmap =
        new BufferedImage(
            (int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < bitmap.getHeight(); y++)
      for (int x = 0; x < bitmap.getWidth(); x++)
        bitmap.setRGB(x, y, image.getPixelReader().getArgb(x, y));
    Files.createDirectories(output.getParent());
    ImageIO.write(bitmap, "png", output.toFile());
    images++;
  }
}
