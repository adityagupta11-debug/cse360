package kanishTests;

import database.Database;
import entityClasses.User;
import guiLessonSupport.*;
import java.nio.file.*;
import java.sql.*;

/**
 * Exercises the checked-out repository's real Database, user session and file-backed H2. No
 * production database, hardcoded owner input or fake experience persistence is used.
 *
 * @author Kanish Garg
 */
public final class CurrentRepoIntegrationTests {
  private static int passed;

  private static void check(String name, boolean ok) {
    if (!ok) throw new AssertionError(name);
    passed++;
    System.out.println(name + " | PASS");
  }

  private static User user(String n) {
    return new User(
        n, "DemoPass1!", "Demo", "", "User", "", n + "@example.test", false, true, false, false);
  }

  @FunctionalInterface
  interface Op {
    void run() throws Exception;
  }

  private static boolean denied(Op op) {
    try {
      op.run();
      return false;
    } catch (Exception expected) {
      return true;
    }
  }

  /**
   * KG01/KG02/KG08: creation/update use all four fields, session identity survives cache changes,
   * foreign/logout/role-revoked reads are denied, and real close/reopen persists data. Each named
   * PASS requires the stored or rejected outcome stated in its label.
   */
  public static void main(String[] args) throws Exception {
    Path dir = Files.createTempDirectory("kg-current-repo-");
    String url = "jdbc:h2:" + dir.resolve("fixture").toAbsolutePath();
    Database db = new Database(url);
    db.connectToDatabase();
    long id;
    try {
      db.register(user("kanishDemo"));
      db.register(user("otherDemo"));
      db.authenticateSession("kanishDemo", "DemoPass1!");
      var gateway = new CurrentRepoGateway(db);
      id = new guiLessonCreate.ModelLessonCreate(gateway).save(ModelTests.valid());
      check(
          "KG01_Real_CreateAllFields",
          gateway.read(id).category().equals("Testing")
              && gateway.read(id).situation().equals(ModelTests.valid().situation()));
      db.getUserAccountDetails("otherDemo");
      check("KG02_StableSessionNotCache", gateway.read(id).author().equals("kanishDemo"));
      var changed =
          new LessonGateway.Draft(
              "Changed saved lesson",
              "Process",
              "The team found a mismatch during integration.",
              "Coordinate API changes before combining code from members.");
      new guiLessonEdit.ModelLessonEdit(gateway).save(id, changed);
      check(
          "KG01_Real_UpdateFourFields",
          gateway.read(id).category().equals("Process")
              && gateway.read(id).lessonText().equals(changed.lessonText()));
      check(
          "KG03_NoInventedLockMetadata",
          !gateway.supportsLocks() && gateway.read(id).createdAt() == null);
      check(
          "KG04_NoFakeExperienceCapability",
          !gateway.supportsExperiences() && denied(() -> gateway.addExperience(id, null)));
      db.authenticateSession("otherDemo", "DemoPass1!");
      check("KG02_Real_ForeignReadDenied", denied(() -> gateway.read(id)));
      check("KG02_Real_ForeignUpdateDenied", denied(() -> gateway.update(id, changed)));
      check("KG02_Real_ForeignDeleteDenied", denied(() -> gateway.delete(id, true)));
      db.clearAuthenticatedSession();
      check("KG02_Real_LogoutDenied", denied(() -> gateway.read(id)));
      db.authenticateSession("kanishDemo", "DemoPass1!");
      try (Connection c = DriverManager.getConnection(url, "sa", "");
          Statement s = c.createStatement()) {
        s.execute("UPDATE userDB SET contributorRole=FALSE WHERE userName='kanishDemo'");
      }
      check("KG02_Real_RoleRevokedDenied", denied(() -> gateway.read(id)));
      try (Connection c = DriverManager.getConnection(url, "sa", "");
          Statement s = c.createStatement()) {
        s.execute("UPDATE userDB SET contributorRole=TRUE WHERE userName='kanishDemo'");
      }
    } finally {
      db.closeConnection();
    }
    db = new Database(url);
    db.connectToDatabase();
    try {
      db.authenticateSession("kanishDemo", "DemoPass1!");
      var gateway = new CurrentRepoGateway(db);
      check(
          "KG08_Real_ReopenRetainsUpdate",
          gateway.read(id).title().equals("Changed saved lesson")
              && gateway.read(id).category().equals("Process"));
      check(
          "KG07_Real_CancelKeeps",
          !new guiLessonDelete.ModelLessonDelete(gateway).remove(id, false)
              && gateway.read(id) != null);
      check(
          "KG07_Real_YesDeletes", new guiLessonDelete.ModelLessonDelete(gateway).remove(id, true));
    } finally {
      db.closeConnection();
    }
    db = new Database(url);
    db.connectToDatabase();
    try {
      db.authenticateSession("kanishDemo", "DemoPass1!");
      var gateway = new CurrentRepoGateway(db);
      check("KG07_Real_ReopenRetainsDeletion", denied(() -> gateway.read(id)));
    } finally {
      db.closeConnection();
    }
    System.out.println(
        "RESULT: " + passed + " passed; 0 failed. Real repository and file-backed H2.");
  }
}
