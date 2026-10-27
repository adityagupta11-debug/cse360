package guiExperienceAdd;

import guiLessonSupport.*;

/**
 * KG04 handles table row entry and persistent experience submission. @author Kanish Garg *
 *
 * <p>Attributes and rationale: model is the persistence/validation boundary; view is the current
 * window whose draft and status are updated. This separation follows Foundations MVC and the Kanish
 * Garg GUI assignment in TP2 Work Split. Controllers do not store a SQL connection, edit the
 * authenticated identity, or modify another member's data implementation.
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
