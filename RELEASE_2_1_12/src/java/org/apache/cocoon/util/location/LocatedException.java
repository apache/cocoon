/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.exception.NestableException;

/**
 * A cascading and located <code>Exception</code>. It is also {@link MultiLocatable} to easily build
 * stack traces.
 *
 * @since 2.1.8
 * @version $Id$
 */
public class LocatedException extends NestableException
                              implements LocatableException, MultiLocatable {

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
        ensureCauseChainIsSet(cause);
        addCauseLocations(this, cause);
        addLocation(location);
    }

    private static Method INIT_CAUSE_METHOD = null;
    static {
        try {
            INIT_CAUSE_METHOD = Throwable.class.getMethod("initCause", new Class[] { Throwable.class} );
        } catch(Exception e) {
            // JDK < 1.4: ignore
        }
    }

    /**
     * Crawl the cause chain and ensure causes are properly set using "initCause" on JDK >= 1.4.
     * This is needed because some exceptions (e.g. SAXException) don't have a getCause() that is
     * used to print stacktraces.
     */
    public static void ensureCauseChainIsSet(Throwable thr) {
        if (INIT_CAUSE_METHOD == null) {
            return;
        }

        // Loop either until null or encountering exceptions that use this method.
        while (thr != null && !(thr instanceof LocatedRuntimeException) && !(thr instanceof LocatedException)) {
            Throwable parent = ExceptionUtils.getCause(thr);
            if (parent != null) {
                try {
                    INIT_CAUSE_METHOD.invoke(thr, new Object[]{ parent });
                } catch (Exception e) {
                    // can happen if parent already set on exception
                }
            }
            thr = parent;
        }
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
     * @param cause a cause of <code>self</code>
     */
    public static void addCauseLocations(MultiLocatable self, Throwable cause) {
        if (cause == null || cause instanceof Locatable) {
            // Locatable handles its location itself
            return;
        }

        // Add parent location first
        addCauseLocations(self, ExceptionUtils.getCause(cause));
        // then ourselve's
        Location loc = LocationUtils.getLocation(cause);
        if (LocationUtils.isKnown(loc)) {
            // Get the exception's short name
            String name = cause.getClass().getName();
            int pos = name.lastIndexOf('.');
            if (pos != -1) {
                name = name.substring(pos+1);
            }
            loc = new LocationImpl("[" + name + "]", loc.getURI(), loc.getLineNumber(), loc.getColumnNumber());
            self.addLocation(loc);
        }
    }

    public Location getLocation() {
        return locations == null ? null : (Location) locations.get(0);
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
            buf.append("\n\tat ").append(LocationUtils.toString((Location)locations.get(i)));
        }
        return buf.toString();
    }

    public String getMessage() {
        return getMessage(super.getMessage(), locations);
    }

    public void addLocation(Location loc) {
        if (LocationUtils.isUnknown(loc)) {
            return;
        }

        if (locations == null) {
            this.locations = new ArrayList(1); // Start small
        }
        locations.add(LocationImpl.get(loc));
    }
}
