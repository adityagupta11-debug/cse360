package entityClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import database.Database;
import database.Database.LessonResult;

/*******
 * <p> Title: LessonsLearnedList Class </p>
 * 
 * <p> Description: This LessonsLearnedList class holds a collection of LessonsLearned objects.
 *  It can hold every lesson stored in the system (the persistent list of all lessons learned)
 *  or any subset of them, such as the lessons owned by one contributor or the lessons that
 *  match a keyword search.  A list may be empty, may contain one or more lessons, and has no
 *  fixed upper limit on the number of lessons it can hold.  The list supports Create, Read,
 *  Update, and Delete both in memory and, through the Database, persistently. </p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * 
 * @version 1.00		2026-10-08 Initial version for HW2 (as LessonList)
 * @version 1.01		2026-10-22 Renamed LessonsLearnedList for TP2; added persistent loading,
 * 							persistent CRUD, and keyword search (Joshua Luther)
 * 
 */

/*
 * How this class supports the user stories:
 *   - Contributor story 2, "I can see a list of all the lessons learned I have added":
 *     loadForContributor() builds that subset from the database.
 *   - Contributor story 4, "I can only see the content I have added or the content that I have
 *     added after a curator has edited it": the subset is chosen by owner only, so a lesson a
 *     curator edited is still included, and no other contributor's lesson ever is.
 *   - Contributor story 1, "Once a lesson has been added to the system, I can see and manage
 *     it": addLesson(), updateLesson(), and deleteLesson() change the database and this list
 *     together, so the list on the screen always matches what is stored.
 *   - Viewer Phase 3 stories (all lessons, keyword search): loadAll() and search() are in
 *     place now so TP3 can build the Viewer pages on them.
 * 
 * An ArrayList is used rather than a fixed-size array because there must be no upper limit on
 * the number of lessons, and an ArrayList grows as needed.  The internal list is never handed
 * out directly; callers receive a read-only view, so the only way to change a list is through
 * its own methods.
 * 
 * The persistent operations take the Database as a parameter rather than storing it.  A list
 * built by search() or filterByOwner() is an in-memory subset, not a separate copy in the
 * database, so it should not carry a database reference that suggests otherwise.  The GUI
 * passes the one shared Database object, exactly as it does for every other database call.
 */
public class LessonsLearnedList {
	
	/** The message shown when a contributor's list is empty.  It is kept here so the GUI and the
	 *  automated tests use exactly the same wording. */
	public static final String EMPTY_LIST_MESSAGE = "You have not added any lessons yet.";
	
	/** The message shown when a search finds no lessons.  It is kept here for the same reason as
	 *  EMPTY_LIST_MESSAGE. */
	public static final String NO_MATCHES_MESSAGE = "No lessons match your search.";
	
	private final List<LessonsLearned> lessons;		// The lessons held by this list, in order
	
	
	/*****
	 * <p> Method: LessonsLearnedList() </p>
	 * 
	 * <p> Description: This constructor establishes an empty list.  An empty list is a valid
	 * result (e.g., a contributor who has not added any lessons), so callers never receive
	 * null. </p>
	 */
	public LessonsLearnedList() {
		lessons = new ArrayList<LessonsLearned>();
	}

	
	/*****
	 * <p> Method: LessonsLearnedList(List&lt;LessonsLearned&gt; lessons) </p>
	 * 
	 * <p> Description: This constructor establishes a list holding a copy of the given lessons.
	 * Copying protects this list from later changes made to the caller's list.  Null entries are
	 * skipped, and a null argument produces an empty list. </p>
	 * 
	 * @param lessons specifies the lessons this list should hold
	 * 
	 */
	public LessonsLearnedList(List<LessonsLearned> lessons) {
		this();
		if (lessons == null) return;
		for (LessonsLearned lesson : lessons) add(lesson);
	}
	
	
	/*-*******************************************************************************************
	
	Persistent loading (Read from the database)
	
	 */
	
	/*****
	 * <p> Method: LessonsLearnedList loadAll(Database database) </p>
	 * 
	 * <p> Description: Builds the persistent list of all lessons learned in the system, in
	 * title order, by reading them from the database.  This is the full list from which every
	 * subset is drawn. </p>
	 * 
	 * @param database is the application's Database
	 * 
	 * @return a list of every stored lesson; empty (never null) if there are none
	 * 
	 */
	// Read: all lessons (the persistent list of all lessons learned)
	public static LessonsLearnedList loadAll(Database database) {
		if (database == null) return new LessonsLearnedList();
		return database.getAllLessons();
	}

	
	/*****
	 * <p> Method: LessonsLearnedList loadForContributor(Database database, String username)
	 * </p>
	 * 
	 * <p> Description: Builds the subset of lessons a contributor is allowed to see: only the
	 * lessons that contributor added, including any a curator has since edited (Contributor
	 * stories 2 and 4).  Lessons added by other contributors are never included. </p>
	 * 
	 * @param database is the application's Database
	 * 
	 * @param username is the logged-in contributor's username
	 * 
	 * @return that contributor's lessons; empty (never null) if there are none
	 * 
	 */
	// Read: the my-lessons subset
	public static LessonsLearnedList loadForContributor(Database database, String username) {
		if (database == null || username == null) return new LessonsLearnedList();
		// Selecting by owner alone is what satisfies Contributor story 4: a curator's edit does
		// not change the owner, so edited lessons stay in the contributor's list
		return database.getLessonsByOwner(username);
	}
	
	
	/*-*******************************************************************************************
	
	Persistent Create, Update, and Delete (the database and this list change together)
	
	 */
	
	/*****
	 * <p> Method: LessonResult addLesson(Database database, LessonsLearned lesson) </p>
	 * 
	 * <p> Description: Stores a new lesson in the database and, only if that succeeds, adds it
	 * to this list.  The Database validates the lesson first, so an invalid lesson is never
	 * stored or added; the specific message is in LessonRecognizer.lessonRecognizerErrorMessage.
	 * </p>
	 * 
	 * @param database is the application's Database
	 * 
	 * @param lesson is the new lesson; its owner is the contributor adding it
	 * 
	 * @return SUCCESS, INVALID_INPUT, or DATABASE_ERROR
	 * 
	 */
	// Create: add a lesson to the database and to this list
	public LessonResult addLesson(Database database, LessonsLearned lesson) {
		if (database == null) return LessonResult.DATABASE_ERROR;
		LessonResult result = database.addLesson(lesson);
		// Storage normalizes core text and ignores caller-supplied lock/curator metadata.
		// Keep the authoritative stored projection, never the still-mutable request object.
		if (result == LessonResult.SUCCESS) {
			LessonsLearned stored = database.getLesson(lesson.getLessonId());
			if (stored != null) add(stored);
		}
		return result;
	}

	
	/*****
	 * <p> Method: LessonResult updateLesson(Database database, LessonsLearned lesson,
	 * 		String actingUser) </p>
	 * 
	 * <p> Description: Saves an edited lesson to the database and, if that succeeds, replaces
	 * the lesson in this list with the stored version.  If the database refuses the update
	 * (invalid input, not found, not the owner, or locked), this list is left unchanged. </p>
	 * 
	 * @param database is the application's Database
	 * 
	 * @param lesson holds the edited values and the ID of the lesson to change
	 * 
	 * @param actingUser is the username of the contributor making the change
	 * 
	 * @return the Database's result (SUCCESS or the reason the update was refused)
	 * 
	 */
	// Update: change a lesson in the database and in this list
	public LessonResult updateLesson(Database database, LessonsLearned lesson,
			String actingUser) {
		if (database == null) return LessonResult.DATABASE_ERROR;
		LessonResult result = database.updateLesson(lesson, actingUser);
		if (result == LessonResult.SUCCESS) {
			// Re-read the stored version rather than keeping the caller's object, because the
			// Database trims the text before storing it; the list must show what was saved
			LessonsLearned stored = database.getLesson(lesson.getLessonId());
			if (stored != null) replace(stored);
		}
		return result;
	}

	
	/*****
	 * <p> Method: LessonResult deleteLesson(Database database, int lessonId, String actingUser,
	 * 		boolean confirmed) </p>
	 * 
	 * <p> Description: Deletes a lesson from the database and, if that succeeds, removes it from
	 * this list.  The confirmed parameter carries the user's answer to "Are you sure?"; a "No"
	 * answer changes nothing. </p>
	 * 
	 * @param database is the application's Database
	 * 
	 * @param lessonId is the database ID of the lesson to delete
	 * 
	 * @param actingUser is the username of the contributor requesting the delete
	 * 
	 * @param confirmed is true only when the user answered "Yes"
	 * 
	 * @return the Database's result (SUCCESS, CANCELLED, or the reason the delete was refused)
	 * 
	 */
	// Delete: remove a lesson from the database and from this list
	public LessonResult deleteLesson(Database database, int lessonId, String actingUser,
			boolean confirmed) {
		if (database == null) return LessonResult.DATABASE_ERROR;
		LessonResult result = database.deleteLesson(lessonId, actingUser, confirmed);
		if (result == LessonResult.SUCCESS) removeById(lessonId);
		return result;
	}
	
	
	/*-*******************************************************************************************
	
	Search and subsets (Read)
	
	 */
	
	/*****
	 * <p> Method: LessonsLearnedList search(String keywords) </p>
	 * 
	 * <p> Description: Builds a new list holding the lessons in this list that match every
	 * keyword.  A keyword matches when it appears anywhere in the lesson's title, category,
	 * situation, or lesson text, ignoring upper and lower case.  Keywords are separated by
	 * spaces.  A blank or null search returns a copy of this whole list.  This list is not
	 * changed, and the result may be empty. </p>
	 * 
	 * @param keywords specifies one or more keywords separated by spaces
	 * 
	 * @return a new LessonsLearnedList containing the matching subset
	 * 
	 */
	// Read: keyword search subset (supports the Viewer and Curator search stories in TP3)
	public LessonsLearnedList search(String keywords) {
		if (keywords == null || keywords.isBlank()) return new LessonsLearnedList(lessons);
		
		// Every keyword must match ("all", not "any"), because adding words to a search should
		// narrow the results, which is what users expect from a search box
		String[] terms = keywords.trim().toLowerCase().split("\\s+");
		LessonsLearnedList matches = new LessonsLearnedList();
		for (LessonsLearned lesson : lessons) {
			String searchable = searchableText(lesson);
			boolean allFound = true;
			for (String term : terms) {
				if (!searchable.contains(term)) { allFound = false; break; }
			}
			if (allFound) matches.add(lesson);
		}
		return matches;
	}

	
	/*****
	 * <p> Method: LessonsLearnedList filterByOwner(String owner) </p>
	 * 
	 * <p> Description: Builds a new list holding only the lessons owned by the specified
	 * contributor.  This list is not changed.  The result may be empty. </p>
	 * 
	 * @param owner specifies the username of the contributor
	 * 
	 * @return a new LessonsLearnedList containing the matching subset
	 * 
	 */
	// Read: the subset of lessons owned by one contributor, taken from a list already in memory
	public LessonsLearnedList filterByOwner(String owner) {
		LessonsLearnedList subset = new LessonsLearnedList();
		if (owner == null) return subset;
		for (LessonsLearned lesson : lessons) {
			if (owner.equals(lesson.getOwner())) subset.add(lesson);
		}
		return subset;
	}
	
	
	/*-*******************************************************************************************
	
	In-memory operations
	
	 */

	/*****
	 * <p> Method: boolean add(LessonsLearned lesson) </p>
	 * 
	 * <p> Description: Adds a lesson to the end of this list in memory only.  Null is rejected
	 * so every element in the list is always a real lesson.  To store a new lesson, use
	 * addLesson(). </p>
	 * 
	 * @param lesson specifies the lesson to be added
	 * 
	 * @return true if the lesson was added, else false (the lesson was null)
	 * 
	 */
	public boolean add(LessonsLearned lesson) {
		if (lesson == null) return false;
		return lessons.add(lesson);
	}

	
	/*****
	 * <p> Method: LessonsLearned get(int index) </p>
	 * 
	 * <p> Description: Returns the lesson at the specified position in the list. </p>
	 * 
	 * @param index specifies the position of the lesson (0 is the first lesson)
	 * 
	 * @return the lesson at that position, or null if the index is out of range
	 * 
	 */
	public LessonsLearned get(int index) {
		if (index < 0 || index >= lessons.size()) return null;
		return lessons.get(index);
	}

	
	/*****
	 * <p> Method: LessonsLearned findById(int lessonId) </p>
	 * 
	 * <p> Description: Returns the lesson in this list with the specified database ID. </p>
	 * 
	 * @param lessonId specifies the database ID of the lesson to find
	 * 
	 * @return the matching lesson, or null if no lesson in this list has that ID
	 * 
	 */
	public LessonsLearned findById(int lessonId) {
		for (LessonsLearned lesson : lessons) {
			if (lesson.getLessonId() == lessonId) return lesson;
		}
		return null;
	}

	
	/*****
	 * <p> Method: boolean replace(LessonsLearned lesson) </p>
	 * 
	 * <p> Description: Replaces the lesson in this list that has the same database ID, in
	 * memory only, keeping its position.  To save an edit, use updateLesson(). </p>
	 * 
	 * @param lesson specifies the new version of the lesson
	 * 
	 * @return true if a lesson with that ID was found and replaced, else false
	 * 
	 */
	public boolean replace(LessonsLearned lesson) {
		if (lesson == null) return false;
		for (int i = 0; i < lessons.size(); i++) {
			if (lessons.get(i).getLessonId() == lesson.getLessonId()) {
				lessons.set(i, lesson);
				return true;
			}
		}
		return false;
	}

	
	/*****
	 * <p> Method: boolean removeById(int lessonId) </p>
	 * 
	 * <p> Description: Removes the lesson with the specified database ID from this list, in
	 * memory only.  To delete a lesson from the database, use deleteLesson(). </p>
	 * 
	 * @param lessonId specifies the database ID of the lesson to remove
	 * 
	 * @return true if a lesson was removed, else false
	 * 
	 */
	public boolean removeById(int lessonId) {
		return lessons.removeIf(lesson -> lesson.getLessonId() == lessonId);
	}

	
	/*****
	 * <p> Method: int size() </p>
	 * 
	 * <p> Description: Returns the number of lessons in the list. </p>
	 * 
	 * @return the number of lessons (0 or more)
	 * 
	 */
	public int size() { return lessons.size(); }

	
	/*****
	 * <p> Method: boolean isEmpty() </p>
	 * 
	 * <p> Description: Determines whether the list holds no lessons. </p>
	 * 
	 * @return true if the list is empty, else false
	 * 
	 */
	public boolean isEmpty() { return lessons.isEmpty(); }

	
	/*****
	 * <p> Method: List&lt;LessonsLearned&gt; getLessons() </p>
	 * 
	 * <p> Description: Returns a read-only view of the lessons, suitable for loading into a
	 * JavaFX ListView or looping over in a test.  Attempting to change the returned list throws
	 * an exception, which protects the contents of this list. </p>
	 * 
	 * @return an unmodifiable view of the lessons in this list
	 * 
	 */
	public List<LessonsLearned> getLessons() { return Collections.unmodifiableList(lessons); }
	
	
	// Builds the lower-case text a search looks through: every core field a contributor writes
	private static String searchableText(LessonsLearned lesson) {
		return (nullToEmpty(lesson.getTitle()) + " " + nullToEmpty(lesson.getCategory()) + " "
				+ nullToEmpty(lesson.getSituation()) + " " + nullToEmpty(lesson.getLessonText()))
				.toLowerCase();
	}
	
	private static String nullToEmpty(String s) { return (s == null) ? "" : s; }
}