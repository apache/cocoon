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
 * @version CVS $Id: MimeMessage.java,v 1.4 2004/03/06 02:25:44 antonio Exp $
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
