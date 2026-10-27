package experienceRecognizer;

import entityClasses.LessonExperience;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/*******
 * <p>Title: ExperienceRecognizer Class.</p>
 * <p>Description: Authoritative experience validation shared by the GUI and storage (VG06).
 * The story supplies the fields; the following limits are team design policies, not quoted
 * course limits: what/how 1-2000 characters, names 1-100, decimal hours within DECIMAL(12,3).
 * Duration is positive, each effort nonnegative, total effort positive, names unique ignoring
 * case and surrounding spaces. Participant names need not be registered system users.</p>
 * @author Vishwam
 */
public final class ExperienceRecognizer {
  private ExperienceRecognizer() {}

  /** Reject malformed text without rejecting ordinary international text or line breaks. */
  public static String text(String field, String value, int maximum) {
    if (value == null || value.isBlank())
      throw new IllegalArgumentException(field + " is required.");
    String result = value.trim();
    if (result.length() > maximum)
      throw new IllegalArgumentException(
          field + " must contain no more than " + maximum + " characters.");
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (Character.isHighSurrogate(c)) {
        if (++i >= value.length() || !Character.isLowSurrogate(value.charAt(i)))
          throw new IllegalArgumentException(field + " contains incomplete Unicode.");
      } else if (Character.isLowSurrogate(c)
          || (Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t')) {
        throw new IllegalArgumentException(field + " contains an unsupported control character.");
      }
    }
    return result;
  }

  /** Reject values storage would otherwise silently round or overflow. */
  public static BigDecimal hours(String field, BigDecimal value, boolean positive) {
    if (value == null || value.signum() < 0 || (positive && value.signum() == 0))
      throw new IllegalArgumentException(
          field + (positive ? " must be greater than zero hours." : " must be nonnegative."));
    BigDecimal normalized = value.stripTrailingZeros();
    if (normalized.scale() > 3 || value.compareTo(new BigDecimal("999999999.999")) > 0)
      throw new IllegalArgumentException(
          field + " allows up to 999999999.999 hours and three decimal places.");
    return value;
  }

  /** VG06: validate the entire record before any INSERT/UPDATE or effort-row replacement. */
  public static void validate(LessonExperience experience) {
    if (experience == null)
      throw new IllegalArgumentException("Enter experience information before saving.");
    text("What was done", experience.what(), 2000);
    text("How it was done", experience.how(), 2000);
    hours("Duration", experience.durationHours(), true);
    if (experience.efforts().isEmpty())
      throw new IllegalArgumentException("Add at least one member and their effort in hours.");
    Set<String> members = new HashSet<>();
    BigDecimal total = BigDecimal.ZERO;
    for (LessonExperience.Effort effort : experience.efforts()) {
      if (effort == null) throw new IllegalArgumentException("Enter each member and their effort.");
      String name = text("Member name", effort.member(), 100);
      if (!members.add(name.toLowerCase(Locale.ROOT)))
        throw new IllegalArgumentException(
            "Each member must appear once. Combine duplicate hours.");
      total = total.add(hours("Member effort", effort.hours(), false));
    }
    if (total.signum() == 0)
      throw new IllegalArgumentException("Enter positive effort for at least one member.");
  }
}
