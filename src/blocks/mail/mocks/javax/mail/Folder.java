package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Folder.java,v 1.1 2003/03/10 16:35:45 stefano Exp $
 */
public class Folder {

	public static final int READ_ONLY = 0;
	public static final int HOLDS_FOLDERS = 0;
	public static final int HOLDS_MESSAGES = 0;
	
	public void open(int mode) {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public boolean isOpen() {
		throw new NoSuchMethodError("This is a mock object");
	}

	public void close(boolean b) {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Message[] getMessages() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public void fetch(Message[] m, FetchProfile profile) {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Folder[] list(String pattern) {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getFullName() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Message getMessage(int id) {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Message[] search(javax.mail.search.SearchTerm term) {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getName() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getURLName() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public boolean isSubscribed() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public int getType() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public boolean hasNewMessages() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public int getMessageCount() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public int getNewMessageCount() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public int getDeletedMessageCount() {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public int getUnreadMessageCount() {
		throw new NoSuchMethodError("This is a mock object");
	}
}
