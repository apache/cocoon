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

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Location-related utility methods.
 *
 * @version $Id$
 * @since 2.1.8
 */
public class LocationUtils {
    
    private static LocationFinder[] finders = new LocationFinder[] {};
    
    /**
     * An finder or object locations
     *
     * @since 2.1.8
     */
    public interface LocationFinder {
        /**
         * Get the location of an object
         * @param obj the object for which to find a location
         * @param description and optional description to be added to the object's location
         * @return the object's location or <code>null</code> if object's class isn't handled
         *         by this finder.
         */
        Location getLocation(Object obj, String description);
    }

    private LocationUtils() {
        // Forbid instanciation
    }
    
    /**
     * Parse a location string of the form "<code><em>uri</em>:<em>line</em>:<em>column</em></code>" (e.g.
     * "<code>path/to/file.xml:3:40</code>") to a Location object. Additionally, a description may
     * also optionally be present, separated with an hyphen (e.g. "<code>foo - path/to/file.xml:3.40</code>").
     * 
     * @param text the text to parse
     * @return the location (possibly <code>null</code> if text was null or in an incorrect format)
     */
    public static LocationImpl parse(String text) throws IllegalArgumentException {
        if (text == null || text.length() == 0) {
            return null;
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
                if (text.endsWith(LocationImpl.UNKNOWN_STRING)) {
                    return LocationImpl.UNKNOWN;
                }
            }
        } catch(Exception e) {
            // Ignore: handled below
        }
        
        return LocationImpl.UNKNOWN;
    }

    /**
     * Checks if a location is known, i.e. it is not null nor equal to {@link Location#UNKNOWN}.
     * 
     * @param location the location to check
     * @return <code>true</code> if the location is known
     */
    public static boolean isKnown(Location location) {
        return location != null && !Location.UNKNOWN.equals(location);
    }

    /**
     * Checks if a location is unknown, i.e. it is either null or equal to {@link Location#UNKNOWN}.
     * 
     * @param location the location to check
     * @return <code>true</code> if the location is unknown
     */
    public static boolean isUnknown(Location location) {
        return location == null || Location.UNKNOWN.equals(location);
    }

    /**
     * Add a {@link LocationFinder} to the list of finders that will be queried for an object's
     * location by {@link #getLocation(Object, String)}.
     * 
     * @param finder the location finder to add
     */
    public static void addFinder(LocationFinder finder) {
        if (finder == null) {
            return;
        }

        synchronized(LocationUtils.class) {
            int length = finders.length;
            LocationFinder[] newFinders = new LocationFinder[length + 1];
            System.arraycopy(finders, 0, newFinders, 0, length);
            newFinders[length] = finder;
            finders = newFinders;
        }
    }
    
    /**
     * Get the location of an object. Some well-known located classes built in the JDK are handled
     * by this method. Handling of other located classes can be handled by adding new location finders.
     * 
     * @param obj the object of which to get the location
     * @return the object's location, or {@link Location#UNKNOWN} if no location could be found
     */
    public static Location getLocation(Object obj) {
        return getLocation(obj, null);
    }
    
    /**
     * Get the location of an object. Some well-known located classes built in the JDK are handled
     * by this method. Handling of other located classes can be handled by adding new location finders.
     * 
     * @param obj the object of which to get the location
     * @param description an optional description of the object's location, used if a Location object
     *        has to be created.
     * @return the object's location, or {@link Location#UNKNOWN} if no location could be found
     */
    public static Location getLocation(Object obj, String description) {
        if (obj instanceof Locatable) {
            return ((Locatable)obj).getLocation();
        }
        
        // Check some well-known locatable exceptions
        if (obj instanceof SAXParseException) {
            SAXParseException spe = (SAXParseException)obj;
            if (spe.getSystemId() != null) {
                return new LocationImpl(description, spe.getSystemId(), spe.getLineNumber(), spe.getColumnNumber());
            } else {
                return Location.UNKNOWN;
            }
        }
        
        if (obj instanceof TransformerException) {
            TransformerException ex = (TransformerException)obj;
            SourceLocator locator = ex.getLocator();
            if (locator != null && locator.getSystemId() != null) {
                return new LocationImpl(description, locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
            } else {
                return Location.UNKNOWN;
            }
        }
        
        if (obj instanceof Locator) {
            Locator locator = (Locator)obj;
            if (locator.getSystemId() != null) {
                return new LocationImpl(description, locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
            } else {
                return Location.UNKNOWN;
            }
        }

        Location result;
        LocationFinder[] currentFinders = finders;
        for (int i = 0; i < currentFinders.length; i++) {
            result = currentFinders[i].getLocation(obj, description);
            if (result != null) {
                return result;
            }
        }
        
        return Location.UNKNOWN;
    }
}
