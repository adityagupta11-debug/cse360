package guiLessonDelete;

import guiLessonSupport.*;

/**
 * KG07 deletion occurs only from the explicit Yes action. @author Kanish Garg *
 *
 * <p>Attributes and rationale: model is the persistence/validation boundary; view is the current
 * window whose draft and status are updated. This separation follows Foundations MVC and the Kanish
 * Garg GUI assignment in TP2 Work Split. Controllers do not store a SQL connection, edit the
 * authenticated identity, or modify another member's data implementation.
 */
public final class ControllerLessonDelete {
  private final ModelLessonDelete model;
  private final ViewLessonDelete view;

  public ControllerLessonDelete(ModelLessonDelete model, ViewLessonDelete view) {
    this.model = model;
    this.view = view;
  }

  /** Refresh confirmation identity and lock state. */
  public void load() {
    try {
      view.populate(model.load(view.id));
    } catch (Exception e) {
      WindowLayout.failure(view.status, e);
    }
  }

  /** Explicit Yes; refusal or exception remains on the confirmation with a correction. */
  public void confirm() {
    try {
      if (model.remove(view.id, true)) view.context.listHome().run();
      else view.status.setText("The lesson was kept. Return to My Lessons and refresh.");
    } catch (Exception e) {
      WindowLayout.failure(view.status, e);
    }
  }

  /** No navigates back without invoking storage. */
  public void cancel() {
    ContributorWindows.details(view.context, view.id);
  }
}
