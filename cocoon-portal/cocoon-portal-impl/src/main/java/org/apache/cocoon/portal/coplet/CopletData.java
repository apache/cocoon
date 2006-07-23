/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.coplet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.cocoon.portal.util.PortalUtils;
import org.apache.commons.lang.StringUtils;

/**
 * A coplet data describes an available coplet. A coplet data can be seen
 * as a class. A user can create one or more instances of the coplet
 * ({@link CopletInstanceData}s).
 *
 * @version $Id$
 */
public class CopletData implements Serializable {

    /** The unique identifier. */
    protected final String id;

    protected String title;

    protected CopletBaseData copletBaseData;

    protected Map attributes = new HashMap();

    protected String allowedRoles;

    protected transient List allowedRolesList;

    /**
     * Constructor to instantiate a new coplet data object.
     * @param id The unique id of the object.
     * @see PortalUtils#testId(String)
     */
    public CopletData(String id) {
        // FIXME - Due to a bug in castor, we have to allow null ids for now
        if ( id == null ) {
            this.id = null;
            return;
        }
        final String idErrorMsg = PortalUtils.testId(id);
        if ( idErrorMsg != null ) {
            throw new IllegalArgumentException(idErrorMsg);
        }
        this.id = id;
    }

    /**
     * Return the unique identifier of this object.
     * @return The unique identifier.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the title.
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     * @param title The title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the copletBaseData.
     * @return CopletBaseData
     */
    public CopletBaseData getCopletBaseData() {
        return copletBaseData;
    }

    /**
     * Sets the copletBaseData.
     * @param copletBaseData The copletBaseData to set
     */
    public void setCopletBaseData(CopletBaseData copletBaseData) {
        this.copletBaseData = copletBaseData;
    }

    public Object removeAttribute(String key) {
        return this.attributes.remove(key);
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public Map getAttributes() {
    	return this.attributes;
    }

    /**
     * @return Returns the allowed roles.
     */
    public String getAllowedRoles() {
        return this.allowedRoles;
    }

    /**
     * @param roles The allowed roles to set.
     */
    public void setAllowedRoles(String roles) {
        this.allowedRoles = roles;
        this.allowedRolesList = null;
    }

    /**
     * Return the list of roles that are allowed to access this coplet
     * @return A list of roles or null if everyone is allowed.
     */
    public List getAllowedRolesList() {
        if ( StringUtils.isBlank(this.allowedRoles) ) {
            return null;
        }
        if ( this.allowedRolesList == null ) {
            this.allowedRolesList = new ArrayList();
            final StringTokenizer tokenizer = new StringTokenizer(this.allowedRoles, ",");
            while ( tokenizer.hasMoreElements() ) {
                String token = (String)tokenizer.nextElement();
                this.allowedRolesList.add(token);
            }
            if ( this.allowedRolesList.size() == 0 ) {
                this.allowedRoles = null;
                this.allowedRolesList = null;
            }
        }
        return this.allowedRolesList;
    }

    public void addToAllowedRoles(String role) {
        List l = this.getAllowedRolesList();
        if ( l == null ) {
            l = new ArrayList();
            l.add(role);
        } else {
            if ( !l.contains(role) ) {
                l.add(role);
            }
        }
        this.buildRolesString(l);
    }

    public void removeFromAllowedRoles(String role) {
        List l = this.getAllowedRolesList();
        if ( l != null && l.contains(role) ) {
            l.remove(role);
            if ( l.size() == 0 ) {
                this.allowedRoles = null;
                this.allowedRolesList = null;
            } else {
                this.buildRolesString(l);
            }
        }
    }

    protected void buildRolesString(List fromList) {
        this.allowedRolesList = fromList;
        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        Iterator i = fromList.iterator();
        while ( i.hasNext() ) {
            String role = (String)i.next();
            if ( !first ) {
                buffer.append(',');
            }
            first = false;
            buffer.append(role);
        }
        this.allowedRoles = buffer.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "CopletData (" + this.hashCode() +
               "), id=" + this.getId() + ", coplet-base-data=" + (this.getCopletBaseData() == null ? "null" : this.getCopletBaseData().getId());
    }
}
