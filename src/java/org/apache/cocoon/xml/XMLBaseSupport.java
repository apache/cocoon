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
import org.xml.sax.SAXException;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Stack;

/**
 * Helper class for handling xml:base attributes.
 *
 * <p>Usage:
 * <ul>
 *  <li>set location of the containing document by calling {@link #setDocumentLocation}.
 *      This is usually done when getting setDocumentLocator SAX event.
 *  <li>forward each startElement and endElement event to this object.
 *  <li>to resolve a relative URL against the current base, call {@link #makeAbsolute}.
 * </ul>
 *
 * Internally this class makes use of the java.net.URL class, and hence only supports
 * URL schemes recognized by the JDK.
 *
 * <p>External entities are not yet taken into account when determing the current base.
 */
public class XMLBaseSupport {
    public static final String XMLBASE_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";
    public static final String XMLBASE_ATTRIBUTE = "base";

    /** Increased on each startElement, decreased on each endElement. */
    private int level = 0;
    /**
     * The stack contains an instance of {@link BaseInfo} for each XML element
     * that contained an xml:base attribute (not for the other elements).
     */
    private Stack bases = new Stack();

    public void setDocumentLocation(String loc) throws MalformedURLException {
        // -2 is used as level to avoid this BaseInfo to be ever popped of the stack
        bases.push(new BaseInfo(new URL(loc), -2));
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
        level++;
        String base = attrs.getValue(XMLBASE_NAMESPACE_URI, XMLBASE_ATTRIBUTE);
        if (base != null) {
            URL baseUrl;
            URL currentBaseUrl = getCurrentBase();
            try {
                if (currentBaseUrl != null)
                    baseUrl = new URL(currentBaseUrl, base);
                else
                    baseUrl = new URL(base);
            } catch (MalformedURLException e) {
                throw new SAXException("Problem with URL in xml:base attribute: \"" + base + "\": " + e.toString());
            }
            bases.push(new BaseInfo(baseUrl, level));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        if (getCurrentBaseLevel() == level)
            bases.peek();
        level--;
    }

    /**
     * Makes the given path absolute based on the current base URL.
     * @param spec any URL (relative or absolute, containing a scheme or not)
     */
    public String makeAbsolute(String spec) throws MalformedURLException {
        if (containsScheme(spec))
            return spec;

        URL currentBaseUrl = getCurrentBase();
        if (currentBaseUrl != null)
            return new URL(currentBaseUrl, spec).toExternalForm();
        else
            return new URL(spec).toExternalForm();
    }

    private URL getCurrentBase() {
        if (bases.size() > 0) {
            BaseInfo baseInfo = (BaseInfo)bases.peek();
            return baseInfo.getUrl();
        }
        return null;
    }

    private int getCurrentBaseLevel() {
        if (bases.size() > 0) {
            BaseInfo baseInfo = (BaseInfo)bases.peek();
            return baseInfo.getLevel();
        }
        return -1;
    }

    /**
     * Returns true if the specified path contains a scheme specification part.
     */
    private boolean containsScheme(String path) {
        return (path.indexOf(':') < path.indexOf('/'));
    }

    private static final class BaseInfo {
        private URL url;
        private int level;

        public BaseInfo(URL url, int level) {
            this.url = url;
            this.level = level;
        }

        public URL getUrl() {
            return url;
        }

        public int getLevel() {
            return level;
        }
    }
}
