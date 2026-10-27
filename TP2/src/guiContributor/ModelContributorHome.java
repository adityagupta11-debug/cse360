package guiContributor;
import database.Database;
import entityClasses.LessonList;
/** K.G. integration model for Joshua's existing own-lesson list. The database reference is the
 * Foundations instance. A stable authenticated Contributor identity, not the mutable account
 * details cache or the displayed User object, determines whose list may be shown.
 * @author Kanish Garg */
public final class ModelContributorHome {
    private final Database database;
    public ModelContributorHome(Database database){this.database=database;}
    /** KG02 read only the current Contributor's lessons; reject logout/role revocation. */
    public LessonList ownLessons()throws Exception{
        String actor=database.getAuthenticatedContributorName();
        if(actor==null)throw new IllegalStateException("Sign in with an active Contributor account, then reopen My Lessons.");
        return database.getLessonsByOwner(actor);
    }
}
