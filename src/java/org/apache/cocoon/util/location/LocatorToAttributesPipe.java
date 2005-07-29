/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.util.location;

import org.apache.cocoon.xml.AbstractXMLPipe;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * A SAX filter that adds the information available from the <code>Locator</code> as attributes.
 * The purpose of having location as attributes is to allow this information to survive transformations
 * of the document (an XSL could copy these attributes over) or conversion of SAX events to a DOM.
 * <p>
 * The location is added as 3 attributes in a specific namespace to each element.
 * <pre>
 * &lt;root xmlns:loc="http://apache.org/cocoon/location"
 *       loc:src="file://path/to/file.xml"
 *       loc:line="1" loc:column="1"&gt;
 *   &lt;foo loc:src="file://path/to/file.xml" loc:line="2" loc:column="3"/&gt;
 * &lt;/root&gt;
 * </pre>
 * <strong>Note:</strong> Although this adds a lot of information to the serialized form of the document,
 * the overhead in SAX events is not that big, as attribute names are interned, and all <code>src</code>
 * attributes point to the same string.
 * 
 * @see org.apache.cocoon.util.location.LocationAttributes
 * @version $Id$
 */
public class LocatorToAttributesPipe extends AbstractXMLPipe {
    
    private Locator locator;
    
    /**
     * Create a filter. It has to be chained to another handler to be really useful.
     */
    public LocatorToAttributesPipe() {
    }

    /**
     * Create a filter that is chained to another handler.
     * @param next the next handler in the chain.
     */
    public LocatorToAttributesPipe(ContentHandler next) {
        setContentHandler(next);
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        super.setDocumentLocator(locator);
    }
    
    public void startDocument() throws SAXException {
        super.startDocument();
        super.startPrefixMapping(LocationAttributes.PREFIX, LocationAttributes.URI);
    }
    
    public void endDocument() throws SAXException {
        endPrefixMapping(LocationAttributes.PREFIX);
        super.endDocument();
    }

    public void startElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
        // Add location attributes to the element
        super.startElement(uri, loc, raw, LocationAttributes.addLocationAttributes(locator, attrs));
    }
}
