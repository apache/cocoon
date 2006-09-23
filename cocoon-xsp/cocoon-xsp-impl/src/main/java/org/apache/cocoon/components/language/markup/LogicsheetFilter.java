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
package org.apache.cocoon.components.language.markup;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.Map;

/**
 * This filter listen for source SAX events, and registers all declared
 * namespaces into a <code>Map</code> object.
 *
 * @see org.xml.sax.XMLFilter
 * @see org.xml.sax.ContentHandler
 * @version $Id$
 */
public class LogicsheetFilter extends XMLFilterImpl {

    protected Locator locator;

    private Map namespaces;

    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * @param locator An object that can return the location of any SAX
     *                document event.
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * The filter needs an initialized <code>Map</code> object where it
     * can store the found namespace declarations.
     * @param originalNamepaceURIs a initialized <code>Map</code> instance.
     */
    public void setNamespaceMap(Map originalNamepaceURIs) {
        this.namespaces = originalNamepaceURIs;
    }

    public void setParent(XMLReader reader) {
        super.setParent(reader);
        reader.setContentHandler(this);
    }

    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (namespaces != null) {
            namespaces.put(uri, prefix);
        }
        super.startPrefixMapping(prefix, uri);
    }
}
