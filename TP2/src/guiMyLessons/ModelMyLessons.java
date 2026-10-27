package guiMyLessons;

import database.Database;
import entityClasses.LessonsLearnedList;

/*******
 * <p> Title: ModelMyLessons Class. </p>
 * 
 * <p> Description: The My Lessons Page Model.  This class holds the data the page displays:
 * the full list of lessons the logged-in contributor is allowed to see, and the search text the
 * contributor has entered.  The View shows whatever subset this Model provides, and the
 * Controller changes the Model when the contributor searches or the data changes. </p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * 
 * @version 1.00		2026-10-22 Initial version for TP2
 * 
 */

/*
 * Unlike most of the Foundations Model classes, which are stubs, this Model holds real state.
 * Keeping the contributor's full list separate from the search text means a search never
 * reloads from the database and clearing the search never loses lessons: the page shows
 * myLessons.search(searchText), and an empty search shows the whole list.
 */
public class ModelMyLessons {
	
	// All the lessons this contributor may see (Contributor stories 2 and 4)
	private static LessonsLearnedList myLessons = new LessonsLearnedList();
	
	// The keywords currently entered in the search field ("" means show every lesson)
	private static String searchText = "";

	/**
	 * Default constructor is not used.
	 */
	public ModelMyLessons() {
	}

	
	/**********
	 * <p> Method: loadLessons(Database database, String username) </p>
	 * 
	 * <p> Description: Reads the contributor's lessons from the database.  Only lessons this
	 * contributor added are loaded, including any a curator has since edited, so another
	 * contributor's lessons can never appear on this page. </p>
	 * 
	 * @param database is the application's Database
	 * 
	 * @param username is the logged-in contributor's username
	 * 
	 */
	protected static void loadLessons(Database database, String username) {
		myLessons = LessonsLearnedList.loadForContributor(database, username);
	}

	
	/**********
	 * <p> Method: LessonsLearnedList getVisibleLessons() </p>
	 * 
	 * <p> Description: Returns the lessons the page should show right now: the contributor's
	 * lessons that match the search text, or all of them when the search is blank. </p>
	 * 
	 * @return the subset to display (may be empty, never null)
	 * 
	 */
	protected static LessonsLearnedList getVisibleLessons() {
		return myLessons.search(searchText);
	}

	
	/**********
	 * <p> Method: int getTotalLessons() </p>
	 * 
	 * <p> Description: Returns how many lessons the contributor has in total, before any search,
	 * so the page can tell "you have no lessons" apart from "no lessons match your search". </p>
	 * 
	 * @return the number of lessons this contributor has
	 * 
	 */
	protected static int getTotalLessons() { return myLessons.size(); }

	
	/**********
	 * <p> Method: setSearchText(String text) </p>
	 * 
	 * <p> Description: Records the keywords to search for.  Null is stored as "" so the Model
	 * never has to check for it. </p>
	 * 
	 * @param text the keywords entered by the contributor
	 * 
	 */
	protected static void setSearchText(String text) { searchText = (text == null) ? "" : text; }

	
	/**********
	 * <p> Method: String getSearchText() </p>
	 * 
	 * <p> Description: Returns the keywords currently being searched for. </p>
	 * 
	 * @return the search text ("" when there is no search)
	 * 
	 */
	protected static String getSearchText() { return searchText; }
}