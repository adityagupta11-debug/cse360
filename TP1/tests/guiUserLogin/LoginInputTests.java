package guiUserLogin;

/**
 * Boundary tests for the invitation field redesigned in K.G.'s login UI.
 *
 * @author Kanish Garg (K.G.) - invitation-field boundary tests
 * @version 1.00 2026-09-17 Initial version (K.G.)
 * @version 1.01 2026-09-23 Attribution documented only; no behavior changed (Vishwam)
 */
public final class LoginInputTests {
    public static void main(String[] args) throws Exception {
        var done = new java.util.concurrent.CompletableFuture<Void>();
        javafx.application.Platform.startup(() -> {
            try { runChecks(); done.complete(null); }
            catch (Throwable ex) { done.completeExceptionally(ex); }
        });
        try { done.get(30, java.util.concurrent.TimeUnit.SECONDS); }
        finally { javafx.application.Platform.exit(); }
    }
    private static void runChecks() {
        String[] values={null,"","   ","a","abcdef","1234567890","12345678901"};
        boolean[] expected={false,false,false,true,true,true,false};
        for(int i=0;i<values.length;i++) {
            if(ControllerUserLogin.validInvitationInput(values[i])!=expected[i])
                throw new AssertionError("FAIL I"+(i+1));
            System.out.println("PASS I"+(i+1)+" invitation boundary");
        }
        System.out.println("RESULT: 7 invitation checks passed; 0 failed.");
    }
}
