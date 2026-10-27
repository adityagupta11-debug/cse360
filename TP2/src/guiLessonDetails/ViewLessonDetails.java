package guiLessonDetails;

import guiLessonSupport.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * KG02/KG03/KG06 four-field read view with current lock capabilities and navigation. No timestamps
 * or lock states are fabricated when the current data model lacks them.
 *
 * @author Kanish Garg
 */
public final class ViewLessonDetails {
  final WindowContext context;
  final long id;
  final Label status = new Label();
  private final LessonFields fields = new LessonFields();
  private final Label meta = WindowLayout.note(""), locks = WindowLayout.note("");
  private final Button edit, delete, experience;
  private boolean loaded;

  private ViewLessonDetails(WindowContext c, long id, String message) {
    context = c;
    this.id = id;
    fields.disableEditing();
    locks.setId("lock-explanation");
    locks.getStyleClass().add("lock-note");
    edit = WindowLayout.button("Edit lesson", "details-edit", () -> ContributorWindows.edit(c, id));
    delete =
        WindowLayout.button(
            "Delete lesson", "details-delete", () -> ContributorWindows.delete(c, id));
    experience =
        WindowLayout.button(
            "Add experience", "details-experience", () -> ContributorWindows.experience(c, id));
    WindowLayout.primary(edit);
    WindowLayout.danger(delete);
    edit.setDisable(true);
    delete.setDisable(true);
    experience.setDisable(true);
    WindowLayout.show(
        c,
        "View Lesson",
        new VBox(12, meta, fields, locks, new HBox(10, edit, experience, delete)),
        status,
        c.listHome());
    new ControllerLessonDetails(new ModelLessonDetails(c.gateway()), this).load();
    if (loaded && !message.isEmpty()) WindowLayout.success(status, message);
  }

  public static void display(WindowContext c, long id, String message) {
    new ViewLessonDetails(c, id, message);
  }

  void populate(LessonGateway.Lesson s) {
    loaded = true;
    fields.populate(s, false);
    meta.setText(
        "Lesson #"
            + s.id()
            + " | Author: "
            + s.author()
            + (s.curatorEdited() ? " | Edited by curator" : "")
            + (s.createdAt() == null
                ? ""
                : "\nCreated: " + s.createdAt() + " | Updated: " + s.updatedAt()));
    String note =
        !context.gateway().supportsLocks()
            ? "Lock metadata is not implemented in this repository yet."
            : s.lessonLocked()
                ? "Lesson locked. Editing and deletion are unavailable."
                : s.deletionLocked()
                    ? "Locked fields are read-only. Delete is unavailable while any field is"
                          + " locked."
                    : "No locks. You may edit or delete this lesson.";
    if (!context.gateway().supportsExperiences())
      note += " Experience storage is not included yet.";
    locks.setText(note);
    edit.setDisable(s.lessonLocked() || s.allFieldsLocked());
    delete.setDisable(s.deletionLocked());
    experience.setDisable(s.lessonLocked() || !context.gateway().supportsExperiences());
  }
}
