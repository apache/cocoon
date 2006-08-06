/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.Constants;
import org.apache.commons.collections.iterators.IteratorEnumeration;

/**
 * 
 * @version $Id$
 *
 */
public class PortalContextImpl
    extends AbstractLogEnabled
    implements PortalContext {

    protected String versionInfo = "Apache Cocoon/" + Constants.VERSION;

    protected final Map properties = new HashMap();

    protected final List portletModes = new ArrayList();

    protected final List windowStates = new ArrayList();

    public PortalContextImpl() {
        this.portletModes.add(new PortletMode("view"));
        this.portletModes.add(new PortletMode("edit"));
        this.portletModes.add(new PortletMode("help"));
        this.portletModes.add(new PortletMode("config"));
    }
    /**
     * @see javax.portlet.PortalContext#getPortalInfo()
     */
    public String getPortalInfo() {
        return this.versionInfo;
    }

    /**
     * @see javax.portlet.PortalContext#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Property name == null");
        }
        return (String)this.properties.get(name);
    }

    /**
     * @see javax.portlet.PortalContext#getPropertyNames()
     */
    public Enumeration getPropertyNames() {
        return new IteratorEnumeration(this.properties.keySet().iterator());
    }

    /**
     * @see javax.portlet.PortalContext#getSupportedPortletModes()
     */
    public Enumeration getSupportedPortletModes() {
        return new IteratorEnumeration(this.portletModes.iterator());
    }

    /**
     * @see javax.portlet.PortalContext#getSupportedWindowStates()
     */
    public Enumeration getSupportedWindowStates() {
        return new IteratorEnumeration(this.windowStates.iterator());
    }
}
