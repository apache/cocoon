/*-- $Id: HTMLFormatter.java,v 1.7 2001-03-01 16:05:39 greenrd Exp $ -- 

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

/**
 * This formatter is used to serialize HTML content. The difference between
 * this formatter and the XMLFormatter is that while the XML formatter doesn't
 * have any semantic information about the document type being formatted,
 * this class handles tags like <em>&lt;br/&gt;</em> and transforms them to
 * HTML that non-XML-aware browsers can understand. Note that this creates
 * markap that is non-well-formed XML. If you want to be able to send HTML 
 * code to old-browser but still create well-formed XML, use the XHTMLFormatter
 * instead.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.7 $ $Date: 2001-03-01 16:05:39 $
 */

public class HTMLFormatter extends AbstractFormatter {

    SerializerFactory factory;

    public HTMLFormatter () {
        this.factory = SerializerFactory.getSerializerFactory(Method.HTML);
        super.MIMEtype = "text/html";
        super.statusMessage = "HTML Formatter";
    }       
        
    public void init(Configurations conf) {
        super.init(conf);
        format.setMethod(Method.HTML);
        format.setOmitXMLDeclaration(true);
    }       
        
    public void format(Document document, OutputStream stream, Dictionary p) throws Exception {
        factory.makeSerializer(stream, format).asDOMSerializer().serialize(document);
    }       
}        
