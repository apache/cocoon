package javax.mail.search;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class SubjectTerm extends SearchTerm {
	public SubjectTerm(String term) {
		throw new NoSuchMethodError("This is a mock object");
	}
}
