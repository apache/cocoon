/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.xml;

import java.util.Enumeration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A SAX filter that strips out redundant namespace declarations.
 * <p>
 * It handles both duplicate declarations (i.e. a namespace already declared by a
 * parent element) and empty namespaces scopes (i.e. start/stopPrefixMapping with
 * no element inbetween) that can be produced by some components (e.g. JXTG or
 * BrowserUpdateTransformer). Such empty scopes confuse the Xalan serializer which
 * then produces weird namespace declarations (<code>xmlns:%@$#^@#="%@$#^@#"</code>).
 * 
 * @version CVS $Id$
 */
public class RedundantNamespacesFilter extends AbstractXMLPipe {
    
    /** Layered storage for all namespace declarations */
    private NamespaceSupport ns = new NamespaceSupport();
    
    /**
     * No-arg constructor. Requires an explicit call to
     * <code>setConsumer()</code>.
     */
    public RedundantNamespacesFilter() {
        // Nothing
    }

    /**
     * Creates a filter directly linked to its consumer
     * 
     * @param consumer
     *            the SAX stream consumer
     */
    public RedundantNamespacesFilter(XMLConsumer consumer) {
        setConsumer(consumer);
    }
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (!uri.equals(ns.getURI(prefix))) {
            // New declaration: store it
            ns.declarePrefix(prefix, uri);
        }
    }

    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
        // Declare namespaces for this scope, if any
        Enumeration prefixes = ns.getDeclaredPrefixes();
        while (prefixes.hasMoreElements()) {
            String prefix = (String) prefixes.nextElement();
            super.startPrefixMapping(prefix, ns.getURI(prefix));
        }
        ns.pushContext();
        super.startElement(uri, loc, raw, a);
    }

    public void endElement(String uri, String loc, String raw) throws SAXException {
        super.endElement(uri, loc, raw);
        ns.popContext();
        // Undeclare namespaces for this scope, if any
        Enumeration prefixes = ns.getDeclaredPrefixes();
        while (prefixes.hasMoreElements()) {
            super.endPrefixMapping((String) prefixes.nextElement());
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        // Ignore, this is handled in endElement()
    }
}
