package guiLessonEdit;

import guiLessonSupport.*;

/**
 * KG01/KG03 update model; the gateway supplies fresh identity/locks before a write.
 *
 * @author Kanish Garg
 */
public final class ModelLessonEdit {
  private final LessonGateway gateway;

  public ModelLessonEdit(LessonGateway gateway) {
    this.gateway = gateway;
  }

  public LessonGateway.Lesson load(long id) throws Exception {
    return gateway.read(id);
  }

  public void save(long id, LessonGateway.Draft draft) throws Exception {
    Rules.lesson(draft);
    Rules.editable(gateway.read(id), draft);
    gateway.update(id, draft);
  }
}
