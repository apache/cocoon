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
package org.apache.cocoon.portal.coplet;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.cocoon.portal.factory.impl.AbstractProducible;
import org.apache.cocoon.portal.pluto.om.common.PreferenceSetImpl;
import org.apache.cocoon.portal.util.AttributedMapItem;
import org.apache.pluto.om.common.PreferenceSet;


/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id$
 */
public final class CopletInstanceData 
    extends AbstractProducible {

	protected CopletData copletData;

    protected Map attributes = new HashMap(3);

    /** Temporary attributes are not persisted */
    protected Map temporaryAttributes = new HashMap(3);

    /** Portlet preferences */
    protected PreferenceSetImpl preferences = new PreferenceSetImpl();

    private String title;

	/**
	 * Constructor
	 */
	public CopletInstanceData() {
        // Nothing to do
	}

	/**
	 * @return CopletData
	 */
	public CopletData getCopletData() {
		return copletData;
	}

	/**
	 * Sets the copletData.
	 * @param copletData The copletData to set
	 */
	public void setCopletData(CopletData copletData) {
		this.copletData = copletData;
	}

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }
    
    public Object removeAttribute(String key) {
        return this.attributes.remove(key);
    }
    
    public Map getAttributes() {
        return this.attributes;
    }

    public final Collection getCastorAttributes() {
        Set set = new HashSet(this.attributes.size());
        Iterator iterator = this.attributes.entrySet().iterator();
        Map.Entry entry;
        while (iterator.hasNext()) {
            entry = (Map.Entry) iterator.next();
            AttributedMapItem item = new AttributedMapItem();
            item.setKey(entry.getKey());
            item.setValue(entry.getValue());
            set.add(item);
        }
        return set;
    }

    public void addAttribute(AttributedMapItem item) {
        this.attributes.put(item.getKey(), item.getValue());
    }

    public Object getTemporaryAttribute(String key) {
        return this.temporaryAttributes.get(key);
    }

    public void setTemporaryAttribute(String key, Object value) {
        this.temporaryAttributes.put(key, value);
    }
    
    public Object removeTemporaryAttribute(String key) {
        return this.temporaryAttributes.remove(key);
    }
    
    public Map getTemporaryAttributes() {
        return this.temporaryAttributes;
    }

    public String getTitle() {
        if (this.title != null) {
            return this.title;
        }
        return this.getCopletData().getTitle();
    }

    public String getInstanceTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPreferences(PreferenceSetImpl preferences) {
        this.preferences = preferences;
    }

    public PreferenceSet getPreferences() {
        return this.preferences;
    }

    public PreferenceSet getCastorPreferences() {
        return getPreferences();
    }

    public void setCastorPreferences(PreferenceSet castorPreferences) {
        setPreferences((PreferenceSetImpl)castorPreferences);
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        CopletInstanceData clone = (CopletInstanceData)super.clone();
        
        clone.copletData = this.copletData;
        clone.attributes = new HashMap(this.attributes);
        clone.temporaryAttributes = new HashMap(this.temporaryAttributes);
        clone.preferences = new PreferenceSetImpl();
        clone.preferences.addAll(this.preferences.getPreferences());

        return clone;
    }
    
    public CopletInstanceData copy() {
        try {
            return (CopletInstanceData)this.clone();
        } catch (CloneNotSupportedException cnse) {
            // ignore
            return null;
        }
    }
}