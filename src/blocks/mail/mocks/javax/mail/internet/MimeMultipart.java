package javax.mail.internet;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class MimeMultipart {

	public int getCount() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public javax.mail.BodyPart getBodyPart(int index) {
		throw new NoSuchMethodError("This is a mock object");
	}
}
