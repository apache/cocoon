/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.util.location;

import java.util.List;

/**
 * An extension of {@link Location} for classes that can hold a list of locations.
 * It will typically be used to build location stacks.
 * <p>
 * The <em>first</em> location of the collection returned by {@link #getLocations()} should be
 * be identical to the result of {@link org.apache.cocoon.util.location.Locatable#getLocation()}.
 * <p>
 * If the list of locations designates a call stack, then its first element should be the deepmost
 * location of this stack. This is consistent with the need for <code>getLocation()</code> to
 * return the most precise location.
 *
 * @since 2.1.8
 * @version $Id$
 */
public interface MultiLocatable extends Locatable {

    /**
     * Return the list of locations.
     *
     * @return a list of locations, possibly empty but never null.
     */
    public List getLocations();

    /**
     * Add a location to the current list of locations.
     * <p>
     * Implementations are free to filter locations that can be added (e.g. {@link Location#UNKNOWN}),
     * and there is therefore no guarantee that the given location will actually be added to the list.
     * Filtered locations are silently ignored.
     *
     * @param location the location to be added.
     */
    public void addLocation(Location location);

}
