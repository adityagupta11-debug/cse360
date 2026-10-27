package contributorTestEvidence;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import testingAutomation.ContributorRequirement;
import testingAutomation.RequirementTest;

/*******
 * <p> Title: ContributorTestEvidence Class. </p>
 *
 * <p> Description: Generates the Task 7 evidence that belongs to the Contributor requirements
 * testbed, from the delivered source and the {@link RequirementTest} annotations, so the evidence
 * always matches the code:</p>
 * <ul>
 * <li>7.4: one screenshot per test, showing the test's Javadoc and code with original line
 *     numbers, named "&lt;requirement&gt;_7.4_T&lt;n&gt;_&lt;method&gt;.png".</li>
 * <li>7.1 / Task 6: Contributor_Requirements_and_Tests.html and .csv, listing every requirement
 *     with its owner, its tests, the Javadoc HTML page and anchor of each test, and the
 *     screenshot file names.</li>
 * <li>Manual_Tests.html: the manual procedure for each requirement, with blank result columns
 *     for the person who runs it.</li>
 * </ul>
 *
 * <p> It uses headless AWT drawing, so it needs no display.  Run it from the TP2 folder (the
 * document.py script does this). </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author Aditya Gupta (agupt515)
 *
 * @version 1.00	2026-10-27	Initial version for TP2
 */
public final class ContributorTestEvidence {

	/** The testbed source whose test methods are captured */
	private static final Path SOURCE =
			Path.of("src/testingAutomation/ContributorRequirementsTestbed.java");

	/** The Task 7 folder */
	private static final Path FOLDER = Path.of("ContributorTests_Javadocs");

	/** The generated Javadoc page for the testbed, relative to the Task 7 folder */
	private static final String JAVADOC =
			"testingAutomation/ContributorRequirementsTestbed.html";

	private ContributorTestEvidence() {}

	/** One test as found in the source: its annotation, method name, and source line range. */
	private record TestSource(RequirementTest info, String method, int first, int last,
			String screenshot) {}

	/*****
	 * <p> Method: main(String[] args) </p>
	 *
	 * <p> Description: Writes the screenshots, the requirements list, and the manual test sheet.
	 * </p>
	 *
	 * @param args not used
	 *
	 * @throws Exception if the source cannot be read or a file cannot be written
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("java.awt.headless", "true");
		List<String> lines = Files.readAllLines(SOURCE, StandardCharsets.UTF_8);
		List<TestSource> tests = locateTests(lines);
		Path screenshots = FOLDER.resolve("source-screenshots");
		Files.createDirectories(screenshots);
		for (TestSource test : tests)
			render(lines, test, screenshots.resolve(test.screenshot()));
		writeRequirementsList(tests);
		writeManualTests();
		System.out.println("Captured " + tests.size() + " per-test screenshots and wrote the "
				+ "requirements list and manual test sheet.");
	}


	/*****
	 * <p> Method: List&lt;TestSource&gt; locateTests(List&lt;String&gt; lines) </p>
	 *
	 * <p> Description: Pairs each annotated method with its source lines: from the start of the
	 * Javadoc comment above it to the method's closing brace. </p>
	 */
	private static List<TestSource> locateTests(List<String> lines) {
		Map<String, RequirementTest> annotated = new LinkedHashMap<>();
		for (Method method : testbedMethods())
			annotated.put(method.getName(), method.getAnnotation(RequirementTest.class));
		List<TestSource> found = new ArrayList<>();
		for (Map.Entry<String, RequirementTest> entry : annotated.entrySet()) {
			String signature = " " + entry.getKey() + "(";
			int header = -1;
			for (int i = 0; i < lines.size(); i++)
				if (lines.get(i).contains("private static void" + signature)) { header = i; break; }
			if (header < 0) throw new IllegalStateException("Source not found: " + entry.getKey());
			int first = header;
			while (first > 0 && !lines.get(first).trim().startsWith("/**")) first--;
			int last = header, depth = 0;
			boolean opened = false;
			for (int i = header; i < lines.size(); i++) {
				for (char c : lines.get(i).toCharArray()) {
					if (c == '{') { depth++; opened = true; }
					if (c == '}') depth--;
				}
				if (opened && depth == 0) { last = i; break; }
			}
			RequirementTest info = entry.getValue();
			String name = info.requirement().id() + "_7.4_T" + info.number() + "_" + entry.getKey()
					+ ".png";
			found.add(new TestSource(info, entry.getKey(), first + 1, last + 1, name));
		}
		return found;
	}

	/** The testbed's test methods in requirement order (the same order the testbed runs them). */
	private static List<Method> testbedMethods() {
		try {
			Method order = Class.forName("testingAutomation.ContributorRequirementsTestbed")
					.getDeclaredMethod("annotatedTests");
			order.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<Method> tests = (List<Method>) order.invoke(null);
			return tests;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Cannot list the testbed's tests", e);
		}
	}


	/*****
	 * <p> Method: void render(...) </p>
	 *
	 * <p> Description: Draws one test's source lines, with their original line numbers, under a
	 * heading that names the file, the line range, and the requirement. </p>
	 */
	private static void render(List<String> lines, TestSource test, Path output) throws Exception {
		Font code = new Font(Font.MONOSPACED, Font.PLAIN, 15);
		Font heading = new Font(Font.SANS_SERIF, Font.BOLD, 16);
		int lineHeight = 20, margin = 18, headingHeight = 54;
		List<String> rows = new ArrayList<>();
		for (int i = test.first(); i <= test.last(); i++)
			rows.add(String.format("%4d  %s", i, lines.get(i - 1).replace("\t", "    ")));
		BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D measure = probe.createGraphics();
		FontMetrics metrics = measure.getFontMetrics(code);
		int width = 900;
		for (String row : rows) width = Math.max(width, metrics.stringWidth(row) + 2 * margin);
		measure.dispose();
		int height = headingHeight + rows.size() * lineHeight + 2 * margin;

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(new Color(235, 238, 242));
		g.fillRect(0, 0, width, headingHeight);
		g.setColor(new Color(20, 20, 20));
		g.setFont(heading);
		RequirementTest info = test.info();
		g.drawString(info.requirement().id() + " T" + info.number() + " (" + info.kind()
				.toString().toLowerCase() + ") - " + info.title(), margin, 23);
		g.setFont(heading.deriveFont(Font.PLAIN, 13f));
		g.drawString(SOURCE + "  |  lines " + test.first() + "-" + test.last()
				+ "  |  original line numbers", margin, 43);
		g.setFont(code);
		int y = headingHeight + margin + 12;
		for (String row : rows) {
			String text = row.substring(6);
			g.setColor(new Color(130, 130, 130));
			g.drawString(row.substring(0, 6), margin, y);
			String trimmed = text.trim();
			boolean comment = trimmed.startsWith("/*") || trimmed.startsWith("*")
					|| trimmed.startsWith("//");
			boolean annotation = trimmed.startsWith("@") || trimmed.startsWith("kind =");
			g.setColor(comment ? new Color(46, 110, 60)
					: annotation ? new Color(120, 70, 20) : new Color(20, 20, 20));
			g.drawString(text, margin + metrics.stringWidth(row.substring(0, 6)), y);
			y += lineHeight;
		}
		g.dispose();
		ImageIO.write(image, "png", output.toFile());
	}


	/*****
	 * <p> Method: void writeRequirementsList(List&lt;TestSource&gt; tests) </p>
	 *
	 * <p> Description: Writes the Task 6 / 7.1 requirements-to-tests list as HTML and CSV. </p>
	 */
	private static void writeRequirementsList(List<TestSource> tests) throws Exception {
		StringBuilder rows = new StringBuilder();
		StringBuilder csv = new StringBuilder("Requirement,Source story,Owner,Requirement text,"
				+ "Test,Kind,Test title,Test method,Javadoc HTML,Source screenshot,"
				+ "Teammate tests\n");
		for (ContributorRequirement requirement : ContributorRequirement.values()) {
			List<TestSource> mine = tests.stream()
					.filter(t -> t.info().requirement() == requirement).toList();
			StringBuilder cells = new StringBuilder();
			for (TestSource t : mine) {
				String label = "T" + t.info().number();
				String anchor = JAVADOC + "#" + t.method() + "()";
				cells.append("<li><b>").append(label).append("</b> <span class=\"")
						.append(t.info().kind() == RequirementTest.Kind.POSITIVE ? "pos" : "neg")
						.append("\">").append(t.info().kind()).append("</span> ")
						.append(escape(t.info().title())).append("<br><a href=\"").append(anchor)
						.append("\">").append(escape(t.method())).append("()</a> &middot; <a href=\"")
						.append("source-screenshots/").append(t.screenshot()).append("\">")
						.append(t.screenshot()).append("</a></li>");
				csv.append(String.join(",", quote(requirement.id()), quote(requirement.source()),
						quote(requirement.owner()), quote(requirement.statement()), quote(label),
						quote(t.info().kind().toString()), quote(t.info().title()),
						quote(t.method() + "()"), quote(anchor),
						quote("source-screenshots/" + t.screenshot()),
						quote(requirement.teammateTests()))).append('\n');
			}
			rows.append("<tr id=\"").append(requirement.id()).append("\"><td><b>")
					.append(requirement.id()).append("</b><br>").append(escape(requirement.source()))
					.append("</td><td>").append(escape(requirement.statement())).append("</td><td>")
					.append(escape(requirement.owner())).append("</td><td><ul>").append(cells)
					.append("</ul></td><td>").append(escape(requirement.teammateTests()))
					.append("</td></tr>\n");
		}
		long positive = tests.stream().filter(t -> t.info().kind() == RequirementTest.Kind.POSITIVE)
				.count();
		String page = """
				<!doctype html>
				<html lang="en"><head><meta charset="utf-8">
				<title>Contributor Requirements and Tests</title>
				<style>
				body{font:15px/1.5 system-ui,sans-serif;margin:32px;color:#111}
				table{border-collapse:collapse;width:100%%}
				th,td{border:1px solid #ccc;padding:8px;vertical-align:top;text-align:left}
				th{background:#eef1f5} ul{margin:0;padding-left:18px} li{margin:4px 0}
				.pos{color:#1d6b33;font-weight:600} .neg{color:#9b2c1c;font-weight:600}
				code{background:#f3f3f3;padding:1px 4px}
				</style></head><body>
				<h1>TP2 Contributor Requirements and Tests (Tasks 6 and 7.1)</h1>
				<p>Every Contributor (Phase Two) requirement from the 2026-08-19 user stories, with its
				owner and two or more tests, at least one positive and one negative. C1-C4 are the four
				Contributor Phase Two stories in order. This page is generated from
				<code>ContributorRequirement</code> and the <code>@RequirementTest</code> annotations in
				<code>ContributorRequirementsTestbed</code>, so it always matches the code.</p>
				<p><b>%d requirements, %d tests (%d positive, %d negative).</b>
				Run the tests: in Eclipse run <code>testingAutomation.ContributorRequirementsTestbed</code>,
				or from the TP2 folder run <code>python3 dev.py test</code> to run this testbed and every
				other team suite. Actual output:
				<a href="results/testingAutomation.ContributorRequirementsTestbed.txt">testbed output</a>.</p>
				<p>Each test link opens its Javadoc (requirement tested, how it is checked, how to read the
				output); each <code>.png</code> link opens the screenshot of that test's code and comments.
				See also the <a href="Test_Javadoc_Template.html">test Javadoc template</a>, the
				<a href="Manual_Tests.html">manual tests</a>, and the
				<a href="index.html">full test Javadoc</a> including every teammate's test classes.</p>
				<table><tr><th>ID / story</th><th>Requirement</th><th>Owner</th>
				<th>Tests (Javadoc and screenshot)</th><th>Teammate tests that also cover it</th></tr>
				%s</table></body></html>
				""".formatted(ContributorRequirement.values().length, tests.size(), positive,
				tests.size() - positive, rows);
		Files.writeString(FOLDER.resolve("Contributor_Requirements_and_Tests.html"), page);
		Files.writeString(FOLDER.resolve("Contributor_Requirements_and_Tests.csv"), csv);
	}


	/*****
	 * <p> Method: void writeManualTests() </p>
	 *
	 * <p> Description: Writes the manual test sheet; the result columns are left blank for the
	 * person who performs each test. </p>
	 */
	private static void writeManualTests() throws Exception {
		StringBuilder rows = new StringBuilder();
		for (ContributorRequirement r : ContributorRequirement.values())
			rows.append("<tr><td><b>M-").append(r.id().substring(3)).append("</b><br>")
					.append(r.id()).append("</td><td>").append(escape(r.manualSteps()))
					.append("</td><td>").append(escape(r.manualExpected()))
					.append("</td><td></td><td></td></tr>\n");
		String page = """
				<!doctype html>
				<html lang="en"><head><meta charset="utf-8"><title>Contributor Manual Tests</title>
				<style>
				body{font:15px/1.5 system-ui,sans-serif;margin:32px;color:#111}
				table{border-collapse:collapse;width:100%%}
				th,td{border:1px solid #ccc;padding:8px;vertical-align:top;text-align:left}
				th{background:#eef1f5} td:nth-child(4),td:nth-child(5){min-width:110px}
				</style></head><body>
				<h1>TP2 Contributor Manual Tests</h1>
				<p>One manual procedure per Contributor requirement, run through the real GUI. Use the
				isolated demo database: from the TP2 folder run <code>python3 dev.py demo</code>, which
				creates Contributor accounts <code>jsmith</code>, <code>akim</code> and
				<code>newcontributor</code> and seeds unlocked, field-locked, whole-locked and curator-edited
				lessons (the demo-only password is printed on the first run and is listed in
				<code>README_VISHWAM_TP2.md</code>). Delete <code>data/TP2Demo.mv.db</code> to start fresh. Record Pass/Fail,
				the tester, and the date in the last two columns; the columns are blank until a person
				runs the procedure.</p>
				<p>Every requirement is also checked automatically; see
				<a href="Contributor_Requirements_and_Tests.html">requirements and tests</a>.</p>
				<table><tr><th>ID</th><th>Steps</th><th>Expected result</th><th>Pass / Fail</th>
				<th>Tester, date</th></tr>
				%s</table></body></html>
				""".formatted(rows);
		Files.writeString(FOLDER.resolve("Manual_Tests.html"), page);
	}

	/** Escapes text for HTML. */
	private static String escape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	/** Quotes a value for CSV. */
	private static String quote(String value) {
		return "\"" + value.replace("\"", "\"\"") + "\"";
	}
}
