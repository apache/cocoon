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

/**
 * A location in a resource. The location is composed of the URI of the resource, and 
 * the line and column numbers within that resource (when available).
 * <p>
 * Locations are mostly used by {@link Locatable}s objects.
 * 
 * @version $Id$
 */
public class Location implements Serializable {
    private final String uri;
    private final int line;
    private final int column;
    private transient String stringValue;
    
    /**
     * Constant for unknown locations.
     */
    public static final Location UNKNOWN = new Location();
    
    /**
     * The string representation of an {@link #UNKNOWN unknown location}: "<code>[unknown location]</code>".
     */
    public static final String UNKNOWN_STRING = "[unknown location]";

    private Location() {
        // Null location
        uri = "";
        line = -1;
        column = -1;
        stringValue = UNKNOWN_STRING;
    }
    
    /**
     * Build a location for a given URI, with unknown line and column numbers.
     * 
     * @param uri the resource URI
     */
    public Location(String uri) {
        this(uri, -1, -1);
    }

    /**
     * Build a location for a given URI and line and columb numbers.
     * 
     * @param uri the resource URI
     * @param line the line number (starts at 1)
     * @param column the column number (starts at 1)
     */
    public Location(String uri, int line, int column) {
        this.uri = uri;
        this.line = line;
        this.column = column;
    }
    
    /**
     * Parse a location string of the form "<code><em>uri</em>:<em>line</em>:<em>column</em></code>" (e.g.
     * "<code>path/to/file.xml:3:40</code>") to a Location object.
     * 
     * @param text the text to parse
     * @return the location
     */
    public static Location parse(String text) throws Exception {
        if (text == null || text.equals(UNKNOWN_STRING)) {
            return UNKNOWN;
        }

        try {
            int colSep = text.lastIndexOf(':');
            if (colSep > -1) {
                int column = Integer.parseInt(text.substring(colSep + 1));
                
                int lineSep = text.lastIndexOf(':', colSep - 1);
                if (lineSep > -1) {
                    int line = Integer.parseInt(text.substring(lineSep + 1, colSep));
                    return new Location(text.substring(0, lineSep), line, column);
                }
            }
        } catch(Exception e) {
            // Ignore: we throw another one below
        }
        
        throw new IllegalArgumentException("Invalid location string: " + text);
    }
    
    /**
     * Get the URI of this location
     * 
     * @return the URI (empty string if unknown).
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * Get the line number of this location
     * 
     * @return the line number (<code>-1</code> if unknown)
     */
    public int getLine() {
        return this.line;
    }
    
    /**
     * Get the column number of this location
     * 
     * @return the column number (<code>-1</code> if unknown)
     */
    public int getColumn() {
        return this.column;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Location) {
            Location other = (Location)obj;
            return this.line == other.line && other.column == other.column && this.uri.equals(other.uri);
        }
        
        return false;
    }
    
    public int hashCode() {
        return uri.hashCode() ^ line ^ column;
    }
    
    /**
     * Builds a string representation of the location, in the "<code><em>uri</em>:<em>line</em>:<em>column</em></code>"
     * format (e.g. "<code>path/to/file.xml:3:40</code>"). For {@link #UNKNOWN an unknown location}, returns
     * {@link #UNKNOWN_STRING}.
     * 
     * @return the string representation
     */
    public String toString() {
        if (stringValue == null) {
            stringValue = uri + ":" + Integer.toString(line) + ":" + Integer.toString(column);
        }
        return stringValue;
    }
    
    /**
     * Ensure serialized unknown location resolve to {@link UNKNOWN}.
     */
    private Object readResolve() {
        return this.equals(UNKNOWN) ? UNKNOWN : this;
    }
}
