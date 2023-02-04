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
package org.apache.cocoon;

import org.apache.cocoon.util.location.Location;

/**
 * This Exception is thrown every time there is a problem in finding
 * a resource.
 *
 * @version $Id$
 */
public class ResourceNotFoundException extends ProcessingException {

    /**
     * Construct a new <code>ResourceNotFoundException</code> instance.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construct a new <code>ResourceNotFoundException</code> that references
     * a parent Exception.
     */
    public ResourceNotFoundException(String message, Throwable t) {
        super(message, t);
    }
    
    public ResourceNotFoundException(String message, Location location) {
        super(message, location);
    }
    
    public ResourceNotFoundException(String message, Throwable t, Location loc) {
        super(message, t, loc);
    }
}
