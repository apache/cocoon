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

import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowCtrl;

/**
 *
 * @version $Id$
 */
public class PortletWindowImpl implements PortletWindow, PortletWindowCtrl {

    private ObjectID objectId;
    private String id;
    private PortletEntity portletEntity;
    private final CopletInstance coplet;

    public PortletWindowImpl(CopletInstance coplet, String id) {
        this.id = id;
        this.coplet = coplet;
    }

    // PortletWindow implementation.

    /**
     * Returns the identifier of this portlet instance window as object id
     *
     * @return the object identifier
     **/
    public ObjectID getId() {
        if (objectId==null) {
            objectId = org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString(id);
        }
        return objectId;
    }

    /**
     * Returns the portlet entity
     *
     * @return the portlet entity
     **/
    public PortletEntity getPortletEntity() {
        return portletEntity;
    }

    // PortletWindowCtrl implementation.
    /**
     * binds an identifier to this portlet window
     *
     * @param id the new identifier
     */
    public void setId(String id) {
        this.id = id;
        this.objectId = null;
    }

    /**
     * binds a portlet instance to this portlet window
     * 
     * @param portletEntity a portlet entity object
     **/
    public void setPortletEntity(PortletEntity portletEntity) {
        this.portletEntity = portletEntity;
    }

    public CopletInstance getCopletInstanceData() {
        return this.coplet;
    }
}