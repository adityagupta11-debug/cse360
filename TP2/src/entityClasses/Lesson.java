package entityClasses;

/*******
 * <p> Title: Lesson Class </p>
 * 
 * <p> Description: This Lesson class represents a lesson learned entity in the system.  It
 *  contains the lesson's core information (title, category, situation, and lesson text), the
 *  unique ID assigned by the database, and the username of the contributor who owns it. </p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * 
 * @version 1.00		2026-10-08 Initial version for HW2, modeled on the User class
 * 
 */

/*
 * Like the User class, this class only carries data between the GUI and the Database.  It does
 * not validate its fields or access the database.  Validation is done by the lesson recognizer
 * before a Lesson is stored, so the rules and error messages live in exactly one place.
 */
public class Lesson {
	
	/** The categories a lesson may be assigned to.  The GUI uses this list to populate its
	 *  category selector and the recognizer uses it to reject any other value, so both always
	 *  agree on the allowed categories. */
	public static final String[] CATEGORIES = {
			"Tools and Setup", "Design", "Coding", "Testing", "Teamwork", "Process"
	};
	
	/*
	 * These are the private attributes for this entity object
	 */
	private int lessonId;			// Assigned by the database; 0 means not yet stored
	private String owner;			// Username of the contributor who added the lesson
	private String title;
	private String category;
	private String situation;		// What happened
	private String lessonText;		// What was learned
	
	
	/*****
	 * <p> Method: Lesson() </p>
	 * 
	 * <p> Description: This default constructor is not used in this system. </p>
	 */
	public Lesson() {
		
	}

	
	/*****
	 * <p> Method: Lesson(String owner, String title, String category, String situation,
	 * 		String lessonText) </p>
	 * 
	 * <p> Description: This constructor is used to establish a new lesson that has not yet been
	 * stored in the database.  The lesson ID is set to 0 because the database assigns the real
	 * ID when the lesson is added. </p>
	 * 
	 * @param owner specifies the username of the contributor adding the lesson
	 * 
	 * @param title specifies the lesson's title
	 * 
	 * @param category specifies the lesson's category (one of CATEGORIES)
	 * 
	 * @param situation specifies what happened that led to the lesson
	 * 
	 * @param lessonText specifies what was learned
	 * 
	 */
	// Constructor for a lesson that has not yet been added to the database
	public Lesson(String owner, String title, String category, String situation,
			String lessonText) {
		this(0, owner, title, category, situation, lessonText);
	}

	
	/*****
	 * <p> Method: Lesson(int lessonId, String owner, String title, String category,
	 * 		String situation, String lessonText) </p>
	 * 
	 * <p> Description: This constructor is used to rebuild a lesson that was read from the
	 * database, so it includes the ID the database assigned. </p>
	 * 
	 * @param lessonId specifies the unique ID assigned by the database
	 * 
	 * @param owner specifies the username of the contributor who added the lesson
	 * 
	 * @param title specifies the lesson's title
	 * 
	 * @param category specifies the lesson's category (one of CATEGORIES)
	 * 
	 * @param situation specifies what happened that led to the lesson
	 * 
	 * @param lessonText specifies what was learned
	 * 
	 */
	// Constructor for a lesson read from the database
	public Lesson(int lessonId, String owner, String title, String category, String situation,
			String lessonText) {
		this.lessonId = lessonId;
		this.owner = owner;
		this.title = title;
		this.category = category;
		this.situation = situation;
		this.lessonText = lessonText;
	}

	
	/*****
	 * <p> Method: int getLessonId() </p>
	 * 
	 * <p> Description: This getter returns the lesson's database ID. </p>
	 * 
	 * @return the lesson ID, or 0 if the lesson has not yet been stored
	 * 
	 */
	public int getLessonId() { return lessonId; }

	
	/*****
	 * <p> Method: void setLessonId(int lessonId) </p>
	 * 
	 * <p> Description: This setter records the ID the database assigned after the lesson is
	 * added. </p>
	 * 
	 * @param lessonId is the new value for the lesson ID
	 * 
	 */
	public void setLessonId(int lessonId) { this.lessonId = lessonId; }

	
	/*****
	 * <p> Method: String getOwner() </p>
	 * 
	 * <p> Description: This getter returns the username of the contributor who owns the
	 * lesson. </p>
	 * 
	 * @return the owner's username
	 * 
	 */
	public String getOwner() { return owner; }

	
	/*****
	 * <p> Method: String getTitle() </p>
	 * 
	 * <p> Description: This getter returns the lesson's title. </p>
	 * 
	 * @return the title
	 * 
	 */
	public String getTitle() { return title; }

	
	/*****
	 * <p> Method: void setTitle(String title) </p>
	 * 
	 * <p> Description: This setter changes the lesson's title. </p>
	 * 
	 * @param title is the new value for the title
	 * 
	 */
	public void setTitle(String title) { this.title = title; }

	
	/*****
	 * <p> Method: String getCategory() </p>
	 * 
	 * <p> Description: This getter returns the lesson's category. </p>
	 * 
	 * @return the category
	 * 
	 */
	public String getCategory() { return category; }

	
	/*****
	 * <p> Method: void setCategory(String category) </p>
	 * 
	 * <p> Description: This setter changes the lesson's category. </p>
	 * 
	 * @param category is the new value for the category (one of CATEGORIES)
	 * 
	 */
	public void setCategory(String category) { this.category = category; }

	
	/*****
	 * <p> Method: String getSituation() </p>
	 * 
	 * <p> Description: This getter returns the description of what happened. </p>
	 * 
	 * @return the situation
	 * 
	 */
	public String getSituation() { return situation; }

	
	/*****
	 * <p> Method: void setSituation(String situation) </p>
	 * 
	 * <p> Description: This setter changes the description of what happened. </p>
	 * 
	 * @param situation is the new value for the situation
	 * 
	 */
	public void setSituation(String situation) { this.situation = situation; }

	
	/*****
	 * <p> Method: String getLessonText() </p>
	 * 
	 * <p> Description: This getter returns the description of what was learned. </p>
	 * 
	 * @return the lesson text
	 * 
	 */
	public String getLessonText() { return lessonText; }

	
	/*****
	 * <p> Method: void setLessonText(String lessonText) </p>
	 * 
	 * <p> Description: This setter changes the description of what was learned. </p>
	 * 
	 * @param lessonText is the new value for the lesson text
	 * 
	 */
	public void setLessonText(String lessonText) { this.lessonText = lessonText; }

	
	/*****
	 * <p> Method: String toString() </p>
	 * 
	 * <p> Description: Returns the title so a JavaFX ListView or ComboBox of lessons displays
	 * readable titles instead of object references.  This follows the same approach as the
	 * Database DeletionCandidate record, which returns the username for display. </p>
	 * 
	 * @return the lesson's title
	 * 
	 */
	@Override
	public String toString() { return title; }
}