package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class Multipart {

	public int getCount() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Part getBodyPart(int index) {
		throw new NoSuchMethodError("This is a mock object");
	}
}
