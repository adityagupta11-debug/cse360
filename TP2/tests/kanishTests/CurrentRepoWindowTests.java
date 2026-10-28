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

  /** Allow the real native window resize and JavaFX layout pulse to complete before capture. */
  private static void waitForResize() {
    Object key = new Object();
    javafx.animation.PauseTransition pause =
        new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
    pause.setOnFinished(e -> Platform.exitNestedEventLoop(key, null));
    pause.play();
    Platform.enterNestedEventLoop(key);
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
  private static ListView<LessonsLearned> list() {
    return (ListView<LessonsLearned>) root().lookup("#home-lessons");
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
    check(
        "VG10_UI_BrowseDoesNotCoverLessonRows",
        !list().getBoundsInParent().intersects(button("home-browse").getBoundsInParent()));
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
        "KG04_UI_RealBackendEnabled",
        !button("details-experience").isDisabled()
            && ((Label) root().lookup("#lock-explanation")).getText().contains("No locks"));
    snapshot("KG02_Real_View.png");
    long parentId =
        db.getLessonsByOwner("kanishDemo").getLessons().stream()
            .filter(x -> x.getTitle().equals("Test the integrated GUI"))
            .findFirst()
            .orElseThrow()
            .getLessonId();
    button("details-experience").fire();
    text("experience-what").setText("Ran the real integrated GUI");
    text("experience-how").setText("Saved through the shared Foundations database");
    text("experience-duration").setText("2.5");
    button("experience-save").fire();
    check(
        "VG10_UI_EmptyExperienceRowsRejected",
        status().getText().contains("at least one") && db.getExperiences((int) parentId).isEmpty());
    text("effort-member").setText("Kanish");
    text("effort-hours").setText("1.25");
    button("effort-add").fire();
    @SuppressWarnings("unchecked")
    TableView<String[]> effort = (TableView<String[]>) root().lookup("#effort-table");
    text("effort-member").setText("kanish");
    text("effort-hours").setText("1");
    button("effort-add").fire();
    check(
        "KG04_UI_DuplicateMemberRejected",
        effort.getItems().size() == 1 && status().getText().contains("already"));
    button("experience-save").fire();
    check(
        "KG04_UI_PendingRowPreventsSave",
        db.getExperiences((int) parentId).isEmpty()
            && status().getText().contains("before saving"));
    button("effort-clear").fire();
    text("effort-member").setText("Joshua");
    text("effort-hours").setText("1.0001");
    button("effort-add").fire();
    check(
        "KG04_UI_PrecisionRejectedBeforeRowAdded",
        effort.getItems().size() == 1 && status().getText().contains("three decimal"));
    text("effort-hours").setText("0.5");
    button("effort-add").fire();
    effort.getSelectionModel().select(1);
    text("effort-hours").setText("0.75");
    button("effort-add").fire();
    check(
        "KG04_UI_SelectedRowUpdated",
        effort.getItems().size() == 2 && effort.getItems().get(1)[1].equals("0.75"));
    effort.getSelectionModel().select(1);
    button("effort-remove").fire();
    check(
        "KG04_UI_SelectedRowRemoved",
        effort.getItems().size() == 1 && text("effort-member").getText().isEmpty());
    stage.setWidth(740);
    stage.setHeight(600);
    waitForResize();
    root();
    check(
        "KG06_UI_ExperienceScrollableAtMinimumSize",
        root().getLayoutBounds().getWidth() < 750
            && root().lookup(".card-scroll") instanceof ScrollPane
            && root().lookup("#nav-back").isVisible());
    snapshot("KG04_Real_Experience_Minimum_Size.png");
    stage.setWidth(1000);
    stage.setHeight(760);
    waitForResize();
    snapshot("VG10_Real_Experience_Entry.png");
    ((ScrollPane) root().lookup(".card-scroll")).setVvalue(1);
    snapshot("KG04_Real_Experience_Actions.png");
    button("experience-save").fire();
    check(
        "VG10_UI_ExperienceActuallyPersisted",
        db.getExperiences((int) parentId).size() == 1 && status().getText().contains("saved"));
    snapshot("VG10_Real_Experience_Saved.png");

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
    button("nav-back").fire(); // Edit Back returns to Details; Details Back returns Home.
    button("home-browse").fire();
    check("VG10_UI_JoshuaListPreserved", root().lookup("#my-lessons-list") != null);
    text("my-lessons-search").setText("no such keyword");
    button("my-lessons-search-button").fire();
    check(
        "VG10_UI_JoshuaSearchWorks",
        ((ListView<?>) root().lookup("#my-lessons-list")).getItems().isEmpty());
    button("my-lessons-clear").fire();
    ((ListView<?>) root().lookup("#my-lessons-list")).getSelectionModel().select(0);
    button("my-lessons-open").fire();
    check("VG10_UI_ListOpensIntegratedDetail", button("details-experience") != null);
    button("nav-logout").fire();
    check("KG02_UI_RealLogoutClearsSession", db.getAuthenticatedContributorName() == null);
  }

  /** VG10: real curator changes travel through the persistent adapter to read-only GUI controls. */
  public static void testRealStorageLocks() throws Exception {
    db.register(
        new User(
            "curatorDemo",
            "DemoPass1!",
            "Curator",
            "",
            "Fixture",
            "",
            "curator@example.test",
            false,
            false,
            false,
            true));
    db.authenticateSession("kanishDemo", "DemoPass1!");
    int id = db.getLessonsByOwner("kanishDemo").get(0).getLessonId();
    db.authenticateSession("curatorDemo", "DemoPass1!");
    db.setFieldLock(id, "Title", true);
    db.setCuratorEdited(id, true);
    db.authenticateSession("kanishDemo", "DemoPass1!");
    WindowContext c =
        new WindowContext(
            stage,
            new CurrentRepoGateway(db),
            () -> guiContributor.ViewContributorHome.displayContributorHome(stage, actor),
            () -> db.clearAuthenticatedSession(),
            () -> {});
    ContributorWindows.edit(c, id);
    check(
        "VG10_UI_RealStoredFieldReadOnly",
        !text("lesson-title").isEditable() && text("lesson-situation").isEditable());
    snapshot("VG10_Real_Stored_Field_Lock.png");
    ContributorWindows.details(c, id);
    check(
        "VG10_UI_RealFieldLockDisablesDelete",
        button("details-delete").isDisabled() && !button("details-edit").isDisabled());
    check("VG10_UI_RealCuratorEditedFlag", new CurrentRepoGateway(db).read(id).curatorEdited());
    ContributorWindows.experience(c, id);
    check("KG04_UI_FieldLockStillAllowsExperience", !button("experience-save").isDisabled());
    text("experience-what").setText("A draft entered before a curator locked the lesson");
    text("experience-how").setText("Verified that current storage state wins over stale widgets");
    text("experience-duration").setText("1");
    text("effort-member").setText("Kanish");
    text("effort-hours").setText("1");
    button("effort-add").fire();
    db.authenticateSession("curatorDemo", "DemoPass1!");
    db.setLessonLock(id, true);
    db.authenticateSession("kanishDemo", "DemoPass1!");
    button("experience-save").fire();
    check(
        "KG04_UI_LockAppliedAfterOpeningRejectsSave",
        db.getExperiences(id).isEmpty()
            && status().getText().contains("locked")
            && !text("experience-what").getText().isBlank());
    ContributorWindows.experience(c, id);
    check(
        "KG04_UI_LockedExperienceIsReadOnly",
        button("experience-save").isDisabled()
            && !text("experience-what").isEditable()
            && button("effort-add").isDisabled());
    ContributorWindows.details(c, id);
    check(
        "VG10_UI_RealWholeLockDisablesActions",
        button("details-delete").isDisabled()
            && button("details-edit").isDisabled()
            && button("details-experience").isDisabled());
    snapshot("VG10_Real_Stored_Whole_Lock.png");
  }

  /**
   * KG03/KG04: these cases use an in-memory full-TP2 fixture ONLY, to isolate each GUI state from
   * storage. The live tests above use the real persistent experience backend. They prove the GUI
   * consumes supplied states correctly.
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
        () ->
            Platform.runLater(
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
                    db.authenticateSession("kanishDemo", "DemoPass1!");
                    db.addLesson(
                        new LessonsLearned(
                            "kanishDemo",
                            "Seed own lesson",
                            "Testing",
                            "A seed lesson provides a selection in the own list.",
                            "Use isolated fixtures when testing a Contributor workflow."));
                    db.authenticateSession("otherDemo", "DemoPass1!");
                    db.addLesson(
                        new LessonsLearned(
                            "otherDemo",
                            "Other user lesson",
                            "Testing",
                            "Another owner's lesson must not be exposed by the own list.",
                            "Check ownership before showing any selected record text."));
                    db.authenticateSession("kanishDemo", "DemoPass1!");
                    stage = new Stage();
                    testLiveCrud();
                    testRealStorageLocks();
                    testMissingBackendContracts();
                    stage.close();
                    db.closeConnection();
                    done.complete(null);
                  } catch (Throwable e) {
                    if (stage != null) stage.close();
                    if (db != null) db.closeConnection();
                    done.completeExceptionally(e);
                  }
                }));
    try {
      done.get(90, TimeUnit.SECONDS);
    } finally {
      Platform.exit();
    }
    System.out.println(
        "RESULT: "
            + passed
            + " passed; 0 failed. Real persistent app navigation plus explicitly marked model"
            + " fixtures.");
  }
}
