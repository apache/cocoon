package javax.mail.internet;

import javax.mail.MessagingException;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: AddressException.java,v 1.3 2003/04/17 20:28:28 haul Exp $
 */
public class AddressException extends MessagingException {

    public AddressException() {
    }
    
    public AddressException(String s){
    }

}
