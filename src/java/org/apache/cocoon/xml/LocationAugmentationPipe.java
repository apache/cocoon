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
package org.apache.cocoon.xml;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A pipe that augments each element in the XML stream 
 * with a location attribute that holds its Locator information.
 * 
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id:
 */
public class LocationAugmentationPipe extends AbstractXMLPipe implements XMLPipe {

    private static final String URI = "";
    
    public static final String LOCATION_ATTR = "location";
    public static final String UNKNOWN_LOCATION = "Unknown";
    
    private Locator m_locator;

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        m_locator = locator;
        super.setDocumentLocator(locator);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
        throws SAXException {
        
        String location = getLocationString();
        AttributesImpl atts = new AttributesImpl(a);
        atts.addAttribute(URI,LOCATION_ATTR,LOCATION_ATTR,AttributeTypes.CDATA,location);
        
        super.startElement(uri, loc, raw, atts);
    }
    
    /**
     * Returns a string showing the current system ID, line number and column number.
     *
     * @return a <code>String</code> value
     */
    private String getLocationString() {
        if(m_locator == null) {
            return UNKNOWN_LOCATION;
        } else {
            final int columnNumber = m_locator.getColumnNumber();
            return
                m_locator.getSystemId() + ":"
                + m_locator.getLineNumber()
                + (columnNumber >= 0 ? (":" + columnNumber) : "");
        }
    }
}
