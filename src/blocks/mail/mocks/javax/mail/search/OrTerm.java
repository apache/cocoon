package javax.mail.search;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class OrTerm extends SearchTerm {
	public OrTerm(SearchTerm a, SearchTerm b) {
		throw new NoSuchMethodError("This is a mock object");
	}
}
