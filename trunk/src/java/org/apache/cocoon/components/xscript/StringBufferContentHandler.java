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
package org.apache.cocoon.components.xscript;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;


/**
 * A <code>ContentHandler</code> that accumulates the SAX stream into
 * a <code>StringBuffer</code> object.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: StringBufferContentHandler.java,v 1.1 2003/03/09 00:09:27 pier Exp $
 * @since August 30, 2001
 */
public class StringBufferContentHandler extends DefaultHandler {
    private static Object marker = new Object();

    private Stack namespaces = new Stack();
    private StringBuffer stringBuffer;

    public StringBufferContentHandler(StringBuffer stringBuffer) {
        this.stringBuffer = stringBuffer;
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        namespaces.push(new NPU(prefix, uri));
    }

    public void endPrefixMapping(String prefix)
            throws SAXException {
        namespaces.pop();
    }

    public void startElement(String uri, String loc, String qName, Attributes a)
            throws SAXException {
        int lastNamespaceIndex = 0;

        lastNamespaceIndex = namespaces.size() - 1;
        while (lastNamespaceIndex >= 0
                && namespaces.elementAt(lastNamespaceIndex) != marker) {
            lastNamespaceIndex--;
        }

        if (lastNamespaceIndex < 0) {
            lastNamespaceIndex = 0;
        } else if (namespaces.elementAt(lastNamespaceIndex) == marker) {
            lastNamespaceIndex++;
        }

        namespaces.push(marker);

        stringBuffer.append("<").append(qName);

        for (int i = 0, len = a.getLength(); i < len; i++) {
            // Check if the attribute is a namespace declaration. Some
            // parsers (Xerces) sometimes pass the namespace declaration
            // as an attribute. We need to catch this case so that we
            // don't end up generating the namespace declaration twice.
            String attrName = a.getQName(i);

            if (attrName.startsWith("xmlns:")) {
                // We have a namespace declaration
                String name = a.getLocalName(i);

                // Check whether this namespace has been already declared
                boolean found = false;
                for (int j = namespaces.size() - 1;
                     j >= lastNamespaceIndex;
                     j--) {
                    Object obj = namespaces.elementAt(j);
                    if (obj == marker) {
                        continue;
                    }
                    NPU npu = (NPU) obj;

                    if (name.equals(npu.prefix)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    namespaces.push(new NPU(name, a.getValue(i)));
                }
            } else {
                // Normal attribute
                stringBuffer.append(" ").append(a.getQName(i)).append("=\"");
                escape(stringBuffer, a.getValue(i));
                stringBuffer.append("\"");
            }
        }

        if (namespaces.size() != 0) {
            for (int i = namespaces.size() - 1; i >= lastNamespaceIndex; i--) {
                Object obj = namespaces.elementAt(i);
                if (obj == marker) {
                    continue;
                }
                NPU npu = (NPU) obj;
                stringBuffer.append(" xmlns:").append(npu.prefix).append("=\"").append(npu.uri).append("\"");
            }
        }
        stringBuffer.append(">");
    }

    public void endElement(String uri, String loc, String qName)
            throws SAXException {
        stringBuffer.append("</").append(qName).append(">");

        Object obj;
        do {
            obj = namespaces.pop();
        } while (obj != marker);
    }

    public void characters(char ch[], int start, int len)
            throws SAXException {
        escape(stringBuffer, ch, start, len);
    }


    /**
     * Copies string into buffer and
     * escapes all '<', '&' and '>' chars in the string with
     * corresponding entities.
     */
    private static void escape(StringBuffer buffer, String s) {
        char[] ch = s.toCharArray();
        escape(buffer, ch, 0, ch.length);
    }

    /**
     * Copies characters from the char array into buffer and
     * escapes all '<', '&' and '>' chars with corresponding
     * entities.
     */
    private static void escape(StringBuffer buffer, char ch[], int start, int len) {
        for (int i = start; i < start + len; i++) {
            switch (ch[i]) {
                case '<':
                    buffer.append("&lt;");
                    break;
                case '&':
                    buffer.append("&amp;");
                    break;
                case '>':
                    buffer.append("&gt;");
                    break;
                default:
                    buffer.append(ch[i]);
                    break;
            }
        }
    }
}

class NPU {
    public String prefix;
    public String uri;

    NPU(String prefix, String uri) {
        this.prefix = prefix;
        this.uri = uri;
    }

    public String toString() {
        return this.prefix + "=" + this.uri;
    }
}
