package passwordPopUpWindow;

import javafx.scene.paint.Color;

/*******
 * <p> Title: Model Class - establishes the required GUI data and the computations.
 * </p>
 *
 * <p> Description: This Model class is a major component of a Model View Controller (MVC)
 * application design that provides the user with a Graphical User Interface using JavaFX
 * widgets as opposed to a command line interface.
 * 
 * In this case the Model deals with an input from the user and checks to see if it conforms to
 * the requirements specified by a graphical representation of a finite state machine.
 * 
 * This is a purely static component of the MVC implementation.  There is no need to instantiate
 * the class.
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 *
 * @version 2.00	2025-07-30 Rewrite of this application for the Fall 2025 offering of CSE 360
 * and other ASU courses.
 * @version 2.01	2026-09-21 Reset all dynamic state safely and never echo passwords (Vishwam)
 */

public class Model {
		
	/*******
	 * <p> Title: updatePassword - Protected Method </p>
	 * 
	 * <p> Description: This method is called every time the user changes the password (e.g., with 
	 * every key pressed) using the GUI from the PasswordEvaluationGUITestbed.  It resets the 
	 * messages associated with each of the requirements and then evaluates the current password
	 * with respect to those requirements.  The results of that evaluation are displayed through the
	 * View; console messages report status without echoing the password.</p>
	 */

	protected static void updatePassword() {
		// Vishwam, every edit starts from a disabled, clean state so stale validity cannot be saved.
		View.resetAssessments();
		View.button_Finish.setDisable(true);
		View.validPassword.setText("");
		View.errPasswordPart1.setText("");
		View.errPasswordPart2.setText("");
		View.errPasswordPart3.setText("");
		String password = View.text_Password.getText();
		
		// If the input is empty, clear the aspects of the user interface having to do with the
		// user input and tell the user that the input is empty.
		if (password.isEmpty()) {
			View.noInputFound.setText("No input text found!");
		}
		else
		{
			View.noInputFound.setText("");
			// There is user input, so evaluate it to see if it satisfies the requirements
			String errMessage = evaluatePassword(password);
			
			// Based on the evaluation, change the flag to green for each satisfied requirement
			updateFlags();
			
			// An empty string means there is no error message, which means the input is valid
			if (!errMessage.isEmpty()) {
				
				// Since the output is not empty, at least one requirement have not been satisfied.
				System.out.println(errMessage);			// Display the message to the console
				
				// Vishwam, preserve the error position without displaying the password itself.
				int markerPosition = Math.max(0,
						Math.min(passwordIndexofError, password.length()));
				View.errPasswordPart1.setText("\u2022".repeat(markerPosition));
				
				// Place the red up arrow into Part 2
				View.errPasswordPart2.setText("\u21EB");
				
				// Tell the user about the meaning of the red up arrow
				View.errPasswordPart3.setText(
						"The red arrow points at the character causing the error!");
				
				// Tell the user that the password is not valid with a red message
				View.validPassword.setTextFill(Color.RED);
				View.validPassword.setText("Failure! The password is not valid.");
				
				// Ensure the button is disabled
				View.button_Finish.setDisable(true);
			}
			else {
				// All the requirements were satisfied - the password is valid
				System.out.println("Success! The password satisfies the requirements.");
				
				// Hide all of the error messages elements
				View.errPasswordPart1.setText("");
				View.errPasswordPart2.setText("");
				View.errPasswordPart3.setText("");
				
				// Tell the user that the password is valid with a green message
				View.validPassword.setTextFill(Color.GREEN);
				View.validPassword.setText("Success! The password satisfies the requirements.");
				
				// Enable the button so the user can accept this password or continue to add
				// more characters to the password and make it longer.
				View.button_Finish.setDisable(false);
			} 
		}
	}
	
	/*-********************************************************************************************
	 * 
	 * Attributes used by the Finite State Machine to inform the user about what was and was not
	 * valid and point to the character of the error.  This will enhance the user experience.
	 * 
	 */

	/**
	 * The largest password the evaluator will examine.  Any longer input is rejected at once,
	 * before the directed graph is run, so a very long string pasted into the field (a standard
	 * hacking tactic) cannot slow the application down or make it fail.
	 */
	public static final int MAX_PASSWORD_LENGTH = 20;

	/**********
	 * Check the separate confirmation field's size before comparing it with the chosen password.
	 * The confirmation does not need another composition pass, but it is still a free-text input and
	 * must have an explicit bound of its own.
	 *
	 * @param input the confirmation text
	 * @return an empty string when the size is acceptable, otherwise a useful validation message
	 */
	// Vishwam, explicitly bound both confirmation-password fields instead of rejecting long pastes
	// only incidentally through a mismatch.
	public static String checkPasswordConfirmation(String input) {
		if (input == null) return "Enter the password again to confirm it.";
		if (input.length() > MAX_PASSWORD_LENGTH)
			return "The confirmation may not exceed " + MAX_PASSWORD_LENGTH + " characters.";
		return "";
	}
	
	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	public static boolean foundUpperCase = false;
	public static boolean foundLowerCase = false;
	public static boolean foundNumericDigit = false;
	public static boolean foundSpecialChar = false;
	public static boolean foundLongEnough = false;
	//added boolean foundShortEnough: Joshua Luther
	private static boolean foundShortEnough = false;
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running

	/*
	 * This optional diagnostic reports only the current position and total length. It deliberately
	 * omits the input and current character so a password cannot leak through console output.
	 */

	private static void displayInputState() {
		// Vishwam, retain safe diagnostic structure without disclosing the password or character.
		System.out.println("Password evaluation position " + currentCharNdx + " of "
				+ inputLine.length());
	}
	
	
	/*
	 * This private method checks each of the requirements and if one is satisfied, it changes the
	 * the text to tell the user of this fact and changes the text color from red to green.
	 * 
	 */
	
	private static void updateFlags() {
		if (foundUpperCase) {
			View.label_UpperCase.setText("At least one upper case letter - Satisfied");
			View.label_UpperCase.setTextFill(Color.GREEN);
		}

		if (foundLowerCase) {
			View.label_LowerCase.setText("At least one lower case letter - Satisfied");
			View.label_LowerCase.setTextFill(Color.GREEN);
		}

		if (foundNumericDigit) {
			View.label_NumericDigit.setText("At least one numeric digit - Satisfied");
			View.label_NumericDigit.setTextFill(Color.GREEN);
		}

		if (foundSpecialChar) {
			View.label_SpecialChar.setText("At least one special character - Satisfied");
			View.label_SpecialChar.setTextFill(Color.GREEN);
		}

		if (foundLongEnough) {
			View.label_LongEnough.setText("At least eight characters - Satisfied");
			View.label_LongEnough.setTextFill(Color.GREEN);
		}
		//added foundShortEnough check for password
		if (foundShortEnough) {
			View.label_ShortEnough.setText("At most twenty characters - Satisfied");
			View.label_ShortEnough.setTextFill(Color.GREEN);
		} else {
			View.label_ShortEnough.setText("At most twenty characters - Not Yet Satisfied");
			View.label_ShortEnough.setTextFill(Color.RED);
		}
	}
	

	/**********
	 * <p> Title: evaluatePassword - Public Method </p>
	 * 
	 * <p> Description: This method is a mechanical transformation of a Directed Graph diagram 
	 * into a Java method. This method is used by both the GUI version of the application as well
	 * as the testing automation version.
	 * 
	 * @param input		The input string evaluated by the directed graph processing
	 * @return			An output string that is empty if every things is okay or it will be
	 * 						a string with a helpful description of the error follow by two lines
	 * 						that shows the input line follow by a line with an up arrow at the
	 *						point where the error was found.
	 */
	
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordErrorMessage = "";
		passwordIndexofError = 0;			// Initialize the IndexofError
		inputLine = input == null ? "" : input;	// Keep the evaluator safe for direct callers
		currentCharNdx = 0;					// The index of the current character

		// Vishwam, reset every requirement before any early return (empty, null, or excessive input).
		passwordInput = "";
		foundUpperCase = false;
		foundLowerCase = false;
		foundNumericDigit = false;
		foundSpecialChar = false;
		foundLongEnough = false;
		foundShortEnough = false;
		
		if (input == null) {
			return "*** Error *** The password is missing!";
		}
		if (input.isEmpty()) {
			return "*** Error *** The password is empty!";
		}
		
		// Check the size of the input before doing anything else with it.  Walking a very long
		// string one character at a time would waste time on input that can never be valid.
		if(input.length() > MAX_PASSWORD_LENGTH) {
			passwordIndexofError = MAX_PASSWORD_LENGTH;
			return "*** Error *** The password may not exceed " + MAX_PASSWORD_LENGTH + 
					" characters!";
		}
		
		// The input is not empty, so we can access the first character
		currentChar = input.charAt(0);		// The current character from the above indexed position

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state.  This
		// local variable is a working copy of the input.
		passwordInput = input;				// Save a copy of the input
		
		// This flag determines whether the directed graph (FSM) loop is operating or not
		running = true;						// Start the loop

		// The Directed Graph simulation continues until the end of the input is reached or at some
		// state the current character does not match any valid transition
		while (running) {
			// Vishwam, production validation never writes plaintext password contents to the console.
			// The cascading if statement sequentially tries the current character against all of
			// the valid transitions, each associated with one of the requirements
			if (currentChar >= 'A' && currentChar <= 'Z') {
				System.out.println("Upper case letter found");
				foundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				System.out.println("Lower case letter found");
				foundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				System.out.println("Digit found");
				foundNumericDigit = true;
			} else if ("~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/".indexOf(currentChar) >= 0) {
				System.out.println("Special character found");
				foundSpecialChar = true;
			} else {
				passwordIndexofError = currentCharNdx;
				return "*** Error *** An invalid character has been found!";
			}
			if (currentCharNdx >= 7) {
				System.out.println("At least 8 characters found");
				foundLongEnough = true;
			}
			if (currentCharNdx <= 20) {
				System.out.println("At most 20 characters found");
				foundShortEnough = true;
			} else {
				System.out.println("At most 20 characters NOT found");
				foundShortEnough = false;
			}
			
			// Go to the next character if there is one
			currentCharNdx++;
			if (currentCharNdx >= inputLine.length())
				running = false;
			else
				currentChar = input.charAt(currentCharNdx);
			
			System.out.println();
		}
		
		// Construct a String with a list of the requirement elements that were found.
		String errMessage = "";
		if (!foundUpperCase)
			errMessage += "Upper case; ";
		
		if (!foundLowerCase)
			errMessage += "Lower case; ";
		
		if (!foundNumericDigit)
			errMessage += "Numeric digits; ";
			
		if (!foundSpecialChar)
			errMessage += "Special character; ";
			
		if (!foundLongEnough)
			errMessage += "Long Enough; ";
		
		if (!foundShortEnough) {
			errMessage += "Short enough; ";
		}
		
		if (errMessage.isEmpty())
			return "";
		
		// If it gets here, there something was not found, so return an appropriate message
		passwordIndexofError = currentCharNdx;
		return errMessage + "conditions were not satisfied";
	}

	// Vishwam, expose the final length flag for focused non-GUI regression tests.
public static boolean isShortEnough() {
		return foundShortEnough;
	}
}
