/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.cocoon.xml.util.NamespacesTable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A SAX filter that strips out redundant namespace declarations.
 *
 * <p>
 * It handles both duplicate declarations (i.e. a namespace already declared by a
 * parent element) and empty namespaces scopes (i.e. start/stopPrefixMapping with
 * no element inbetween) that can be produced by some components (e.g. JXTG or
 * BrowserUpdateTransformer). Such empty scopes confuse the Xalan serializer which
 * then produces weird namespace declarations (<code>xmlns:%@$#^@#="%@$#^@#"</code>).
 *
 * <p>
 * This is a the most simple use of {@link NamespacesTable}.
 *
 * @version $Id$
 */
public class RedundantNamespacesFilter extends AbstractXMLPipe {

    /** Layered storage for all namespace declarations */
    private NamespacesTable ns = new NamespacesTable();

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
        // Just declare it: duplicate declarations are ignorede by NamespacesTable
        ns.addDeclaration(prefix, uri);
    }

    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
        // Declare namespaces for this scope, if any
        ns.enterScope(this.contentHandler);
        super.startElement(uri, loc, raw, a);
    }

    public void endElement(String uri, String loc, String raw) throws SAXException {
        super.endElement(uri, loc, raw);
        ns.leaveScope(this.contentHandler);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        ns.removeDeclaration(prefix);
    }
}
