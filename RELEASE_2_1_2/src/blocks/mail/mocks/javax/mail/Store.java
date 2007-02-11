package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class Store {
	
	public void connect() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}

	public boolean isConnected() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public void close() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Folder getDefaultFolder() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}

	public Folder getFolder(String name) throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}	
}
