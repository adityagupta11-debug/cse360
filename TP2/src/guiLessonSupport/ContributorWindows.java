package guiLessonSupport;

/**
 * Entry points called from Joshua's list/home controllers; each window has its own MVC package. The
 * caller supplies the same authenticated gateway and navigation callbacks on every visit.
 *
 * @author Kanish Garg
 *     <p>Requirement source: TP2 Work Split, Kanish Garg and shared-duties rows. Shared helpers
 *     keep the supplied UX, interface contracts and validation consistent across all five windows.
 *     Local KG identifiers are mapped in the requirements evidence index.
 */
public final class ContributorWindows {
  private ContributorWindows() {}

  /** Open a fresh draft without retaining prior users' values. */
  public static void create(WindowContext c) {
    guiLessonCreate.ViewLessonCreate.display(c);
  }

  /** Open one authorized stored lesson by stable identity. */
  public static void details(WindowContext c, long id) {
    guiLessonDetails.ViewLessonDetails.display(c, id, "");
  }

  /** Open the edit window for an existing lesson. */
  public static void edit(WindowContext c, long id) {
    guiLessonEdit.ViewLessonEdit.display(c, id);
  }

  /** Open a confirmation window; displaying this window never deletes anything. */
  public static void delete(WindowContext c, long id) {
    guiLessonDelete.ViewLessonDelete.display(c, id);
  }

  /** Open experience input linked to a stable stored lesson identity. */
  public static void experience(WindowContext c, long id) {
    guiExperienceAdd.ViewExperienceAdd.display(c, id);
  }
}
