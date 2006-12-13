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
package org.apache.cocoon.portal.pluto.om;

import java.util.HashMap;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowList;
import org.apache.pluto.om.window.PortletWindowListCtrl;

/**
 *
 * @version $Id$
 */
public class PortletWindowListImpl implements PortletWindowList, PortletWindowListCtrl {

    protected final HashMap windows;

    /**
     * Constructor.
     */
    public PortletWindowListImpl() {
        windows = new HashMap();
    }

    /**
     * Returns the elements of this set
     * 
     * @return An iterator containg all elements
     */
    public java.util.Iterator iterator() {
        return windows.values().iterator();
    }

    /**
     * Returns the portlet window object of the given id
     *
     * @param id  id of the portlet window object
     *
     * @return the portlet window object or null if the list does not
     *         contain a portlet window with the given id
     **/
    public PortletWindow get(ObjectID id) {
        if ( id == null ) {
            return null;
        }
        return (PortletWindow)windows.get(id.toString());
    }

    // PortletWindowListCtrl implementation.
    
    /**
     * Add a portlet window to the list
     * 
     * @param window the porlet window to add
     **/
    public void add(PortletWindow window) {
        if (window != null) {
            windows.put(window.getId().toString(), window);
        }
    }

    /**
     * Remove the portlet window with the given Id from the list
     * 
     * @param id the Id of the portlet window which should be removed
     **/
    public void remove(ObjectID id) {
        if (id != null) {
            windows.remove(id.toString());
        }
    }
}
