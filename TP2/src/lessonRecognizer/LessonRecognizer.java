package lessonRecognizer;

import entityClasses.LessonsLearned;

public class LessonRecognizer {
	/**
	 * <p> Title: LessonRecognizer. </p>
	 * 
	 * <p> Description: Validates the core information of a lesson learned (title, category,
	 * situation, and lesson text) before it is added to or updated in the database.  Each check
	 * returns an empty string when the input is valid, or a helpful, field-specific error message
	 * when it is not, following the same convention as the UserNameRecognizer. </p>
	 * 
	 * <p> Copyright: Joshua Luther © 2026 </p>
	 * 
	 * @author Joshua Luther
	 * 
	 * @version 1.00		2026-10-08	Initial version for HW2, modeled on the UserNameRecognizer
	 * 
	 */

	/*
	 * Why this is not a Finite State Machine: the UserNameRecognizer is an FSM because a UserName
	 * has a character-by-character grammar (it must start with a letter, a separator must be
	 * followed by a letter or digit, and so on).  The lesson fields have no such grammar.  Their
	 * rules are only "required", "minimum length", "maximum length", and "one of a fixed list",
	 * so simple checks are clearer and easier to test than an FSM would be.  The public result
	 * attributes, the empty-string-means-valid return convention, and the index of the error are
	 * kept the same as the UserNameRecognizer so the GUI can use both recognizers the same way.
	 */

	/**********************************************************************************************
	 * 
	 * The length limits for each field.  Keeping them as named constants means the rules, the
	 * error messages, and the automated tests all use the same values.
	 * 
	 */
	
	/** The minimum number of characters in a title */
	public static final int TITLE_MIN = 5;
	/** The maximum number of characters in a title */
	public static final int TITLE_MAX = 100;
	/** The minimum number of characters in the situation and the lesson text */
	public static final int TEXT_MIN = 20;
	/** The maximum number of characters in the situation and the lesson text (this must not
	 *  exceed the VARCHAR size of the matching database columns) */
	public static final int TEXT_MAX = 2000;

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message, the name
	 * of the field in error, and a pointer to the location of the error will enhance the user
	 * experience.
	 * 
	 */
	
	public static String lessonRecognizerErrorMessage = "";	// The error message text
	public static String lessonRecognizerErrorField = "";	// The field in error (e.g., "Title")
	public static String lessonRecognizerInput = "";			// The input being processed
	public static int lessonRecognizerIndexofError = -1;		// The index of error location
	
	
	/**********
	 * <p> Method: String checkForValidTitle(String input) </p>
	 * 
	 * <p> Description: Checks that a title is present and is between TITLE_MIN and TITLE_MAX
	 * characters long.  Leading and trailing spaces are ignored, so a title of only spaces is
	 * treated as missing. </p>
	 * 
	 * @param input		The title to be checked
	 * @return			An empty string if the title is valid, else a helpful error message
	 */
	public static String checkForValidTitle(String input) {
		// A null or blank title is missing, which is a different problem from being too short,
		// so it gets its own message
		if (input == null || input.isBlank())
			return setError("Title", input, 0, "Title is required.");
		
		String title = input.trim();
		if (title.length() < TITLE_MIN)
			return setError("Title", input, title.length(),
					"Title must be at least " + TITLE_MIN + " characters.");
		if (title.length() > TITLE_MAX)
			return setError("Title", input, TITLE_MAX,
					"Title must be " + TITLE_MAX + " characters or fewer.");
		
		return setValid(input);
	}
	
	
	/**********
	 * <p> Method: String checkForValidCategory(String input) </p>
	 * 
	 * <p> Description: Checks that a category has been selected and that it is one of the values
	 * in Lesson.CATEGORIES.  The comparison is exact, so the GUI must pass the value exactly as it
	 * appears in that list. </p>
	 * 
	 * @param input		The category to be checked
	 * @return			An empty string if the category is valid, else a helpful error message
	 */
	public static String checkForValidCategory(String input) {
		// The category list lives in the Lesson class so the GUI and this recognizer can never
		// disagree about which categories are allowed
		if (input != null) {
			for (String category : LessonsLearned.CATEGORIES) {
				if (category.equals(input)) return setValid(input);
			}
		}
		// A missing selection and an unknown value get the same message, because in both cases
		// the fix is the same: choose a category from the list
		return setError("Category", input, 0, "Please select a category for this lesson.");
	}
	
	
	/**********
	 * <p> Method: String checkForValidSituation(String input) </p>
	 * 
	 * <p> Description: Checks that the description of what happened is between TEXT_MIN and
	 * TEXT_MAX characters long, ignoring leading and trailing spaces. </p>
	 * 
	 * @param input		The situation to be checked
	 * @return			An empty string if the situation is valid, else a helpful error message
	 */
	public static String checkForValidSituation(String input) {
		return checkText("Situation", "situation", input);
	}
	
	
	/**********
	 * <p> Method: String checkForValidLessonText(String input) </p>
	 * 
	 * <p> Description: Checks that the description of what was learned is between TEXT_MIN and
	 * TEXT_MAX characters long, ignoring leading and trailing spaces. </p>
	 * 
	 * @param input		The lesson text to be checked
	 * @return			An empty string if the lesson text is valid, else a helpful error message
	 */
	public static String checkForValidLessonText(String input) {
		return checkText("Lesson", "lesson", input);
	}
	
	
	/**********
	 * <p> Method: String checkForValidLesson(Lesson lesson) </p>
	 * 
	 * <p> Description: Checks every core field of a lesson in the order the fields appear on the
	 * screen (title, category, situation, lesson text) and stops at the first error.  The Database
	 * class calls this before any create or update, so invalid data can never be stored even if a
	 * caller forgets to validate first. </p>
	 * 
	 * @param lesson	The lesson to be checked
	 * @return			An empty string if every field is valid, else the first error message
	 */
	public static String checkForValidLesson(LessonsLearned lesson) {
		if (lesson == null)
			return setError("Lesson", null, 0, "No lesson was provided.");
		
		String result = checkForValidTitle(lesson.getTitle());
		if (!result.isEmpty()) return result;
		
		result = checkForValidCategory(lesson.getCategory());
		if (!result.isEmpty()) return result;
		
		result = checkForValidSituation(lesson.getSituation());
		if (!result.isEmpty()) return result;
		
		return checkForValidLessonText(lesson.getLessonText());
	}
	
	
	/*
	 * The situation and lesson text share the same length rules, so one private method performs
	 * both checks.  The field name is passed in twice: capitalized for the start of the "too long"
	 * message and in lower case for the middle of the "too short" message.
	 */
	private static String checkText(String fieldName, String fieldWord, String input) {
		// An empty field gets the "too short" message rather than a separate "required" message,
		// because telling the user how much to write is more helpful than just saying "required"
		String text = (input == null) ? "" : input.trim();
		if (text.length() < TEXT_MIN)
			return setError(fieldName, input, text.length(),
					"Describe the " + fieldWord + " in at least " + TEXT_MIN + " characters.");
		if (text.length() > TEXT_MAX)
			return setError(fieldName, input, TEXT_MAX,
					fieldName + " must be " + String.format("%,d", TEXT_MAX) +
					" characters or fewer.");
		return setValid(input);
	}
	
	
	// Private method to record an error in the result attributes and return the message
	private static String setError(String field, String input, int index, String message) {
		lessonRecognizerErrorField = field;
		lessonRecognizerInput = (input == null) ? "" : input;
		lessonRecognizerIndexofError = index;
		lessonRecognizerErrorMessage = message;
		return lessonRecognizerErrorMessage;
	}
	
	
	// Private method to record a valid result in the result attributes and return ""
	private static String setValid(String input) {
		lessonRecognizerErrorField = "";
		lessonRecognizerInput = (input == null) ? "" : input;
		lessonRecognizerIndexofError = -1;
		lessonRecognizerErrorMessage = "";
		return lessonRecognizerErrorMessage;
	}
}