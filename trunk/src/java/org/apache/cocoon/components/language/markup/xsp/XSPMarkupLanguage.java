/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.components.language.markup.xsp;

import java.util.Stack;

import org.apache.cocoon.components.language.markup.CocoonMarkupLanguage;
import org.apache.cocoon.components.language.markup.MarkupLanguage;
import org.apache.cocoon.components.language.programming.ProgrammingLanguage;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class implements <code>MarkupLanguage</code> for Cocoon's
 * <a href="http://cocoon.apache.org/userdocs/xsp/">XSP</a>.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:ssahuc@apache.org">Sebastien Sahuc</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: XSPMarkupLanguage.java,v 1.4 2003/12/29 14:56:20 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=MarkupLanguage
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=xsp-markup
 */
public class XSPMarkupLanguage extends CocoonMarkupLanguage implements MarkupLanguage {

    /**
     * Returns the root element for a valid XSP page: page element!
     */
    public String getRootElement() {
        return "page";
    }

    /**
     * Prepare the input source for logicsheet processing and code generation
     * with a preprocess filter.
     * The return <code>XMLFilter</code> object is the first filter on the
     * transformer chain.
     *
     * Wraps PCDATA nodes with xsp:text nodes.
     *
     * @param filename The source filename
     * @param language The target programming language
     * @return The preprocess filter
     *
     * @see XSPMarkupLanguage.PreProcessFilter
     */
    protected AbstractXMLPipe getPreprocessFilter(String filename,
                                                  AbstractXMLPipe filter,
                                                  ProgrammingLanguage language)
    {
        PreProcessFilter prefilter = new PreProcessFilter(filter, filename, language);
        prefilter.enableLogging(getLogger());
        return prefilter;
    }

//
//  Inner classes
//

    /**
     * This preprocessor wraps the PCDATA into xsp:text elements.
     * @see org.xml.sax.ContentHandler
     */
    protected class PreProcessFilter extends CocoonMarkupLanguage.PreProcessFilter {

        private Stack stack;

        public PreProcessFilter (AbstractXMLPipe filter, String filename, ProgrammingLanguage language) {
            super(filter, filename, language);
        }

        public void startDocument() throws SAXException {
            super.startDocument();
            stack = new Stack();
        }

        public void startElement (String namespaceURI, String localName,
                                  String qName, Attributes atts) throws SAXException {
            stack.push(new String[] { namespaceURI, localName, qName} );
            super.startElement(namespaceURI, localName, qName, atts);
        }

        public void endElement (String namespaceURI, String localName,
                                String qName) throws SAXException {
            stack.pop();
            super.endElement(namespaceURI, localName, qName);
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            String[] tag = (String[]) stack.peek();
            String tagURI = tag[0];
            String tagLName = tag[1];

            boolean flag = XSPMarkupLanguage.this.getURI().equals(tagURI);
            if (flag && tagLName.equals("page")) {
                // Characters after xsp:page and before first element.
                super.characters(ch, start, length);
            } else if (flag && (tagLName.equals("expr") ||
                    tagLName.equals("logic") || tagLName.equals("structure") ||
                    tagLName.equals("include"))) {
                super.characters(ch, start, length);
            } else {
                // Quote the string depending on the programming language
                String value = String.valueOf(ch, start, length);
                // Create a new element <xsp:text> that wrap the quoted PCDATA
                super.startElement(XSPMarkupLanguage.this.getURI(), "text",
                        localPrefix + ":text", new AttributesImpl());
                super.characters(value.toCharArray(), 0, value.length());
                super.endElement(XSPMarkupLanguage.this.getURI(), "text",
                        localPrefix + ":text");
            }
        }
    }
}
