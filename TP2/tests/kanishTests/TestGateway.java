package kanishTests;

import guiLessonSupport.*;
import java.util.*;

/**
 * IN-MEMORY TEST FIXTURE ONLY for full TP2 lock/experience contracts absent in current main. Does
 * not certify production persistence. @author Kanish Garg
 */
public final class TestGateway implements LessonGateway {
  public Lesson lesson;
  public int creates, updates, deletes, experiences;
  public boolean authorized = true, fail = false;
  public Experience experience;

  public TestGateway() {
    reset(false, false, false, false, false);
  }

  public void reset(boolean all, boolean t, boolean c, boolean s, boolean l) {
    lesson =
        new Lesson(
            1,
            "kanishDemo",
            "Preserve project data",
            "Tools and Setup",
            "Runtime updates changed the local environment.",
            "Back up persistent data before changing runtime settings.",
            all,
            t,
            c,
            s,
            l,
            true,
            null,
            null);
  }

  public Lesson read(long id) {
    if (!authorized || lesson == null || lesson.id() != id)
      throw new IllegalArgumentException("Lesson is unavailable.");
    return lesson;
  }

  private void write() {
    if (fail) throw new IllegalStateException("Storage unavailable. Retry later.");
  }

  public long create(Draft d) {
    if (!authorized) throw new IllegalArgumentException("Sign in first.");
    write();
    creates++;
    lesson =
        new Lesson(
            2,
            "kanishDemo",
            d.title(),
            d.category(),
            d.situation(),
            d.lessonText(),
            false,
            false,
            false,
            false,
            false,
            false,
            null,
            null);
    return 2;
  }

  public void update(long id, Draft d) {
    var s = read(id);
    Rules.editable(s, d);
    write();
    updates++;
    lesson =
        new Lesson(
            id,
            s.author(),
            d.title(),
            d.category(),
            d.situation(),
            d.lessonText(),
            s.lessonLocked(),
            s.titleLocked(),
            s.categoryLocked(),
            s.situationLocked(),
            s.lessonTextLocked(),
            s.curatorEdited(),
            null,
            null);
  }

  public boolean delete(long id, boolean confirmed) {
    if (!confirmed) return false;
    if (read(id).deletionLocked()) throw new IllegalArgumentException("Lesson is locked.");
    write();
    deletes++;
    lesson = null;
    return true;
  }

  public boolean supportsLocks() {
    return true;
  }

  public boolean supportsExperiences() {
    return true;
  }

  public long addExperience(long id, Experience e) {
    if (read(id).lessonLocked()) throw new IllegalArgumentException("Lesson is locked.");
    write();
    experience = e;
    experiences++;
    return 9;
  }
}
