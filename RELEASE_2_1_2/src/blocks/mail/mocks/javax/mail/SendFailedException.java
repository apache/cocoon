package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: SendFailedException.java,v 1.1 2003/08/08 11:35:03 cziegeler Exp $
 */
public class SendFailedException extends MessagingException {

	public SendFailedException() {
		throw new NoSuchMethodError("This is a mock object");
	}

    public SendFailedException(String s) {
        throw new NoSuchMethodError("This is a mock object");
    }
	
	public SendFailedException(String message, java.io.IOException ioe) {
		throw new NoSuchMethodError("This is a mock object");
	}
    
    public Address[] getInvalidAddresses() {
        throw new NoSuchMethodError("This is a mock object");
    }

    public Address[] getValidUnsentAddresses() {
        throw new NoSuchMethodError("This is a mock object");
    }
}
