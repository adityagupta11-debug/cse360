package testingAutomation;

import database.Database;
import database.Database.LessonResult;
import entityClasses.*;

/**
 * Authenticated fixtures for inherited lesson/list regression drivers. Older tests wrote records
 * without accounts and inspected every owner through raw reads. These helpers explicitly sign in as
 * the fixture owner for setup/inspection, restoring jsmith afterward; operations under test
 * (updates/deletes) still call the real guarded Database directly. BackendTests separately tests
 * unauthenticated and forged-identity attacks without these helpers. Never used by GUI code.
 *
 * @author Vishwam
 */
public final class LessonTestFixtures {
  private static final String PASSWORD = "RegressionPass1!";

  private LessonTestFixtures() {}

  /** Register isolated test identities and authenticate the default Contributor. */
  public static void prepare(Database db) throws Exception {
    for (String name : new String[] {"jsmith", "akim", "newcontributor", "fixtureAdmin"}) {
      if (!db.doesUserExist(name))
        db.register(
            new User(
                name,
                PASSWORD,
                "Test",
                "",
                "User",
                "",
                name + "@example.test",
                name.equals("fixtureAdmin"),
                true,
                false,
                name.equals("fixtureAdmin")));
    }
    as(db, "jsmith");
  }

  /** Explicitly authenticate a test identity; failures must fail the suite. */
  public static void as(Database db, String name) {
    if (!db.authenticateSession(name, PASSWORD))
      throw new AssertionError("Fixture login failed: " + name);
  }

  /** Seed/validate an add as its real owner, then restore the default actor. */
  public static LessonResult add(Database db, LessonsLearned lesson) {
    as(db, lesson.getOwner());
    try {
      return db.addLesson(lesson);
    } finally {
      as(db, "jsmith");
    }
  }

  /** Inspect each owner's own projection explicitly; does not bypass storage authorization. */
  public static LessonsLearned read(Database db, int id) {
    try {
      for (String owner : new String[] {"jsmith", "akim"}) {
        as(db, owner);
        var s = db.getLesson(id);
        if (s != null) return s;
      }
      return null;
    } finally {
      as(db, "jsmith");
    }
  }

  /** A fixture owner reads its own list, then restores the operation-under-test actor. */
  public static LessonsLearnedList list(Database db, String owner) {
    as(db, owner);
    try {
      return db.getLessonsByOwner(owner);
    } finally {
      as(db, "jsmith");
    }
  }

  /** Only the fixture administrator may inspect the persistent all-owner list. */
  public static LessonsLearnedList all(Database db) {
    as(db, "fixtureAdmin");
    try {
      return db.getAllLessons();
    } finally {
      as(db, "jsmith");
    }
  }

  /** Fixture setup for curator-edited metadata, requiring the real curator authorization. */
  public static LessonResult edited(Database db, int id, boolean value) {
    as(db, "fixtureAdmin");
    try {
      return db.setCuratorEdited(id, value);
    } finally {
      as(db, "jsmith");
    }
  }

  /** Fixture setup for whole-lesson locks. */
  public static LessonResult lock(Database db, int id, boolean value) {
    as(db, "fixtureAdmin");
    try {
      return db.setLessonLock(id, value);
    } finally {
      as(db, "jsmith");
    }
  }

  /** Fixture setup for per-field locks. */
  public static LessonResult fieldLock(Database db, int id, String field, boolean value) {
    as(db, "fixtureAdmin");
    try {
      return db.setFieldLock(id, field, value);
    } finally {
      as(db, "jsmith");
    }
  }
}
