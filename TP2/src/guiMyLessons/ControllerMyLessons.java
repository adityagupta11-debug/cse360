package guiMyLessons;

import database.Database;
import entityClasses.LessonsLearned;
import entityClasses.LessonsLearnedList;

/*******
 * <p> Title: ControllerMyLessons Class. </p>
 *
 * <p> Description: The Java/FX-based My Lessons Page.  This class provides the controller
 * actions based on the contributor's use of the JavaFX GUI widgets defined by the View class:
 * loading the contributor's lessons, searching them, opening one, adding a new one, and
 * leaving the page.
 *
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 *
 * <p> Copyright: Joshua Luther © 2026 </p>
 *
 * @author Joshua Luther
 *
 * @version 1.00		2026-10-22 Initial version for TP2
 */

public class ControllerMyLessons {

	/*-*******************************************************************************************

	User Interface Actions for this page

	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and
	the Model.

	 */
	
	// Reference for the in-memory database so this package has access (the same approach the
	// Foundations View classes use)
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**
	 * Default constructor is not used.
	 */
	public ControllerMyLessons() {
	}

	
	/**********
	 * <p> Method: refreshList() </p>
	 *
	 * <p> Description: Reloads the contributor's lessons from the database and shows them.  It
	 * is called every time the page is displayed, so a lesson added, edited, or deleted on
	 * another page appears correctly when the contributor comes back. </p>
	 *
	 */
	protected static void refreshList() {
		// The owner comes from the user this page was displayed for, not from the Database's
		// current-user cache, which changes whenever any user's details are read
		ModelMyLessons.loadLessons(theDatabase, ViewMyLessons.theUser.getUserName());
		showVisibleLessons();
	}

	
	/**********
	 * <p> Method: performSearch() </p>
	 *
	 * <p> Description: Shows only the contributor's lessons that contain every keyword in the
	 * search field.  The search runs on the lessons already loaded, so it never shows another
	 * contributor's lessons. </p>
	 *
	 */
	protected static void performSearch() {
        ModelMyLessons.loadLessons(theDatabase, ViewMyLessons.theUser.getUserName());
		ViewMyLessons.label_Status.setText("");		// An earlier confirmation no longer applies
		ModelMyLessons.setSearchText(ViewMyLessons.text_Search.getText());
		showVisibleLessons();
	}

	
	/**********
	 * <p> Method: performClearSearch() </p>
	 *
	 * <p> Description: Empties the search field and shows all of the contributor's lessons
	 * again. </p>
	 *
	 */
	protected static void performClearSearch() {
        ModelMyLessons.loadLessons(theDatabase, ViewMyLessons.theUser.getUserName());
		ViewMyLessons.label_Status.setText("");
		ViewMyLessons.text_Search.clear();
		ModelMyLessons.setSearchText("");
		showVisibleLessons();
	}

	
	/**********
	 * <p> Method: performOpenLesson() </p>
	 *
	 * <p> Description: Opens the selected lesson so the contributor can examine it more closely,
	 * see which fields are locked, and edit or delete it (Contributor story 2: "I can use that
	 * list to specify which lesson learned to examine more closely"). </p>
	 *
	 */
	protected static void performOpenLesson() {
		LessonsLearned selected =
				ViewMyLessons.listView_Lessons.getSelectionModel().getSelectedItem();
		if (selected == null) return;		// The button is disabled when nothing is selected
		guiLessonSupport.ContributorWindows.details(windowContext(), selected.getLessonId());
	}

	
	/**********
	 * <p> Method: performAddLesson() </p>
	 *
	 * <p> Description: Goes to the Add Lesson page (Contributor story 1). </p>
	 *
	 */
	protected static void performAddLesson() {
		guiLessonSupport.ContributorWindows.create(windowContext());
	}

	
	/**********
	 * <p> Method: performBackToHome() </p>
	 *
	 * <p> Description: Returns to the Contributor Home page. </p>
	 *
	 */
	protected static void performBackToHome() {
		guiContributor.ViewContributorHome.displayContributorHome(ViewMyLessons.theStage,
				ViewMyLessons.theUser);
	}

	
	/**********
	 * <p> Method: performLogout() </p>
	 *
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page. </p>
	 *
	 */
	protected static void performLogout() {
        theDatabase.clearAuthenticatedSession();
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewMyLessons.theStage);
	}

	
	/**********
	 * <p> Method: performQuit() </p>
	 *
	 * <p> Description: This method terminates the execution of the program. </p>
	 *
	 */
	protected static void performQuit() {
		theDatabase.closeConnection();
        javafx.application.Platform.exit();
	}
	
	
	/*
	 * Puts the Model's current subset into the list and updates the count and the empty-list
	 * message.  The message depends on why the list is empty: a contributor with no lessons at
	 * all needs to be told to add one, while a search with no matches needs to be told to change
	 * the search.  Showing the wrong one would be confusing.
	 */
	private static void showVisibleLessons() {
		LessonsLearnedList visible = ModelMyLessons.getVisibleLessons();
		int total = ModelMyLessons.getTotalLessons();
		ViewMyLessons.listView_Lessons.getItems().setAll(visible.getLessons());
		
		if (total == 0)
			ViewMyLessons.label_Placeholder.setText(LessonsLearnedList.EMPTY_LIST_MESSAGE);
		else
			ViewMyLessons.label_Placeholder.setText(LessonsLearnedList.NO_MATCHES_MESSAGE);
		
		if (ModelMyLessons.getSearchText().isBlank())
			ViewMyLessons.label_Count.setText(total + (total == 1 ? " lesson" : " lessons"));
		else
			ViewMyLessons.label_Count.setText(visible.size() + " of " + total
					+ (total == 1 ? " lesson matches" : " lessons match") + " \""
					+ ModelMyLessons.getSearchText().trim() + "\"");
	}

    /** VG10: use the same gateway/session when returning from Kanish's windows to Joshua's list. */
    private static guiLessonSupport.WindowContext windowContext() {
        return new guiLessonSupport.WindowContext(ViewMyLessons.theStage,
            new guiLessonSupport.CurrentRepoGateway(theDatabase),
            () -> guiMyLessons.ViewMyLessons.displayMyLessons(ViewMyLessons.theStage, ViewMyLessons.theUser),
            () -> performLogout(), () -> performQuit());
    }
}