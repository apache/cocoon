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

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * A cascading and located <code>RuntimeException</code>. It is also {@link MultiLocatable} to easily build
 * stack traces.
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class LocatedRuntimeException extends CascadingRuntimeException implements LocatableException, MultiLocatable {
    
    private List locations;

    public LocatedRuntimeException(String message) {
        this(message, null, null);
    }
    
    public LocatedRuntimeException(String message, Throwable cause) {
        this(message, cause, null);
    }
    
    public LocatedRuntimeException(String message, Location location) {
        this(message, null, location);
        addLocation(location);
    }
    
    public LocatedRuntimeException(String message, Throwable cause, Location location) {
        super(message, cause);
        LocatedException.addCauseLocations(this, cause);
        addLocation(location);
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

    public String getMessage() {
        return LocatedException.getMessage(super.getMessage(), locations);
    }
    
    public void addLocation(Location loc) {
        if (loc == null || loc.equals(Location.UNKNOWN))
            return;

        if (locations == null) {
            this.locations = new ArrayList(1); // Start small
        }
        locations.add(LocationImpl.get(loc));
    }

    /**
     * Build a located exception given an existing exception and the location where
     * this exception was catched. If the exception is already a <code>LocatedRuntimeException</code>,
     * then the location is added to the original exception's location chain and the result is
     * the original exception (and <code>description</code> is ignored. Otherwise, a new
     * <code>LocatedRuntimeException</code> is built, wrapping the original exception.
     * 
     * @param message a message (can be <code>null</code>)
     * @param thr the original exception (can be <code>null</code>)
     * @param location the location (can be <code>null</code>)
     * @return a located exception
     */
    public static LocatedRuntimeException getLocatedException(String message, Throwable thr, Location location) {
        if (thr instanceof LocatedRuntimeException) {
            LocatedRuntimeException re = (LocatedRuntimeException)thr;
            re.addLocation(location);
            return re;
        }
        
        return new LocatedRuntimeException(message, thr, location);
    }
}
