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
 *  <li>set location of the containing document by calling {@link #setDocumentLocation}.
 *      This is usually done when getting setDocumentLocator SAX event.
 *  <li>forward each startElement and endElement event to this object.
 *  <li>to resolve a relative URL against the current base, call {@link #makeAbsolute}.
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
