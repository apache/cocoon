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

import java.io.IOException;
import java.util.Enumeration;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;


/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id:
 */
public class MimeBodyPart extends BodyPart implements MimePart {

    public MimeBodyPart(){
        throw new NoSuchMethodError("This is a mock object");
    }

    public boolean isMimeType(String type) throws MessagingException {
        return false;
    }

    public String getContentType() throws MessagingException {
        return null;
    }

    public void setText(String s) throws MessagingException {
    }

    public Object getContent() throws IOException, MessagingException {
        return null;
    }

    public Enumeration getAllHeaders() throws MessagingException {
        return null;
    }

    public String getDescription() throws MessagingException {
        return null;
    }

    public String getDisposition() throws MessagingException {
        return null;
    }

    public String getFileName() throws MessagingException {
        return null;
    }

    public void setDataHandler(DataHandler dh) throws MessagingException {
    }

    public void setFileName(String filename) throws MessagingException {
    }

}
