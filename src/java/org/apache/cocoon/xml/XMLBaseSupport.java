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
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.avalon.framework.logger.Logger;

import java.util.Stack;
import java.util.Collections;
import java.io.IOException;

/**
 * Helper class for handling xml:base attributes.
 *
 * <p>Usage:
 * <ul>
 *  <li>set location of the containing document by calling {@link #setDocumentLocation(String)}.
 *      This is usually done when getting setDocumentLocator SAX event.
 *  <li>forward each startElement and endElement event to this object.
 *  <li>to resolve a relative URL against the current base, call {@link #makeAbsolute(String)}.
 * </ul>
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
    private SourceResolver resolver;
    private Logger logger;

    public XMLBaseSupport(SourceResolver resolver, Logger logger) {
        this.resolver = resolver;
        this.logger = logger;
    }

    public void setDocumentLocation(String loc) throws SAXException {
        // -2 is used as level to avoid this BaseInfo to be ever popped of the stack
        bases.push(new BaseInfo(loc, -2));
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
        level++;
        String base = attrs.getValue(XMLBASE_NAMESPACE_URI, XMLBASE_ATTRIBUTE);
        if (base != null) {
            Source baseSource = null;
            String baseUrl;
            try {
                baseSource = resolve(getCurrentBase(), base);
                baseUrl = baseSource.getURI();
            } finally {
                if (baseSource != null) {
                    resolver.release(baseSource);
                }
            }
            bases.push(new BaseInfo(baseUrl, level));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        if (getCurrentBaseLevel() == level)
            bases.pop();
        level--;
    }

    /**
     * Warning: do not forget to release the source returned by this method.
     */
    private Source resolve(String baseURI, String location) throws SAXException {
        try {
            Source source;
            if (baseURI != null) {
                source = resolver.resolveURI(location, baseURI, Collections.EMPTY_MAP);
            } else {
                source = resolver.resolveURI(location);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("XMLBaseSupport: resolved location " + location +
                             " against base URI " + baseURI + " to " + source.getURI());
            }
            return source;
        } catch (IOException e) {
            throw new SAXException("XMLBaseSupport: problem resolving uri.", e);
        }
    }

    /**
     * Makes the given path absolute based on the current base URL. Do not forget to release
     * the returned source object!
     * @param spec any URL (relative or absolute, containing a scheme or not)
     */
    public Source makeAbsolute(String spec) throws SAXException {
        return resolve(getCurrentBase(), spec);
    }

    private String getCurrentBase() {
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

    private static final class BaseInfo {
        private String url;
        private int level;

        public BaseInfo(String url, int level) {
            this.url = url;
            this.level = level;
        }

        public String getUrl() {
            return url;
        }

        public int getLevel() {
            return level;
        }
    }
}
