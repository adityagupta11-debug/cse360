package guiContributor;


/*******
 * <p> Title: ControllerContributorHome Class. </p>
 *
 * <p> Description: The Java/FX-based Contributor Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class: going to
 * the My Lessons and Add Lesson pages, updating the account, logging out, and quitting.
 *
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 * @author Joshua Luther (J.L.) - lessons learned navigation
 *
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *
 * @version 1.02		2026-10-08 Lesson create, read, update, and delete actions for HW2
 * 							(Joshua Luther)
 * @version 1.03		2026-10-22 Lesson actions moved to the My Lessons, Add Lesson, and Lesson
 * 							Detail pages for TP2; this page now navigates to them (Joshua Luther)
 */

public class ControllerContributorHome {

	/*-*******************************************************************************************

	User Interface Actions for this page

	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and
	the Model is often just a stub, or will be a singleton instantiated object.

	 */

	/**
	 * Default constructor is not used.
	 */
	public ControllerContributorHome() {
	}

	/**********
	 * <p> Method: performMyLessons() </p>
	 *
	 * <p> Description: This method directs the contributor to the My Lessons page, which lists
	 * the lessons the contributor has added (Contributor stories 2 and 4). </p>
	 *
	 */
	protected static void performMyLessons() {
		guiMyLessons.ViewMyLessons.displayMyLessons(ViewContributorHome.theStage,
				ViewContributorHome.theUser);
	}

	/**********
	 * <p> Method: performAddLesson() </p>
	 *
	 * <p> Description: This method directs the contributor to the Add Lesson page (Contributor
	 * story 1). </p>
	 *
	 */
	protected static void performAddLesson() {
		guiAddLesson.ViewAddLesson.displayAddLesson(ViewContributorHome.theStage,
				ViewContributorHome.theUser);
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 *
	 * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 *
	 */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewContributorHome.theStage, ViewContributorHome.theUser);
	}

	/**********
	 * <p> Method: performLogout() </p>
	 *
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 *
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewContributorHome.theStage);
	}

	/**********
	 * <p> Method: performQuit() </p>
	 *
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 *
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}