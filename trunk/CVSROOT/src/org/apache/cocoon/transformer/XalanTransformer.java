/*-- $Id: XalanTransformer.java,v 1.1 1999-12-14 23:41:08 stefano Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
package org.apache.cocoon.transformer;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.xalan.xslt.*;
import org.apache.xalan.xpath.xml.*;
import org.apache.cocoon.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * This class implements the transformer interface for the Apache
 * Xalan XSLT processor.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1 $ $Date: 1999-12-14 23:41:08 $
 */

public class XalanTransformer extends AbstractActor implements Transformer, Status {

    XSLTProcessor processor;

    public void init(Director director) {
        super.init(director);
        this.processor = XSLTProcessorFactory.getProcessor(
            new XMLParser(
                (Parser) director.getActor("parser")
            )
        );
    }

    public Document transform(Document in, Document sheet, Document out) throws Exception {
        XSLTInputSource i = new XSLTInputSource(in);
        XSLTInputSource s = new XSLTInputSource(sheet);
        XSLTResultTarget o = new XSLTResultTarget(out);
        processor.process(i, s, o);
        return out;
    }

    public String getStatus() {
        return "Apache Xalan XSLT Processor";
    }
 
    class XMLParser extends XMLParserLiaisonDefault {
        Parser parser;
        Document document;

        public XMLParser(Parser parser) {
            this.parser = parser;
        }

        public Document createDocument() {
            return this.parser.createEmptyDocument();
        }

        public void parse(InputSource in) throws IOException, SAXException {
            this.document = this.parser.parse(in);
        }

        public Document getDocument() {
            return this.document;
        }

        public boolean getShouldExpandEntityRefs() {
            return true;
        }
        
        public boolean supportsSAX() {
            return true;
        }
    }
}