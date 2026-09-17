package entityClasses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*******
 * <p> Title: Invitation Class </p>
 *
 * <p> Description: This Invitation class represents one outstanding invitation in the system.  An
 * invitation is created by an Admin for a specific email address and role and carries a deadline
 * after which the invitation code can no longer be used.  This entity is read-only: it is a
 * snapshot of a row in the InvitationCodes table that the Manage Invitations page displays. </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author A.G. (agupt545)
 *
 * @version 1.00		2026-09-17 Initial version for the Admin "Manage Invitations" user story
 *
 */

public class Invitation {

	// The format used whenever a deadline is shown to the Admin
	private static final DateTimeFormatter DEADLINE_FORMAT =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	/*
	 * These are the private attributes for this entity object
	 */
	private String code;					// The six character invitation code
	private String emailAddress;			// The email address the invitation was sent to
	private String role;					// The role the new user will play
	private LocalDateTime deadline;			// When the invitation stops being usable


	/*****
	 * <p> Method: Invitation(String code, String emailAddress, String role,
	 * 		LocalDateTime deadline) </p>
	 *
	 * <p> Description: Construct a snapshot of one invitation. </p>
	 *
	 * @param code			the six character invitation code
	 * @param emailAddress	the email address the invitation was sent to
	 * @param role			the role the invitation grants
	 * @param deadline		the date and time after which the code cannot be used (may be null)
	 */
	public Invitation(String code, String emailAddress, String role, LocalDateTime deadline) {
		this.code = code;
		this.emailAddress = emailAddress;
		this.role = role;
		this.deadline = deadline;
	}


	/*****
	 * <p> Method: String getCode() </p>
	 * @return the six character invitation code
	 */
	public String getCode() { return code; }

	/*****
	 * <p> Method: String getEmailAddress() </p>
	 * @return the email address the invitation was sent to
	 */
	public String getEmailAddress() { return emailAddress; }

	/*****
	 * <p> Method: String getRole() </p>
	 * @return the role the invitation grants
	 */
	public String getRole() { return role; }

	/*****
	 * <p> Method: LocalDateTime getDeadline() </p>
	 * @return the deadline for the invitation (may be null for a legacy row)
	 */
	public LocalDateTime getDeadline() { return deadline; }


	/*****
	 * <p> Method: String getDeadlineText() </p>
	 *
	 * <p> Description: The deadline formatted for display, or "none" when no deadline is stored.
	 * </p>
	 *
	 * @return the formatted deadline
	 */
	public String getDeadlineText() {
		if (deadline == null) return "none";
		return deadline.format(DEADLINE_FORMAT);
	}


	/*****
	 * <p> Method: boolean isExpired() </p>
	 *
	 * <p> Description: An invitation with no deadline, or whose deadline has passed, is expired.
	 * </p>
	 *
	 * @return true when the invitation can no longer be used
	 */
	public boolean isExpired() {
		if (deadline == null) return true;
		return !deadline.isAfter(LocalDateTime.now());
	}


	/*****
	 * <p> Method: String getStatus() </p>
	 *
	 * @return "Expired" or "Active" for display on the Manage Invitations page
	 */
	public String getStatus() { return isExpired() ? "Expired" : "Active"; }
}
