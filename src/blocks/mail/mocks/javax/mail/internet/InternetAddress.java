package javax.mail.internet;

import javax.mail.Address;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: InternetAddress.java,v 1.2 2003/03/10 16:35:44 stefano Exp $
 */
public class InternetAddress extends Address {

	public InternetAddress(String from) {
        throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getAddress() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getPersonal() {
		throw new NoSuchMethodError("This is a mock object");
	}
}
