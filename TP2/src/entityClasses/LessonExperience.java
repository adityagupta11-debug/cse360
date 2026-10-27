package entityClasses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/*******
 * <p>Title: LessonExperience Class.</p>
 * <p>Description: One attempt to apply an existing lesson (Contributor Phase Two, story 3).
 * What/how describe the attempt, durationHours is elapsed time, and efforts records each
 * person's work separately so parallel effort need not equal elapsed duration. Names are
 * historical participant labels, not account identities or authorization credentials.</p>
 * <p>Source/rationale: the experience story requires what, how, how long and effort by member.
 * id and lessonId provide stable linkage; owner is derived from the authenticated session;
 * timestamps support persistence evidence and future curator aggregation. Immutable copies
 * prevent edits to a caller's member table from changing an already validated request.</p>
 * @param id generated experience identity (zero for a draft)
 * @param lessonId existing parent lesson identity
 * @param owner authenticated author, assigned by storage rather than trusted from input
 * @param what description of what was done
 * @param how description of how it was done
 * @param durationHours positive elapsed hours, at most three fractional digits
 * @param efforts nonnegative hours for unique members, with positive total
 * @param createdAt database creation time
 * @param updatedAt database last-change time
 * @author Vishwam
 */
public record LessonExperience(
    long id,
    int lessonId,
    String owner,
    String what,
    String how,
    BigDecimal durationHours,
    List<Effort> efforts,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  /** One person's effort; independent of elapsed duration (Contributor story 3). */
  public record Effort(String member, BigDecimal hours) {}

  public LessonExperience {
    efforts = efforts == null ? List.of() : List.copyOf(efforts);
  }

  /** VG05: build a draft; identity, author and timestamps are assigned by storage. */
  public LessonExperience(
      int lessonId, String what, String how, BigDecimal durationHours, List<Effort> efforts) {
    this(0, lessonId, null, what, how, durationHours, efforts, null, null);
  }
}
