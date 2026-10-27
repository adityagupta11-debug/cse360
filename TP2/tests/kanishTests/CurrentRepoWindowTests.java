package kanishTests;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.*;
import guiLessonSupport.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 * Actual JavaFX navigation from Joshua's Contributor Home into Kanish's windows using the real
 * current Database. Extra lock/experience GUI contracts are separately marked fixtures.
 *
 * @author Kanish Garg
 */
public final class CurrentRepoWindowTests {
  private static Stage stage;
  private static Database db;
  private static User actor;
  private static int passed;

  private static Parent root() {
    Parent r = stage.getScene().getRoot();
    r.applyCss();
    r.layout();
    return r;
  }

  private static Button button(String id) {
    return (Button) root().lookup("#" + id);
  }

  private static TextInputControl text(String id) {
    return (TextInputControl) root().lookup("#" + id);
  }

  private static Label status() {
    return (Label) root().lookup("#window-status");
  }

  @SuppressWarnings("unchecked")
  private static ComboBox<String> category() {
    return (ComboBox<String>) root().lookup("#lesson-category");
  }

  @SuppressWarnings("unchecked")
  private static ListView<Lesson> list() {
    return (ListView<Lesson>) root().lookup("#home-lessons");
  }

  private static void check(String id, boolean ok) {
    if (!ok) throw new AssertionError(id);
    passed++;
    System.out.println(id + " | PASS");
  }

  private static void fill(String title) {
    text("lesson-title").setText(title);
    category().setValue("Testing");
    text("lesson-situation").setText("The team changed a shared API during integration.");
    text("lesson-text").setText("Agree interface changes before merging code from teammates.");
  }

  /**
   * KG01/KG05/KG06: real Home New/View/Edit navigation, field preservation and persistence. A PASS
   * line checks both actual widgets and the current database, not a fake CRUD adapter.
   */
  public static void testLiveCrud() throws Exception {
    guiContributor.ViewContributorHome.displayContributorHome(stage, actor);
    check(
        "KG02_UI_HomeOnlyOwnLessons",
        list().getItems().size() == 1
            && list().getItems().getFirst().getOwner().equals("kanishDemo"));
    check("KG06_UI_RealHomeRoutes", button("home-new") != null && button("home-view").isDisabled());
    snapshot("KG06_Real_Contributor_Home.png");
    button("home-new").fire();
    fill("");
    button("create-save").fire();
    check(
        "KG05_UI_InvalidDraftPreserved",
        status().getText().contains("Title")
            && category().getValue().equals("Testing")
            && !text("lesson-situation").getText().isBlank());
    fill("Test the integrated GUI");
    snapshot("KG01_Real_Create.png");
    button("create-save").fire();
    check(
        "KG01_UI_RealCreate",
        db.getLessonsByOwner("kanishDemo").size() == 2
            && !text("lesson-title").isEditable()
            && text("lesson-title").getText().equals("Test the integrated GUI"));
    check(
        "KG04_UI_MissingBackendDisabled",
        button("details-experience").isDisabled()
            && ((Label) root().lookup("#lock-explanation")).getText().contains("not implemented"));
    snapshot("KG02_Real_View.png");
    button("details-edit").fire();
    fill("Preserve the four lesson fields");
    category().setValue("Process");
    button("edit-save").fire();
    check(
        "KG01_UI_RealEdit",
        text("lesson-title").getText().equals("Preserve the four lesson fields")
            && category().getValue().equals("Process"));
    button("nav-back").fire();
    check("KG06_UI_BackReloadsRealHome", list().getItems().size() == 2);
    list()
        .getSelectionModel()
        .select(
            list().getItems().stream()
                .filter(x -> x.getTitle().equals("Preserve the four lesson fields"))
                .findFirst()
                .orElseThrow());
    button("home-view").fire();
    check("KG06_UI_HomeViewWired", button("details-edit") != null);
    button("details-delete").fire();
    check(
        "KG07_UI_NoDefault",
        button("delete-no").isDefaultButton() && !button("delete-yes").isDefaultButton());
    snapshot("KG07_Real_Delete.png");
    button("delete-no").fire();
    check("KG07_UI_NoKeepsRealData", db.getLessonsByOwner("kanishDemo").size() == 2);
    button("details-delete").fire();
    button("delete-yes").fire();
    check(
        "KG07_UI_YesDeletesRealData",
        list().getItems().size() == 1 && db.getLessonsByOwner("kanishDemo").size() == 1);
    list().getSelectionModel().select(0);
    button("home-edit").fire();
    check("KG06_UI_HomeEditWired", button("edit-save") != null);
    button("nav-back").fire();
    button("nav-logout").fire();
    check("KG02_UI_RealLogoutClearsSession", db.getAuthenticatedContributorName() == null);
  }

  /**
   * KG03/KG04: these cases use an in-memory full-TP2 fixture ONLY, because main has no
   * experience/lock storage. They prove the GUI consumes supplied states correctly.
   */
  public static void testMissingBackendContracts() throws Exception {
    TestGateway g = new TestGateway();
    WindowContext c = new WindowContext(stage, g, () -> {}, () -> {}, () -> {});
    g.reset(false, true, true, false, false);
    ContributorWindows.edit(c, 1);
    check(
        "KG03_FIXTURE_TitleCategoryReadOnly",
        !text("lesson-title").isEditable()
            && category().isDisabled()
            && text("lesson-situation").isEditable());
    snapshot("KG03_FIXTURE_Locked_Edit.png");
    g.reset(false, false, false, true, true);
    ContributorWindows.edit(c, 1);
    check(
        "KG03_FIXTURE_SituationTextReadOnly",
        !text("lesson-situation").isEditable()
            && !text("lesson-text").isEditable()
            && text("lesson-title").isEditable());
    g.reset(true, false, false, false, false);
    ContributorWindows.details(c, 1);
    check(
        "KG03_FIXTURE_FullLockActionsDisabled",
        button("details-edit").isDisabled() && button("details-delete").isDisabled());
    g.reset(false, false, false, false, false);
    ContributorWindows.experience(c, 1);
    text("experience-what").setText("Ran integration tests");
    text("experience-how").setText("Used controlled fixtures");
    text("experience-duration").setText("2.5");
    button("experience-save").fire();
    check(
        "KG04_FIXTURE_EmptyRowsRejected",
        status().getText().contains("at least one") && g.experiences == 0);
    text("effort-member").setText("Kanish Garg");
    text("effort-hours").setText("1.5");
    button("effort-add").fire();
    snapshot("KG04_FIXTURE_Experience_Top.png");
    for (Node n : root().lookupAll(".scroll-pane")) if (n instanceof ScrollPane s) s.setVvalue(1);
    snapshot("KG04_FIXTURE_Experience_Actions.png");
    button("experience-save").fire();
    check("KG04_FIXTURE_SaveDelegates", g.experiences == 1 && g.experience.efforts().size() == 1);
  }

  private static void snapshot(String name) throws Exception {
    WritableImage im = root().snapshot(null, null);
    BufferedImage out =
        new BufferedImage((int) im.getWidth(), (int) im.getHeight(), BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < out.getHeight(); y++)
      for (int x = 0; x < out.getWidth(); x++) out.setRGB(x, y, im.getPixelReader().getArgb(x, y));
    Path target = Path.of("Contributor_Javadocs/ui-evidence", name);
    Files.createDirectories(target.getParent());
    ImageIO.write(out, "png", target.toFile());
  }

  public static void main(String[] args) throws Exception {
    var done = new CompletableFuture<Void>();
    Platform.startup(
        () -> {
          try {
            Platform.setImplicitExit(false);
            db = new Database("jdbc:h2:mem:kgLiveWindows;DB_CLOSE_DELAY=-1");
            db.connectToDatabase();
            FoundationsMain.database = db;
            actor =
                new User(
                    "kanishDemo",
                    "DemoPass1!",
                    "Kanish",
                    "",
                    "Demo",
                    "",
                    "kg@example.test",
                    false,
                    true,
                    false,
                    false);
            db.register(actor);
            db.register(
                new User(
                    "otherDemo",
                    "DemoPass1!",
                    "Other",
                    "",
                    "Demo",
                    "",
                    "other@example.test",
                    false,
                    true,
                    false,
                    false));
            db.addLesson(
                new Lesson(
                    "kanishDemo",
                    "Seed own lesson",
                    "Testing",
                    "A seed lesson provides a selection in the own list.",
                    "Use isolated fixtures when testing a Contributor workflow."));
            db.addLesson(
                new Lesson(
                    "otherDemo",
                    "Other user lesson",
                    "Testing",
                    "Another owner's lesson must not be exposed by the own list.",
                    "Check ownership before showing any selected record text."));
            db.authenticateSession("kanishDemo", "DemoPass1!");
            stage = new Stage();
            testLiveCrud();
            testMissingBackendContracts();
            stage.close();
            db.closeConnection();
            done.complete(null);
          } catch (Throwable e) {
            if (stage != null) stage.close();
            if (db != null) db.closeConnection();
            done.completeExceptionally(e);
          }
        });
    try {
      done.get(90, TimeUnit.SECONDS);
    } finally {
      Platform.exit();
    }
    System.out.println(
        "RESULT: "
            + passed
            + " passed; 0 failed. Real app navigation plus explicitly marked missing-backend"
            + " fixtures.");
  }
}
