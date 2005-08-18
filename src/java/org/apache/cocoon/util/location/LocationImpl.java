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

import java.io.Serializable;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.commons.lang.ObjectUtils;
import org.xml.sax.Locator;

/**
 * A simple immutable and serializable implementation of {@link Location}.
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class LocationImpl implements Location, Serializable {
    private final String uri;
    private final int line;
    private final int column;
    private final String description;
    
    // Package private: outside this package, use Location.UNKNOWN.
    static final LocationImpl UNKNOWN = new LocationImpl(null, null);

    /**
     * The string representation of an unknown location: "<code>[unknown location]</code>".
     */
    public static final String UNKNOWN_STRING = "[unknown location]";

    /**
     * Build a location for a given URI, with unknown line and column numbers.
     * 
     * @param uri the resource URI
     */
    public LocationImpl(String description, String uri) {
        this(description, uri, -1, -1);
    }

    /**
     * Build a location for a given URI and line and columb numbers.
     * 
     * @param uri the resource URI
     * @param line the line number (starts at 1)
     * @param column the column number (starts at 1)
     */
    public LocationImpl(String description, String uri, int line, int column) {
        if (uri == null || uri.length() == 0) {
            this.uri = null;
            this.line = -1;
            this.column = -1;
        } else {
            this.uri = uri;
            this.line = line;
            this.column = column;
        }
        
        if (description != null && description.length() == 0) {
            description = null;
        }
        this.description = description;
    }
    
    /**
     * Copy constructor.
     * 
     * @param location the location to be copied
     */
    public LocationImpl(Location location) {
        this(location.getDescription(), location.getURI(), location.getLineNumber(), location.getColumnNumber());
    }
    
    /**
     * Obtain a <code>LocationImpl</code> from a {@link Location}. If <code>location</code> is
     * alredy a <code>LocationImpl</code>, it is returned, otherwise it is copied.
     * <p>
     * This method is useful when an immutable and serializable location is needed, such as in locatable
     * exceptions.
     * 
     * @param location the location
     * @return an immutable and serializable version of <code>location</code>
     */
    public static LocationImpl get(Location location) {
        if (location instanceof LocationImpl) {
            return (LocationImpl)location;
        } else if (location == null) {
            return UNKNOWN;
        } else {
            return new LocationImpl(location);
        }
    }

    /**
     * Parse a location string of the form "<code><em>uri</em>:<em>line</em>:<em>column</em></code>" (e.g.
     * "<code>path/to/file.xml:3:40</code>") to a Location object.
     * 
     * @param text the text to parse
     * @return the location (possibly UNKNOWN if text was null or in an incorrect format)
     */
    public static LocationImpl get(String text) throws IllegalArgumentException {
        if (text == null || text.length() == 0) {
            return UNKNOWN;
        }

        // Do we have a description?
        String description;
        int uriStart = text.lastIndexOf(" - "); // lastIndexOf to allow the separator to be in the description
        if (uriStart > -1) {
            description = text.substring(0, uriStart);
            uriStart += 3; // strip " - "
        } else {
            description = null;
            uriStart = 0;
        }
        
        try {
            int colSep = text.lastIndexOf(':');
            if (colSep > -1) {
                int column = Integer.parseInt(text.substring(colSep + 1));
                
                int lineSep = text.lastIndexOf(':', colSep - 1);
                if (lineSep > -1) {
                    int line = Integer.parseInt(text.substring(lineSep + 1, colSep));
                    return new LocationImpl(description, text.substring(uriStart, lineSep), line, column);
                }
            } else {
                // unkonwn?
                if (text.endsWith(UNKNOWN_STRING)) {
                    return new LocationImpl(description, null);
                }
            }
        } catch(Exception e) {
            // Ignore: handled below
        }
        
        return UNKNOWN;
    }

    /**
     * Returns the {@link Location} pointed to by a SAX <code>Locator</code>.
     * 
     * @param locator the locator (can be null)
     * @param description a description for the location (can be null)
     * @return the location (possibly UNKNOWN)
     */
    public static LocationImpl get(Locator locator, String description) {
        if (locator == null || locator.getSystemId() == null) {
            return UNKNOWN;
        }
        
        return new LocationImpl(description, locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
    }
    
    /**
     * Returns the {@link Location} of an Avalon <code>Configuration</code> object.
     * 
     * 
     */
    public static LocationImpl get(Configuration config) {
        if (config == null) {
            return UNKNOWN;
        }
        
        // Why in hell is "if (config instanceof Locatable)" producing a compilation error???
        Object obj = config;
        // We may have a locatable implementation of configuration
        if (obj instanceof Locatable) {
            return get(((Locatable)obj).getLocation());
        }
        
        String locString = config.getLocation();
        return get(locString);
    }
    
    /**
     * Get the description of this location
     * 
     * @return the description (can be <code>null</code>)
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Get the URI of this location
     * 
     * @return the URI (<code>null</code> if unknown).
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * Get the line number of this location
     * 
     * @return the line number (<code>-1</code> if unknown)
     */
    public int getLineNumber() {
        return this.line;
    }
    
    /**
     * Get the column number of this location
     * 
     * @return the column number (<code>-1</code> if unknown)
     */
    public int getColumnNumber() {
        return this.column;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Location) {
            Location other = (Location)obj;
            return this.line == other.getLineNumber() && this.column == other.getColumnNumber()
                   && ObjectUtils.equals(this.uri, other.getURI())
                   && ObjectUtils.equals(this.description, other.getDescription());
        }
        
        return false;
    }
    
    public int hashCode() {
        int hash = line ^ column;
        if (uri != null) hash ^= uri.hashCode();
        if (description != null) hash ^= description.hashCode();
        
        return hash;
    }
    
    public String toString() {
        return toString(this);
    }
    
    /**
     * Builds a string representation of a location, in the
     * "<code><em>descripton</em> - <em>uri</em>:<em>line</em>:<em>column</em></code>"
     * format (e.g. "<code>path/to/file.xml:3:40</code>"). For {@link Location#UNKNOWN an unknown location}, returns
     * {@link #UNKNOWN_STRING}.
     * 
     * @return the string representation
     */
    public static String toString(Location location) {
        StringBuffer result = new StringBuffer();

        String description = location.getDescription();
        if (description != null) {
            result.append(description).append(" - ");
        }

        String uri = location.getURI();
        if (uri != null) {
            result.append(uri).append(':').append(location.getLineNumber()).append(':').append(location.getColumnNumber());
        } else {
            result.append(UNKNOWN_STRING);
        }
        
        return result.toString();
    }
    
    /**
     * Ensure serialized unknown location resolve to {@link Location#UNKNOWN}.
     */
    private Object readResolve() {
        return this.equals(Location.UNKNOWN) ? Location.UNKNOWN : this;
    }
}
