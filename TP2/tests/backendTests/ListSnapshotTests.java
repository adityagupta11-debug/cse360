package backendTests;

import database.Database;
import database.Database.LessonResult;
import entityClasses.LessonsLearned;
import entityClasses.LessonsLearnedList;
import entityClasses.User;

/**
 * Tests that persistent list CRUD retains the authoritative stored projection rather than the
 * caller's mutable draft. Each PASS requires the actual in-memory snapshot and H2 row to agree; any
 * failed assertion exits nonzero. Uses only registered/authenticated fixture accounts and a private
 * in-memory database. Local VG01/VG03 identifiers map to Contributor story 1 CRUD and story 2
 * curator locks; they are not official course story numbers.
 *
 * @author Vishwam
 */
public final class ListSnapshotTests {
  private static int passed;
  private static final String PASSWORD = "ListFixture1!";
  private static final String SITUATION =
      "The contributor saved a lesson through a persistent list.";
  private static final String LESSON =
      "Retain the canonical stored snapshot after every successful write.";

  private static void check(String name, boolean condition) {
    if (!condition) throw new AssertionError(name);
    System.out.println(name + " | PASS");
    passed++;
  }

  private static void login(Database database, String name) {
    if (!database.authenticateSession(name, PASSWORD))
      throw new AssertionError("Fixture authentication failed: " + name);
  }

  /**
   * VG01: add a padded draft containing fake locks and curator state; the list must contain a
   * separate stored record with trimmed fields and database-controlled metadata. Mutating the
   * original draft afterward must not change the list.
   */
  private static int testAddUsesStoredSnapshot(Database database, LessonsLearnedList list) {
    LessonsLearned draft =
        new LessonsLearned(
            "listOwner",
            "  Canonical list lesson  ",
            "Testing",
            "  " + SITUATION + "  ",
            "  " + LESSON + "  ");
    draft.setLessonLocked(true);
    draft.setTitleLocked(true);
    draft.setCuratorEdited(true);
    check("VG01_ListAddSucceeds", list.addLesson(database, draft) == LessonResult.SUCCESS);
    LessonsLearned saved = list.get(0);
    check("VG01_ListAddUsesSeparateStoredObject", saved != draft);
    check(
        "VG01_ListAddCanonicalCoreFields",
        saved.getTitle().equals("Canonical list lesson")
            && saved.getSituation().equals(SITUATION)
            && saved.getLessonText().equals(LESSON));
    check("VG01_ListAddRejectsFakeLockMetadata", !saved.hasAnyLock() && !saved.isCuratorEdited());
    check(
        "VG01_ListAddIncludesStoredIdentityAndTimestamps",
        saved.getLessonId() == draft.getLessonId()
            && saved.getCreatedAt() != null
            && saved.getUpdatedAt() != null);
    draft.setTitle("Later mutation of caller draft");
    check(
        "VG01_ListDoesNotRetainMutableCallerAlias",
        saved.getTitle().equals("Canonical list lesson"));
    return saved.getLessonId();
  }

  /**
   * VG03: update retains canonical data, ignores forged metadata and refreshes actual stored
   * curator/field-lock values. A refused edit must preserve the previously stored list item.
   */
  private static void testUpdateUsesStoredSnapshot(
      Database database, LessonsLearnedList list, int id) {
    LessonsLearned edited =
        new LessonsLearned(
            id, "listOwner", "  Updated canonical lesson  ", "Testing", SITUATION, LESSON);
    edited.setLessonLocked(true);
    edited.setCuratorEdited(true);
    check(
        "VG03_ListUpdateSucceeds",
        list.updateLesson(database, edited, "listOwner") == LessonResult.SUCCESS);
    LessonsLearned saved = list.findById(id);
    check(
        "VG03_ListUpdateCanonicalDataAndFlags",
        saved != edited
            && saved.getTitle().equals("Updated canonical lesson")
            && !saved.hasAnyLock()
            && !saved.isCuratorEdited());
    login(database, "listCurator");
    check(
        "VG03_FixtureStoresRealFieldLock",
        database.setFieldLock(id, "Title", true) == LessonResult.SUCCESS);
    check(
        "VG03_FixtureStoresRealCuratorFlag",
        database.setCuratorEdited(id, true) == LessonResult.SUCCESS);
    login(database, "listOwner");
    LessonsLearned another =
        new LessonsLearned(
            id,
            "listOwner",
            "Updated canonical lesson",
            "Testing",
            SITUATION + " Updated.",
            LESSON);
    check(
        "VG03_ListUpdateAllowsOtherUnlockedField",
        list.updateLesson(database, another, "listOwner") == LessonResult.SUCCESS);
    saved = list.findById(id);
    check(
        "VG03_ListUpdateRefreshesRealStoredMetadata",
        saved.isTitleLocked()
            && saved.isCuratorEdited()
            && saved.getSituation().endsWith(" Updated."));
    another.setTitle("Forbidden locked title replacement");
    check(
        "VG03_RefusedListUpdatePreservesSnapshot",
        list.updateLesson(database, another, "listOwner") == LessonResult.LOCKED
            && list.findById(id) == saved
            && saved.getTitle().equals("Updated canonical lesson"));
  }

  /** Run both requirement groups; close the isolated database even if a check fails. */
  public static void main(String[] args) throws Exception {
    Database database = new Database("jdbc:h2:mem:listSnapshotAudit");
    database.connectToDatabase();
    try {
      database.register(
          new User(
              "listOwner",
              PASSWORD,
              "List",
              "",
              "Owner",
              "",
              "owner@example.test",
              false,
              true,
              false,
              false));
      database.register(
          new User(
              "listCurator",
              PASSWORD,
              "List",
              "",
              "Curator",
              "",
              "curator@example.test",
              false,
              false,
              false,
              true));
      login(database, "listOwner");
      LessonsLearnedList list = new LessonsLearnedList();
      int id = testAddUsesStoredSnapshot(database, list);
      testUpdateUsesStoredSnapshot(database, list, id);
    } finally {
      database.closeConnection();
    }
    System.out.println(
        "RESULT: " + passed + " passed; 0 failed. Real authenticated persistent list snapshots.");
  }
}
