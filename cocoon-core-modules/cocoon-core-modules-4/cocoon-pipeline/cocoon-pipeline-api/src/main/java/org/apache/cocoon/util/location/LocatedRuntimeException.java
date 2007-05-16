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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * A cascading and located <code>RuntimeException</code>. It is also {@link MultiLocatable} to easily build
 * location stack traces.
 * <p>
 * If a <code>LocatedRuntimeException</code> is built with a location and a cause which is also a
 * <code>LocatedRuntimeException</code>, then the default behavior is to add the location to the cause
 * exception and immediately rethrow the cause. This avoids exception nesting and builds a location
 * stack.
 *
 * @since 2.1.8
 * @version $Id$
 */
public class LocatedRuntimeException extends NestableRuntimeException
                                     implements LocatableException, MultiLocatable {

    private List locations;

    public LocatedRuntimeException(String message) {
        this(message, null, null, true);
    }

    public LocatedRuntimeException(String message, Throwable cause)
    throws LocatedRuntimeException {
        this(message, cause, null, true);
    }

    public LocatedRuntimeException(String message, Location location) {
        this(message, null, location, true);
    }

    public LocatedRuntimeException(String message, Throwable cause, Location location)
    throws LocatedRuntimeException {
        this(message, cause, location, true);
    }

    public LocatedRuntimeException(String message, Throwable cause, Location location, boolean rethrowLocated)
    throws LocatedRuntimeException {
        super(message, cause);
        if (rethrowLocated && cause instanceof LocatedRuntimeException) {
            LocatedRuntimeException lreCause = (LocatedRuntimeException)cause;
            lreCause.addLocation(location);
            // Rethrow the cause
            throw lreCause;
        }

        LocatedException.ensureCauseChainIsSet(cause);
        LocatedException.addCauseLocations(this, cause);
        addLocation(location);
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

    public String getMessage() {
        return LocatedException.getMessage(super.getMessage(), locations);
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
