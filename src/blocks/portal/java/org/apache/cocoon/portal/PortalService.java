/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal;

import java.util.Iterator;

import org.apache.avalon.framework.component.Component;

/**
 * This is the central component in the portal. It holds the configuration
 * of the portal, the current name etc.
 * The main use of this component is to get the {@link PortalComponentManager}
 * to get all the other portal components.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: PortalService.java,v 1.3 2004/03/05 13:02:08 bdelacretaz Exp $
 */
public interface PortalService extends Component {

    String ROLE = PortalService.class.getName();
    
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
}
