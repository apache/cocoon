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

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Part.java,v 1.4 2004/03/06 02:25:46 antonio Exp $
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
