package kanishTests;

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
 * Captures the actual JavaFX source viewer for each authored file. Visual wrapping and original
 * line numbers are explicit; screenshot content comes directly from delivered source files. This
 * utility is evidence capture, not an application test. @author Kanish Garg
 */
public final class SourceEvidence {
  private static int images;

  public static void main(String[] args) throws Exception {
    var done = new CompletableFuture<Void>();
    Platform.startup(
        () -> {
          Stage stage = new Stage();
          try {
            List<Path> gui = new ArrayList<>();
            for (String pkg :
                List.of(
                    "guiContributor",
                    "guiMyLessons",
                    "guiAddLesson",
                    "guiLessonDetail",
                    "guiLessonSupport",
                    "guiLessonCreate",
                    "guiLessonDetails",
                    "guiLessonEdit",
                    "guiLessonDelete",
                    "guiExperienceAdd"))
              try (var files = Files.list(Path.of("src", pkg))) {
                files.filter(x -> x.toString().endsWith(".java")).sorted().forEach(gui::add);
              }
            List<Path> tests;
            try (var files = Files.list(Path.of("tests/kanishTests"))) {
              tests =
                  files
                      .filter(
                          x ->
                              x.toString().endsWith(".java")
                                  && !x.getFileName().toString().equals("SourceEvidence.java"))
                      .sorted()
                      .toList();
            }
            packageTree(stage);
            for (Path file : gui)
              capture(stage, file, Path.of("Contributor_Javadocs/source-screenshots"));
            for (Path file : tests)
              capture(stage, file, Path.of("ContributorTests_Javadocs/source-screenshots"));
            stage.close();
            done.complete(null);
          } catch (Throwable ex) {
            stage.close();
            done.completeExceptionally(ex);
          }
        });
    try {
      done.get(120, TimeUnit.SECONDS);
    } finally {
      Platform.exit();
    }
    System.out.println("Captured " + images + " source/package screenshots from delivered files.");
  }

  private static void packageTree(Stage stage) throws Exception {
    StringBuilder text = new StringBuilder("Task 4.1 - Kanish GUI packages and classes\n\n");
    for (String pkg :
        List.of(
            "guiLessonCreate",
            "guiLessonDetails",
            "guiLessonEdit",
            "guiLessonDelete",
            "guiExperienceAdd",
            "guiLessonSupport",
            "guiMyLessons",
            "guiAddLesson",
            "guiLessonDetail",
            "guiContributor")) {
      text.append("src/").append(pkg).append("/\n");
      try (var files = Files.list(Path.of("src", pkg))) {
        for (Path f : files.sorted().toList())
          text.append("    ").append(f.getFileName()).append("\n");
      }
      text.append("\n");
    }
    viewer(
        stage,
        "Actual package inventory from delivered source",
        text.toString(),
        Path.of("Contributor_Javadocs/source-screenshots/KG06_4.1_Package_Class_Layout.png"));
  }

  private static void capture(Stage stage, Path file, Path folder) throws Exception {
    List<String> rows = new ArrayList<>();
    List<String> source = Files.readAllLines(file);
    for (int i = 0; i < source.size(); i++) {
      String line = source.get(i).replace("\t", "    ");
      String prefix = String.format("%4d  ", i + 1);
      if (line.isEmpty()) rows.add(prefix);
      else {
        while (line.length() > 126) {
          rows.add(prefix + line.substring(0, 126));
          line = line.substring(126);
          prefix = "      ";
        }
        rows.add(prefix + line);
      }
    }
    for (int offset = 0; offset < rows.size(); offset += 43) {
      int end = Math.min(offset + 43, rows.size());
      String code = String.join("\n", rows.subList(offset, end));
      String requirementPrefix =
          switch (file.getParent().getFileName().toString()) {
            case "guiLessonCreate" -> "KG01_4.2";
            case "guiLessonDetails" -> "KG02_4.2";
            case "guiLessonEdit" -> "KG03_4.2";
            case "guiLessonDelete" -> "KG07_4.2";
            case "guiExperienceAdd" -> "KG04_4.2";
            case "guiLessonSupport", "guiContributor" -> "KG06_4.2";
            default -> "KG01-KG08_7.4";
          };
      String name =
          requirementPrefix
              + "_"
              + file.getParent().getFileName()
              + "_"
              + file.getFileName().toString().replace(".java", "")
              + "_"
              + String.format("%02d", offset / 43 + 1)
              + ".png";
      viewer(
          stage,
          file.toString()
              + " | visual page "
              + (offset / 43 + 1)
              + " | original line numbers; long lines wrap",
          code,
          folder.resolve(name));
    }
  }

  private static void viewer(Stage stage, String title, String text, Path out) throws Exception {
    Label heading = new Label(title);
    heading.setStyle("-fx-font: bold 15px Arial;");
    TextArea code = new TextArea(text);
    code.setEditable(false);
    code.setWrapText(false);
    code.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");
    VBox root = new VBox(10, heading, code);
    root.setPadding(new Insets(16));
    VBox.setVgrow(code, Priority.ALWAYS);
    stage.setScene(new Scene(root, 1500, 1030));
    stage.show();
    root.applyCss();
    root.layout();
    WritableImage image = root.snapshot(null, null);
    BufferedImage b =
        new BufferedImage(
            (int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < b.getHeight(); y++)
      for (int x = 0; x < b.getWidth(); x++) b.setRGB(x, y, image.getPixelReader().getArgb(x, y));
    Files.createDirectories(out.getParent());
    ImageIO.write(b, "png", out.toFile());
    images++;
  }
}
