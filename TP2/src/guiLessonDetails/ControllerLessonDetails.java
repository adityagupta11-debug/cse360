package guiLessonDetails;

import guiLessonSupport.*;

/**
 * KG02 load and fail-closed behavior; inaccessible records have no active edit/delete actions.
 *
 * @author Kanish Garg *
 *     <p>Attributes and rationale: model is the persistence/validation boundary; view is the
 *     current window whose draft and status are updated. This separation follows Foundations MVC
 *     and the Kanish Garg GUI assignment in TP2 Work Split. Controllers do not store a SQL
 *     connection, edit the authenticated identity, or modify another member's data implementation.
 */
public final class ControllerLessonDetails {
  private final ModelLessonDetails model;
  private final ViewLessonDetails view;

  public ControllerLessonDetails(ModelLessonDetails model, ViewLessonDetails view) {
    this.model = model;
    this.view = view;
  }

  /** Show current data only after successful authorization. */
  public void load() {
    try {
      view.populate(model.load(view.id));
    } catch (Exception e) {
      WindowLayout.failure(view.status, e);
    }
  }
}
