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
package org.apache.cocoon.xml;

import org.xml.sax.Attributes;

/**
 * A helper Class creating SAX Attributes
 * 
 * @author <a href="mailto:volker.schmitt@basf-ag.de">Volker Schmitt</a>
 * @version CVS $Id: AttributesImpl.java,v 1.3 2004/01/29 11:13:16 cziegeler Exp $
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
  