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


/**
 * A location in a resource. The location is composed of the URI of the resource, and
 * the line and column numbers within that resource (when available), along with a description.
 * <p>
 * Locations are mostly provided by {@link Locatable}s objects.
 *
 * @since 2.1.8
 * @version $Id$
 */
public interface Location {

    /**
     * Constant for unknown locations.
     */
    public static final Location UNKNOWN = LocationImpl.UNKNOWN;

    /**
     * Get the description of this location
     *
     * @return the description (can be <code>null</code>)
     */
    public String getDescription();

    /**
     * Get the URI of this location
     *
     * @return the URI (<code>null</code> if unknown).
     */
    public String getURI();
    /**
     * Get the line number of this location
     *
     * @return the line number (<code>-1</code> if unknown)
     */
    public int getLineNumber();

    /**
     * Get the column number of this location
     *
     * @return the column number (<code>-1</code> if unknown)
     */
    public int getColumnNumber();

}
