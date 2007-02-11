package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public interface UIDFolder {

	Message getMessageByUID(int uid);
	
}
