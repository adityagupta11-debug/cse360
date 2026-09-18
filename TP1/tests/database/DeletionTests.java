package database;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.*;
import entityClasses.User;
import database.Database.DeletionResult;

/** Semiautomated tests in the supplied password-testbed style, using real isolated H2. */
public final class DeletionTests {
    private static int passed;
    private static final String PASSWORD = "DemoPass1!";
    private static User user(String name, boolean admin) {
        return new User(name, PASSWORD, "Demo", "", "User", "", name + "@example.test", admin, true, true, true);
    }
    private static void check(String id, boolean result) {
        if (!result) throw new AssertionError("FAIL " + id);
        passed++;
        System.out.println("PASS " + id);
    }
    private static int id(Database db, String name) throws SQLException {
        return db.getDeletionCandidates().stream().filter(u -> u.username().equals(name))
                .findFirst().orElseThrow().id();
    }
    public static void main(String[] args) throws Exception {
        String url = "jdbc:h2:mem:kg_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        Database db = new Database(url);
        db.connectToDatabase();
        try {
            db.register(user("adminOne", true)); db.register(user("adminTwo", true));
            db.register(user("memberOne", false));
            check("D01 registration does not authenticate", !db.isAuthenticatedAdmin());
            check("D02 valid admin authenticates", db.authenticateSession("adminOne", PASSWORD));
            int actor=id(db,"adminOne"), other=id(db,"adminTwo"), member=id(db,"memberOne");
            check("D03 No cancels with unchanged count", db.deleteUserAccount(member,false)==DeletionResult.CANCELLED && db.getNumberOfUsers()==3);
            check("D04 missing selection", db.deleteUserAccount(0,true)==DeletionResult.NO_SELECTION);
            check("D05 self denied with multiple admins", db.deleteUserAccount(actor,true)==DeletionResult.SELF_PROTECTED);
            db.getUserAccountDetails("memberOne");
            check("D06 reading another user does not change actor", db.isAuthenticatedAdmin() && db.deleteUserAccount(actor,true)==DeletionResult.SELF_PROTECTED);
            db.authenticateSession("memberOne",PASSWORD);
            check("D07 non-admin denied", db.deleteUserAccount(other,true)==DeletionResult.UNAUTHORIZED);
            boolean denied=false; try { db.getDeletionCandidates(); } catch(SQLException expected) { denied=true; }
            check("D08 non-admin cannot list deletion choices", denied);
            db.clearAuthenticatedSession();
            check("D09 logged-out caller denied", db.deleteUserAccount(member,true)==DeletionResult.UNAUTHORIZED);
            db.authenticateSession("adminOne",PASSWORD);
            check("D10 nonexistent identity", db.deleteUserAccount(Integer.MAX_VALUE,true)==DeletionResult.NOT_FOUND);
            check("D11 Yes deletes selected member only", db.deleteUserAccount(member,true)==DeletionResult.DELETED && db.getNumberOfUsers()==2 && db.doesUserExist("adminTwo"));
            check("D12 removed account cannot authenticate", !db.authenticateSession("memberOne",PASSWORD));
            check("D13 deleted account fails all role logins", !db.loginAdmin(user("memberOne",false)) && !db.loginContributor(user("memberOne",false)) && !db.loginViewer(user("memberOne",false)) && !db.loginCurator(user("memberOne",false)));
            check("D14 deleted details unavailable", !db.getUserAccountDetails("memberOne"));
            db.authenticateSession("adminOne",PASSWORD);
            check("D15 repeat delete does not remove another account", db.deleteUserAccount(member,true)==DeletionResult.NOT_FOUND && db.getNumberOfUsers()==2);
            db.register(user("memberOne",false));
            check("D16 reused username has a new identity", id(db,"memberOne")!=member && db.deleteUserAccount(member,true)==DeletionResult.NOT_FOUND && db.doesUserExist("memberOne"));
            check("D17 another admin may be deleted", db.deleteUserAccount(other,true)==DeletionResult.DELETED && db.isAuthenticatedAdmin());
            check("D18 sole admin cannot self-delete", db.deleteUserAccount(actor,true)==DeletionResult.SELF_PROTECTED && !db.isDatabaseEmpty());
            db.closeConnection(); db.connectToDatabase();
            check("D19 deletion survives reconnect", !db.doesUserExist("adminTwo") && db.doesUserExist("adminOne"));
            check("D20 reconnect clears session", !db.isAuthenticatedAdmin());
            db.authenticateSession("adminOne",PASSWORD);
            check("D21 failed login clears prior authorization", !db.authenticateSession("adminOne","wrong") && !db.isAuthenticatedAdmin());
            check("D22 oversize username rejected", !db.authenticateSession("a".repeat(33),PASSWORD));
            check("D23 oversize password rejected", !db.authenticateSession("adminOne","a".repeat(65)));
            check("D24 null credentials rejected", !db.authenticateSession(null,null));
            check("D25 SQL syntax in credentials is treated as data", !db.authenticateSession("' OR '1'='1",PASSWORD) && db.getNumberOfUsers()==2);
            db.authenticateSession("adminOne",PASSWORD);
            int replacement=id(db,"memberOne");
            try(Connection external=DriverManager.getConnection(url,"sa",""); Statement sql=external.createStatement()) {
                sql.executeUpdate("UPDATE userDB SET adminRole=FALSE WHERE userName='adminOne'");
                check("D26 revoked role is rechecked", db.deleteUserAccount(replacement,true)==DeletionResult.UNAUTHORIZED && db.doesUserExist("memberOne"));
                sql.executeUpdate("UPDATE userDB SET adminRole=TRUE WHERE userName='adminOne'");
                sql.execute("CREATE TABLE dependent (userId INT REFERENCES userDB(id))");
                sql.execute("INSERT INTO dependent VALUES ("+replacement+")");
                boolean failed=false;try { db.deleteUserAccount(replacement,true); } catch(SQLException expected) { failed=true; }
                check("D27 storage failure rolls back", failed && db.doesUserExist("memberOne"));
                sql.execute("DROP TABLE dependent");
                check("D28 transaction usable after rollback", db.deleteUserAccount(replacement,true)==DeletionResult.DELETED);
            }
            check("D30 admin rename keeps stable self-protection", db.updateUserName("adminOne","adminRenamed") && db.isAuthenticatedAdmin() && db.deleteUserAccount(actor,true)==DeletionResult.SELF_PROTECTED);
            db.updatePassword("adminRenamed","ChangedPass2!");
            check("D31 password update retains current identity", db.isAuthenticatedAdmin() && db.deleteUserAccount(actor,true)==DeletionResult.SELF_PROTECTED);
            check("D32 new credentials authenticate after rename", db.authenticateSession("adminRenamed","ChangedPass2!") && !db.authenticateSession("adminOne",PASSWORD));
            db.register(user("otpMember",false));
            db.setOneTimePassword("otpMember","Temporary1!",java.time.LocalDateTime.now().plusHours(1));
            check("D33 legacy entry rejects logged-out calls", !db.deleteUser("otpMember") && db.doesUserExist("otpMember"));
            db.authenticateSession("adminRenamed","ChangedPass2!");
            check("D34 legacy entry blocks self-deletion", !db.deleteUser("adminRenamed"));
            check("D35 deletion also removes OTP access", db.deleteUserAccount(id(db,"otpMember"),true)==DeletionResult.DELETED && !db.loginWithOneTimePassword("otpMember","Temporary1!"));
        } finally { db.closeConnection(); }
        concurrentDeletion();
        System.out.println("RESULT: " + passed + " database checks passed; 0 failed.");
    }

    private static void concurrentDeletion() throws Exception {
        String url="jdbc:h2:mem:race_"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000";
        Database a=new Database(url), b=new Database(url);
        a.connectToDatabase(); b.connectToDatabase();
        ExecutorService threads=Executors.newFixedThreadPool(2);
        try {
            a.register(user("adminLeft",true)); a.register(user("adminRight",true));
            a.authenticateSession("adminLeft",PASSWORD); b.authenticateSession("adminRight",PASSWORD);
            int left=id(a,"adminLeft"),right=id(a,"adminRight");
            CountDownLatch start=new CountDownLatch(1);
            Future<DeletionResult> f1=threads.submit(()->{start.await();return a.deleteUserAccount(right,true);});
            Future<DeletionResult> f2=threads.submit(()->{start.await();return b.deleteUserAccount(left,true);});
            start.countDown();
            DeletionResult r1=f1.get(10,TimeUnit.SECONDS),r2=f2.get(10,TimeUnit.SECONDS);
            check("D29 concurrent cross-deletion preserves an admin", a.getNumberOfUsers()==1 &&
                ((r1==DeletionResult.DELETED && r2==DeletionResult.UNAUTHORIZED)||
                 (r2==DeletionResult.DELETED && r1==DeletionResult.UNAUTHORIZED)));
        } finally { threads.shutdownNow(); a.closeConnection(); b.closeConnection(); }
    }
}
