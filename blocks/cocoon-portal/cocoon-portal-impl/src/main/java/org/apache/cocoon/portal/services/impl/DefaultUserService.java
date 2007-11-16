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
package org.apache.cocoon.portal.services.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.user.UserEvent;
import org.apache.cocoon.portal.om.PortalUser;
import org.apache.cocoon.portal.services.UserService;
import org.apache.cocoon.portal.util.AbstractBean;
import org.springframework.core.Ordered;


/**
 * @version $Id$
 */
public class DefaultUserService
    extends AbstractBean
    implements UserService, Receiver, Ordered {

    /** Attribute to store the current user. */
    protected static final String USER_ATTRIBUTE = DefaultUserService.class.getName() + "/User";

    /** The attribute prefix used to prefix attributes in the session and request. */
    protected String attributeName;

    /** The default profile name. */
    protected String defaultProfileName;

    /**
     * Initialize this bean.
     */
    public void init() {
        this.attributeName = DefaultUserService.class.getName() + '/' + this.portalService.getPortalName();
        // FIXME - We should use a better default than 'portal'
        this.defaultProfileName = this.portalService.getConfiguration("default-profile-name", "portal");
    }

    /**
     * Receives any user related event and invokes login, logout etc.
     * @see Receiver
     */
    public void inform(UserEvent event) {
        this.setTemporaryAttribute(USER_ATTRIBUTE, event.getPortalUser());
    }

    protected Map getSessionMap() {
        final HttpServletRequest request = this.portalService.getRequestContext().getRequest();
        final HttpSession session = request.getSession(false);
        if ( session == null ) {
            return null;
        }
        final Map map = (Map) session.getAttribute(this.attributeName);
        return map;
    }

    protected Map getRequestMap() {
        final HttpServletRequest request = this.portalService.getRequestContext().getRequest();
        final Map map = (Map) request.getAttribute(this.attributeName);
        return map;
    }

    protected void setSessionMap(Map map) {
        final HttpServletRequest request = this.portalService.getRequestContext().getRequest();
        final HttpSession session = request.getSession(false);
        session.setAttribute(this.attributeName, map);
    }

    protected void setRequestMap(Map map) {
        final HttpServletRequest request = this.portalService.getRequestContext().getRequest();
        request.setAttribute(this.attributeName, map);
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#getAttribute(java.lang.String)
     */
    public Object getAttribute(String key) {
        Map map = this.getSessionMap();
        if ( map != null ) {
            return map.get(key);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#getAttributeNames()
     */
    public Collection getAttributeNames() {
        Map map = this.getSessionMap();
        if ( map != null ) {
            return map.keySet();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#getTemporaryAttribute(java.lang.String)
     */
    public Object getTemporaryAttribute(String key) {
        Map map = this.getRequestMap();
        if ( map != null ) {
            return map.get(key);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#getTemporaryAttributeNames()
     */
    public Collection getTemporaryAttributeNames() {
        Map map = this.getRequestMap();
        if ( map != null ) {
            return map.keySet();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#getUser()
     */
    public PortalUser getUser() {
        return (PortalUser)this.getTemporaryAttribute(USER_ATTRIBUTE);
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#removeAttribute(java.lang.String)
     */
    public Object removeAttribute(String key) {
        Map map = this.getSessionMap();
        if ( map != null ) {
            final Object result = map.remove(key);
            this.setSessionMap(map);
            return result;
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#removeTemporaryAttribute(java.lang.String)
     */
    public Object removeTemporaryAttribute(String key) {
        Map map = this.getRequestMap();
        if ( map != null ) {
            final Object result = map.remove(key);
            this.setRequestMap(map);
            return result;
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String key, Object value) {
        Map map = this.getSessionMap();
        if ( map == null ) {
            synchronized ( this ) {
                map = this.getSessionMap();
                if ( map == null ) {
                    map = new HashMap();
                    this.setSessionMap(map);
                }
            }
        }
        map.put(key, value);
        this.setSessionMap(map);
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#setTemporaryAttribute(java.lang.String, java.lang.Object)
     */
    public void setTemporaryAttribute(String key, Object value) {
        Map map = this.getRequestMap();
        if ( map == null ) {
            synchronized ( this ) {
                map = this.getRequestMap();
                if ( map == null ) {
                    map = new HashMap();
                    this.setRequestMap(map);
                }
            }
        }
        map.put(key, value);
        this.setRequestMap(map);
    }

    /**
     * @see org.apache.cocoon.portal.services.UserService#getDefaultProfileName()
     */
    public String getDefaultProfileName() {
        String key = this.getUser().getDefaultProfileName();
        if ( key == null ) {
            return this.defaultProfileName;
        }
        return key;
    }

    /**
     * This component should have a high priority (low order) as
     * other components might access it during event processing.
     * @see org.springframework.core.Ordered#getOrder()
     */
    public int getOrder() {
        return -5;
    }
}