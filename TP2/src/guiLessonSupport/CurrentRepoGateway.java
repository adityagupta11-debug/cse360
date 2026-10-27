package guiLessonSupport;

import database.Database;
import lessonRecognizer.LessonRecognizer;

/**
 * Uses the current repository's Database methods, not the incompatible older ZIP API. The acting
 * contributor is obtained from the stable authenticated session on every call, never from widgets
 * or Database's mutable account-details cache. Missing TP2 capabilities are explicit rather than
 * guessed from absent fields. @author Kanish Garg
 */
public final class CurrentRepoGateway implements LessonGateway {
  private final Database database;
  private final AdditionalAccess additional;

  /**
   * Integration point for the data owner's missing full-TP2 behavior. Every implementation must
   * authorize the real session and enforce locks at the write in persistent storage.
   */
  public interface AdditionalAccess {
    LessonGateway.Lesson read(long id) throws Exception;

    void update(long id, Draft draft) throws Exception;

    boolean delete(long id, boolean confirmed) throws Exception;

    long addExperience(long lessonId, Experience experience) throws Exception;
  }

  public CurrentRepoGateway(Database database) {
    this(database, null);
  }

  public CurrentRepoGateway(Database database, AdditionalAccess additional) {
    this.database = java.util.Objects.requireNonNull(database);
    this.additional = additional;
  }

  private String actor() throws Exception {
    String name = database.getAuthenticatedContributorName();
    if (name == null)
      throw new IllegalStateException(
          "Sign in with an active Contributor account, then reopen My Lessons.");
    return name;
  }

  /** KG02 checks ownership before returning text; raw getLesson is not authorization scoped. */
  public LessonGateway.Lesson read(long id) throws Exception {
    String actor = actor();
    if (id <= 0 || id > Integer.MAX_VALUE) throw unavailable();
    entityClasses.Lesson s = database.getLesson((int) id);
    if (s == null || !actor.equals(s.getOwner())) throw unavailable();
    if (additional != null) return additional.read(id);
    return new LessonGateway.Lesson(
        s.getLessonId(),
        s.getOwner(),
        s.getTitle(),
        s.getCategory(),
        s.getSituation(),
        s.getLessonText(),
        false,
        false,
        false,
        false,
        false,
        false,
        null,
        null);
  }

  public long create(Draft d) throws Exception {
    Rules.lesson(d);
    entityClasses.Lesson s =
        new entityClasses.Lesson(actor(), d.title(), d.category(), d.situation(), d.lessonText());
    requireSuccess(database.addLesson(s));
    if (s.getLessonId() <= 0)
      throw new IllegalStateException(
          "The save returned no lesson identity. Refresh My Lessons before trying another create.");
    return s.getLessonId();
  }

  public void update(long id, Draft d) throws Exception {
    Rules.lesson(d);
    read(id);
    String actor = actor();
    if (additional != null) {
      additional.update(id, d);
      return;
    }
    requireSuccess(
        database.updateLesson(
            new entityClasses.Lesson(
                (int) id, actor, d.title(), d.category(), d.situation(), d.lessonText()),
            actor));
  }

  public boolean delete(long id, boolean confirmed) throws Exception {
    if (!confirmed) return false;
    read(id);
    String actor = actor();
    if (additional != null) return additional.delete(id, true);
    requireSuccess(database.deleteLesson((int) id, actor, true));
    return true;
  }

  public boolean supportsLocks() {
    return additional != null;
  }

  public boolean supportsExperiences() {
    return additional != null;
  }

  public long addExperience(long id, Experience e) throws Exception {
    read(id);
    if (additional == null)
      throw new IllegalStateException(
          "Experience storage is not included in this repository yet. Ask the data owner to connect"
              + " it.");
    return additional.addExperience(id, e);
  }

  private static IllegalArgumentException unavailable() {
    return new IllegalArgumentException(
        "Lesson is unavailable. Refresh your own lesson list and select it again.");
  }

  private static void requireSuccess(Database.LessonResult result) {
    if (result == Database.LessonResult.SUCCESS) return;
    if (result == Database.LessonResult.INVALID_INPUT)
      throw new IllegalArgumentException(LessonRecognizer.lessonRecognizerErrorMessage);
    if (result == Database.LessonResult.DATABASE_ERROR)
      throw new IllegalStateException(
          "The database could not complete this save. Your input is kept; retry after the database"
              + " is available.");
    throw new IllegalArgumentException(result.getMessage());
  }
}
