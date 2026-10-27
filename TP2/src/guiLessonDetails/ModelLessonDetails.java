package guiLessonDetails;

import guiLessonSupport.*;

/**
 * KG02 authorized reads expose no curator-only data. @author Kanish Garg *
 *
 * <p>Attribute and rationale: gateway is the session-bound interface supplied by the data owner. It
 * lets GUI validation and lock checks be exercised separately while keeping
 * persistence/authorization in Vishwam's methods, as required by TP2 Work Split.
 */
public final class ModelLessonDetails {
  private final LessonGateway gateway;

  public ModelLessonDetails(LessonGateway gateway) {
    this.gateway = gateway;
  }

  /** Refresh the stored state rather than trusting list snapshots. */
  public LessonGateway.Lesson load(long id) throws Exception {
    return gateway.read(id);
  }
}
