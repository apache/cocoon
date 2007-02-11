package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: MessagingException.java,v 1.3 2003/04/17 20:27:03 haul Exp $
 */
public class MessagingException extends Exception {

	public MessagingException() {
		throw new NoSuchMethodError("This is a mock object");
	}

    public MessagingException(String s) {
        throw new NoSuchMethodError("This is a mock object");
    }
	
	public MessagingException(String message, java.io.IOException ioe) {
		throw new NoSuchMethodError("This is a mock object");
	}
}
