/*-- $Id: AbstractFormatter.java,v 1.7 2001-03-26 15:30:30 greenrd Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.cocoon.formatter;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.xml.serialize.*;
import org.apache.cocoon.framework.*;
import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.7 $ $Date: 2001-03-26 15:30:30 $
 */

public abstract class AbstractFormatter 
implements Configurable, Formatter, Status, Cacheable {
 
    protected String statusMessage = "Abstract Formatter";
    protected String omitXMLDeclaration;
    protected String MIMEtype;
    protected String encoding;
    protected String doctypePublic;
    protected String doctypeSystem;
    protected String indent;
    protected String lineWidth;
    protected String preserveSpace;
    protected OutputFormat format;
        
    public void init(Configurations conf) {

        String mt = (String) conf.get("MIME-type");
        if (mt != null) {
            this.MIMEtype = mt;
        }

        format = new OutputFormat();
        format.setPreserveSpace(true);

        encoding = (String) conf.get("encoding");
        if (encoding != null) {
            format.setEncoding(encoding);
        }
        else {
            encoding = format.getEncoding ();
        }

        doctypePublic = (String) conf.get("doctype-public");
        doctypeSystem = (String) conf.get("doctype-system");
        if (doctypeSystem != null) {
            format.setDoctype(doctypePublic, doctypeSystem);
        }

        indent = (String) conf.get("indent");
        if (indent != null) {
            format.setIndenting(true);
            format.setIndent(Integer.parseInt(indent));
        }

        preserveSpace = (String) conf.get("preserve-space");
        if (preserveSpace!= null) {
            format.setPreserveSpace(Boolean.valueOf(preserveSpace).booleanValue());
        }
        
        lineWidth = (String) conf.get("line-width");
        if (lineWidth != null) {
            format.setLineWidth(Integer.parseInt(lineWidth));
        }        

        omitXMLDeclaration = (String) conf.get("omit-XML-declaration");
    }

    public String getEncoding() {
        return format.getEncoding ();
    }

    public String getMIMEType() {
        return MIMEtype;
    }
    
    public String getStatus() {
        StringBuffer message = new StringBuffer();
        message.append(statusMessage);
        message.append("<br>");
        if (MIMEtype != null) {
            message.append("[ MIME type:  ");
            message.append(MIMEtype);
            message.append(" ]<br>");
        }
        if (encoding != null) {
            message.append("[ Encoding:  ");
            message.append(encoding);
            message.append(" ]<br>");
        }
        if (doctypeSystem != null) {
            message.append("[ Doctype:  ");
            if (doctypePublic != null) {
                message.append(doctypePublic);
                message.append(" ");
            }
            message.append(doctypeSystem);
            message.append(" ]<br>");
        }
        if (preserveSpace != null) {
            message.append("[ Preserve Space:  ");
            message.append(preserveSpace);
            message.append(" ]<br>");
        }
        if (indent != null) {
            message.append("[ Indent:  ");
            message.append(indent);
            message.append(" ]<br>");
        }
        if (lineWidth != null) {
            message.append("[ Line Width:  ");
            message.append(lineWidth);
            message.append(" ]<br>");
        }
        if (omitXMLDeclaration != null) {
            message.append("[ Omit XML Declaration:  ");
            message.append(omitXMLDeclaration);
            message.append(" ]<br>");
        }
        message.append("<br>");
        return message.toString();
    }       

    public boolean isCacheable (HttpServletRequest request) {
        return true;
    }
}
