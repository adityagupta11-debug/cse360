package entityClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*******
 * <p> Title: LessonList Class </p>
 * 
 * <p> Description: This LessonList class holds a collection of Lesson objects.  It can hold every
 *  lesson stored in the system or any subset of them, such as the lessons owned by one
 *  contributor or the results of a search.  A list may be empty, may contain one or more
 *  lessons, and has no fixed upper limit on the number of lessons it can hold. </p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * 
 * @version 1.00		2026-10-08 Initial version for HW2
 * 
 */

/*
 * An ArrayList is used rather than a fixed-size array because the assignment requires that there
 * be no upper limit on the number of lessons, and an ArrayList grows as needed.  The internal list
 * is never handed out directly; callers receive a read-only view, so the only way to change a
 * LessonList is through its own methods.
 */


public class LessonList {
	
	private final List<Lesson> lessons;		// The lessons held by this list, in insertion order
	
	/** The message shown when a contributor's list is empty.  It is kept here so the GUI and the
	 *  automated tests use exactly the same wording. */
	public static final String EMPTY_LIST_MESSAGE = "You have not added any lessons yet.";
	
	/*****
	 * <p> Method: LessonList() </p>
	 * 
	 * <p> Description: This constructor establishes an empty list.  An empty list is a valid
	 * result (e.g., a contributor who has not added any lessons), so callers never receive
	 * null. </p>
	 */
	public LessonList() {
		lessons = new ArrayList<Lesson>();
	}

	
	/*****
	 * <p> Method: LessonList(List&lt;Lesson&gt; lessons) </p>
	 * 
	 * <p> Description: This constructor establishes a list holding a copy of the given lessons.
	 * Copying protects this list from later changes made to the caller's list.  Null entries are
	 * skipped, and a null argument produces an empty list. </p>
	 * 
	 * @param lessons specifies the lessons this list should hold
	 * 
	 */
	public LessonList(List<Lesson> lessons) {
		this();
		if (lessons == null) return;
		for (Lesson lesson : lessons) add(lesson);
	}

	
	/*****
	 * <p> Method: boolean add(Lesson lesson) </p>
	 * 
	 * <p> Description: Adds a lesson to the end of the list.  Null is rejected so every element
	 * in the list is always a real lesson. </p>
	 * 
	 * @param lesson specifies the lesson to be added
	 * 
	 * @return true if the lesson was added, else false (the lesson was null)
	 * 
	 */
	// Add a lesson to the list
	public boolean add(Lesson lesson) {
		if (lesson == null) return false;
		return lessons.add(lesson);
	}

	
	/*****
	 * <p> Method: Lesson get(int index) </p>
	 * 
	 * <p> Description: Returns the lesson at the specified position in the list. </p>
	 * 
	 * @param index specifies the position of the lesson (0 is the first lesson)
	 * 
	 * @return the lesson at that position, or null if the index is out of range
	 * 
	 */
	// Get a lesson by its position in the list
	public Lesson get(int index) {
		if (index < 0 || index >= lessons.size()) return null;
		return lessons.get(index);
	}

	
	/*****
	 * <p> Method: Lesson findById(int lessonId) </p>
	 * 
	 * <p> Description: Returns the lesson in this list with the specified database ID. </p>
	 * 
	 * @param lessonId specifies the database ID of the lesson to find
	 * 
	 * @return the matching lesson, or null if no lesson in this list has that ID
	 * 
	 */
	// Find a lesson by its database ID
	public Lesson findById(int lessonId) {
		for (Lesson lesson : lessons) {
			if (lesson.getLessonId() == lessonId) return lesson;
		}
		return null;
	}

	
	/*****
	 * <p> Method: boolean removeById(int lessonId) </p>
	 * 
	 * <p> Description: Removes the lesson with the specified database ID from this list.  This
	 * only changes the list in memory; deleting a lesson from the database is done by the
	 * Database class. </p>
	 * 
	 * @param lessonId specifies the database ID of the lesson to remove
	 * 
	 * @return true if a lesson was removed, else false
	 * 
	 */
	// Remove a lesson from this list by its database ID
	public boolean removeById(int lessonId) {
		return lessons.removeIf(lesson -> lesson.getLessonId() == lessonId);
	}

	
	/*****
	 * <p> Method: LessonList filterByOwner(String owner) </p>
	 * 
	 * <p> Description: Builds a new list holding only the lessons owned by the specified
	 * contributor.  This list is not changed.  The result may be empty. </p>
	 * 
	 * @param owner specifies the username of the contributor
	 * 
	 * @return a new LessonList containing the matching subset
	 * 
	 */
	// Build the subset of lessons owned by one contributor
	public LessonList filterByOwner(String owner) {
		LessonList subset = new LessonList();
		if (owner == null) return subset;
		for (Lesson lesson : lessons) {
			if (owner.equals(lesson.getOwner())) subset.add(lesson);
		}
		return subset;
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
	 * <p> Method: List&lt;Lesson&gt; getLessons() </p>
	 * 
	 * <p> Description: Returns a read-only view of the lessons, suitable for loading into a
	 * JavaFX ListView or looping over in a test.  Attempting to change the returned list throws
	 * an exception, which protects the contents of this LessonList. </p>
	 * 
	 * @return an unmodifiable view of the lessons in this list
	 * 
	 */
	public List<Lesson> getLessons() { return Collections.unmodifiableList(lessons); }
}