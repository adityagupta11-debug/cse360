package guiLessonSupport;

import java.math.BigDecimal;
import java.util.*;
import lessonRecognizer.LessonRecognizer;

/**
 * KG05 reuses the current repository's authoritative four-field recognizer. Experience input
 * policies are provisional until the data owner supplies that API. @author Kanish Garg
 */
public final class Rules {
  private Rules() {}

  /** Honor Joshua's 5-100 title, known category, and 20-2000 situation/lesson text limits. */
  public static void lesson(LessonGateway.Draft d) {
    if (d == null) throw new IllegalArgumentException("Enter the lesson fields before saving.");
    String error =
        LessonRecognizer.checkForValidLesson(
            new entityClasses.Lesson(null, d.title(), d.category(), d.situation(), d.lessonText()));
    if (!error.isEmpty()) throw new IllegalArgumentException(error);
  }

  /** Bound free-text experience values without inventing a database schema. */
  public static String text(String field, String s) {
    if (s == null || s.isBlank())
      throw new IllegalArgumentException(field + " is required. Enter meaningful text.");
    if (s.length() > 2000)
      throw new IllegalArgumentException(field + " must contain no more than 2000 characters.");
    return s;
  }

  public static BigDecimal hours(String field, String value, boolean positive) {
    if (value == null || !value.trim().matches("[0-9]+(?:\\.[0-9]+)?"))
      throw new IllegalArgumentException(
          field + " must be a decimal number in hours, such as 1.5.");
    BigDecimal n = new BigDecimal(value.trim());
    if (positive && n.signum() == 0)
      throw new IllegalArgumentException(field + " must be greater than zero hours.");
    return n;
  }

  public static LessonGateway.Experience experience(
      String what, String how, String duration, List<String[]> rows) {
    text("What was done", what);
    text("How it was done", how);
    BigDecimal elapsed = hours("Duration", duration, true);
    if (rows.isEmpty())
      throw new IllegalArgumentException("Add at least one member and their effort in hours.");
    List<LessonGateway.Effort> effort = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    BigDecimal total = BigDecimal.ZERO;
    for (String[] row : rows) {
      String member = row[0] == null ? "" : row[0].trim();
      text("Member name", member);
      if (!seen.add(member.toLowerCase(Locale.ROOT)))
        throw new IllegalArgumentException(
            "Each member must appear once. Combine duplicate hours.");
      BigDecimal n = hours("Member effort", row[1], false);
      effort.add(new LessonGateway.Effort(member, n));
      total = total.add(n);
    }
    if (total.signum() == 0)
      throw new IllegalArgumentException("Enter positive effort for at least one member.");
    return new LessonGateway.Experience(what, how, elapsed, effort);
  }

  /** Current locks are checked again on save; the future backend must enforce them atomically. */
  public static void editable(LessonGateway.Lesson s, LessonGateway.Draft d) {
    if (s.lessonLocked())
      throw new IllegalArgumentException("This lesson is locked. Return to View Lesson.");
    if (s.titleLocked() && !s.title().equals(d.title()))
      throw new IllegalArgumentException("Title is locked. Reload the lesson.");
    if (s.categoryLocked() && !s.category().equals(d.category()))
      throw new IllegalArgumentException("Category is locked. Reload the lesson.");
    if (s.situationLocked() && !s.situation().equals(d.situation()))
      throw new IllegalArgumentException("Situation is locked. Reload the lesson.");
    if (s.lessonTextLocked() && !s.lessonText().equals(d.lessonText()))
      throw new IllegalArgumentException("Lesson text is locked. Reload the lesson.");
  }
}
