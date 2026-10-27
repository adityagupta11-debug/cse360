package guiLessonSupport;

import javafx.stage.Stage;

/**
 * KG06 navigation supplied by the existing authenticated application. No second login or database.
 *
 * @param stage shared Foundations stage
 * @param gateway current contributor's authorized service
 * @param listHome returns to Joshua's My Lessons page
 * @param logout must clear the authenticated session and show login
 * @param quit closes the database and exits
 * @author Kanish Garg
 *     <p>Requirement source: TP2 Work Split, Kanish Garg and shared-duties rows. Shared helpers
 *     keep the supplied UX, interface contracts and validation consistent across all five windows.
 *     Local KG identifiers are mapped in the requirements evidence index.
 */
public record WindowContext(
    Stage stage, LessonGateway gateway, Runnable listHome, Runnable logout, Runnable quit) {
  public WindowContext {
    java.util.Objects.requireNonNull(stage);
    java.util.Objects.requireNonNull(gateway);
    java.util.Objects.requireNonNull(listHome);
    java.util.Objects.requireNonNull(logout);
    java.util.Objects.requireNonNull(quit);
  }
}
