package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Transport.java,v 1.2 2003/03/10 16:35:45 stefano Exp $
 */
public abstract class Transport {

	public static void send(Message message) {
        throw new NoSuchMethodError("This is a mock object");
	}
}
