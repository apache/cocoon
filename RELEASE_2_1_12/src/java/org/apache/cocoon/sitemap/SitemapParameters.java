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
package org.apache.cocoon.sitemap;

import java.util.HashMap;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.util.location.Locatable;
import org.apache.cocoon.util.location.Location;

/**
 * Extension to the Avalon Parameters to give location information
 *
 * @version CVS $Id$
 */
public class SitemapParameters extends Parameters implements Locatable {
    
    private Location location = Location.UNKNOWN;
    
    public SitemapParameters(Location location) {
        this.location = location;
    }
    
    /*
     * Get the location of the statement defining these parameters.
     * 
     * @since 2.1.8
     * @see org.apache.cocoon.util.location.Locatable#getLocation()
     */
    public Location getLocation() {
        return this.location;
    }
    
    /**
     * Get the location of a <code>Parameters</code> object, returning
     * {@link Location#UNKNOWN} if no location could be found.
     * 
     * @param param
     * @return the location
     * @since 2.1.8
     */
    public static Location getLocation(Parameters param) {
        Location loc = null;
        if (param instanceof Locatable) {
            loc = ((Locatable)param).getLocation();
        }
        return loc == null ? Location.UNKNOWN : loc;
    }

    /**
     * @deprecated use {@link #getLocation(Parameters)}
     */
    public static String getStatementLocation(Parameters param) {
        return getLocation(param).toString();
    }    

    /**
     * For internal use only.
     */
    public static class LocatedHashMap extends HashMap implements Locatable {
        private Location loc;

        public Location getLocation() {
            return this.loc;
        }
        
        public LocatedHashMap(Location loc) {
            this.loc = loc;
        }

        public LocatedHashMap(Location loc, int size) {
            super(size);
            this.loc = loc;
        }
    }
}
