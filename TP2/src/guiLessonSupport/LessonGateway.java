package guiLessonSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kanish's session-bound window contract, aligned with Joshua's current four lesson fields. KG01
 * core CRUD; KG02 authorized visibility; KG03 locks; KG04 experiences. These are local traceability
 * identifiers, not official story numbers. Storage owns authorization and atomic lock enforcement.
 * No view accepts an editable lesson owner or curator-only data.
 *
 * @author Kanish Garg
 */
public interface LessonGateway {
  /** Fields come directly from current entityClasses.Lesson and LessonRecognizer. */
  record Draft(String title, String category, String situation, String lessonText) {}

  /**
   * Contributor-visible state. The integrated persistent backend supplies locks, curator flag and timestamps.
   * Private curator fields are deliberately excluded from this projection.
   */
  record Lesson(
      long id,
      String author,
      String title,
      String category,
      String situation,
      String lessonText,
      boolean lessonLocked,
      boolean titleLocked,
      boolean categoryLocked,
      boolean situationLocked,
      boolean lessonTextLocked,
      boolean curatorEdited,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    public boolean deletionLocked() {
      return lessonLocked || titleLocked || categoryLocked || situationLocked || lessonTextLocked;
    }

    public boolean allFieldsLocked() {
      return titleLocked && categoryLocked && situationLocked && lessonTextLocked;
    }

    public Draft draft() {
      return new Draft(title, category, situation, lessonText);
    }
  }

  /** One member's effort in hours, distinct from elapsed duration. */
  record Effort(String member, BigDecimal hours) {}

  /** Experience inputs proposed in the team work split; storage must supply the linked write. */
  record Experience(String what, String how, BigDecimal durationHours, List<Effort> efforts) {
    public Experience {
      efforts = List.copyOf(efforts);
    }
  }

  /** Read the current authorized identity, or reject without exposing another owner's data. */
  Lesson read(long id) throws Exception;

  /** Create for the authenticated contributor; return the stored generated identity. */
  long create(Draft draft) throws Exception;

  /** Persist a permitted edit; backend is responsible for atomic lock checks. */
  void update(long id, Draft draft) throws Exception;

  /** Delete only after explicit confirmation and current authorization/lock checks. */
  boolean delete(long id, boolean confirmed) throws Exception;

  /** Whether this backend actually provides full lesson/field lock metadata. */
  boolean supportsLocks();

  /** Whether linked experience persistence is actually connected. */
  boolean supportsExperiences();

  /** Persist a real linked experience and return its stored identity. */
  long addExperience(long lessonId, Experience experience) throws Exception;
}
