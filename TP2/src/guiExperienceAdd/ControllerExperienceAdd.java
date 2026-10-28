package guiExperienceAdd;

import guiLessonSupport.*;

/**
 * Loads the authorized parent lesson and submits the experience draft through the model (KG04). The
 * view retains rejected input; success returns to lesson details only after storage succeeds.
 *
 * @author Kanish Garg
 */
public final class ControllerExperienceAdd {
  private final ModelExperienceAdd model;
  private final ViewExperienceAdd view;

  public ControllerExperienceAdd(ModelExperienceAdd model, ViewExperienceAdd view) {
    this.model = model;
    this.view = view;
  }

  /** Load parent identity and gate missing/locked backend capabilities. */
  public void load() {
    try {
      view.populate(model.load(view.id));
    } catch (Exception e) {
      WindowLayout.failure(view.status, e);
    }
  }

  /** Keep form contents on rejected input or service failure. */
  public void save() {
    try {
      long saved =
          model.save(
              view.id,
              view.what.getText(),
              view.how.getText(),
              view.duration.getText(),
              view.rows());
      guiLessonDetails.ViewLessonDetails.display(
          view.context, view.id, "Experience #" + saved + " saved for this lesson.");
    } catch (Exception e) {
      WindowLayout.failure(view.status, e);
    }
  }
}
