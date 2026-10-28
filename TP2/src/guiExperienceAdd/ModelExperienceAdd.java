package guiExperienceAdd;

import guiLessonSupport.*;
import java.util.List;

/**
 * Validates what/how, duration and member effort for KG04 using the shared experience rules. The
 * session-bound gateway rechecks ownership and current locks before the persistent write.
 *
 * @author Kanish Garg
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
