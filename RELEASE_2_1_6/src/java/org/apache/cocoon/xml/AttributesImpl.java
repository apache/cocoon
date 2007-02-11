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

/**
 * A helper Class creating SAX Attributes
 * 
 * @author <a href="mailto:volker.schmitt@basf-ag.de">Volker Schmitt</a>
 * @version CVS $Id: AttributesImpl.java,v 1.4 2004/03/05 13:03:01 bdelacretaz Exp $
 */
public class AttributesImpl extends org.xml.sax.helpers.AttributesImpl {

    /**
     * Constructor
     */
    public AttributesImpl() {
        super();
    }

    /**
     *  Constructor
     */
    public AttributesImpl(Attributes attr) {
        super(attr);
    }

	/**
	 * Add an attribute of type CDATA with empty Namespace to the end of the list.
	 *
	 * <p>For the sake of speed, this method does no checking
	 * to see if the attribute is already in the list: that is
	 * the responsibility of the application.</p>
	 *
	 * @param localName The local name.
	 * @param value The attribute value.
	 */
	public void addCDATAAttribute(String localName, String value) {
		addAttribute("", localName, localName, AttributeTypes.CDATA, value);
	}
    
	/**
	 * Add an attribute of type CDATA to the end of the list.
	 *
	 * <p>For the sake of speed, this method does no checking
	 * to see if the attribute is already in the list: that is
	 * the responsibility of the application.</p>
	 *
	 * @param uri The Namespace URI, or the empty string if
	 *        none is available or Namespace processing is not
	 *        being performed.
	 * @param localName The local name, or the empty string if
	 *        Namespace processing is not being performed.
	 * @param qName The qualified (prefixed) name, or the empty string
	 *        if qualified names are not available.
	 * @param value The attribute value.
	 */
	public void addCDATAAttribute(String uri,
                            		String localName,
                            		String qName,
                            		String value) {
		addAttribute(uri, localName, qName, AttributeTypes.CDATA, value);
	}
    
    /**
     * Remove an attribute
     */
    public void removeAttribute(String localName) {
        final int index = this.getIndex(localName);
        if ( index != -1 ) {
            this.removeAttribute(index);
        }
    }

    /**
     * Remove an attribute
     */
    public void removeAttribute(String uri, String localName) {
        final int index = this.getIndex(uri, localName);
        if ( index != -1 ) {
            this.removeAttribute(index);
        }
    }
}
  