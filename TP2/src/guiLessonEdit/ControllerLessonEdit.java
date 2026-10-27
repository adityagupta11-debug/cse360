package guiLessonEdit;

import guiLessonSupport.*;

/**
 * KG03/KG08 view events delegate to the model; errors preserve all four input values.
 *
 * @author Kanish Garg
 */
public final class ControllerLessonEdit {
  private final ModelLessonEdit model;
  private final ViewLessonEdit view;

  public ControllerLessonEdit(ModelLessonEdit model, ViewLessonEdit view) {
    this.model = model;
    this.view = view;
  }

  public void load() {
    try {
      view.populate(model.load(view.id));
    } catch (Exception e) {
      WindowLayout.failure(view.status, e);
    }
  }

  public void save() {
    try {
      model.save(view.id, view.fields.draft());
      guiLessonDetails.ViewLessonDetails.display(view.context, view.id, "Lesson changes saved.");
    } catch (Exception e) {
      WindowLayout.failure(view.status, e);
    }
  }
}
