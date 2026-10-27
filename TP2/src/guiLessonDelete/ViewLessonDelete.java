package guiLessonDelete;

import guiLessonSupport.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * KG07 named Are you sure confirmation with No as the keyboard default. This page is the delete
 * window; opening it never performs a mutation.
 *
 * @author Kanish Garg *
 *     <p>Attributes and rationale: context retains the existing stage, authorized service and
 *     navigation callbacks so the screen shares the Foundations session. The stored id, where
 *     present, identifies the selected lesson independently of its editable title. JavaFX text
 *     controls hold the current draft so rejected requests do not discard input. Labels expose
 *     metadata, field names, lock explanations and corrective status. Buttons expose only the
 *     actions appropriate to the loaded lock/capability state. All values originate in the Kanish
 *     Garg GUI row of TP2 Work Split, the shared UX rules, or the authorized service response. No
 *     author input or curator-only field is retained in the view.
 */
public final class ViewLessonDelete {
  final WindowContext context;
  final long id;
  final Label status = new Label();
  private final Label question = WindowLayout.note("Loading selected lesson...");
  private final Button yes, no;

  private ViewLessonDelete(WindowContext c, long id) {
    this.context = c;
    this.id = id;
    ControllerLessonDelete controller =
        new ControllerLessonDelete(new ModelLessonDelete(c.gateway()), this);
    yes = WindowLayout.button("Yes, delete lesson", "delete-yes", controller::confirm);
    no = WindowLayout.button("No, keep lesson", "delete-no", controller::cancel);
    WindowLayout.danger(yes);
    yes.setDefaultButton(false);
    yes.setDisable(true);
    no.setDefaultButton(true);
    no.setCancelButton(true);
    VBox card =
        new VBox(
            14,
            WindowLayout.note("Are you sure?"),
            question,
            WindowLayout.note("This action removes the selected lesson. Choose No to keep it."),
            new HBox(12, no, yes));
    WindowLayout.show(c, "Delete Lesson", card, status, controller::cancel);
    controller.load();
    no.requestFocus();
  }

  /** Load a separate confirmation page. */
  public static void display(WindowContext c, long id) {
    new ViewLessonDelete(c, id);
  }

  void populate(LessonGateway.Lesson s) {
    question.setText("Delete lesson #" + s.id() + ": " + s.title() + "?");
    yes.setDisable(s.deletionLocked());
    if (s.deletionLocked())
      status.setText(
          "Delete is unavailable because this lesson or a field is locked. Choose No to return.");
  }
}
