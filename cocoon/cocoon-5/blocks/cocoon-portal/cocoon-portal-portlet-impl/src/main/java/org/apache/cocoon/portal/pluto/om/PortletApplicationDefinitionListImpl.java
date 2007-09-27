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
import java.util.Iterator;
import java.util.Map;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletApplicationDefinitionList;

/**
 *
 * @version $Id$
 */
public class PortletApplicationDefinitionListImpl
implements PortletApplicationDefinitionList {

    /** The portlet application definitions. */
    protected Map portletApplicationDefinitions;

    /**
     * Constructor.
     */
    public PortletApplicationDefinitionListImpl() {
        this.portletApplicationDefinitions = new HashMap();
    }

    /**
     * @see org.apache.pluto.om.portlet.PortletApplicationDefinitionList#get(org.apache.pluto.om.common.ObjectID)
     */
    public PortletApplicationDefinition get(ObjectID objectId) {
        return (PortletApplicationDefinition)this.portletApplicationDefinitions.get(objectId);
    }

    /**
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator() {
        return this.portletApplicationDefinitions.values().iterator();
    }

    /**
     * Add a new portlet application definition
     */
    public void add(PortletApplicationDefinition def) {
        this.portletApplicationDefinitions.put(def.getId(), def);
    }
}
