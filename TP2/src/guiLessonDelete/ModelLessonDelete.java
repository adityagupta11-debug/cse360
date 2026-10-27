package guiLessonDelete;

import guiLessonSupport.*;

/**
 * KG07 safe deletion with a fresh read and backend confirmation. @author Kanish Garg *
 *
 * <p>Attribute and rationale: gateway is the session-bound interface supplied by the data owner. It
 * lets GUI validation and lock checks be exercised separately while keeping
 * persistence/authorization in Vishwam's methods, as required by TP2 Work Split.
 */
public final class ModelLessonDelete {
  private final LessonGateway gateway;

  public ModelLessonDelete(LessonGateway gateway) {
    this.gateway = gateway;
  }

  /** Load current title for the named confirmation and current locks. */
  public LessonGateway.Lesson load(long id) throws Exception {
    return gateway.read(id);
  }

  /** Cancellation makes no service call; any current lock prevents deletion. */
  public boolean remove(long id, boolean confirmed) throws Exception {
    if (!confirmed) return false;
    if (gateway.read(id).deletionLocked())
      throw new IllegalArgumentException(
          "Delete is unavailable because this lesson or one of its fields is locked.");
    return gateway.delete(id, true);
  }
}
