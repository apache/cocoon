package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Part.java,v 1.3 2003/04/17 20:27:03 haul Exp $
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

    void setDataHandler(javax.activation.DataHandler dh)
        throws MessagingException;

    void setFileName(java.lang.String filename) throws MessagingException;
}
