package javax.mail;

import java.util.Date;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Message.java,v 1.2 2003/03/10 16:35:45 stefano Exp $
 */
public abstract class Message implements Part {

    public static class RecipientType {
        public static RecipientType BCC = null;
        public static RecipientType CC = null;
        public static RecipientType TO = null;
    }

	public void setFrom(Address addr) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
	}

	public void setRecipient(
		RecipientType type,
		Address address) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
	}

	public void setSubject(String subject) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
	}

	public void setText(String body) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
	}

	public void setSentDate(Date date) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
	}
	
	public Object getContent() throws java.io.IOException, MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Address[] getFrom() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Address[] getRecipients(RecipientType type) throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Address[] getReplyTo() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public String getSubject() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public java.util.Date getSentDate() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public java.util.Date getReceivedDate() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public int getSize() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public int getMessageNumber() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
	
	public Flags getFlags() throws MessagingException {
		throw new NoSuchMethodError("This is a mock object");
	}
}
