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
package org.apache.cocoon.components.xpointer;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.ProcessingException;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.io.IOException;

/**
 * A custom XPointer scheme that allows to include the content of a specific element without
 * building a DOM. The element must be specified using an absolute path reference such as
 * <tt>/html/body</tt>. Namespace prefixes within these element names are supported.
 *
 * <p>This xpointer scheme will always be succesful (thus any further xpointer parts will
 * never be executed).
 *
 * <p>The scheme name for this XPointer scheme is 'elementpath' and its namespace is
 * http://apache.org/cocoon/xpointer.
 *
 * <p>See the samples for a usage example.
 */
public class ElementPathPart implements PointerPart {
    private String expression;

    public ElementPathPart(String expression) {
        this.expression = expression;
    }

    public boolean process(XPointerContext xpointerContext) throws SAXException {
        PathInclusionPipe pipe = new PathInclusionPipe(expression, xpointerContext);
        pipe.setConsumer(xpointerContext.getXmlConsumer());
        try {
            SourceUtil.toSAX(xpointerContext.getSource(), pipe);
        } catch (IOException e) {
            throw new SAXException("Exception while trying to XInclude data: " + e.getMessage(), e);
        } catch (ProcessingException e) {
            throw new SAXException("Exception while trying to XInclude data: " + e.getMessage(), e);
        }
        return true;
    }

    public static class PathInclusionPipe extends AbstractXMLPipe {
        /** The QNames that must be matched before inclusion can start. */
        private QName[] elementPath;
        /** The current element nesting level. */
        private int level;
        /** Should we currently be including? */
        private boolean include;
        /** The element nesting level since we started inclusion, used to know when to stop inclusion. */
        private int includeLevel;

        /** The element nesting level that should currently be matched. */
        private int levelToMatch;
        private boolean done;

        public PathInclusionPipe(String expression, XPointerContext xpointerContext) throws SAXException {
            // parse the expression to an array of QName objects
            ArrayList path = new ArrayList();
            StringTokenizer tokenizer = new StringTokenizer(expression, "/");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                try {
                    path.add(QName.parse(token, xpointerContext));
                } catch (SAXException e) {
                    throw new SAXException("Error in element path xpointer expression \"" + expression + "\": " + e.getMessage());
                }
            }
            if (path.size() < 1)
                throw new SAXException("Invalid element path xpointer expression \"" + expression + "\".");

            this.elementPath = (QName[])path.toArray(new QName[0]);
            this.level = -1;
            this.include = false;
            this.levelToMatch = 0;
            this.done = false;
        }

        public void startElement(String namespaceURI, String localName, String raw, Attributes a)
                throws SAXException {
            level++;

            if (include) {
                super.startElement(namespaceURI, localName, raw, a);
                return;
            }

            if (!done && level == levelToMatch && elementPath[level].matches(namespaceURI, localName)) {
                levelToMatch++;
                if (levelToMatch == elementPath.length) {
                    include = true;
                    done = true;
                    includeLevel = level;
                }
            }
        }

        public void endElement(String uri, String loc, String raw)
                throws SAXException {
            if (include && level == includeLevel)
                include = false;

            if (include)
                super.endElement(uri, loc, raw);

            level--;
        }

        public void setDocumentLocator(Locator locator) {
            if (include)
                super.setDocumentLocator(locator);
        }

        public void startDocument()
                throws SAXException {
            if (include)
                super.startDocument();
        }

        public void endDocument()
                throws SAXException {
            if (include)
                super.endDocument();
        }

        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            // let namespace prefix alway through
            super.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(String prefix)
                throws SAXException {
            // let namespace prefix alway through
            super.endPrefixMapping(prefix);
        }

        public void characters(char c[], int start, int len)
                throws SAXException {
            if (include)
                super.characters(c, start, len);
        }

        public void ignorableWhitespace(char c[], int start, int len)
                throws SAXException {
            if (include)
                super.ignorableWhitespace(c, start, len);
        }

        public void processingInstruction(String target, String data)
                throws SAXException {
            if (include)
                super.processingInstruction(target, data);
        }

        public void skippedEntity(String name)
                throws SAXException {
            if (include)
                super.skippedEntity(name);
        }

        public void startDTD(String name, String publicId, String systemId)
                throws SAXException {
            if (include)
                super.startDTD(name, publicId, systemId);
        }

        public void endDTD()
                throws SAXException {
            if (include)
                super.endDTD();
        }

        public void startEntity(String name)
                throws SAXException {
            if (include)
                super.startEntity(name);
        }

        public void endEntity(String name)
                throws SAXException {
            if (include)
                super.endEntity(name);
        }

        public void startCDATA()
                throws SAXException {
            if (include)
                super.startCDATA();
        }

        public void endCDATA()
                throws SAXException {
            if (include)
                super.endCDATA();
        }

        public void comment(char ch[], int start, int len)
                throws SAXException {
            if (include)
                super.comment(ch, start, len);
        }

        public static class QName {
            private String namespaceURI;
            private String localName;

            public QName(String namespaceURI, String localName) {
                this.namespaceURI = namespaceURI;
                this.localName = localName;
            }

            public static QName parse(String qName, XPointerContext xpointerContext) throws SAXException {
                int pos = qName.indexOf(':');
                if (pos > 0) {
                    String prefix = qName.substring(0, pos);
                    String localName = qName.substring(pos + 1);
                    String namespaceURI = xpointerContext.prefixToNamespace(prefix);
                    if (namespaceURI == null)
                        throw new SAXException("Namespace prefix \"" + prefix + "\" not declared.");
                    return new QName(prefix, localName);
                }
                return new QName("", qName);
            }

            public String getNamespaceURI() {
                return namespaceURI;
            }

            public String getLocalName() {
                return localName;
            }

            public boolean matches(String namespaceURI, String localName) {
                return this.localName.equals(localName) && this.namespaceURI.equals(namespaceURI);
            }
        }
    }
}
