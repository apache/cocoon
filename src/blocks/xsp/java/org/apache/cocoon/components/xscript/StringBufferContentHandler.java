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
 * @version CVS $Id: StringBufferContentHandler.java,v 1.1 2004/03/10 12:58:08 stephan Exp $
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
                if ("".equals(npu.prefix)) {
                    // Default namespace
                    stringBuffer.append(" xmlns").append("=\"").append(npu.uri).append("\"");
                } else {
                    stringBuffer.append(" xmlns:").append(npu.prefix).append("=\"").append(npu.uri).append("\"");
                }
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
