package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class Header {

	public String getName() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getValue() {
		throw new NoSuchMethodError("This is a mock object");
	}
}
