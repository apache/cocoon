package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Part.java,v 1.2 2003/03/10 16:35:45 stefano Exp $
 */
public interface Part {
	
	public static final String INLINE = null;
	public static final String ATTACHMENT = null;
	
    boolean isMimeType(String type) throws MessagingException;

    String getContentType() throws MessagingException;

    void setText(String s) throws MessagingException;

	Object getContent() throws java.io.IOException, MessagingException;

	java.util.Enumeration getAllHeaders() throws MessagingException;

	String getDescription() throws MessagingException;

	String getDisposition() throws MessagingException;
	
	String getFileName() throws MessagingException;
}
