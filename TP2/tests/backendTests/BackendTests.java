package backendTests;

import database.Database;
import database.Database.LessonResult;
import entityClasses.*;
import guiLessonSupport.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/*******
 * <p>Title: BackendTests Class.</p>
 * <p>Description: Semi-automated TP2 backend requirement tests (VG01-10). Each named PASS line
 * asserts the stored outcome; a failed assertion throws and exits nonzero. Fixtures use private
 * temporary file-backed H2 databases and registered/authenticated users, never production data.
 * Requirement identifiers map to Contributor Phase Two stories in Backend_Requirements.md.</p>
 * @author Vishwam
 */
public final class BackendTests {
  private static int passed;
  private static final String PASSWORD = "FixturePass1!";
  private static final LessonGateway.Draft DRAFT =
      new LessonGateway.Draft(
          "Integration lesson",
          "Testing",
          "Our team tested an integrated Contributor workflow.",
          "Test persistence and access checks before sharing the build.");

  private interface Action {
    void run() throws Exception;
  }

  private static void check(String name, boolean condition) {
    if (!condition) throw new AssertionError(name);
    System.out.println(name + " | PASS");
    passed++;
  }

  private static boolean denied(Action action) throws Exception {
    try {
      action.run();
      return false;
    } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
      return true;
    }
  }

  private static void login(Database db, String user) {
    if (!db.authenticateSession(user, PASSWORD)) throw new AssertionError("Fixture login " + user);
  }

  private static void register(Database db, String name, boolean contributor, boolean curator)
      throws Exception {
    db.register(
        new User(
            name,
            PASSWORD,
            "Fixture",
            "",
            "User",
            "",
            name + "@example.test",
            false,
            contributor,
            false,
            curator));
  }

  private static LessonsLearned lesson(String owner) {
    return new LessonsLearned(
        owner, DRAFT.title(), DRAFT.category(), DRAFT.situation(), DRAFT.lessonText());
  }

  private static LessonExperience experience(int id) {
    return new LessonExperience(
        id,
        "Ran the integrated tests",
        "Used isolated real H2 records",
        new BigDecimal("2.5"),
        List.of(
            new LessonExperience.Effort("Vishwam", new BigDecimal("1.25")),
            new LessonExperience.Effort("Kanish", new BigDecimal("2"))));
  }

  private static long count(String url, String table) throws Exception {
    try (Connection c = DriverManager.getConnection(url, "sa", "");
        Statement s = c.createStatement();
        ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
      r.next();
      return r.getLong(1);
    }
  }

  private static void sql(String url, String query) throws Exception {
    try (Connection c = DriverManager.getConnection(url, "sa", "");
        Statement s = c.createStatement()) {
      s.execute(query);
    }
  }

  /**
   * VG01/VG02: actual creates, direct API spoof attempts, owner isolation, logout and revocation.
   */
  private static int creationAndAuthorization(Database db, String url) throws Exception {
    check(
        "VG01_NoSessionCreateRejected", db.addLesson(lesson("alice")) == LessonResult.UNAUTHORIZED);
    login(db, "alice");
    var s = lesson("alice");
    s.setLessonLocked(true);
    s.setCuratorEdited(true);
    check("VG01_CreateOwnValid", s.create(db) == LessonResult.SUCCESS && s.getLessonId() > 0);
    int id = s.getLessonId();
    check(
        "VG01_UntrustedFlagsIgnored",
        !db.getLesson(id).hasAnyLock() && !db.getLesson(id).isCuratorEdited());
    check(
        "VG01_InvalidCreateChangesNothing",
        db.addLesson(
                    new LessonsLearned(
                        "alice", "bad", "Testing", DRAFT.situation(), DRAFT.lessonText()))
                == LessonResult.INVALID_INPUT
            && db.getNumberOfLessons() == 1);
    check("VG01_ForgedOwnerRejected", db.addLesson(lesson("bob")) == LessonResult.NOT_OWNER);
    login(db, "bob");
    var other = lesson("bob");
    db.addLesson(other);
    login(db, "alice");
    check("VG02_OwnReadVisible", db.getLesson(id) != null);
    check("VG02_ForeignReadHidden", db.getLesson(other.getLessonId()) == null);
    check("VG02_ForeignOwnerListHidden", db.getLessonsByOwner("bob").isEmpty());
    check(
        "VG02_AllListCannotBypassScope",
        db.getAllLessons().size() == 1 && db.getAllLessons().get(0).getOwner().equals("alice"));
    db.getUserAccountDetails("bob");
    check("VG02_CacheDoesNotChangeActor", db.getLesson(id).getOwner().equals("alice"));
    check("VG02_ForgedActingUserRejected", db.updateLesson(other, "bob") == LessonResult.NOT_OWNER);
    check("VG02_ForeignUpdateRejected", db.updateLesson(other, "alice") == LessonResult.NOT_OWNER);
    check(
        "VG02_ForeignDeleteRejected",
        db.deleteLesson(other.getLessonId(), "alice", true) == LessonResult.NOT_OWNER);
    check(
        "VG03_ContributorCannotSetLocks", db.setLessonLock(id, true) == LessonResult.UNAUTHORIZED);
    db.clearAuthenticatedSession();
    check(
        "VG02_LogoutHidesReads",
        db.getLesson(id) == null && db.getLessonsByOwner("alice").isEmpty());
    check("VG02_LogoutRejectsUpdate", db.updateLesson(s, "alice") == LessonResult.UNAUTHORIZED);
    check(
        "VG04_LogoutRejectsDelete",
        db.deleteLesson(id, "alice", true) == LessonResult.UNAUTHORIZED);
    login(db, "alice");
    sql(url, "UPDATE userDB SET contributorRole=FALSE WHERE userName='alice'");
    check("VG02_RevokedRoleHidesReads", db.getLesson(id) == null);
    check(
        "VG02_RevokedRoleRejectsWrite", db.addLesson(lesson("alice")) == LessonResult.UNAUTHORIZED);
    sql(url, "UPDATE userDB SET contributorRole=TRUE WHERE userName='alice'");
    return id;
  }

  /** VG03/VG04: each field independently rejects changes, permits other edits and blocks delete. */
  private static void locks(Database db, int id) throws Exception {
    for (String field : LessonsLearned.LOCKABLE_FIELDS) {
      login(db, "curator");
      check(
          "VG03_" + field + "_LockStored",
          db.setFieldLock(id, field, true) == LessonResult.SUCCESS);
      login(db, "alice");
      var loaded = db.getLesson(id);
      switch (field) {
        case "Title" -> loaded.setTitle("Forbidden new title");
        case "Category" -> loaded.setCategory("Process");
        case "Situation" -> loaded.setSituation("Forbidden changed situation information.");
        case "Lesson" -> loaded.setLessonText("Forbidden changed lesson information.");
      }
      check(
          "VG03_" + field + "_ChangedLockedFieldRejected",
          db.updateLesson(loaded, "alice") == LessonResult.LOCKED);
      check(
          "VG04_" + field + "_LockBlocksDeletion",
          db.deleteLesson(id, "alice", true) == LessonResult.LOCKED);
      loaded = db.getLesson(id);
      if (field.equals("Title")) loaded.setSituation(loaded.getSituation() + " More.");
      else loaded.setTitle(loaded.getTitle() + "!");
      check(
          "VG03_" + field + "_UnchangedLockedFieldAllowsOtherEdit",
          db.updateLesson(loaded, "alice") == LessonResult.SUCCESS);
      login(db, "curator");
      db.setFieldLock(id, field, false);
    }
    login(db, "alice");
    var gateway = new CurrentRepoGateway(db);
    var stale = gateway.read(id);
    // A second authenticated connection changes state AFTER the screen has loaded.
    login(db, "curator");
    db.setLessonLock(id, true);
    login(db, "alice");
    check(
        "VG03_WholeLockRejectsEvenUnchangedUpdate",
        db.updateLesson(db.getLesson(id), "alice") == LessonResult.LOCKED);
    check("VG03_StaleGUIWriteRejected", denied(() -> gateway.update(id, stale.draft())));
    check("VG04_WholeLockRejectsDelete", db.deleteLesson(id, "alice", true) == LessonResult.LOCKED);
    check("VG05_WholeLockRejectsExperience", denied(() -> db.addExperience(id, experience(id))));
    check(
        "VG04_CancelDoesNotDeleteLockedLesson",
        db.deleteLesson(id, "alice", false) == LessonResult.CANCELLED && db.getLesson(id) != null);
    login(db, "curator");
    db.setLessonLock(id, false);
    check(
        "VG03_InvalidFieldNameRejected",
        db.setFieldLock(id, "title; DROP TABLE lessonDB", true) == LessonResult.INVALID_INPUT);
    login(db, "alice");
    check(
        "VG03_UnlockAllowsUpdate",
        db.updateLesson(db.getLesson(id), "alice") == LessonResult.SUCCESS);
  }

  /**
   * VG05-07: linked CRUD, independent effort, invalid input and rollback of mid-write SQL failures.
   */
  private static long experiences(Database db, String url, int id) throws Exception {
    login(db, "alice");
    long saved = db.addExperience(id, experience(id));
    var stored = db.getExperiences(id).getFirst();
    check(
        "VG05_CreateLinkedExperience",
        saved == stored.id() && stored.lessonId() == id && stored.owner().equals("alice"));
    check(
        "VG05_ReadMemberHoursExactly",
        stored.efforts().size() == 2
            && stored.efforts().getFirst().hours().compareTo(new BigDecimal("1.25")) == 0);
    check(
        "VG05_DurationIndependentOfEffort",
        stored.durationHours().compareTo(new BigDecimal("2.5")) == 0);
    check(
        "VG08_ExperienceTimestampsAssigned",
        stored.createdAt() != null && stored.updatedAt() != null);
    List<LessonExperience> invalid =
        List.of(
            new LessonExperience(id, " ", "How", BigDecimal.ONE, experience(id).efforts()),
            new LessonExperience(id, "What", " ", BigDecimal.ONE, experience(id).efforts()),
            new LessonExperience(id, "What", "How", BigDecimal.ZERO, experience(id).efforts()),
            new LessonExperience(id, "What", "How", new BigDecimal("-1"), experience(id).efforts()),
            new LessonExperience(
                id, "What", "How", new BigDecimal("1.0001"), experience(id).efforts()),
            new LessonExperience(
                id, "What", "How", new BigDecimal("1000000000"), experience(id).efforts()),
            new LessonExperience(id, "What", "How", BigDecimal.ONE, List.of()),
            new LessonExperience(
                id,
                "What",
                "How",
                BigDecimal.ONE,
                List.of(new LessonExperience.Effort("Alice", BigDecimal.ZERO))),
            new LessonExperience(
                id,
                "What",
                "How",
                BigDecimal.ONE,
                List.of(new LessonExperience.Effort("Alice", BigDecimal.ONE.negate()))),
            new LessonExperience(
                id,
                "What",
                "How",
                BigDecimal.ONE,
                List.of(
                    new LessonExperience.Effort("Alice", BigDecimal.ONE),
                    new LessonExperience.Effort(" alice ", BigDecimal.ONE))),
            new LessonExperience(
                id,
                "What",
                "How",
                BigDecimal.ONE,
                List.of(new LessonExperience.Effort("m".repeat(101), BigDecimal.ONE))),
            new LessonExperience(
                id, "x".repeat(2001), "How", BigDecimal.ONE, experience(id).efforts()),
            new LessonExperience(
                id, "What\u0000", "How", BigDecimal.ONE, experience(id).efforts()));
    for (int i = 0; i < invalid.size(); i++) {
      var bad = invalid.get(i);
      check(
          "VG06_InvalidCase" + (i + 1) + "_RejectedWithoutWrite",
          denied(() -> db.addExperience(id, bad)) && db.getExperiences(id).size() == 1);
    }
    check("VG06_NullInputRejected", denied(() -> db.addExperience(id, null)));
    login(db, "bob");
    check("VG05_ForeignExperienceReadRejected", denied(() -> db.getExperiences(id)));
    check(
        "VG05_ForeignExperienceWriteRejected", denied(() -> db.addExperience(id, experience(id))));
    check(
        "VG07_ForeignExperienceDeleteRejected", denied(() -> db.deleteExperience(saved, id, true)));
    login(db, "alice");
    var replacement =
        new LessonExperience(
            id,
            "Repeated the experiment",
            "Used the revised workflow",
            BigDecimal.ONE,
            List.of(new LessonExperience.Effort("New member", new BigDecimal("0.5"))));
    db.updateExperience(saved, id, replacement);
    var updated = db.getExperiences(id).getFirst();
    check(
        "VG07_UpdateReplacesTextAndEffort",
        updated.what().equals(replacement.what())
            && updated.efforts().size() == 1
            && updated.efforts().getFirst().member().equals("New member"));
    check(
        "VG07_UpdatePreservesIdentityAuthorCreatedAt",
        updated.id() == saved
            && updated.owner().equals("alice")
            && updated.createdAt().equals(stored.createdAt()));
    check(
        "VG07_InvalidUpdatePreservesRecord",
        denied(() -> db.updateExperience(saved, id, invalid.getFirst()))
            && db.getExperiences(id).getFirst().what().equals(replacement.what()));
    check(
        "VG07_CancelExperienceDeleteKeepsRecord",
        !db.deleteExperience(saved, id, false) && db.getExperiences(id).size() == 1);
    check(
        "VG07_MissingExperienceUpdateRejected",
        denied(() -> db.updateExperience(99999, id, replacement)));
    long before = count(url, "lessonExperienceDB");
    sql(
        url,
        "ALTER TABLE lessonEffortDB ADD CONSTRAINT reject_test_member CHECK(memberName<>'Fail')");
    var fail =
        new LessonExperience(
            id,
            "Test atomic rollback",
            "Force a member insertion error",
            BigDecimal.ONE,
            List.of(new LessonExperience.Effort("Fail", BigDecimal.ONE)));
    boolean rolledBack = false;
    try {
      db.addExperience(id, fail);
    } catch (SQLException expected) {
      rolledBack = true;
    }
    check(
        "VG07_InsertSQLFailureRollsBackParentRecord",
        rolledBack && count(url, "lessonExperienceDB") == before);
    rolledBack = false;
    try {
      db.updateExperience(saved, id, fail);
    } catch (SQLException expected) {
      rolledBack = true;
    }
    check(
        "VG07_UpdateSQLFailureRestoresOldEfforts",
        rolledBack
            && db.getExperiences(id).getFirst().efforts().getFirst().member().equals("New member"));
    sql(url, "ALTER TABLE lessonEffortDB DROP CONSTRAINT reject_test_member");
    long disposable = db.addExperience(id, experience(id));
    check("VG07_RecoveryAfterRollback", disposable > 0 && db.getExperiences(id).size() == 2);
    check(
        "VG07_DeleteExperienceRemovesOnlySelected",
        db.deleteExperience(disposable, id, true) && db.getExperiences(id).size() == 1);
    return saved;
  }

  /**
   * VG03/VG04/VG05: another connection locks a lesson while a stale write is waiting. A timeout
   * proves the operation waits on the row; after commit it must reject the write. These tests
   * expose check-then-write races that sequential GUI validation cannot detect.
   */
  private static void concurrentLockChecks(String url, int id) throws Exception {
    Database writer = new Database(url);
    writer.connectToDatabase();
    login(writer, "alice");
    var old = writer.getLesson(id);
    old.setTitle("Stale concurrent edit");
    var executor = java.util.concurrent.Executors.newSingleThreadExecutor();
    try (Connection blocker = DriverManager.getConnection(url, "sa", "")) {
      for (String action : List.of("Update", "Delete", "Experience")) {
        blocker.setAutoCommit(false);
        try (PreparedStatement p =
            blocker.prepareStatement("UPDATE lessonDB SET lessonLocked=TRUE WHERE id=?")) {
          p.setInt(1, id);
          p.executeUpdate();
        }
        var started = new java.util.concurrent.CountDownLatch(1);
        var future =
            executor.submit(
                () -> {
                  started.countDown();
                  if (action.equals("Update"))
                    return writer.updateLesson(old, "alice") == LessonResult.LOCKED;
                  if (action.equals("Delete"))
                    return writer.deleteLesson(id, "alice", true) == LessonResult.LOCKED;
                  return denied(() -> writer.addExperience(id, experience(id)));
                });
        started.await();
        boolean waited = false;
        try {
          future.get(200, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException expected) {
          waited = true;
        }
        check("VG03_Concurrent" + action + "WaitsForRow", waited);
        blocker.commit();
        check(
            "VG03_Concurrent" + action + "RechecksCommittedLock",
            future.get(5, java.util.concurrent.TimeUnit.SECONDS));
        blocker.setAutoCommit(true);
        try (PreparedStatement p =
            blocker.prepareStatement("UPDATE lessonDB SET lessonLocked=FALSE WHERE id=?")) {
          p.setInt(1, id);
          p.executeUpdate();
        }
      }
    } finally {
      executor.shutdownNow();
      writer.closeConnection();
    }
  }

  /** VG09/VG10: private curator data remains inaccessible while real GUI capability is enabled. */
  private static void metadataAndGateway(Database db, int id) throws Exception {
    login(db, "curator");
    db.setCuratorData(id, "PRIVATE_CURATOR_NOTE", "search synonym");
    db.setCuratorEdited(id, true);
    check("VG09_CuratorCanReadOwnMetadata", db.getCuratorNotes(id).equals("PRIVATE_CURATOR_NOTE"));
    login(db, "alice");
    check("VG09_ContributorPrivateReadRejected", denied(() -> db.getCuratorNotes(id)));
    check(
        "VG09_ContributorPrivateWriteRejected",
        denied(() -> db.setCuratorData(id, "overwrite", "")));
    var g = new CurrentRepoGateway(db);
    var snapshot = g.read(id);
    check(
        "VG09_CuratorEditedLessonRemainsVisible",
        snapshot.curatorEdited() && snapshot.author().equals("alice"));
    check(
        "VG09_ProjectionContainsNoPrivateContent",
        !snapshot.toString().contains("PRIVATE_CURATOR_NOTE"));
    check("VG10_RealCapabilitiesEnabled", g.supportsLocks() && g.supportsExperiences());
    check(
        "VG08_LessonTimestampsAssigned",
        snapshot.createdAt() != null && snapshot.updatedAt() != null);
    long throughGui =
        g.addExperience(
            id,
            new LessonGateway.Experience(
                "GUI contract write",
                "Use the connected adapter",
                BigDecimal.ONE,
                List.of(new LessonGateway.Effort("GUI member", BigDecimal.ONE))));
    check(
        "VG10_GatewayCreatesRealExperience",
        db.getExperiences(id).stream().anyMatch(e -> e.id() == throughGui));
    check("VG10_GatewayCancelPreservesLesson", !g.delete(id, false) && g.read(id) != null);
    check("VG10_MissingLessonRejected", denied(() -> g.read(Integer.MAX_VALUE)));
    check("VG10_OverflowIdentityRejected", denied(() -> g.read(Long.MAX_VALUE)));
  }

  /** VG08: add missing columns without replacing existing HW2 rows. */
  private static void migration(Path directory) throws Exception {
    String url = "jdbc:h2:" + directory.resolve("old-hw2");
    sql(
        url,
        "CREATE TABLE lessonDB(id INT AUTO_INCREMENT PRIMARY KEY,owner VARCHAR(32),title"
            + " VARCHAR(100),category VARCHAR(30),situation VARCHAR(2000),lessonText"
            + " VARCHAR(2000))");
    sql(
        url,
        "INSERT INTO lessonDB(owner,title,category,situation,lessonText) VALUES('alice','Legacy"
            + " retained lesson','Testing','A valid old situation with enough text.','A valid old"
            + " lesson with enough text.')");
    Database old = new Database(url);
    old.connectToDatabase();
    register(old, "alice", true, false);
    login(old, "alice");
    check(
        "VG08_MigrationRetainsOldCoreFields",
        old.getLesson(1).getTitle().equals("Legacy retained lesson"));
    check(
        "VG08_MigrationAddsDefaultLocksAndTimes",
        !old.getLesson(1).hasAnyLock() && old.getLesson(1).getCreatedAt() != null);
    old.addExperience(1, experience(1));
    check("VG08_MigrationEnablesExperienceStorage", old.getExperiences(1).size() == 1);
    old.closeConnection();
  }

  /** Run every requirement group; always close fixture databases and return nonzero on failure. */
  public static void main(String[] args) throws Exception {
    Path dir = Files.createTempDirectory("tp2-backend-");
    String url = "jdbc:h2:" + dir.resolve("fixture");
    Database db = new Database(url);
    db.connectToDatabase();
    int id;
    try {
      register(db, "alice", true, false);
      register(db, "bob", true, false);
      register(db, "curator", false, true);
      id = creationAndAuthorization(db, url);
      locks(db, id);
      concurrentLockChecks(url, id);
      experiences(db, url, id);
      metadataAndGateway(db, id);
    } finally {
      db.closeConnection();
    }
    db = new Database(url);
    db.connectToDatabase();
    try {
      check("VG08_ReopenRequiresLogin", db.getLesson(id) == null);
      login(db, "alice");
      check("VG08_ReopenRetainsCoreAndCuratorFlag", db.getLesson(id).isCuratorEdited());
      check(
          "VG08_ReopenRetainsExperienceAndHours",
          db.getExperiences(id).size() == 2
              && db.getExperiences(id)
                  .getFirst()
                  .efforts()
                  .getFirst()
                  .member()
                  .equals("New member"));
      login(db, "curator");
      db.setFieldLock(id, "Title", true);
      login(db, "alice");
    } finally {
      db.closeConnection();
    }
    db = new Database(url);
    db.connectToDatabase();
    try {
      login(db, "alice");
      check("VG08_ReopenRetainsLocks", db.getLesson(id).isTitleLocked());
      check(
          "VG04_ReopenedLockStillEnforced",
          db.deleteLesson(id, "alice", true) == LessonResult.LOCKED);
      login(db, "curator");
      db.setFieldLock(id, "Title", false);
      login(db, "alice");
      check(
          "VG04_UnlockedDeleteSucceeds",
          db.deleteLesson(id, "alice", true) == LessonResult.SUCCESS);
      check(
          "VG04_CascadeRemovesExperiencesAndEfforts",
          count(url, "lessonExperienceDB") == 0
              && count(url, "lessonEffortDB") == 0
              && count(url, "lessonCuratorDB") == 0);
      check(
          "VG04_MissingDeleteReported",
          db.deleteLesson(id, "alice", true) == LessonResult.NOT_FOUND);
    } finally {
      db.closeConnection();
    }
    migration(dir);
    try (var files = Files.walk(dir)) {
      for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
    }
    System.out.println(
        "RESULT: " + passed + " passed; 0 failed. Real session-bound file-backed H2.");
  }
}
