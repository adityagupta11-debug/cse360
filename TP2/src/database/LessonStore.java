package database;

import database.Database.LessonResult;
import entityClasses.LessonExperience;
import entityClasses.LessonsLearned;
import entityClasses.LessonsLearnedList;
import experienceRecognizer.ExperienceRecognizer;
import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import lessonRecognizer.LessonRecognizer;

/*******
 * <p>Title: LessonStore Class.</p>
 * <p>Description: Session-bound persistence used exclusively by the shared Database instance.
 * VG01-04 implement Contributor stories 1, 2 and 4; VG05-07 implement experience story 3.
 * Database synchronizes these calls on its one connection. Prepared conditional writes check
 * current locks in the same SQL statement as the write. Experience changes lock the parent
 * row and commit the parent/experience/member rows together, or roll back all of them.</p>
 * <p>Attributes: connection is the existing Foundations connection (no second database);
 * session supplies the stable authenticated user ID, independent of mutable account caches.</p>
 * @author Vishwam
 */
final class LessonStore {
  private final Connection connection;
  private final Supplier<Integer> session;

  LessonStore(Connection connection, Supplier<Integer> session) {
    this.connection = connection;
    this.session = session;
  }

  /** Stable account lookup rechecks the persisted Contributor role on every operation. */
  String actor() throws SQLException {
    Integer id = session.get();
    if (id == null) return null;
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT userName FROM userDB WHERE id=? AND contributorRole=TRUE")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? r.getString(1) : null;
      }
    }
  }

  private boolean privileged() throws SQLException {
    Integer id = session.get();
    if (id == null) return false;
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT id FROM userDB WHERE id=? AND (curatorRole=TRUE OR adminRole=TRUE)")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        return r.next();
      }
    }
  }

  LessonResult add(LessonsLearned lesson) throws SQLException {
    if (!LessonRecognizer.checkForValidLesson(lesson).isEmpty()) return LessonResult.INVALID_INPUT;
    String actor = actor();
    if (actor == null) return LessonResult.UNAUTHORIZED;
    if (!actor.equals(lesson.getOwner())) return LessonResult.NOT_OWNER;
    // VG01: never persist client-supplied locks, curator flags, timestamps or ownership.
    try (PreparedStatement p =
        connection.prepareStatement(
            "INSERT INTO lessonDB(owner,title,category,situation,lessonText) VALUES(?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      p.setString(1, actor);
      bindCore(p, lesson, 2);
      p.executeUpdate();
      try (ResultSet keys = p.getGeneratedKeys()) {
        if (keys.next()) lesson.setLessonId(keys.getInt(1));
      }
    }
    LessonsLearned saved = read(lesson.getLessonId());
    lesson.setCreatedAt(saved.getCreatedAt());
    lesson.setUpdatedAt(saved.getUpdatedAt());
    return LessonResult.SUCCESS;
  }

  LessonsLearned read(int id) throws SQLException {
    String actor = actor();
    if (actor == null) return null;
    try (PreparedStatement p =
        connection.prepareStatement("SELECT * FROM lessonDB WHERE id=? AND owner=?")) {
      p.setInt(1, id);
      p.setString(2, actor);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? row(r) : null;
      }
    }
  }

  LessonsLearnedList byOwner(String owner) throws SQLException {
    String actor = actor();
    if (actor == null || !actor.equals(owner)) return new LessonsLearnedList();
    return queryList("SELECT * FROM lessonDB WHERE owner=? ORDER BY title,id", actor);
  }

  LessonsLearnedList all() throws SQLException {
    // VG02: the all-lessons API cannot be used by a Contributor to bypass own-list rules.
    if (privileged()) return queryList("SELECT * FROM lessonDB ORDER BY title,id", null);
    String actor = actor();
    return actor == null ? new LessonsLearnedList() : byOwner(actor);
  }

  private LessonsLearnedList queryList(String sql, String owner) throws SQLException {
    LessonsLearnedList list = new LessonsLearnedList();
    try (PreparedStatement p = connection.prepareStatement(sql)) {
      if (owner != null) p.setString(1, owner);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) list.add(row(r));
      }
    }
    return list;
  }

  LessonResult update(LessonsLearned lesson, String claimedActor) throws SQLException {
    if (!LessonRecognizer.checkForValidLesson(lesson).isEmpty()) return LessonResult.INVALID_INPUT;
    String actor = actor();
    if (actor == null) return LessonResult.UNAUTHORIZED;
    if (!actor.equals(claimedActor)) return LessonResult.NOT_OWNER;
    // VG03: unchanged locked fields are allowed, changed locked fields are rejected atomically.
    String sql =
        "UPDATE lessonDB SET"
            + " title=?,category=?,situation=?,lessonText=?,updatedAt=CURRENT_TIMESTAMP WHERE id=?"
            + " AND owner=? AND lessonLocked=FALSE AND (titleLocked=FALSE OR title=?) AND"
            + " (categoryLocked=FALSE OR category=?) AND (situationLocked=FALSE OR situation=?) AND"
            + " (lessonTextLocked=FALSE OR lessonText=?)";
    try (PreparedStatement p = connection.prepareStatement(sql)) {
      bindCore(p, lesson, 1);
      p.setInt(5, lesson.getLessonId());
      p.setString(6, actor);
      bindCore(p, lesson, 7);
      if (p.executeUpdate() == 1) return LessonResult.SUCCESS;
    }
    return refused(lesson.getLessonId(), actor);
  }

  LessonResult delete(int id, String claimedActor, boolean confirmed) throws SQLException {
    if (!confirmed) return LessonResult.CANCELLED;
    String actor = actor();
    if (actor == null) return LessonResult.UNAUTHORIZED;
    if (!actor.equals(claimedActor)) return LessonResult.NOT_OWNER;
    // VG04: the current row's lock predicates and delete are one operation; FK cascade is atomic.
    try (PreparedStatement p =
        connection.prepareStatement(
            "DELETE FROM lessonDB WHERE id=? AND owner=? AND lessonLocked=FALSE AND"
                + " titleLocked=FALSE AND categoryLocked=FALSE AND situationLocked=FALSE AND"
                + " lessonTextLocked=FALSE")) {
      p.setInt(1, id);
      p.setString(2, actor);
      if (p.executeUpdate() == 1) return LessonResult.SUCCESS;
    }
    return refused(id, actor);
  }

  private LessonResult refused(int id, String actor) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement("SELECT owner FROM lessonDB WHERE id=?")) {
      p.setInt(1, id);
      try (ResultSet r = p.executeQuery()) {
        if (!r.next()) return LessonResult.NOT_FOUND;
        return actor.equals(r.getString(1)) ? LessonResult.LOCKED : LessonResult.NOT_OWNER;
      }
    }
  }

  LessonResult flag(int id, String column, boolean value) throws SQLException {
    if (!privileged()) return LessonResult.UNAUTHORIZED;
    // column is supplied only by Database's fixed whitelist, never by a widget or SQL input.
    try (PreparedStatement p =
        connection.prepareStatement(
            "UPDATE lessonDB SET " + column + "=?,updatedAt=CURRENT_TIMESTAMP WHERE id=?")) {
      p.setBoolean(1, value);
      p.setInt(2, id);
      return p.executeUpdate() == 1 ? LessonResult.SUCCESS : LessonResult.NOT_FOUND;
    }
  }

  private static void bindCore(PreparedStatement p, LessonsLearned lesson, int offset)
      throws SQLException {
    p.setString(offset, lesson.getTitle().trim());
    p.setString(offset + 1, lesson.getCategory());
    p.setString(offset + 2, lesson.getSituation().trim());
    p.setString(offset + 3, lesson.getLessonText().trim());
  }

  private static LessonsLearned row(ResultSet r) throws SQLException {
    LessonsLearned s =
        new LessonsLearned(
            r.getInt("id"),
            r.getString("owner"),
            r.getString("title"),
            r.getString("category"),
            r.getString("situation"),
            r.getString("lessonText"));
    s.setLessonLocked(r.getBoolean("lessonLocked"));
    s.setTitleLocked(r.getBoolean("titleLocked"));
    s.setCategoryLocked(r.getBoolean("categoryLocked"));
    s.setSituationLocked(r.getBoolean("situationLocked"));
    s.setLessonTextLocked(r.getBoolean("lessonTextLocked"));
    s.setCuratorEdited(r.getBoolean("curatorEdited"));
    s.setCreatedAt(r.getTimestamp("createdAt").toLocalDateTime());
    s.setUpdatedAt(r.getTimestamp("updatedAt").toLocalDateTime());
    return s;
  }

  private String requireActor() throws SQLException {
    String actor = actor();
    if (actor == null) throw new SecurityException("Sign in with an active Contributor account.");
    return actor;
  }

  private void parent(int id, String actor, boolean writing) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT lessonLocked FROM lessonDB WHERE id=? AND owner=?"
                + (writing ? " FOR UPDATE" : ""))) {
      p.setInt(1, id);
      p.setString(2, actor);
      try (ResultSet r = p.executeQuery()) {
        if (!r.next())
          throw new IllegalArgumentException(
              "Lesson is unavailable. Refresh your own lesson list.");
        if (writing && r.getBoolean(1))
          throw new IllegalArgumentException(
              "This lesson is locked. Experience changes are unavailable.");
      }
    }
  }

  /** VG05/VG07: all experience and effort writes succeed or roll back as one transaction. */
  long saveExperience(long id, int lessonId, LessonExperience input) throws SQLException {
    String actor = requireActor();
    ExperienceRecognizer.validate(input);
    return transaction(
        () -> {
          parent(lessonId, actor, true);
          long result = id;
          if (id == 0) {
            try (PreparedStatement p =
                connection.prepareStatement(
                    "INSERT INTO lessonExperienceDB(lessonId,owner,whatDone,howDone,durationHours)"
                        + " VALUES(?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
              p.setInt(1, lessonId);
              p.setString(2, actor);
              bindExperience(p, input, 3);
              p.executeUpdate();
              try (ResultSet keys = p.getGeneratedKeys()) {
                keys.next();
                result = keys.getLong(1);
              }
            }
          } else {
            try (PreparedStatement p =
                connection.prepareStatement(
                    "UPDATE lessonExperienceDB SET"
                        + " whatDone=?,howDone=?,durationHours=?,updatedAt=CURRENT_TIMESTAMP WHERE"
                        + " id=? AND lessonId=? AND owner=?")) {
              bindExperience(p, input, 1);
              p.setLong(4, id);
              p.setInt(5, lessonId);
              p.setString(6, actor);
              if (p.executeUpdate() != 1)
                throw new IllegalArgumentException(
                    "Experience is unavailable. Refresh the lesson.");
            }
            try (PreparedStatement p =
                connection.prepareStatement("DELETE FROM lessonEffortDB WHERE experienceId=?")) {
              p.setLong(1, id);
              p.executeUpdate();
            }
          }
          try (PreparedStatement p =
              connection.prepareStatement(
                  "INSERT INTO lessonEffortDB(experienceId,memberName,effortHours,position)"
                      + " VALUES(?,?,?,?)")) {
            int position = 0;
            for (var effort : input.efforts()) {
              p.setLong(1, result);
              p.setString(2, effort.member().trim());
              p.setBigDecimal(3, effort.hours());
              p.setInt(4, position++);
              p.addBatch();
            }
            p.executeBatch();
          }
          touch(lessonId);
          return result;
        });
  }

  private static void bindExperience(PreparedStatement p, LessonExperience input, int offset)
      throws SQLException {
    p.setString(offset, input.what().trim());
    p.setString(offset + 1, input.how().trim());
    p.setBigDecimal(offset + 2, input.durationHours());
  }

  List<LessonExperience> experiences(int lessonId) throws SQLException {
    String actor = requireActor();
    parent(lessonId, actor, false);
    List<LessonExperience> list = new ArrayList<>();
    try (PreparedStatement p =
        connection.prepareStatement(
            "SELECT * FROM lessonExperienceDB WHERE lessonId=? AND owner=? ORDER BY id")) {
      p.setInt(1, lessonId);
      p.setString(2, actor);
      try (ResultSet r = p.executeQuery()) {
        while (r.next()) {
          List<LessonExperience.Effort> efforts = new ArrayList<>();
          try (PreparedStatement e =
              connection.prepareStatement(
                  "SELECT memberName,effortHours FROM lessonEffortDB WHERE experienceId=? ORDER BY"
                      + " position")) {
            e.setLong(1, r.getLong("id"));
            try (ResultSet rows = e.executeQuery()) {
              while (rows.next())
                efforts.add(new LessonExperience.Effort(rows.getString(1), rows.getBigDecimal(2)));
            }
          }
          list.add(
              new LessonExperience(
                  r.getLong("id"),
                  lessonId,
                  r.getString("owner"),
                  r.getString("whatDone"),
                  r.getString("howDone"),
                  r.getBigDecimal("durationHours"),
                  efforts,
                  r.getTimestamp("createdAt").toLocalDateTime(),
                  r.getTimestamp("updatedAt").toLocalDateTime()));
        }
      }
    }
    return List.copyOf(list);
  }

  boolean deleteExperience(long id, int lessonId, boolean confirmed) throws SQLException {
    if (!confirmed) return false;
    String actor = requireActor();
    return transaction(
        () -> {
          parent(lessonId, actor, true);
          try (PreparedStatement p =
              connection.prepareStatement(
                  "DELETE FROM lessonExperienceDB WHERE id=? AND lessonId=? AND owner=?")) {
            p.setLong(1, id);
            p.setInt(2, lessonId);
            p.setString(3, actor);
            boolean deleted = p.executeUpdate() == 1;
            if (deleted) touch(lessonId);
            return deleted;
          }
        });
  }

  private void touch(int id) throws SQLException {
    try (PreparedStatement p =
        connection.prepareStatement("UPDATE lessonDB SET updatedAt=CURRENT_TIMESTAMP WHERE id=?")) {
      p.setInt(1, id);
      p.executeUpdate();
    }
  }

  /** VG09: curator notes/search terms prepare TP3; no Contributor projection contains them. */
  void curatorData(int lessonId, String notes, String searchTerms) throws SQLException {
    if (!privileged())
      throw new SecurityException("A Curator or Administrator session is required.");
    if (notes == null
        || notes.length() > 8000
        || searchTerms == null
        || searchTerms.length() > 2000)
      throw new IllegalArgumentException(
          "Curator notes allow 8000 characters and search terms 2000.");
    try (PreparedStatement p =
        connection.prepareStatement(
            "MERGE INTO lessonCuratorDB(lessonId,notes,searchTerms) KEY(lessonId) VALUES(?,?,?)")) {
      p.setInt(1, lessonId);
      p.setString(2, notes);
      p.setString(3, searchTerms);
      p.executeUpdate();
    }
  }

  String curatorNotes(int lessonId) throws SQLException {
    if (!privileged())
      throw new SecurityException("A Curator or Administrator session is required.");
    try (PreparedStatement p =
        connection.prepareStatement("SELECT notes FROM lessonCuratorDB WHERE lessonId=?")) {
      p.setInt(1, lessonId);
      try (ResultSet r = p.executeQuery()) {
        return r.next() ? r.getString(1) : null;
      }
    }
  }

  private interface Work<T> {
    T run() throws SQLException;
  }

  private <T> T transaction(Work<T> work) throws SQLException {
    if (!connection.getAutoCommit())
      throw new SQLException("Experience changes require their own transaction.");
    connection.setAutoCommit(false);
    try {
      T result = work.run();
      connection.commit();
      return result;
    } catch (SQLException | RuntimeException failure) {
      try {
        connection.rollback();
      } catch (SQLException rollback) {
        failure.addSuppressed(rollback);
      }
      throw failure;
    } finally {
      connection.setAutoCommit(true);
    }
  }
}
