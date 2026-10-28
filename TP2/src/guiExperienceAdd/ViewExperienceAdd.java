package guiExperienceAdd;

import guiLessonSupport.*;
import java.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Add Experience form for Contributor story 3 and work-split task KG04. Uses the shared lesson page
 * styling and session-bound WindowContext. The selected lesson ID links the experience to its
 * parent; the member table holds the draft effort rows until Save. Selecting a row loads it for
 * correction. Uncommitted member input blocks Save so edits are never silently discarded.
 * Validation and persistent ownership/lock checks run again on Save.
 *
 * @author Kanish Garg
 */
public final class ViewExperienceAdd {
  final WindowContext context;
  final long id;
  final TextArea what = new TextArea(), how = new TextArea();
  final TextField duration = new TextField();
  final Label status = new Label();
  private final TextField member = new TextField(), hours = new TextField();
  private final TableView<String[]> effort = new TableView<>();
  private final Label parent = WindowLayout.note("");
  private final Button save;

  @SuppressWarnings("unchecked")
  private ViewExperienceAdd(WindowContext c, long id) {
    this.context = c;
    this.id = id;
    what.setId("experience-what");
    how.setId("experience-how");
    duration.setId("experience-duration");
    member.setId("effort-member");
    hours.setId("effort-hours");
    effort.setId("effort-table");
    what.setWrapText(true);
    how.setWrapText(true);
    what.setPrefRowCount(3);
    how.setPrefRowCount(3);
    duration.setPromptText("Elapsed hours, such as 2.5");
    hours.setPromptText("Effort hours, such as 1.25");
    member.setPromptText("Team member name");
    TableColumn<String[], String> name = new TableColumn<>("Member");
    name.setCellValueFactory(x -> new SimpleStringProperty(x.getValue()[0]));
    TableColumn<String[], String> amount = new TableColumn<>("Effort hours");
    amount.setCellValueFactory(x -> new SimpleStringProperty(x.getValue()[1]));
    effort.getColumns().setAll(name, amount);
    effort.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    effort.setPrefHeight(130);
    effort.setPlaceholder(new Label("Add each member and their effort below."));
    effort
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (o, a, b) -> {
              if (b != null) {
                member.setText(b[0]);
                hours.setText(b[1]);
              }
            });
    Button add = WindowLayout.button("Add or update member", "effort-add", this::addRow);
    Button remove =
        WindowLayout.button(
            "Remove selected member",
            "effort-remove",
            () -> {
              String[] selected = effort.getSelectionModel().getSelectedItem();
              if (selected == null) {
                WindowLayout.failure(
                    status,
                    new IllegalArgumentException("Select a member row before removing it."));
                return;
              }
              effort.getItems().remove(selected);
              member.clear();
              hours.clear();
              WindowLayout.success(
                  status, "Member row removed. Save the experience when finished.");
            });
    Button clear =
        WindowLayout.button(
            "Clear member inputs",
            "effort-clear",
            () -> {
              effort.getSelectionModel().clearSelection();
              member.clear();
              hours.clear();
              status.setText("");
            });
    ControllerExperienceAdd controller =
        new ControllerExperienceAdd(new ModelExperienceAdd(c.gateway()), this);
    save =
        WindowLayout.button(
            "Save experience",
            "experience-save",
            () -> {
              if (!member.getText().isBlank() || !hours.getText().isBlank()) {
                WindowLayout.failure(
                    status,
                    new IllegalArgumentException(
                        "Add or update the member row before saving the experience."));
                return;
              }
              controller.save();
            });
    WindowLayout.primary(save);
    save.setDisable(true);
    VBox card =
        new VBox(
            10,
            parent,
            WindowLayout.field("What was done (required, up to 2,000 characters)", what),
            WindowLayout.field("How it was done (required, up to 2,000 characters)", how),
            WindowLayout.field("Duration in hours (required)", duration),
            WindowLayout.note(
                "Use up to three decimal places for hours. Member effort is time spent by each"
                    + " person; it can differ from elapsed duration. Select a row to edit it, then"
                    + " choose Add or update member."),
            effort,
            new HBox(
                10,
                WindowLayout.field("Member name (up to 100 characters)", member),
                WindowLayout.field("Effort in hours", hours)),
            new FlowPane(10, 10, add, remove, clear),
            save);
    WindowLayout.show(c, "Add Experience", card, status, () -> ContributorWindows.details(c, id));
    controller.load();
  }

  /** Open a blank experience tied to the authorized parent lesson. */
  public static void display(WindowContext c, long id) {
    new ViewExperienceAdd(c, id);
  }

  void populate(LessonGateway.Lesson s) {
    parent.setText("Experience for lesson #" + s.id() + ": " + s.title());
    boolean locked = s.lessonLocked() || !context.gateway().supportsExperiences();
    save.setDisable(locked);
    what.setEditable(!locked);
    how.setEditable(!locked);
    duration.setEditable(!locked);
    member.setDisable(locked);
    hours.setDisable(locked);
    for (String action : new String[] {"effort-add", "effort-remove", "effort-clear"})
      context.stage().getScene().lookup("#" + action).setDisable(locked);
    if (!context.gateway().supportsExperiences())
      status.setText(
          "Experience saving is not connected in this backend. Ask the team to connect the"
              + " experience data service.");
    else if (s.lessonLocked())
      status.setText("This lesson is locked. Return to the lesson before adding experience.");
  }

  private void addRow() {
    try {
      String n = member.getText().trim();
      experienceRecognizer.ExperienceRecognizer.text("Member name", n, 100);
      experienceRecognizer.ExperienceRecognizer.hours(
          "Member effort", Rules.hours("Member effort", hours.getText(), false), false);
      String[] selected = effort.getSelectionModel().getSelectedItem();
      for (String[] row : effort.getItems())
        if (row != selected && row[0].equalsIgnoreCase(n))
          throw new IllegalArgumentException(
              "This member already has a row. Select it to update their hours.");
      String[] entry = {n, hours.getText().trim()};
      if (selected == null) effort.getItems().add(entry);
      else effort.getItems().set(effort.getItems().indexOf(selected), entry);
      effort.getSelectionModel().clearSelection();
      member.clear();
      hours.clear();
      WindowLayout.success(status, "Member effort added. Save the experience when finished.");
    } catch (Exception e) {
      WindowLayout.failure(status, e);
    }
  }

  List<String[]> rows() {
    return new ArrayList<>(effort.getItems());
  }
}
