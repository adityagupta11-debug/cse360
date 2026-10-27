package guiLessonCreate;

import guiLessonSupport.*;

/**
 * KG01/KG05 create model: recognizer validation and session-bound service delegation. The gateway
 * attribute is supplied by the existing application; no second database is opened.
 *
 * @author Kanish Garg
 */
public final class ModelLessonCreate {
  private final LessonGateway gateway;

  public ModelLessonCreate(LessonGateway gateway) {
    this.gateway = gateway;
  }

  public long save(LessonGateway.Draft draft) throws Exception {
    Rules.lesson(draft);
    return gateway.create(draft);
  }
}
