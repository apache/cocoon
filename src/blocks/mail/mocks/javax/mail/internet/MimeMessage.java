package javax.mail.internet;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.MessagingException;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: MimeMessage.java,v 1.2 2003/03/10 16:35:44 stefano Exp $
 */
public class MimeMessage extends Message implements MimePart {
    
    public MimeMessage(Session session) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public boolean isMimeType(String type) throws MessagingException {
    	throw new NoSuchMethodError("This is a mock object");
    }
    
    public java.util.Enumeration getAllHeaders() throws MessagingException{
		throw new NoSuchMethodError("This is a mock object");
    }
    
	public String getContentType() throws MessagingException{
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
}
