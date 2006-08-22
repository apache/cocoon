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
package org.apache.cocoon.portal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.portal.layout.Layout;

/**
 * This is the central component in the portal. It holds the configuration
 * of the portal, the current name etc.
 * The main use of this component is to get the {@link PortalComponentManager}
 * to get all the other portal components.
 * This component is a singleton.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public interface PortalService extends Component {

    /** The role to lookup this component. */
    String ROLE = PortalService.class.getName();
    
    /**
     * The name of the portal - as defined in the portal configuration.
     */
    String getPortalName();
    
    void setPortalName(String value);
    
    /**
     * Return the value of an attribute
     * @param key The key of the attribute
     * @return The value of the attribute or null.
     */
    Object getAttribute(String key);
    
    /**
     * Set an attribute
     * @param key    The key of the attribute
     * @param value  The new value
     */
    void setAttribute(String key, Object value);
    
    /**
     * Remove an attribute
     * @param key The key of the attribute
     */
    void removeAttribute(String key);
    
    /**
     * Return the names of all attributes
     */
    Iterator getAttributeNames();
    
    /**
     * Return the value of a temporary attribute
     * @param key The key of the attribute
     * @return The value of the attribute or null.
     */
    Object getTemporaryAttribute(String key);
    
    /**
     * Set a temporary attribute
     * @param key    The key of the attribute
     * @param value  The new value
     */
    void setTemporaryAttribute(String key, Object value);
    
    /**
     * Remove a temporary attribute
     * @param key The key of the attribute
     */
    void removeTemporaryAttribute(String key);
    
    /**
     * Return the names of all temporary attributes
     */
    Iterator getTemporaryAttributeNames();

    /**
     * Return the component manager for the current portal
     */
    PortalComponentManager getComponentManager();

    /**
     * FIXME this is for the full-screen function
     * @param layoutKey TODO
     */
    void setEntryLayout(String layoutKey, Layout object);
    Layout getEntryLayout(String layoutKey);
    
    /**
     * Change the default layout key for most functions
     */
    void setDefaultLayoutKey(String layoutKey);
    
    /**
     * Get the default layout key
     */
    String getDefaultLayoutKey();
    
    /**
     * Return all skins
     */
    List getSkinDescriptions();

    /**
     * Return the current object model.
     * @since 2.1.8
     */
    Map getObjectModel();

    /**
     * Indicates whether aspects which are sensitive to rendering state should render
     * @param renderable true if all aspects should render
     */
    void setRenderable(Boolean renderable);

    /**
     * Returns true if all aspects should render, false if only "static" aspects should
     * render.
     */
    Boolean isRenderable();
}
