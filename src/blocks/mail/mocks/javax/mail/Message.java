/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.mail;

import java.util.Date;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Message.java,v 1.4 2004/03/06 02:25:46 antonio Exp $
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
    
    public void setRecipients(Message.RecipientType type, Address[] addresses) 
    throws MessagingException {     
       throw new NoSuchMethodError("This is a mock object");
    }

    public void setContent(Multipart mp)
    throws MessagingException {     
       throw new NoSuchMethodError("This is a mock object");
    }

    public void saveChanges()
    throws MessagingException {     
       throw new NoSuchMethodError("This is a mock object");
    }
}