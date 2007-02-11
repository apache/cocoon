package javax.mail.internet;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.MessagingException;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: MimeMessage.java,v 1.3 2003/04/17 20:28:28 haul Exp $
 */
public class MimeMessage extends Message implements MimePart {

    public MimeMessage(Session session) {
        throw new NoSuchMethodError("This is a mock object");
    }

    public boolean isMimeType(String type) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public java.util.Enumeration getAllHeaders() throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public String getContentType() throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public void setText(String s) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public Object getContent() throws java.io.IOException, MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public String getDescription() throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public String getDisposition() throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public String getFileName() throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public String getMessageID() throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public void setRecipients(Message.RecipientType type, Address[] addresses)
        throws MessagingException {
    }

    public void setText(java.lang.String text, java.lang.String charset)
        throws MessagingException {
    }

    public void setDataHandler(DataHandler dh) throws MessagingException {
    }

    public void setFileName(String filename) throws MessagingException {
    }
    
    public void setContent(Multipart mp) throws MessagingException {
    }
}
