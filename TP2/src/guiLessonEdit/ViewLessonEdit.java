package guiLessonEdit;

import guiLessonSupport.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * KG03/KG06 editor with explicit full/per-field lock labels. The existing ID is immutable; fields
 * hold the user's draft, and the save button opens only after an authorized read.
 *
 * @author Kanish Garg
 */
public final class ViewLessonEdit {
  final WindowContext context;
  final long id;
  final LessonFields fields = new LessonFields();
  final Label status = new Label();
  private final Label note = WindowLayout.note("");
  private final Button save;

  private ViewLessonEdit(WindowContext c, long id) {
    context = c;
    this.id = id;
    fields.disableEditing();
    var controller = new ControllerLessonEdit(new ModelLessonEdit(c.gateway()), this);
    save = WindowLayout.button("Save changes", "edit-save", controller::save);
    WindowLayout.primary(save);
    save.setDisable(true);
    WindowLayout.show(
        c,
        "Edit Lesson",
        new VBox(12, WindowLayout.note("Editing lesson #" + id), fields, note, save),
        status,
        () -> ContributorWindows.details(c, id));
    controller.load();
  }

  public static void display(WindowContext c, long id) {
    new ViewLessonEdit(c, id);
  }

  void populate(LessonGateway.Lesson s) {
    fields.populate(s, true);
    save.setDisable(s.lessonLocked() || s.allFieldsLocked());
    note.setText(
        !context.gateway().supportsLocks()
            ? "Lock metadata is not implemented in this repository yet."
            : s.lessonLocked()
                ? "This lesson is locked. Return to View Lesson."
                : s.deletionLocked()
                    ? "Locked fields are read-only; only unlocked fields can be changed."
                    : "All fields are editable.");
  }
}
