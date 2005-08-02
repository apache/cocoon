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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A class to handle location information stored in attributes.
 * These attributes are typically setup using {@link LocatorToAttributesPipe}
 * 
 * @see LocatorToAttributesPipe
 * @version $Id$
 */
public class LocationAttributes {
    /** Prefix for the location namespace */
    public static final String PREFIX = "loc";
    /** Namespace URI for location attributes */
    public static final String URI = "http://apache.org/cocoon/location";

    /** Attribute name for the location URI */
    public static final String SRC_ATTR  = "src";
    /** Attribute name for the line number */
    public static final String LINE_ATTR = "line";
    /** Attribute name for the column number */
    public static final String COL_ATTR  = "column";

    /** Attribute qualified name for the location URI */
    public static final String Q_SRC_ATTR  = "loc:src";
    /** Attribute qualified name for the line number */
    public static final String Q_LINE_ATTR = "loc:line";
    /** Attribute qualified name for the column number */
    public static final String Q_COL_ATTR  = "loc:column";
    
    // Private constructor, we only have static methods
    private LocationAttributes() {
        // Nothing
    }
    
    /**
     * Add location attributes to a set of SAX attributes.
     * 
     * @param locator the <code>Locator</code> (can be null)
     * @param attrs the <code>Attributes</code> where locator information should be added
     * @return
     */
    public static Attributes addLocationAttributes(Locator locator, Attributes attrs) {
        if (locator == null || attrs.getIndex(URI, SRC_ATTR) != -1) {
            // No location information known, or already has it
            return attrs;
        }
        
        // Get an AttributeImpl so that we can add new attributes.
        AttributesImpl newAttrs = attrs instanceof AttributesImpl ?
            (AttributesImpl)attrs : new AttributesImpl(attrs);

        newAttrs.addAttribute(URI, SRC_ATTR, Q_SRC_ATTR, "CDATA", locator.getSystemId());
        newAttrs.addAttribute(URI, LINE_ATTR, Q_LINE_ATTR, "CDATA", Integer.toString(locator.getLineNumber()));
        newAttrs.addAttribute(URI, COL_ATTR, Q_COL_ATTR, "CDATA", Integer.toString(locator.getColumnNumber()));
        
        return newAttrs;
    }
    
    /**
     * Returns the {@link Location} pointed to by a SAX <code>Locator</code>.
     * 
     * @param locator the locator (can be null)
     * @return the location
     */
    public static Location getLocation(Locator locator) {
        if (locator == null || locator.getSystemId() == null) {
            return Location.UNKNOWN;
        }
        
        return new Location(locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
    }
    
    /**
     * Returns the {@link Location} of an element (SAX flavor).
     * 
     * @param attrs the element's attributes that hold the location information
     * @return a {@link Location} object
     */
    public static Location getLocation(Attributes attrs) {
        String src = attrs.getValue(URI, SRC_ATTR);
        if (src == null) {
            return Location.UNKNOWN;
        }
        
        return new Location(src, getLine(attrs), getColumn(attrs));
    }

    /**
     * Returns the location of an element (SAX flavor). If the location is to be kept
     * into an object built from this element, consider using {@link #getLocation(Attributes)}
     * and the {@link Locatable} interface.
     * 
     * @param attrs the element's attributes that hold the location information
     * @return a location string as defined by {@link Location#toString()}.
     */
    public static String getLocationString(Attributes attrs) {
        String src = attrs.getValue(URI, SRC_ATTR);
        if (src == null) {
            return Location.UNKNOWN_STRING;
        }
        
        return src + ":" + attrs.getValue(URI, LINE_ATTR) + ":" + attrs.getValue(URI, COL_ATTR);
    }
    
    /**
     * Returns the URI of an element (SAX flavor)
     * 
     * @param attrs the element's attributes that hold the location information
     * @return the element's URI or "<code>[unknown location]</code>" if <code>attrs</code>
     *         has no location information.
     */
    public static String getURI(Attributes attrs) {
        String src = attrs.getValue(URI, SRC_ATTR);
        return src != null ? src : Location.UNKNOWN_STRING;
    }
    
    /**
     * Returns the line number of an element (SAX flavor)
     * 
     * @param attrs the element's attributes that hold the location information
     * @return the element's line number or <code>-1</code> if <code>attrs</code>
     *         has no location information.
     */
    public static int getLine(Attributes attrs) {
        String line = attrs.getValue(URI, LINE_ATTR);
        return line != null ? Integer.parseInt(line) : -1;
    }
    
    /**
     * Returns the column number of an element (SAX flavor)
     * 
     * @param attrs the element's attributes that hold the location information
     * @return the element's column number or <code>-1</code> if <code>attrs</code>
     *         has no location information.
     */
    public static int getColumn(Attributes attrs) {
        String col = attrs.getValue(URI, COL_ATTR);
        return col != null ? Integer.parseInt(col) : -1;
    }
    
    /**
     * Returns the {@link Location} of an element (DOM flavor).
     * 
     * @param attrs the element that holds the location information
     * @return a {@link Location} object
     */
    public static Location getLocation(Element elem) {
        Attr srcAttr = elem.getAttributeNodeNS(URI, SRC_ATTR);
        if (srcAttr == null) {
            return Location.UNKNOWN;
        }
        
        return new Location(srcAttr.getValue(), getLine(elem), getColumn(elem));
    }

    /**
     * Returns the location of an element that has been processed by this pipe (DOM flavor).
     * If the location is to be kept into an object built from this element, consider using
     * {@link #getLocation(Element)} and the {@link Locatable} interface.
     * 
     * @param elem the element that holds the location information
     * @return a location string as defined by {@link Location#toString()}.
     */
    public static String getLocationString(Element elem) {
        Attr srcAttr = elem.getAttributeNodeNS(URI, SRC_ATTR);
        if (srcAttr == null) {
            return Location.UNKNOWN_STRING;
        }
        
        return srcAttr.getValue() + ":" + elem.getAttributeNS(URI, LINE_ATTR) + ":" + elem.getAttributeNS(URI, COL_ATTR);
    }
    
    /**
     * Returns the URI of an element (DOM flavor)
     * 
     * @param elem the element that holds the location information
     * @return the element's URI or "<code>[unknown location]</code>" if <code>elem</code>
     *         has no location information.
     */
    public static String getURI(Element elem) {
        Attr attr = elem.getAttributeNodeNS(URI, SRC_ATTR);
        return attr != null ? attr.getValue() : Location.UNKNOWN_STRING;
    }

    /**
     * Returns the line number of an element (DOM flavor)
     * 
     * @param elem the element that holds the location information
     * @return the element's line number or <code>-1</code> if <code>elem</code>
     *         has no location information.
     */
    public static int getLine(Element elem) {
        Attr attr = elem.getAttributeNodeNS(URI, LINE_ATTR);
        return attr != null ? Integer.parseInt(attr.getValue()) : -1;
    }

    /**
     * Returns the column number of an element (DOM flavor)
     * 
     * @param elem the element that holds the location information
     * @return the element's column number or <code>-1</code> if <code>elem</code>
     *         has no location information.
     */
    public static int getColumn(Element elem) {
        Attr attr = elem.getAttributeNodeNS(URI, COL_ATTR);
        return attr != null ? Integer.parseInt(attr.getValue()) : -1;
    }
}
