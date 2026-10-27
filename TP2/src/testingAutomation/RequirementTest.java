package testingAutomation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*******
 * <p> Title: RequirementTest Annotation. </p>
 *
 * <p> Description: Marks one test method in {@link ContributorRequirementsTestbed} and records
 * which Contributor requirement it tests, its number within that requirement, and whether it is a
 * positive test (valid use succeeds) or a negative test (invalid use is refused or handled
 * safely).  The testbed runner, the coverage check, and the generated requirements-to-tests list
 * (Task 6 / Task 7.1) all read this annotation, so the list can never drift from the code. </p>
 *
 * <p> Copyright: CSE 360 Team Project © 2026 </p>
 *
 * @author Aditya Gupta (agupt515)
 *
 * @version 1.00	2026-10-27	Initial version for TP2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequirementTest {

	/** Positive tests show valid use succeeds; negative tests show invalid use is refused. */
	enum Kind { POSITIVE, NEGATIVE }

	/** @return the Contributor requirement this test covers */
	ContributorRequirement requirement();

	/** @return the test's number within its requirement (T1, T2, ...) */
	int number();

	/** @return whether this is a positive or a negative test */
	Kind kind();

	/** @return one-line description printed in the output and in the requirements list */
	String title();
}
