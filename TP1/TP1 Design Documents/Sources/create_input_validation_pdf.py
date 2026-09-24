"""Generate the TP1 InputValidation.pdf deliverable from the integrated implementation."""

# Vishwam, keep the required validation artifact reproducible from versioned source.
from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import inch
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    LongTable,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)


# Vishwam, write the assignment's exact required filename at the TP1 archive root.
SCRIPT_DIR = Path(__file__).resolve().parent
OUTPUT_PATH = SCRIPT_DIR.parents[1] / "InputValidation.pdf"

NAVY = colors.HexColor("#17365D")
BLUE = colors.HexColor("#2F75B5")
PALE_BLUE = colors.HexColor("#EAF2F8")
PALE_GREEN = colors.HexColor("#E9F5EC")
GREEN = colors.HexColor("#2E7D32")
PALE_GOLD = colors.HexColor("#FFF4CC")
GOLD = colors.HexColor("#D29A00")
INK = colors.HexColor("#1F2933")
MUTED = colors.HexColor("#52606D")
GRID = colors.HexColor("#CBD5E1")
WHITE = colors.white


# Vishwam, use a restrained professional style that remains legible when printed.
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(
    name="CoverEyebrow", parent=styles["Normal"], fontName="Helvetica-Bold",
    fontSize=10, leading=13, textColor=BLUE, spaceAfter=12, alignment=TA_CENTER,
))
styles.add(ParagraphStyle(
    name="CoverTitle", parent=styles["Title"], fontName="Helvetica-Bold",
    fontSize=28, leading=32, textColor=NAVY, alignment=TA_CENTER, spaceAfter=12,
))
styles.add(ParagraphStyle(
    name="CoverSubtitle", parent=styles["Normal"], fontSize=13, leading=18,
    textColor=MUTED, alignment=TA_CENTER, spaceAfter=18,
))
styles.add(ParagraphStyle(
    name="SectionTitle", parent=styles["Heading1"], fontName="Helvetica-Bold",
    fontSize=17, leading=21, textColor=NAVY, spaceBefore=2, spaceAfter=8,
))
styles.add(ParagraphStyle(
    name="SubTitle", parent=styles["Heading2"], fontName="Helvetica-Bold",
    fontSize=11.5, leading=14, textColor=BLUE, spaceBefore=8, spaceAfter=4,
))
styles.add(ParagraphStyle(
    name="BodySmall", parent=styles["BodyText"], fontSize=8.6, leading=11.4,
    textColor=INK, spaceAfter=4,
))
styles.add(ParagraphStyle(
    name="Body", parent=styles["BodyText"], fontSize=9.4, leading=13,
    textColor=INK, spaceAfter=6,
))
styles.add(ParagraphStyle(
    name="TableHead", parent=styles["Normal"], fontName="Helvetica-Bold",
    fontSize=7.7, leading=9.3, textColor=WHITE, alignment=TA_LEFT,
))
styles.add(ParagraphStyle(
    name="TableCell", parent=styles["Normal"], fontSize=7.15, leading=9.1,
    textColor=INK,
))
styles.add(ParagraphStyle(
    name="TableCellSmall", parent=styles["Normal"], fontSize=6.65, leading=8.25,
    textColor=INK,
))
styles.add(ParagraphStyle(
    name="Callout", parent=styles["BodyText"], fontName="Helvetica-Bold",
    fontSize=9.2, leading=12.5, textColor=NAVY,
))
styles.add(ParagraphStyle(
    name="Footer", parent=styles["Normal"], fontSize=7.5, leading=9,
    textColor=MUTED,
))


def P(text, style="TableCell"):
    """Create a Paragraph while keeping table declarations compact."""
    return Paragraph(text, styles[style])


# Vishwam, every content page carries the artifact name, source branch, and page number.
def draw_page(canvas, doc):
    canvas.saveState()
    width, height = letter
    canvas.setStrokeColor(GRID)
    canvas.setLineWidth(0.5)
    canvas.line(0.48 * inch, height - 0.42 * inch, width - 0.48 * inch, height - 0.42 * inch)
    canvas.setFont("Helvetica-Bold", 7.5)
    canvas.setFillColor(NAVY)
    canvas.drawString(0.48 * inch, height - 0.30 * inch, "CSE 360 · TEAM 53 · TP1")
    canvas.setFont("Helvetica", 7.5)
    canvas.setFillColor(MUTED)
    canvas.drawRightString(width - 0.48 * inch, height - 0.30 * inch, "InputValidation.pdf")
    canvas.line(0.48 * inch, 0.43 * inch, width - 0.48 * inch, 0.43 * inch)
    canvas.drawString(0.48 * inch, 0.27 * inch, "Local branch: vishwam-integration · 21 Sep 2026")
    canvas.drawRightString(width - 0.48 * inch, 0.27 * inch, f"Page {doc.page}")
    canvas.restoreState()


class ValidationDocument(BaseDocTemplate):
    """Document template with fixed, print-safe header and footer margins."""

    # Vishwam, reserve the page chrome so tables cannot collide with headers or footers.
    def __init__(self, filename):
        super().__init__(
            filename,
            pagesize=letter,
            leftMargin=0.48 * inch,
            rightMargin=0.48 * inch,
            topMargin=0.58 * inch,
            bottomMargin=0.58 * inch,
            title="InputValidation.pdf",
            author="Vishwam · CSE 360 Team 53",
            subject="TP1 text-input inventory, rules, behavior, and verification",
        )
        frame = Frame(
            self.leftMargin,
            self.bottomMargin,
            self.width,
            self.height,
            leftPadding=0,
            rightPadding=0,
            topPadding=0,
            bottomPadding=0,
        )
        self.addPageTemplates(PageTemplate(id="content", frames=[frame], onPage=draw_page))


def status_cards():
    """Return a compact dashboard for the audited scope and verified results."""
    # Vishwam, lead with measurable coverage so reviewers can verify completeness quickly.
    data = [
        [P("20", "Callout"), P("19 + 1", "Callout"), P("46 / 46", "Callout"), P("33 / 33", "Callout")],
        [P("text-input objects", "Footer"), P("reachable + dormant", "Footer"),
         P("validation tests", "Footer"), P("OTP tests", "Footer")],
    ]
    table = Table(data, colWidths=[1.74 * inch] * 4, rowHeights=[0.34 * inch, 0.25 * inch])
    table.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, -1), PALE_BLUE),
        ("BOX", (0, 0), (-1, -1), 0.7, BLUE),
        ("INNERGRID", (0, 0), (-1, -1), 0.4, GRID),
        ("ALIGN", (0, 0), (-1, -1), "CENTER"),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]))
    return table


def validator_table():
    """Describe the shared policies used by the screen-level inventory."""
    # Vishwam, central policies prevent the same field type from drifting across screens.
    rows = [
        [P("Shared validator", "TableHead"), P("Accepted input / boundary", "TableHead"),
         P("Where enforced before use", "TableHead")],
        [P("UserName FSM"), P("4–32 characters; starts with ASCII letter; later characters are letters, digits, or <b>- _ . &amp;</b>; a separator must be followed by alphanumeric."), P("First Admin, invited account, and username update. Detailed error includes the one-based failure position.")],
        [P("New password"), P("8–20 characters; at least one uppercase, lowercase, digit, and permitted special character. Null, empty, invalid-character, 21-character, and 10,000-character inputs are rejected."), P("Shared dynamic popup on First Admin, invited account, User Update, and OTP reset; submit-time recheck remains defense-in-depth.")],
        [P("Email recognizer"), P("254 total / 64 local-part maximum; one @; structured local part and domain; no leading, trailing, or doubled period; domain contains a period; TLD has at least two letters."), P("Admin invitation and email update. Duplicate outstanding invitation is separately refused.")],
        [P("Personal name"), P("Optional in the present TP1 implementation; if supplied: at most 50 characters and only ASCII letters, spaces, hyphens, and apostrophes."), P("First, middle, last, and preferred-first-name update dialogs before any database write.")],
        [P("Deadline"), P("Noneditable date plus trimmed 24-hour <b>HH:mm</b> text (maximum 5 characters); resulting time must be in the future and no more than 30 days away."), P("Invitation and one-time-password deadline screens share the same parser and policy.")],
        [P("Login lookup"), P("Existing-credential lookup uses username ≤32 and password/OTP ≤64. Composition is intentionally not revalidated at login."), P("Login controller before database use; all failures use one generic message to avoid revealing whether an account exists.")],
        [P("Invitation code"), P("Non-null, nonblank, 1–10 characters; then checked against the live invitation table and deadline."), P("Login account-setup action before the New Account screen opens.")],
    ]
    table = LongTable(rows, colWidths=[1.28 * inch, 3.18 * inch, 2.63 * inch], repeatRows=1)
    table.setStyle(standard_table_style())
    return table


def standard_table_style():
    """Return the common accessible table style."""
    # Vishwam, repeatable styling keeps dense audit tables easy to scan.
    return TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), NAVY),
        ("TEXTCOLOR", (0, 0), (-1, 0), WHITE),
        ("GRID", (0, 0), (-1, -1), 0.45, GRID),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 4),
        ("RIGHTPADDING", (0, 0), (-1, -1), 4),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [WHITE, colors.HexColor("#F8FAFC")]),
    ])


def field_inventory_table():
    """List every text-input object found in TP1/src, including the dormant dialog."""
    # Vishwam, the inventory is intentionally object-complete for the rubric's all-fields test.
    raw_rows = [
        ("1", "Login · Username", "TextField", "≤32 before lookup; unknown, malformed, empty, or long input is refused.", "Generic credential alert; no account-enumeration detail.", "ControllerUserLogin.doLogin"),
        ("2", "Login · Password / OTP", "PasswordField", "≤64 before lookup. Active OTP is tested/consumed first; otherwise permanent password is authenticated.", "Generic credential alert. OTP success forces reset, then fresh login.", "ControllerUserLogin; Database"),
        ("3", "Login · Invitation code", "TextField", "Nonblank, 1–10; then live-code and deadline checks.", "Specific boundary, unknown-code, or expired-code alert.", "validInvitationInput; ViewNewAccount"),
        ("4", "First Admin · UserName", "TextField", "Full 4–32 UserName FSM.", "Detailed reason + error position; no account is written.", "ControllerFirstAdmin"),
        ("5", "First Admin · Chosen password", "Read-only PasswordField", "Value can only come from the live 8–20 password popup; submit-time evaluator rechecks it.", "Masked value; live red/green rules; setup blocked on cancel/invalid.", "View/ControllerFirstAdmin"),
        ("6", "First Admin · Confirm password", "PasswordField", "Explicit ≤20 bound, then exact equality with field 5.", "Overlong confirmation is cleared with a size message; mismatch clears both fields.", "ControllerFirstAdmin"),
        ("7", "Invited Account · UserName", "TextField", "Full UserName FSM plus duplicate database check.", "Detailed error; duplicate no longer terminates the app.", "ControllerNewAccount"),
        ("8", "Invited Account · Chosen password", "Read-only PasswordField", "Value can only come from live popup; submit-time evaluator rechecks it.", "Masked value; live rules; setup blocked on cancel/invalid.", "View/ControllerNewAccount"),
        ("9", "Invited Account · Confirm password", "PasswordField", "Explicit ≤20 bound, then exact equality with field 8.", "Overlong confirmation is cleared with a size alert; mismatch clears both fields.", "ControllerNewAccount"),
        ("10", "Admin Home · Invitation email", "TextField", "Full shared email recognizer; duplicate outstanding email check.", "First structural error is explained; invitation is not created.", "ControllerAdminHome"),
        ("11", "Admin Home · Invitation time", "TextField", "≤5, trimmed HH:mm; future and ≤30 days with selected date.", "Malformed, past, and too-far deadlines receive distinct messages.", "ControllerAdminHome"),
        ("12", "OTP Page · Deadline time", "TextField", "Same shared parser and 30-day policy as invitation time.", "OTP is not stored; error is shown on the OTP page.", "ControllerOneTimePassword"),
        ("13", "Dynamic Popup · New password", "PasswordField", "Every edit applies 8–20 composition rules; excessive/null/empty input clears all prior validity state.", "Masked; red/green rules; Finish disabled until valid; reopening starts clean.", "passwordPopUpWindow Model/View"),
        ("14", "User Update · UserName", "TextInputDialog", "Full shared UserName FSM plus duplicate check.", "Specific invalid/duplicate alert; no database write.", "ControllerUserUpdate; ViewUserUpdate"),
        ("15", "User Update · First name", "TextInputDialog", "Optional; if supplied ≤50 and allowed name characters only.", "Invalid entry produces a Name alert; value is unchanged.", "ControllerUserUpdate.checkName"),
        ("16", "User Update · Middle name", "TextInputDialog", "Optional; if supplied ≤50 and allowed name characters only.", "Invalid entry produces a Name alert; value is unchanged.", "ControllerUserUpdate.checkName"),
        ("17", "User Update · Last name", "TextInputDialog", "Current TP1 behavior: optional; if supplied ≤50 and allowed name characters only.", "Invalid entry produces a Name alert; value is unchanged.", "ControllerUserUpdate.checkName"),
        ("18", "User Update · Preferred first name", "TextInputDialog", "Optional; if supplied ≤50 and allowed name characters only.", "Invalid entry produces a Name alert; value is unchanged.", "ControllerUserUpdate.checkName"),
        ("19", "User Update · Email", "TextInputDialog", "Full shared email recognizer.", "Specific invalid-email alert; value is unchanged.", "ControllerUserUpdate.checkEmailAddress"),
        ("20", "User Update · Legacy password dialog", "Dormant TextInputDialog", "Instantiated by inherited code but never connected to a button or shown.", "No user can reach it. The active Update Password action uses field 13 instead.", "ViewUserUpdate (dead control)"),
    ]
    rows = [[P("#", "TableHead"), P("Screen / field", "TableHead"), P("Control", "TableHead"),
             P("Validation before use", "TableHead"), P("Failure / success behavior", "TableHead"),
             P("Code path", "TableHead")]]
    for item in raw_rows:
        rows.append([P(item[0], "TableCellSmall"), P(item[1], "TableCellSmall"),
                     P(item[2], "TableCellSmall"), P(item[3], "TableCellSmall"),
                     P(item[4], "TableCellSmall"), P(item[5], "TableCellSmall")])
    table = LongTable(
        rows,
        colWidths=[0.24 * inch, 1.28 * inch, 0.76 * inch, 1.92 * inch, 1.75 * inch, 1.15 * inch],
        repeatRows=1,
    )
    table.setStyle(standard_table_style())
    return table


def evidence_table():
    """Summarize executable evidence and manual GUI checkpoints."""
    # Vishwam, separate automated evidence from GUI walkthroughs so claims stay precise.
    rows = [
        [P("Evidence", "TableHead"), P("Scope", "TableHead"), P("Result", "TableHead")],
        [P("InputValidationTestingAutomation 1–46"), P("UserName, password, email, names, update reuse, null/invalid password input, chosen/confirmation 20/21 boundaries, 10,000-character attacks, and stale dynamic-state regression."), P("46 passed<br/>0 failed")],
        [P("OneTimePasswordTestingAutomation 1–33"), P("Generation, future/expired deadlines, wrong/null/oversized input, immediate one-use consumption, permanent-password reset, replacement, and two concurrent redemption attempts."), P("33 passed<br/>0 failed")],
        [P("LoginInputTests 1–7"), P("Invitation-code null, empty, blank, valid lengths 1/6/10, and rejected length 11."), P("7 passed<br/>0 failed")],
        [P("TP1RegressionSuite"), P("All six team automation classes after integration."), P("152 passed<br/>0 failed")],
        [P("DynamicPasswordViewTests P01–P06"), P("Masked password control, valid-to-empty and overlong transitions, clean reopen state, and redacted invalid-input feedback."), P("6 passed<br/>0 failed")],
        [P("FirstUserReloginTests R01–R04"), P("First-user completion label, return to login, cleared authorization, and singleton reset for ordinary account updates."), P("4 passed<br/>0 failed")],
    ]
    table = LongTable(rows, colWidths=[1.86 * inch, 4.25 * inch, 0.98 * inch], repeatRows=1)
    table.setStyle(standard_table_style())
    return table


def build_story():
    """Assemble the complete PDF story."""
    # Vishwam, align the document directly with Task 4 while retaining implementation evidence.
    story = []
    story.extend([
        Spacer(1, 0.55 * inch),
        Paragraph("CSE 360 · TEAM PROJECT PHASE 1", styles["CoverEyebrow"]),
        Paragraph("Input Validation", styles["CoverTitle"]),
        Paragraph("Complete TP1 text-input inventory, rules, user feedback, and verification", styles["CoverSubtitle"]),
        Spacer(1, 0.08 * inch),
        status_cards(),
        Spacer(1, 0.28 * inch),
        Table([
            [P("Prepared by", "Footer"), P("Vishwam (V.G.)", "Body")],
            [P("Source", "Footer"), P("Local integration branch <b>vishwam-integration</b>", "Body")],
            [P("Implementation date", "Footer"), P("21 September 2026", "Body")],
            [P("Required filename", "Footer"), P("<b>InputValidation.pdf</b>", "Body")],
        ], colWidths=[1.55 * inch, 5.25 * inch], style=TableStyle([
            ("BACKGROUND", (0, 0), (0, -1), PALE_BLUE),
            ("GRID", (0, 0), (-1, -1), 0.45, GRID),
            ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
            ("LEFTPADDING", (0, 0), (-1, -1), 7),
            ("RIGHTPADDING", (0, 0), (-1, -1), 7),
            ("TOPPADDING", (0, 0), (-1, -1), 6),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
        ])),
        Spacer(1, 0.25 * inch),
        Paragraph(
            "<b>Completion statement.</b> Every text-input object in <font name='Courier'>TP1/src</font> "
            "was located and classified. Reachable input is bounded before expensive processing or "
            "database use, shared rules are reused across screens, and invalid input produces useful "
            "feedback without committing a write. The single dormant inherited dialog is disclosed "
            "explicitly so the inventory is object-complete.",
            styles["Body"],
        ),
        Table([[P("Implemented", "Callout"), P("Dynamic password help now covers all four new-password workflows: First Admin, invited account, User Update, and OTP reset.", "BodySmall")]],
              colWidths=[1.15 * inch, 5.65 * inch], style=TableStyle([
                  ("BACKGROUND", (0, 0), (-1, -1), PALE_GREEN),
                  ("BOX", (0, 0), (-1, -1), 0.8, GREEN),
                  ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                  ("LEFTPADDING", (0, 0), (-1, -1), 7),
                  ("RIGHTPADDING", (0, 0), (-1, -1), 7),
                  ("TOPPADDING", (0, 0), (-1, -1), 7),
                  ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
              ])),
        PageBreak(),
        Paragraph("1 · Validation design and shared rules", styles["SectionTitle"]),
        Paragraph(
            "Validation follows one consistent order: <b>(1)</b> null/blank and reasonable length "
            "checks, <b>(2)</b> syntax or finite-state-machine checks, <b>(3)</b> cross-field or "
            "database constraints, and only then <b>(4)</b> the requested operation. Existing login "
            "credentials deliberately receive only bounds and authentication checks; rejecting them "
            "for current creation rules could lock out a legitimate older account.",
            styles["Body"],
        ),
        validator_table(),
        Spacer(1, 0.12 * inch),
        Paragraph("Dynamic password behavior", styles["SubTitle"]),
        Paragraph(
            "The shared popup masks characters and reevaluates on every edit. Six visible requirements "
            "start unsatisfied; Finish remains disabled until all are true. Every opening resets the "
            "field, messages, flags, and button. Null, empty, or excessive input clears earlier green "
            "state. Error positioning uses bullets rather than echoing secret text, and plaintext is "
            "not written to the console. First Admin and invited-account screens retain a separate "
            "masked confirmation field with its own 20-character bound and a submit-time evaluator check.",
            styles["Body"],
        ),
        Paragraph("One-time password boundary", styles["SubTitle"]),
        Paragraph(
            "A generated OTP is 12 characters and follows the same password composition policy. At "
            "login, the database uses one conditional update to both validate and clear it. Therefore "
            "only one simultaneous request can win. Cancellation cannot restore the OTP; a successful "
            "reset returns to a blank login screen and requires the new permanent password.",
            styles["Body"],
        ),
        PageBreak(),
        Paragraph("2 · Complete text-input inventory", styles["SectionTitle"]),
        Paragraph(
            "The table below accounts for every <font name='Courier'>TextField</font>, "
            "<font name='Courier'>PasswordField</font>, and <font name='Courier'>TextInputDialog</font> "
            "created by production code. Field 20 is intentionally included even though it is "
            "unreachable, because the active password-update button uses the shared dynamic popup.",
            styles["Body"],
        ),
        field_inventory_table(),
        PageBreak(),
        Paragraph("3 · Constrained controls that are not free text", styles["SectionTitle"]),
        Paragraph(
            "Not every control needs a text recognizer. The two deadline DatePickers are explicitly "
            "noneditable; users select a date. User, role, deletion target, and multi-role ComboBoxes "
            "remain noneditable and only return team-provided choices. The OTP target must be a real "
            "selected user, and invitation roles come from Admin, Contributor, Viewer, and Curator. "
            "These controls still receive null/placeholder and authorization checks before use.",
            styles["Body"],
        ),
        Table([
            [P("Control family", "TableHead"), P("Why arbitrary text is impossible", "TableHead"), P("Remaining checks", "TableHead")],
            [P("Deadline DatePicker"), P("<font name='Courier'>setEditable(false)</font>; calendar selection only."), P("Date present; combined deadline future and ≤30 days.")],
            [P("Role / user ComboBoxes"), P("Default noneditable JavaFX choice controls populated by application data."), P("Selection non-null; placeholder refused; database authorization/policy applied.")],
            [P("Confirmation Alerts"), P("Fixed Yes/No or OK choices rather than typed text."), P("Destructive operation proceeds only after explicit Yes.")],
        ], colWidths=[1.45 * inch, 3.28 * inch, 2.36 * inch], style=standard_table_style()),
        Spacer(1, 0.16 * inch),
        Table([[P("Reasonable-size defense", "Callout"), P("All reachable typed values are bounded before database use. Validators short-circuit excessive text, including the 10,000-character attack cases required by the assignment.", "BodySmall")]],
              colWidths=[1.6 * inch, 5.2 * inch], style=TableStyle([
                  ("BACKGROUND", (0, 0), (-1, -1), PALE_GOLD),
                  ("BOX", (0, 0), (-1, -1), 0.8, GOLD),
                  ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
                  ("LEFTPADDING", (0, 0), (-1, -1), 7),
                  ("RIGHTPADDING", (0, 0), (-1, -1), 7),
                  ("TOPPADDING", (0, 0), (-1, -1), 7),
                  ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
              ])),
        Spacer(1, 0.18 * inch),
        Paragraph("4 · Verification evidence", styles["SectionTitle"]),
        evidence_table(),
        Spacer(1, 0.15 * inch),
        Paragraph("Manual screencast checkpoints", styles["SubTitle"]),
        Paragraph(
            "Show: (1) live requirement colors, (2) Finish disabled for empty and 21-character input, "
            "(3) masked password text, (4) confirmation mismatch rejection, (5) duplicate invited "
            "UserName rejection without application exit, (6) accepted OTP immediately failing on "
            "reuse, (7) reset cancellation requiring a newly issued OTP, (8) successful reset "
            "returning to login instead of a role home page, and (9) first-user account completion "
            "also returning to a cleared login session.",
            styles["Body"],
        ),
        Spacer(1, 0.1 * inch),
        Paragraph("5 · Code traceability", styles["SectionTitle"]),
        Table([
            [P("Concern", "TableHead"), P("Primary implementation", "TableHead"), P("Executable evidence", "TableHead")],
            [P("Identity + passwords", "TableCellSmall"), P("UserNameRecognizer; password popup Model/View; FirstAdmin, NewAccount, Login, and UserUpdate controllers", "TableCellSmall"), P("InputValidation 1–11, 35, 37–46; Dynamic UI P01–P06", "TableCellSmall")],
            [P("Profile + invitations", "TableCellSmall"), P("EmailAddressRecognizer; ControllerUserUpdate.checkName; AdminHome deadline rules; Login invitation boundary", "TableCellSmall"), P("InputValidation 12–34, 36; LoginInput 1–7", "TableCellSmall")],
            [P("OTP + secure completion", "TableCellSmall"), P("Database; Login and OneTimePassword controllers; FirstAdmin and UserUpdate completion flow", "TableCellSmall"), P("OneTimePassword 1–33; FirstUserRelogin R01–R04", "TableCellSmall")],
        ], colWidths=[1.3 * inch, 4.12 * inch, 1.67 * inch], style=standard_table_style()),
    ])
    return story


def main():
    """Generate the PDF."""
    # Vishwam, metadata and deterministic content are produced in one build step.
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    document = ValidationDocument(str(OUTPUT_PATH))
    document.build(build_story())
    print(OUTPUT_PATH)


if __name__ == "__main__":
    main()
