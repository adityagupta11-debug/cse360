package guiLessonSupport;

import java.net.URL;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * KG06 consistent breadcrumb, bold title, labeled card, blue primary action, red destructive action
 * and Back/Logout/Quit footer. Labels use labelFor for keyboard/accessibility support.
 *
 * @author Kanish Garg
 *     <p>Requirement source: TP2 Work Split, Kanish Garg and shared-duties rows. Shared helpers
 *     keep the supplied UX, interface contracts and validation consistent across all five windows.
 *     Local KG identifiers are mapped in the requirements evidence index.
 */
public final class WindowLayout {
  private WindowLayout() {}

  /** Construct a window around a scrollable, labeled content card. */
  public static void show(WindowContext c, String title, VBox card, Label status, Runnable back) {
    Label crumb =
        new Label(
            "TEAM 53 / CONTRIBUTOR / MY LESSONS / " + title.toUpperCase(java.util.Locale.ROOT));
    crumb.getStyleClass().add("breadcrumb");
    Label heading = new Label(title);
    heading.getStyleClass().add("page-heading");
    card.getStyleClass().add("lesson-card");
    card.setFillWidth(true);
    ScrollPane scroll = new ScrollPane(card);
    scroll.setFitToWidth(true);
    scroll.getStyleClass().add("card-scroll");
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    VBox.setVgrow(scroll, Priority.ALWAYS);
    status.setId("window-status");
    status.setWrapText(true);
    status.setMinHeight(48);
    status.setAccessibleRole(javafx.scene.AccessibleRole.TEXT);
    Button b = button("Back", "nav-back", back),
        out = button("Logout", "nav-logout", c.logout()),
        q = button("Quit", "nav-quit", c.quit());
    Region space = new Region();
    HBox.setHgrow(space, Priority.ALWAYS);
    HBox footer = new HBox(12, b, space, out, q);
    footer.getStyleClass().add("window-footer");
    VBox root = new VBox(12, crumb, heading, scroll, status, footer);
    root.setPadding(new Insets(30));
    Scene scene = new Scene(root, 1000, 760);
    URL css = WindowLayout.class.getResource("windows.css");
    if (css != null) scene.getStylesheets().add(css.toExternalForm());
    c.stage().setScene(scene);
    c.stage().setTitle("TP2 - " + title);
    c.stage().setMinWidth(740);
    c.stage().setMinHeight(600);
    c.stage().show();
  }

  /** Create a stable-ID button for manual use and repeatable desktop tests. */
  public static Button button(String text, String id, Runnable action) {
    Button b = new Button(text);
    b.setId(id);
    b.setOnAction(e -> action.run());
    return b;
  }

  /** Labeled editable or read-only control. */
  public static VBox field(String text, Control control) {
    Label l = new Label(text);
    l.setLabelFor(control);
    l.getStyleClass().add("field-label");
    return new VBox(5, l, control);
  }

  /** Wrap long metadata or explanatory text. */
  public static Label note(String text) {
    Label l = new Label(text);
    l.setWrapText(true);
    return l;
  }

  /** Primary styling without hiding disabled-state feedback. */
  public static void primary(Button button) {
    button.getStyleClass().add("primary-action");
  }

  /** Destructive styling; confirmation pages explicitly default to No. */
  public static void danger(Button button) {
    button.getStyleClass().add("danger-action");
  }

  /** Keep errors actionable and avoid showing raw SQL or stack traces to contributors. */
  public static void failure(Label status, Exception failure) {
    status.getStyleClass().remove("success-message");
    if (!status.getStyleClass().contains("error-message"))
      status.getStyleClass().add("error-message");
    if (failure instanceof IllegalArgumentException || failure instanceof IllegalStateException)
      status.setText(failure.getMessage());
    else
      status.setText(
          "The request could not be completed. Your input is kept. Return to My Lessons, refresh,"
              + " and retry with an active Contributor account.");
    System.err.println("Contributor window request failed: " + failure.getClass().getSimpleName());
  }

  /** Show success only after a service call returns, never on a failed write. */
  public static void success(Label status, String message) {
    status.getStyleClass().remove("error-message");
    status.getStyleClass().add("success-message");
    status.setText(message);
  }
}
