/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @version CVS $Id: XSPMarkupLanguage.java,v 1.5 2004/03/08 13:58:31 cziegeler Exp $
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
