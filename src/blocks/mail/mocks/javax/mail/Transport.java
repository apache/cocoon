package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Transport.java,v 1.4 2003/08/08 11:35:03 cziegeler Exp $
 */
public abstract class Transport {

	public static void send(Message message) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
	}
    
    public void connect() {
        throw new NoSuchMethodError("This is a mock object");        
    }

    public void close() {
        throw new NoSuchMethodError("This is a mock object");        
    }

    public abstract void sendMessage(Message msg,
                                     Address[] addresses)
    throws MessagingException;
}
