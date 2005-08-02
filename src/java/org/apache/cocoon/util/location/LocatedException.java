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
import java.util.List;

import org.apache.avalon.framework.CascadingException;

/**
 * A cascading and located <code>Exception</code>. It is also {@link MultiLocatable} to easily build
 * stack traces.
 * 
 * @version $Id$
 */
public class LocatedException extends CascadingException implements LocatableException, MultiLocatable {

    private List locations;

    public LocatedException(String message) {
        super(message, null);
    }
    
    public LocatedException(String message, Throwable thr) {
        super(message, thr);
    }
    
    public LocatedException(String message, Location location) {
        super(message, null);
        addLocation(location);
    }
    
    public LocatedException(String message, Throwable thr, Location location) {
        super(message, thr);
        addLocation(location);
    }

    public Location getLocation() {
        return locations == null ? null : (Location)locations.get(0);
    }

    public List getLocations() {
        return locations;
    }

    public String getRawMessage() {
        return super.getMessage();
    }

    public String getMessage() {
        if (this.locations == null) {
            return super.getMessage();
        }

        // Produce a Java-like stacktrace with locations
        StringBuffer buf = new StringBuffer(super.getMessage());
        for (int i = 0; i < locations.size(); i++) {
            buf.append("\n\tat ").append(locations.get(i));
        }
        return buf.toString();
    }
    
    public void addLocation(Location loc) {
        if (loc == null || loc.equals(Location.UNKNOWN))
            return;

        if (locations == null) {
            this.locations = new ArrayList(1); // Start small
        }
        locations.add(loc);
    }
}
