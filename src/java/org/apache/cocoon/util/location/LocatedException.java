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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avalon.framework.CascadingException;
import org.apache.cocoon.util.ExceptionUtils;

/**
 * A cascading and located <code>Exception</code>. It is also {@link MultiLocatable} to easily build
 * stack traces.
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class LocatedException extends CascadingException implements LocatableException, MultiLocatable {

    private List locations;

    public LocatedException(String message) {
        this(message, null, null);
    }
    
    public LocatedException(String message, Throwable cause) {
        this(message, cause, null);
    }
    
    public LocatedException(String message, Location location) {
        this(message, null, location);
    }
    
    public LocatedException(String message, Throwable cause, Location location) {
        super(message, cause);
        addCauseLocations(this, cause);
        addLocation(location);
    }
    
    /**
     * Add to the location stack all locations of an exception chain. This allows to have all possible
     * location information in the stacktrace, as some exceptions like SAXParseException don't output
     * their location in printStackTrace().
     * <p>
     * Traversal of the call chain stops at the first <code>Locatable</code> exception which is supposed
     * to handle the loction of its causes by itself.
     * <p>
     * This method is static as a convenience for {@link LocatedRuntimeException other implementations}
     * of locatable exceptions.
     * 
     * @param self the current locatable exception
     * @param cause the cause of <code>self</code>
     */
    public static void addCauseLocations(MultiLocatable self, Throwable cause) {
        if (cause == null || cause instanceof Locatable) {
            // Locatable handles its location itself
            return;
        }
        // Add parent location first
        addCauseLocations(self, ExceptionUtils.getCause(cause));
        // then ourselve's
        Location loc = ExceptionUtils.getLocation(cause);
        if (loc != null) {
            loc = new LocationImpl("[cause location]", loc.getURI(), loc.getLineNumber(), loc.getColumnNumber());
            self.addLocation(loc);
        }
    }

    public Location getLocation() {
        return locations == null ? null : (Location)locations.get(0);
    }

    public List getLocations() {
        return locations == null ? Collections.EMPTY_LIST : locations;
    }

    public String getRawMessage() {
        return super.getMessage();
    }

    /**
     * Standard way of building the message of a {@link LocatableException}, as a Java-like
     * stack trace of locations.
     * 
     * @param message the exception's message, given by <code>super.getMessage()</code> (can be null)
     * @param locations the location list (can be null)
     * 
     * @return the message, or <code>null</code> no message and locations were given.
     */
    public static String getMessage(String message, List locations) {
        if (locations == null || locations.isEmpty()) {
            return message;
        }

        // Produce a Java-like stacktrace with locations
        StringBuffer buf = message == null ? new StringBuffer() : new StringBuffer(message);
        for (int i = 0; i < locations.size(); i++) {
            buf.append("\n\tat ").append(locations.get(i));
        }
        return buf.toString();
    }

    public String getMessage() {
        return getMessage(super.getMessage(), locations);
    }
    
    public void addLocation(Location loc) {
        if (loc == null || loc.equals(Location.UNKNOWN))
            return;

        if (locations == null) {
            this.locations = new ArrayList(1); // Start small
        }
        locations.add(LocationImpl.get(loc));
    }
}
