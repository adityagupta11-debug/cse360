package guiTools;

import javafx.scene.Scene;
import javafx.stage.Stage;

/** K.G.'s shared appearance entry point; existing views retain their event handlers and layout. */
public final class TeamTheme {
    private TeamTheme() { }
    public static String stylesheet() {
        return TeamTheme.class.getResource("team53.css").toExternalForm();
    }
    public static void apply(Scene scene) {
        if (scene != null && !scene.getStylesheets().contains(stylesheet()))
            scene.getStylesheets().add(stylesheet());
    }
    public static void install(Stage stage) {
        stage.sceneProperty().addListener((observable, oldScene, newScene) -> apply(newScene));
        apply(stage.getScene());
    }
}
