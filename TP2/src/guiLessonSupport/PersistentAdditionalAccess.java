package guiLessonSupport;

import database.Database;
import entityClasses.LessonExperience;

/*******
 * <p>Title: PersistentAdditionalAccess Class.</p>
 * <p>Description: VG10 connects Kanish's AdditionalAccess contract to the shared Foundations
 * database. All reads are Contributor-safe projections; all writes use authenticated storage
 * checks, including locks added after a screen loaded. No fixture or second database is used.</p>
 * @author Vishwam
 */
public final class PersistentAdditionalAccess implements CurrentRepoGateway.AdditionalAccess {
  private final Database database;

  /** The same session-bound Database instance used by the application. */
  public PersistentAdditionalAccess(Database database) {
    this.database = java.util.Objects.requireNonNull(database);
  }

  private int id(long value) {
    if (value <= 0 || value > Integer.MAX_VALUE)
      throw new IllegalArgumentException("Select an available lesson.");
    return (int) value;
  }

  /** VG02/VG10: map only the core fields, locks, curator-edit flag and timestamps. */
  public LessonGateway.Lesson read(long id) {
    var s = database.getLesson(id(id));
    if (s == null)
      throw new IllegalArgumentException("Lesson is unavailable. Refresh your own lesson list.");
    return new LessonGateway.Lesson(
        s.getLessonId(),
        s.getOwner(),
        s.getTitle(),
        s.getCategory(),
        s.getSituation(),
        s.getLessonText(),
        s.isLessonLocked(),
        s.isTitleLocked(),
        s.isCategoryLocked(),
        s.isSituationLocked(),
        s.isLessonTextLocked(),
        s.isCuratorEdited(),
        s.getCreatedAt(),
        s.getUpdatedAt());
  }

  /** VG03: storage rechecks current locks instead of trusting the loaded GUI state. */
  public void update(long id, LessonGateway.Draft draft) throws Exception {
    var actor = database.getAuthenticatedContributorName();
    CurrentRepoGateway.requireSuccess(
        database.updateLesson(
            new entityClasses.LessonsLearned(
                id(id),
                actor,
                draft.title(),
                draft.category(),
                draft.situation(),
                draft.lessonText()),
            actor));
  }

  /** VG04: explicit confirmation and storage-level ownership/lock checks. */
  public boolean delete(long id, boolean confirmed) throws Exception {
    if (!confirmed) return false;
    CurrentRepoGateway.requireSuccess(
        database.deleteLesson(id(id), database.getAuthenticatedContributorName(), true));
    return true;
  }

  /** VG05: immutable GUI inputs become one real persistent experience and its effort rows. */
  public long addExperience(long id, LessonGateway.Experience input) throws Exception {
    if (input == null)
      throw new IllegalArgumentException("Enter experience information before saving.");
    return database.addExperience(
        id(id),
        new LessonExperience(
            id(id),
            input.what(),
            input.how(),
            input.durationHours(),
            input.efforts().stream()
                .map(e -> new LessonExperience.Effort(e.member(), e.hours()))
                .toList()));
  }
}
