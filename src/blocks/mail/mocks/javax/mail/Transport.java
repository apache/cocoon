package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Transport.java,v 1.3 2003/04/17 20:27:03 haul Exp $
 */
public abstract class Transport {

	public static void send(Message message) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
	}
}
