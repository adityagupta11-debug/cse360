package guiExperienceAdd;

import guiLessonSupport.*;
import java.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * KG04 experience window: what, how, duration and a real member-and-hours TableView. Add/update
 * rows with labeled inputs; selecting a row loads it for correction. This avoids silently losing an
 * uncommitted in-cell edit when Save is clicked.
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
                status.setText("Select a member row before removing it.");
                return;
              }
              effort.getItems().remove(selected);
              member.clear();
              hours.clear();
              WindowLayout.success(status, "Member row removed. Save the experience when finished.");
            });
    ControllerExperienceAdd controller =
        new ControllerExperienceAdd(new ModelExperienceAdd(c.gateway()), this);
    save =
        WindowLayout.button(
            "Save experience",
            "experience-save",
            () -> {
              if (!member.getText().isBlank() || !hours.getText().isBlank()) {
                status.setText("Add or update the member row before saving the experience.");
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
            WindowLayout.field("What was done (required)", what),
            WindowLayout.field("How it was done (required)", how),
            WindowLayout.field("Duration in hours (required)", duration),
            WindowLayout.note(
                "Member effort is time spent by each person; it can differ from elapsed duration."),
            effort,
            new HBox(
                10,
                WindowLayout.field("Member name", member),
                WindowLayout.field("Effort in hours", hours)),
            new HBox(10, add, remove),
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
    save.setDisable(s.lessonLocked() || !context.gateway().supportsExperiences());
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
      Rules.text("Member name", n);
      Rules.hours("Member effort", hours.getText(), false);
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
