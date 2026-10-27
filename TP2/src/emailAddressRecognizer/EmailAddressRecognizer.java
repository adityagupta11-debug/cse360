package emailAddressRecognizer;

/*******
 * <p> Title: EmailAddressRecognizer Class. </p>
 *
 * <p> Description: A syntactic validator for the email address the Admin types when sending an
 * invitation.  It follows the same approach as the UserNameRecognizer: the input is checked for
 * a reasonable maximum size before anything else is done with it, the characters are then walked
 * left to right, and the first problem found is reported together with the index where it was
 * found so the GUI can point at it. </p>
 *
 * <p> The rules enforced (a practical subset of RFC 5322) are: </p>
 * <ul>
 * <li>The whole address is at most MAX_LENGTH characters and is not empty.</li>
 * <li>There is exactly one "@" with a non-empty local part before it and a domain after it.</li>
 * <li>The local part (at most 64 characters) is made of letters, digits, and the characters
 *     ". _ % + -", does not start or end with a period, and has no two periods in a row.</li>
 * <li>The domain is made of one or more labels separated by periods.  Each label is letters,
 *     digits, or hyphens, and does not start or end with a hyphen.</li>
 * <li>The domain has at least one period, and the last label (the top-level domain) is at least
 *     two letters.</li>
 * </ul>
 *
 * <p> Whether the mailbox actually exists cannot be determined syntactically; that is out of
 * scope for this phase. </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version for the Admin "Send Invitation" user story
 *
 */
public class EmailAddressRecognizer {

	/**
	 * Default constructor is not used; every method of this class is static.
	 */
	public EmailAddressRecognizer() {
	}

	/**********************************************************************************************
	 *
	 * Result attributes used to inform the user about what was and was not valid.  They are set
	 * as a side effect of checkForValidEmailAddress, mirroring the UserNameRecognizer.
	 *
	 */
	/** The message describing the first problem found, or an empty string when there was none */
	public static String emailAddressErrorMessage = "";

	/** The input that was examined by the most recent call */
	public static String emailAddressInput = "";

	/** The index into the input where the problem was found, or -1 when there was none */
	public static int emailAddressIndexofError = -1;

	/** The longest email address that will be accepted */
	public static final int MAX_LENGTH = 254;

	/** The longest mailbox name (the part before the "@") that will be accepted */
	public static final int MAX_LOCAL_PART_LENGTH = 64;

	// The characters allowed in the local part in addition to letters and digits
	private static final String LOCAL_SPECIALS = "._%+-";


	/**********
	 * <p> Method: boolean isLetterOrDigit(char c) </p>
	 *
	 * <p> Description: A private helper limited to the ASCII letters and digits, since the
	 * validator deliberately does not accept non-ASCII characters.</p>
	 *
	 * @param c the character to be examined
	 *
	 * @return true if the character is an ASCII letter or digit
	 */
	private static boolean isLetterOrDigit(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
	}


	/**********
	 * <p> Method: String checkForValidEmailAddress(String input) </p>
	 *
	 * <p> Description: This method checks the email address and returns an empty string when it
	 * is acceptable.  Otherwise it returns a message describing the first problem that was
	 * found, and the public attributes above hold the input and the index of the error.</p>
	 *
	 * @param input the email address to be checked
	 *
	 * @return an empty string if the address is valid, else an error message
	 */
	public static String checkForValidEmailAddress(String input) {
		emailAddressErrorMessage = "";
		emailAddressIndexofError = -1;
		emailAddressInput = (input == null) ? "" : input;

		// Check the size of the input before doing anything else with it
		if (emailAddressInput.length() == 0) {
			return fail(0, "*** Error *** The email address is empty!");
		}
		if (emailAddressInput.length() > MAX_LENGTH) {
			return fail(MAX_LENGTH, "*** Error *** The email address may not exceed " +
					MAX_LENGTH + " characters!");
		}

		// Locate the "@" that splits the local part from the domain.  There must be exactly one.
		int at = emailAddressInput.indexOf('@');
		if (at < 0) {
			return fail(emailAddressInput.length(),
					"*** Error *** The email address must contain an \"@\"!");
		}
		if (emailAddressInput.indexOf('@', at + 1) >= 0) {
			return fail(emailAddressInput.indexOf('@', at + 1),
					"*** Error *** The email address may contain only one \"@\"!");
		}

		String local = emailAddressInput.substring(0, at);
		String domain = emailAddressInput.substring(at + 1);

		// Check the local part (the mailbox name)
		if (local.length() == 0) {
			return fail(0, "*** Error *** There must be a mailbox name before the \"@\"!");
		}
		if (local.length() > MAX_LOCAL_PART_LENGTH) {
			return fail(MAX_LOCAL_PART_LENGTH, "*** Error *** The part before the \"@\" may not "
					+ "exceed " + MAX_LOCAL_PART_LENGTH + " characters!");
		}
		for (int i = 0; i < local.length(); i++) {
			char c = local.charAt(i);
			if (!isLetterOrDigit(c) && LOCAL_SPECIALS.indexOf(c) < 0) {
				return fail(i, "*** Error *** The character \"" + c + "\" is not allowed before "
						+ "the \"@\"!");
			}
			if (c == '.' && (i == 0 || i == local.length() - 1)) {
				return fail(i, "*** Error *** The part before the \"@\" may not start or end "
						+ "with a period!");
			}
			if (c == '.' && i > 0 && local.charAt(i - 1) == '.') {
				return fail(i, "*** Error *** Two periods in a row are not allowed!");
			}
		}

		// Check the domain
		if (domain.length() == 0) {
			return fail(at + 1, "*** Error *** There must be a domain after the \"@\"!");
		}
		if (domain.indexOf('.') < 0) {
			return fail(emailAddressInput.length(),
					"*** Error *** The domain must contain a period (e.g., asu.edu)!");
		}
		int labelStart = 0;						// Index within the domain of the current label
		for (int i = 0; i <= domain.length(); i++) {
			boolean endOfLabel = (i == domain.length()) || domain.charAt(i) == '.';
			if (endOfLabel) {
				if (i == labelStart) {
					return fail(at + 1 + i, "*** Error *** The domain has an empty part; "
							+ "check the periods!");
				}
				if (domain.charAt(labelStart) == '-' || domain.charAt(i - 1) == '-') {
					return fail(at + 1 + labelStart, "*** Error *** A domain part may not start "
							+ "or end with a hyphen!");
				}
				labelStart = i + 1;
			} else {
				char c = domain.charAt(i);
				if (!isLetterOrDigit(c) && c != '-') {
					return fail(at + 1 + i, "*** Error *** The character \"" + c +
							"\" is not allowed in the domain!");
				}
			}
		}

		// The top-level domain (the text after the last period) must be at least two letters
		String tld = domain.substring(domain.lastIndexOf('.') + 1);
		if (tld.length() < 2) {
			return fail(at + 1 + domain.lastIndexOf('.') + 1,
					"*** Error *** The domain must end with at least two letters (e.g., .edu)!");
		}
		for (int i = 0; i < tld.length(); i++) {
			char c = tld.charAt(i);
			if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
				return fail(at + 1 + domain.lastIndexOf('.') + 1 + i,
						"*** Error *** The domain must end with letters only (e.g., .edu)!");
			}
		}

		return "";								// Every check passed
	}


	/**********
	 * <p> Method: String fail(int index, String message) </p>
	 *
	 * <p> Description: A private helper that records where the error was found and the message
	 * describing it, and returns that message so the caller can use it directly.</p>
	 *
	 * @param index the index into the input where the error was found
	 * @param message the message describing the error
	 *
	 * @return the message
	 */
	private static String fail(int index, String message) {
		emailAddressIndexofError = index;
		emailAddressErrorMessage = message;
		return message;
	}
}
