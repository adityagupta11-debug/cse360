package guiLessonCreate;

import guiLessonSupport.*;

/**
 * KG01/KG08 maps create events to the model and retains rejected drafts in the view.
 *
 * @author Kanish Garg
 */
public final class ControllerLessonCreate {
  private final ModelLessonCreate model;
  private final ViewLessonCreate view;

  public ControllerLessonCreate(ModelLessonCreate model, ViewLessonCreate view) {
    this.model = model;
    this.view = view;
  }

  public void save() {
    try {
      long id = model.save(view.fields.draft());
      guiLessonDetails.ViewLessonDetails.display(view.context, id, "Lesson created successfully.");
    } catch (Exception e) {
      WindowLayout.failure(view.status, e);
    }
  }
}
