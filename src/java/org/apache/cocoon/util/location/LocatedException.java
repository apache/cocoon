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

import org.apache.avalon.framework.CascadingException;

/**
 * A cascading and located <code>Exception</code>.
 * 
 * @version $Id$
 */
public class LocatedException extends CascadingException implements LocatableException {
    
    private Location location;

    public LocatedException(String message) {
        super(message, null);
    }
    
    public LocatedException(String message, Throwable thr) {
        super(message, thr);
    }
    
    public LocatedException(String message, Location location) {
        super(message, null);
        this.location = location;
    }
    
    public LocatedException(String message, Throwable thr, Location location) {
        super(message, thr);
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getRawMessage() {
        return super.getMessage();
    }

    public String getMessage() {
        return this.location == null ? super.getMessage() :
            super.getMessage() + " (" + this.location.toString() + ")";
    }
}
