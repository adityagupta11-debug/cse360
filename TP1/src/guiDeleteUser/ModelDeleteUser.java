package guiDeleteUser;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import database.Database;
import database.Database.DeletionCandidate;
import database.Database.DeletionResult;

/*******
 * <p>Title: ModelDeleteUser</p>
 * <p>Description: K.G.'s model boundary for confirmed account removal. Database owns
 * authentication, authorization and persistence; the model exposes view-ready outcomes.</p>
 * @author A.G. (agupt545) - original model scaffold
 * @author Kanish Garg (Team 53) - deletion model behavior
 */
public final class ModelDeleteUser {
    private final Database database;

    public ModelDeleteUser(Database database) { this.database = Objects.requireNonNull(database); }

    protected List<DeletionCandidate> choices() throws SQLException {
        return database.getDeletionCandidates();
    }

    protected DeletionResult remove(DeletionCandidate selected, boolean yes) throws SQLException {
        return database.deleteUserAccount(selected == null ? 0 : selected.id(), yes);
    }

    protected static String message(DeletionResult result) {
        return switch (result) {
            case DELETED -> "Account deleted. This account can no longer log in.";
            case CANCELLED -> "Cancelled. No account was changed.";
            case NO_SELECTION -> "Select an account before choosing Delete account.";
            case UNAUTHORIZED -> "Your administrator session is no longer valid. Log in again.";
            case SELF_PROTECTED -> "You cannot delete your own account. Ask another administrator.";
            case NOT_FOUND -> "This account no longer exists. Select another account.";
            case LAST_ADMIN_PROTECTED -> "The last administrator account must remain available.";
        };
    }
}
