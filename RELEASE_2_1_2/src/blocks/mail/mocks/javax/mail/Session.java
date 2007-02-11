package javax.mail;

import java.util.Properties;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Session.java,v 1.3 2003/08/08 11:35:03 cziegeler Exp $
 */

public class Session {
    
    public static Session getDefaultInstance(Properties props) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public static Session getDefaultInstance(Properties props, Authenticator auth) {
    	throw new NoSuchMethodError("This is a mock object");
    }
    
    public Store getStore(URLName name) {
		throw new NoSuchMethodError("This is a mock object");
    }

	public Provider[] getProviders() {
		throw new NoSuchMethodError("This is a mock object");
	}

    public Transport getTransport(String name) {
        throw new NoSuchMethodError("This is a mock object");
    }
}
