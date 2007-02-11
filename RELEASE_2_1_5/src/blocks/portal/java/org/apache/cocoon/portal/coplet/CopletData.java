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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.factory.impl.AbstractProducible;
import org.apache.cocoon.portal.util.DeltaApplicable;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: CopletData.java,v 1.10 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public class CopletData 
extends AbstractProducible
implements DeltaApplicable {

    protected String title;

    protected CopletBaseData copletBaseData;

    protected Map attributes = new HashMap();

	/**
	 * Signals whether a delta has been applied.
	 */
	private boolean deltaApplied = false;

    /**
     * Constructor
     */
    public CopletData() {
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
}
