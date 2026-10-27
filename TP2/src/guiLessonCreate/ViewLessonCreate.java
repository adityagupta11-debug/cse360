package guiLessonCreate;

import guiLessonSupport.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * KG01/KG06 creation window: a fresh four-field draft, labeled card and common footer. context
 * shares the authenticated application; fields hold the draft; status explains fixes.
 *
 * @author Kanish Garg
 */
public final class ViewLessonCreate {
  final WindowContext context;
  final LessonFields fields = new LessonFields();
  final Label status = new Label();

  private ViewLessonCreate(WindowContext c) {
    context = c;
    ControllerLessonCreate controller =
        new ControllerLessonCreate(new ModelLessonCreate(c.gateway()), this);
    Button save = WindowLayout.button("Create lesson", "create-save", controller::save);
    WindowLayout.primary(save);
    WindowLayout.show(
        c,
        "Create Lesson",
        new VBox(
            12,
            WindowLayout.note("The lesson belongs to your signed-in Contributor account."),
            fields,
            save),
        status,
        c.listHome());
  }

  public static void display(WindowContext c) {
    new ViewLessonCreate(c);
  }
}
