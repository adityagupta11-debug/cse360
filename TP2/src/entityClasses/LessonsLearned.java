package entityClasses;

/*******
 * <p> Title: LessonsLearned Class </p>
 * 
 * <p> Description: This LessonsLearned class represents one lesson learned in the system.  It
 *  contains the lesson's core information (title, category, situation, and lesson text), the
 *  unique ID assigned by the database, the username of the contributor who owns it, and the
 *  curator-controlled state that the Contributor user stories require the contributor to see:
 *  whether the whole lesson or individual fields are locked, and whether a curator has edited
 *  the lesson. </p>
 * 
 * <p> Copyright: Joshua Luther © 2026 </p>
 * 
 * @author Joshua Luther
 * @author Vishwam - persistent CRUD facade and timestamps
 * 
 * @version 1.00		2026-10-08 Initial version for HW2 (as Lesson), modeled on the User class
 * @version 1.01		2026-10-22 Renamed LessonsLearned for TP2; added lesson and field locks
 * 							and the curator-edited flag (Joshua Luther)
 * 
 */

/*
 * Like the User class, this class only carries data between the GUI and the Database.  It does
 * not validate its fields or access the database.  Validation is done by the lesson recognizer
 * before a lesson is stored, so the rules and error messages live in exactly one place.
 * 
 * The lock and curator-edited attributes are set by curators (Curator Phase 3 epics), but they
 * are stored now because two Phase Two Contributor stories depend on them:
 *   - "When I examine a lesson more closely, I can see the fields I can alter, and which are
 *     locked and unavailable for me to edit.  (When a lesson I created is locked or has a
 *     locked field, I am unable to delete that lesson.)"
 *   - "I can only see the content I have added or the content that I have added after a
 *     curator has edited it."
 */
public class LessonsLearned {
    /** VG08: database creation time; pre-TP2 records use schema migration time. */
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    /** VG08: latest persisted core/lock/experience change; supports future curator auditing. */
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    /** Persistence mapping only; save operations ignore caller-supplied timestamps. */
    public void setCreatedAt(java.time.LocalDateTime value) { createdAt = value; }
    /** Persistence mapping only; save operations assign their own database timestamp. */
    public void setUpdatedAt(java.time.LocalDateTime value) { updatedAt = value; }
    /** VG01: validate and persist this new lesson for the authenticated owner. */
    public database.Database.LessonResult create(database.Database database) { return database.addLesson(this); }
    /** VG02: read an authorized Contributor projection, without curator-private data. */
    public static LessonsLearned read(database.Database database, int id) { return database.getLesson(id); }
    /** VG03: save core fields; the database rechecks current ownership and all locks. */
    public database.Database.LessonResult update(database.Database database, String actor) { return database.updateLesson(this, actor); }
    /** VG04: remove this lesson and linked experiences only after explicit confirmation. */
    public database.Database.LessonResult delete(database.Database database, String actor, boolean confirmed) { return database.deleteLesson(lessonId, actor, confirmed); }

	
	/** The categories a lesson may be assigned to.  The GUI uses this list to populate its
	 *  category selector and the recognizer uses it to reject any other value, so both always
	 *  agree on the allowed categories. */
	public static final String[] CATEGORIES = {
			"Tools and Setup", "Design", "Coding", "Testing", "Teamwork", "Process"
	};
	
	/** The names of the fields a curator can lock individually.  These are the same names the
	 *  LessonRecognizer reports in lessonRecognizerErrorField, so the GUI can use one name to
	 *  find both a field's validation error and its lock state. */
	public static final String[] LOCKABLE_FIELDS = { "Title", "Category", "Situation", "Lesson" };
	
	/*
	 * Core information (Contributor story 1: "add new lessons learned... specifying core
	 * information")
	 */
	private int lessonId;			// Assigned by the database; 0 means not yet stored
	private String owner;			// Username of the contributor who added the lesson
	private String title;
	private String category;
	private String situation;		// What happened
	private String lessonText;		// What was learned
	
	/*
	 * Curator-controlled state (Contributor story 2 and the Curator locking epic).  All of
	 * these start false, which is the state of every lesson a contributor creates.
	 */
	private boolean lessonLocked;		// The whole lesson is locked: no edits, no delete
	private boolean titleLocked;		// Individual field locks: that field cannot be edited,
	private boolean categoryLocked;		// and the lesson cannot be deleted while any one of
	private boolean situationLocked;	// them is set
	private boolean lessonTextLocked;
	private java.time.LocalDateTime createdAt; // VG08: creation/migration timestamp
	private java.time.LocalDateTime updatedAt; // VG08: most recent persisted change
	private boolean curatorEdited;		// A curator has edited this lesson (Contributor story 4)
	
	
	/*****
	 * <p> Method: LessonsLearned() </p>
	 * 
	 * <p> Description: This default constructor is not used in this system. </p>
	 */
	public LessonsLearned() {
		
	}

	
	/*****
	 * <p> Method: LessonsLearned(String owner, String title, String category, String situation,
	 * 		String lessonText) </p>
	 * 
	 * <p> Description: This constructor is used to establish a new lesson that has not yet been
	 * stored in the database.  The lesson ID is set to 0 because the database assigns the real
	 * ID when the lesson is added.  A new lesson has no locks and has not been edited by a
	 * curator. </p>
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
	public LessonsLearned(String owner, String title, String category, String situation,
			String lessonText) {
		this(0, owner, title, category, situation, lessonText);
	}

	
	/*****
	 * <p> Method: LessonsLearned(int lessonId, String owner, String title, String category,
	 * 		String situation, String lessonText) </p>
	 * 
	 * <p> Description: This constructor is used to rebuild a lesson that was read from the
	 * database, so it includes the ID the database assigned.  The lock and curator-edited flags
	 * are set afterward with their setters, which keeps this constructor the same shape as the
	 * one used for new lessons. </p>
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
	public LessonsLearned(int lessonId, String owner, String title, String category,
			String situation, String lessonText) {
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
	 * lesson.  There is no setter because a lesson's owner never changes. </p>
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
	 * <p> Method: boolean isLessonLocked() </p>
	 * 
	 * <p> Description: This getter reports whether a curator has locked the entire lesson, in
	 * which case the contributor can neither edit nor delete it. </p>
	 * 
	 * @return true if the whole lesson is locked
	 * 
	 */
	public boolean isLessonLocked() { return lessonLocked; }

	
	/*****
	 * <p> Method: void setLessonLocked(boolean lessonLocked) </p>
	 * 
	 * <p> Description: This setter records whether the entire lesson is locked.  It is used by
	 * the Database when a lesson is read; only a curator can change the stored value. </p>
	 * 
	 * @param lessonLocked is true to lock the whole lesson
	 * 
	 */
	public void setLessonLocked(boolean lessonLocked) { this.lessonLocked = lessonLocked; }

	
	/*****
	 * <p> Method: boolean isTitleLocked() </p>
	 * 
	 * <p> Description: This getter reports whether the title field is locked. </p>
	 * 
	 * @return true if the title cannot be edited by the contributor
	 * 
	 */
	public boolean isTitleLocked() { return titleLocked; }

	
	/*****
	 * <p> Method: void setTitleLocked(boolean titleLocked) </p>
	 * 
	 * <p> Description: This setter records whether the title field is locked. </p>
	 * 
	 * @param titleLocked is true to lock the title
	 * 
	 */
	public void setTitleLocked(boolean titleLocked) { this.titleLocked = titleLocked; }

	
	/*****
	 * <p> Method: boolean isCategoryLocked() </p>
	 * 
	 * <p> Description: This getter reports whether the category field is locked. </p>
	 * 
	 * @return true if the category cannot be edited by the contributor
	 * 
	 */
	public boolean isCategoryLocked() { return categoryLocked; }

	
	/*****
	 * <p> Method: void setCategoryLocked(boolean categoryLocked) </p>
	 * 
	 * <p> Description: This setter records whether the category field is locked. </p>
	 * 
	 * @param categoryLocked is true to lock the category
	 * 
	 */
	public void setCategoryLocked(boolean categoryLocked) { this.categoryLocked = categoryLocked; }

	
	/*****
	 * <p> Method: boolean isSituationLocked() </p>
	 * 
	 * <p> Description: This getter reports whether the situation field is locked. </p>
	 * 
	 * @return true if the situation cannot be edited by the contributor
	 * 
	 */
	public boolean isSituationLocked() { return situationLocked; }

	
	/*****
	 * <p> Method: void setSituationLocked(boolean situationLocked) </p>
	 * 
	 * <p> Description: This setter records whether the situation field is locked. </p>
	 * 
	 * @param situationLocked is true to lock the situation
	 * 
	 */
	public void setSituationLocked(boolean situationLocked) {
		this.situationLocked = situationLocked;
	}

	
	/*****
	 * <p> Method: boolean isLessonTextLocked() </p>
	 * 
	 * <p> Description: This getter reports whether the lesson text field is locked. </p>
	 * 
	 * @return true if the lesson text cannot be edited by the contributor
	 * 
	 */
	public boolean isLessonTextLocked() { return lessonTextLocked; }

	
	/*****
	 * <p> Method: void setLessonTextLocked(boolean lessonTextLocked) </p>
	 * 
	 * <p> Description: This setter records whether the lesson text field is locked. </p>
	 * 
	 * @param lessonTextLocked is true to lock the lesson text
	 * 
	 */
	public void setLessonTextLocked(boolean lessonTextLocked) {
		this.lessonTextLocked = lessonTextLocked;
	}

	
	/*****
	 * <p> Method: boolean isFieldLocked(String fieldName) </p>
	 * 
	 * <p> Description: Reports whether the named field cannot be edited by the contributor.  A
	 * field is uneditable when it is locked itself or when the whole lesson is locked, so the
	 * GUI can ask one question per field instead of checking both flags. </p>
	 * 
	 * @param fieldName is one of LOCKABLE_FIELDS ("Title", "Category", "Situation", "Lesson")
	 * 
	 * @return true if the field cannot be edited; false for an unknown field name
	 * 
	 */
	// Contributor story 2: "I can see the fields I can alter, and which are locked"
	public boolean isFieldLocked(String fieldName) {
		if (lessonLocked) return true;
		if (fieldName == null) return false;
		switch (fieldName) {
		case "Title":		return titleLocked;
		case "Category":	return categoryLocked;
		case "Situation":	return situationLocked;
		case "Lesson":		return lessonTextLocked;
		default:			return false;
		}
	}

	
	/*****
	 * <p> Method: boolean hasAnyLock() </p>
	 * 
	 * <p> Description: Reports whether the lesson is locked or has at least one locked field.
	 * </p>
	 * 
	 * @return true if any lock is set
	 * 
	 */
	// Contributor story 2: "When a lesson I created is locked or has a locked field, I am unable
	// to delete that lesson."  The Database and the GUI both use this one test.
	public boolean hasAnyLock() {
		return lessonLocked || titleLocked || categoryLocked || situationLocked
				|| lessonTextLocked;
	}

	
	/*****
	 * <p> Method: boolean isCuratorEdited() </p>
	 * 
	 * <p> Description: Reports whether a curator has edited this lesson.  The lesson still
	 * belongs to its contributor, who sees the edited version and a tag saying it was edited.
	 * </p>
	 * 
	 * @return true if a curator has edited the lesson
	 * 
	 */
	public boolean isCuratorEdited() { return curatorEdited; }

	
	/*****
	 * <p> Method: void setCuratorEdited(boolean curatorEdited) </p>
	 * 
	 * <p> Description: This setter records whether a curator has edited the lesson.  It is
	 * used by the Database when a lesson is read. </p>
	 * 
	 * @param curatorEdited is true if a curator has edited the lesson
	 * 
	 */
	public void setCuratorEdited(boolean curatorEdited) { this.curatorEdited = curatorEdited; }

	
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