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
package org.apache.cocoon.portal.coplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.cocoon.portal.factory.impl.AbstractProducible;
import org.apache.cocoon.portal.util.DeltaApplicable;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id$
 */
public class CopletData 
extends AbstractProducible
implements DeltaApplicable {

    protected String title;

    protected CopletBaseData copletBaseData;

    protected Map attributes = new HashMap();

    protected String allowedRoles;
    
    protected String deniedRoles;
    
    protected transient List allowedRolesList;
    
    protected transient List deniedRolesList;
    
	/**
	 * Signals whether a delta has been applied.
	 */
	private boolean deltaApplied = false;

    /**
     * Constructor
     */
    public CopletData() {
        // Nothing to do
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

    public void removeAttribute(String key) {
        this.attributes.remove(key);
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
	 * Applies the specified delta.
	 * @throws ClassCastException If the object is not of the expected type.
	 */
	public boolean applyDelta(Object object) {
		CopletData data = (CopletData)object;
		
		this.deltaApplied = true;
		
		String title = data.getTitle();
		if (title != null) {
            this.setTitle(title);
		}
			
		CopletBaseData copletBaseData = data.getCopletBaseData();
		if (copletBaseData != null)	{
			this.setCopletBaseData(copletBaseData);
		}
			
		Iterator iterator = data.getAttributes().entrySet().iterator();
		Object attribute, delta;
		String key;
		Map.Entry entry;
		while (iterator.hasNext()) {
			entry = (Map.Entry)iterator.next();
			key = (String)entry.getKey();
			delta = entry.getValue();

			attribute = this.getAttribute(key);
			if (attribute == null) {
				// add new attribute
				this.setAttribute(key, delta);
			} else if (attribute instanceof DeltaApplicable) {
				// apply delta
				boolean success = ((DeltaApplicable)attribute).applyDelta(delta);
				if (!success) {
					// replace attribute
					this.setAttribute(key, delta);
				}
			} else {
				// replace attribute
				this.setAttribute(key, delta);
			}
		}
		
		return true;
	}
	
	/**
	 * Checks if a delta has been applied.
	 */
	public boolean deltaApplied() {
		return this.deltaApplied;
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
     * @return Returns the denied roles.
     */
    public String getDeniedRoles() {
        return this.deniedRoles;
    }
    /**
     * @param roles The allowed roles to set.
     */
    public void setDeniedRoles(String roles) {
        this.deniedRoles = roles;
        this.deniedRolesList = null;
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
    
    /**
     * Return the list of roles that are denied to access this coplet
     * @return A list of roles or null if everyone is allowed.
     */
    public List getDeniedRolesList() {
        if ( StringUtils.isBlank(this.deniedRoles) ) {
            return null;
        }
        if ( this.deniedRolesList == null ) {
            this.deniedRolesList = new ArrayList();
            final StringTokenizer tokenizer = new StringTokenizer(this.deniedRoles, ",");
            while ( tokenizer.hasMoreElements() ) {
                String token = (String)tokenizer.nextElement();
                this.deniedRolesList.add(token);
            }
            if ( this.deniedRolesList.size() == 0 ) {
                this.deniedRoles = null;
                this.deniedRolesList = null;
            }
        }
        return this.deniedRolesList;
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
        this.buildRolesString(l, true);
    }
    
    public void removeFromAllowedRoles(String role) {
        List l = this.getAllowedRolesList();
        if ( l != null && l.contains(role) ) {
            l.remove(role);
            if ( l.size() == 0 ) {
                this.allowedRoles = null;
                this.allowedRolesList = null;
            } else {
                this.buildRolesString(l, true);
            }
        }
    }
    
    public void addToDeniedRoles(String role) {
        List l = this.getDeniedRolesList();
        if ( l == null ) {
            l = new ArrayList();
            l.add(role);
        } else {
            if ( !l.contains(role) ) {
                l.add(role);
            }
        }
        this.buildRolesString(l, false);
    }
    
    public void removeFromDeniedRoles(String role) {
        List l = this.getDeniedRolesList();
        if ( l != null && l.contains(role) ) {
            l.remove(role);
            if ( l.size() == 0 ) {
                this.deniedRoles = null;
                this.deniedRolesList = null;
            } else {
                this.buildRolesString(l, false);
            }
        }
    }

    protected void buildRolesString(List fromList, boolean allow) {
        if ( allow ) {
            this.allowedRolesList = fromList;
        } else {
            this.deniedRolesList = fromList;
        }
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
        if ( allow ) {
            this.allowedRoles = buffer.toString();
        } else {
            this.deniedRoles = buffer.toString();
        }
    }
}
