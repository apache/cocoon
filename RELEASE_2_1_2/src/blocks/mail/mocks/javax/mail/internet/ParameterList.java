package javax.mail.internet;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class ParameterList {

	public java.util.Enumeration getNames() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String get(String name) {
		throw new NoSuchMethodError("This is a mock object");
	}
}
