package guiLessonSupport;

import entityClasses.Lesson;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * KG01/KG03/KG06 labeled four-field card, shared by create/read/edit views. Each control's source
 * is entityClasses.Lesson in the checked-out repository; limits come from its recognizer. The
 * fields retain user drafts on rejected writes. Locked controls remain readable and labeled.
 *
 * @author Kanish Garg
 */
public final class LessonFields extends VBox {
  public final TextField title = new TextField();
  public final ComboBox<String> category = new ComboBox<>();
  public final TextArea situation = new TextArea(), lessonText = new TextArea();
  private final Label titleLabel = new Label(),
      categoryLabel = new Label(),
      situationLabel = new Label(),
      textLabel = new Label();

  public LessonFields() {
    setSpacing(7);
    title.setId("lesson-title");
    category.setId("lesson-category");
    situation.setId("lesson-situation");
    lessonText.setId("lesson-text");
    title.setPromptText("5 to 100 characters");
    category.getItems().setAll(Lesson.CATEGORIES);
    category.setPromptText("Choose a category");
    category.setMaxWidth(Double.MAX_VALUE);
    situation.setPromptText("What happened? 20 to 2000 characters");
    lessonText.setPromptText("What was learned? 20 to 2000 characters");
    situation.setPrefRowCount(4);
    lessonText.setPrefRowCount(4);
    situation.setWrapText(true);
    lessonText.setWrapText(true);
    titleLabel.setLabelFor(title);
    categoryLabel.setLabelFor(category);
    situationLabel.setLabelFor(situation);
    textLabel.setLabelFor(lessonText);
    getChildren()
        .addAll(
            titleLabel,
            title,
            categoryLabel,
            category,
            situationLabel,
            situation,
            textLabel,
            lessonText);
    labels(false, false, false, false);
  }

  private void labels(boolean t, boolean c, boolean s, boolean l) {
    titleLabel.setText("Title (required)" + (t ? " - Locked" : ""));
    categoryLabel.setText("Category (required)" + (c ? " - Locked" : ""));
    situationLabel.setText("Situation (required)" + (s ? " - Locked" : ""));
    textLabel.setText("Lesson Text (required)" + (l ? " - Locked" : ""));
  }

  /** Copy widgets into a value object without assigning an owner or ID. */
  public LessonGateway.Draft draft() {
    return new LessonGateway.Draft(
        title.getText(), category.getValue(), situation.getText(), lessonText.getText());
  }

  /** Populate current data, honoring full and per-field locks when provided. */
  public void populate(LessonGateway.Lesson s, boolean editing) {
    title.setText(s.title());
    category.setValue(s.category());
    situation.setText(s.situation());
    lessonText.setText(s.lessonText());
    title.setEditable(editing && !s.lessonLocked() && !s.titleLocked());
    category.setDisable(!editing || s.lessonLocked() || s.categoryLocked());
    situation.setEditable(editing && !s.lessonLocked() && !s.situationLocked());
    lessonText.setEditable(editing && !s.lessonLocked() && !s.lessonTextLocked());
    labels(
        s.lessonLocked() || s.titleLocked(),
        s.lessonLocked() || s.categoryLocked(),
        s.lessonLocked() || s.situationLocked(),
        s.lessonLocked() || s.lessonTextLocked());
  }

  /** Fail-closed state until an authorized load succeeds. */
  public void disableEditing() {
    title.setEditable(false);
    category.setDisable(true);
    situation.setEditable(false);
    lessonText.setEditable(false);
  }
}
