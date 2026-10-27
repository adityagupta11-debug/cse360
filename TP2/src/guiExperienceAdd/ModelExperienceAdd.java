package guiExperienceAdd;

import guiLessonSupport.*;
import java.util.List;

/**
 * KG04 validates linked experience input, including the member-and-hours table. The UI cannot
 * manufacture a saved identity when experience storage is not connected.
 *
 * @author Kanish Garg *
 *     <p>Attribute and rationale: gateway is the session-bound interface supplied by the data
 *     owner. It lets GUI validation and lock checks be exercised separately while keeping
 *     persistence/authorization in Vishwam's methods, as required by TP2 Work Split.
 */
public final class ModelExperienceAdd {
  private final LessonGateway gateway;

  public ModelExperienceAdd(LessonGateway gateway) {
    this.gateway = gateway;
  }

  /** Read the stored parent, checking ownership before opening Add Experience. */
  public LessonGateway.Lesson load(long id) throws Exception {
    return gateway.read(id);
  }

  /** Validate, refresh authorization/lock state and delegate the linked write. */
  public long save(long id, String what, String how, String duration, List<String[]> rows)
      throws Exception {
    var input = Rules.experience(what, how, duration, rows);
    if (!gateway.supportsExperiences())
      throw new IllegalStateException(
          "Experience saving is not connected yet. Ask the team to connect the experience data"
              + " service.");
    if (gateway.read(id).lessonLocked())
      throw new IllegalArgumentException(
          "This lesson is locked. Return to the lesson before adding experience.");
    return gateway.addExperience(id, input);
  }
}
