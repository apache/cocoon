package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: MessagingException.java,v 1.2 2003/03/10 16:35:45 stefano Exp $
 */
public class MessagingException extends Exception {

	public MessagingException() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public MessagingException(String message, java.io.IOException ioe) {
		throw new NoSuchMethodError("This is a mock object");
	}
}
