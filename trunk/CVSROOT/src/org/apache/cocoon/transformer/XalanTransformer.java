/*-- $Id: XalanTransformer.java,v 1.13 2000-10-20 21:39:42 greenrd Exp $ --

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
 * @version $Revision: 1.13 $ $Date: 2000-10-20 21:39:42 $
 */

public class XalanTransformer extends AbstractActor implements Transformer, Status {

    Parser parser;

    public void init(Director director) {
        super.init(director);
        this.parser = (Parser) director.getActor("parser");
    }

    public Document transform(Document in, String inBase, Document sheet,
        String sheetBase, Document out, Dictionary params)
    throws Exception {
        XSLTProcessor processor = XSLTProcessorFactory.getProcessor(new XMLParser(parser));

        Enumeration enum = params.keys();
        while (enum.hasMoreElements()) {
            String name = (String) enum.nextElement();
			String value = (String)params.get(name);
           	processor.setStylesheetParam(name,processor.createXString(value));
        }

        XSLTInputSource i = new XSLTInputSource(in);
        // inBase needed for document function with 2 args to resolve correctly
        i.setSystemId(inBase);
        XSLTInputSource s = new XSLTInputSource(sheet);
        s.setSystemId(sheetBase);
        XSLTResultTarget o = new XSLTResultTarget(out);
        processor.process(i, s, o);
        return out;
    }

    public String getStatus() {
        return "Xalan XSLT Processor";
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
            this.document = this.parser.parse(in, false);

            // The Xalan stylesheet is normally built from SAX events,
            // so if a DocumentHandler is specified, we need to produce
            // SAX events from the DOM tree.
            if (m_docHandler != null) {
                (new TreeWalker(m_docHandler)).traverse(this.document);

                // Note that when cocoon transitions to being more SAX based,
                // this function will be called recursivly while the parser is
                // still in the middle of a parse, and thus the parser will have
                // created on the fly (or perhaps cloned) since the Xerces parser
                // is not (to my knowledge) reentrant.
            }
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
