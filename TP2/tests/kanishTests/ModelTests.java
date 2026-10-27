package kanishTests;

import guiLessonSupport.*;
import java.util.*;

/**
 * Semi-automated GUI-model contracts, using an explicitly nonpersistent full-TP2 fixture. Each
 * documented test names the local requirement, exercise and pass interpretation.
 *
 * @author Kanish Garg
 */
public final class ModelTests {
  private static int passed, failed;

  @FunctionalInterface
  interface Operation {
    void run() throws Exception;
  }

  private static void check(boolean ok) {
    if (!ok) throw new AssertionError("Expected state missing");
  }

  private static void reject(Operation op, String fragment) throws Exception {
    try {
      op.run();
    } catch (IllegalArgumentException | IllegalStateException e) {
      check(e.getMessage().contains(fragment));
      return;
    }
    throw new AssertionError("Expected rejection: " + fragment);
  }

  private static void run(String id, Operation op) {
    try {
      op.run();
      passed++;
      System.out.println(id + " | PASS");
    } catch (Throwable e) {
      failed++;
      System.out.println(id + " | FAIL | " + e);
    }
  }

  public static LessonGateway.Draft valid() {
    return new LessonGateway.Draft(
        "Check all four fields",
        "Testing",
        "An integration changed the stored lesson fields.",
        "Validate and preserve all four fields during every update.");
  }

  private static List<String[]> rows() {
    return List.<String[]>of(new String[] {"Kanish Garg", "1.5"});
  }

  public static void main(String[] args) throws Exception {
    run("KG01_CreateValid", ModelTests::testKG01_CreateValid);
    run("KG01_UpdateValid", ModelTests::testKG01_UpdateValid);
    run("KG05_BlankTitle", ModelTests::testKG05_BlankTitle);
    run("KG05_TitleBounds", ModelTests::testKG05_TitleBounds);
    run("KG05_Category", ModelTests::testKG05_Category);
    run("KG05_Situation", ModelTests::testKG05_Situation);
    run("KG05_LessonText", ModelTests::testKG05_LessonText);
    run("KG03_TitleLock", ModelTests::testKG03_TitleLock);
    run("KG03_CategoryLock", ModelTests::testKG03_CategoryLock);
    run("KG03_SituationLock", ModelTests::testKG03_SituationLock);
    run("KG03_TextLock", ModelTests::testKG03_TextLock);
    run("KG03_OtherEditable", ModelTests::testKG03_OtherEditable);
    run("KG03_FullLock", ModelTests::testKG03_FullLock);
    run("KG07_DeleteNo", ModelTests::testKG07_DeleteNo);
    run("KG07_DeleteYes", ModelTests::testKG07_DeleteYes);
    run("KG07_AnyLock", ModelTests::testKG07_AnyLock);
    run("KG02_Denied", ModelTests::testKG02_Denied);
    run("KG04_ValidExperience", ModelTests::testKG04_ValidExperience);
    run("KG04_MissingText", ModelTests::testKG04_MissingText);
    run("KG04_Hours", ModelTests::testKG04_Hours);
    run("KG04_Members", ModelTests::testKG04_Members);
    run("KG08_FailedCreate", ModelTests::testKG08_FailedCreate);
    run("KG08_FailedEdit", ModelTests::testKG08_FailedEdit);
    System.out.println("RESULT: " + passed + " passed; " + failed + " failed. Test fixture only.");
    if (failed > 0) throw new AssertionError("Model tests failed");
  }

  /**
   * KG01. Exercise: valid four-field creation delegates once. PASS means the expected
   * state/rejection occurred; any failed assertion exits the suite unsuccessfully.
   */
  public static void testKG01_CreateValid() throws Exception {
    var g = new TestGateway();
    check(new guiLessonCreate.ModelLessonCreate(g).save(valid()) == 2 && g.creates == 1);
  }
  /**
   * KG01. Exercise: all four values update the existing identity. PASS means the expected
   * state/rejection occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG01_UpdateValid() throws Exception {
    var g = new TestGateway();
    new guiLessonEdit.ModelLessonEdit(g).save(1, valid());
    check(g.updates == 1 && g.lesson.category().equals("Testing") && g.lesson.id() == 1);
  }
  /**
   * KG05. Exercise: missing title does not write. PASS means the expected state/rejection occurred;
   * any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG05_BlankTitle() throws Exception {
    var g = new TestGateway();
    reject(
        () ->
            new guiLessonCreate.ModelLessonCreate(g)
                .save(
                    new LessonGateway.Draft(
                        "", "Testing", valid().situation(), valid().lessonText())),
        "Title");
    check(g.creates == 0);
  }
  /**
   * KG05. Exercise: repository title boundaries are reused. PASS means the expected state/rejection
   * occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG05_TitleBounds() throws Exception {
    Rules.lesson(
        new LessonGateway.Draft(
            "t".repeat(100), "Testing", valid().situation(), valid().lessonText()));
    reject(
        () ->
            Rules.lesson(
                new LessonGateway.Draft(
                    "tiny", "Testing", valid().situation(), valid().lessonText())),
        "5");
    reject(
        () ->
            Rules.lesson(
                new LessonGateway.Draft(
                    "t".repeat(101), "Testing", valid().situation(), valid().lessonText())),
        "100");
  }
  /**
   * KG05. Exercise: invalid category is rejected. PASS means the expected state/rejection occurred;
   * any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG05_Category() throws Exception {
    reject(
        () ->
            Rules.lesson(
                new LessonGateway.Draft(
                    valid().title(), "Unknown", valid().situation(), valid().lessonText())),
        "category");
  }
  /**
   * KG05. Exercise: short situation is rejected. PASS means the expected state/rejection occurred;
   * any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG05_Situation() throws Exception {
    reject(
        () ->
            Rules.lesson(
                new LessonGateway.Draft(valid().title(), "Testing", "short", valid().lessonText())),
        "20");
  }
  /**
   * KG05. Exercise: short lesson text is rejected. PASS means the expected state/rejection
   * occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG05_LessonText() throws Exception {
    reject(
        () ->
            Rules.lesson(
                new LessonGateway.Draft(valid().title(), "Testing", valid().situation(), "short")),
        "20");
  }
  /**
   * KG03. Exercise: locked title cannot change. PASS means the expected state/rejection occurred;
   * any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG03_TitleLock() throws Exception {
    var g = new TestGateway();
    g.reset(false, true, false, false, false);
    reject(() -> new guiLessonEdit.ModelLessonEdit(g).save(1, valid()), "Title is locked");
    check(g.updates == 0);
  }
  /**
   * KG03. Exercise: locked category cannot change. PASS means the expected state/rejection
   * occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG03_CategoryLock() throws Exception {
    var g = new TestGateway();
    g.reset(false, false, true, false, false);
    reject(
        () ->
            new guiLessonEdit.ModelLessonEdit(g)
                .save(
                    1,
                    new LessonGateway.Draft(
                        g.lesson.title(), "Testing", g.lesson.situation(), g.lesson.lessonText())),
        "Category is locked");
  }
  /**
   * KG03. Exercise: locked situation cannot change. PASS means the expected state/rejection
   * occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG03_SituationLock() throws Exception {
    var g = new TestGateway();
    g.reset(false, false, false, true, false);
    reject(
        () ->
            new guiLessonEdit.ModelLessonEdit(g)
                .save(
                    1,
                    new LessonGateway.Draft(
                        g.lesson.title(),
                        g.lesson.category(),
                        valid().situation(),
                        g.lesson.lessonText())),
        "Situation is locked");
  }
  /**
   * KG03. Exercise: locked lesson text cannot change. PASS means the expected state/rejection
   * occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG03_TextLock() throws Exception {
    var g = new TestGateway();
    g.reset(false, false, false, false, true);
    reject(
        () ->
            new guiLessonEdit.ModelLessonEdit(g)
                .save(
                    1,
                    new LessonGateway.Draft(
                        g.lesson.title(),
                        g.lesson.category(),
                        g.lesson.situation(),
                        valid().lessonText())),
        "Lesson text is locked");
  }
  /**
   * KG03. Exercise: unlocked fields remain editable beside a title lock. PASS means the expected
   * state/rejection occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG03_OtherEditable() throws Exception {
    var g = new TestGateway();
    g.reset(false, true, false, false, false);
    new guiLessonEdit.ModelLessonEdit(g)
        .save(
            1,
            new LessonGateway.Draft(
                g.lesson.title(), "Testing", valid().situation(), valid().lessonText()));
    check(g.updates == 1);
  }
  /**
   * KG03. Exercise: full lock defeats editing. PASS means the expected state/rejection occurred;
   * any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG03_FullLock() throws Exception {
    var g = new TestGateway();
    g.reset(true, false, false, false, false);
    reject(() -> new guiLessonEdit.ModelLessonEdit(g).save(1, valid()), "lesson is locked");
  }
  /**
   * KG07. Exercise: cancel never mutates. PASS means the expected state/rejection occurred; any
   * failed assertion exits the suite unsuccessfully.
   */

  public static void testKG07_DeleteNo() throws Exception {
    var g = new TestGateway();
    check(!new guiLessonDelete.ModelLessonDelete(g).remove(1, false) && g.deletes == 0);
  }
  /**
   * KG07. Exercise: explicit Yes removes unlocked data. PASS means the expected state/rejection
   * occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG07_DeleteYes() throws Exception {
    var g = new TestGateway();
    check(new guiLessonDelete.ModelLessonDelete(g).remove(1, true) && g.deletes == 1);
  }
  /**
   * KG07 KG03. Exercise: a category lock also prevents deletion. PASS means the expected
   * state/rejection occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG07_AnyLock() throws Exception {
    var g = new TestGateway();
    g.reset(false, false, true, false, false);
    reject(() -> new guiLessonDelete.ModelLessonDelete(g).remove(1, true), "locked");
  }
  /**
   * KG02. Exercise: inaccessible records do not load. PASS means the expected state/rejection
   * occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG02_Denied() throws Exception {
    var g = new TestGateway();
    reject(() -> new guiLessonDetails.ModelLessonDetails(g).load(99), "unavailable");
    g.authorized = false;
    reject(() -> new guiLessonDetails.ModelLessonDetails(g).load(1), "unavailable");
  }
  /**
   * KG04. Exercise: valid table values are delegated to a supplied experience backend. PASS means
   * the expected state/rejection occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG04_ValidExperience() throws Exception {
    var g = new TestGateway();
    long id =
        new guiExperienceAdd.ModelExperienceAdd(g)
            .save(1, "Ran regression tests", "Used isolated fixtures", "2.5", rows());
    check(
        id == 9
            && g.experiences == 1
            && g.experience.durationHours().toPlainString().equals("2.5"));
  }
  /**
   * KG04 KG05. Exercise: missing what/how is rejected. PASS means the expected state/rejection
   * occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG04_MissingText() throws Exception {
    reject(() -> Rules.experience("", "How", "1", rows()), "What was done");
    reject(() -> Rules.experience("What", "", "1", rows()), "How it was done");
  }
  /**
   * KG04 KG05. Exercise: zero duration and malformed hours are rejected. PASS means the expected
   * state/rejection occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG04_Hours() throws Exception {
    reject(() -> Rules.experience("What", "How", "0", rows()), "greater than zero");
    reject(() -> Rules.hours("Effort", "NaN", false), "decimal");
  }
  /**
   * KG04 KG05. Exercise: empty and duplicate member tables are rejected. PASS means the expected
   * state/rejection occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG04_Members() throws Exception {
    reject(() -> Rules.experience("What", "How", "1", List.of()), "at least one");
    reject(
        () ->
            Rules.experience(
                "What",
                "How",
                "1",
                List.of(new String[] {"Kanish", "1"}, new String[] {"kanish", "2"})),
        "once");
  }
  /**
   * KG08. Exercise: service failure does not report a created record. PASS means the expected
   * state/rejection occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG08_FailedCreate() throws Exception {
    var g = new TestGateway();
    g.fail = true;
    reject(() -> new guiLessonCreate.ModelLessonCreate(g).save(valid()), "unavailable");
    check(g.creates == 0);
  }
  /**
   * KG08. Exercise: service failure retains stored text. PASS means the expected state/rejection
   * occurred; any failed assertion exits the suite unsuccessfully.
   */

  public static void testKG08_FailedEdit() throws Exception {
    var g = new TestGateway();
    String old = g.lesson.title();
    g.fail = true;
    reject(() -> new guiLessonEdit.ModelLessonEdit(g).save(1, valid()), "unavailable");
    check(g.lesson.title().equals(old) && g.updates == 0);
  }
}
