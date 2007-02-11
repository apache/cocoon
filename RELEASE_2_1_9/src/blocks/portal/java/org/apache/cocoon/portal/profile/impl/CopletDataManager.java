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
package org.apache.cocoon.portal.profile.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.util.DeltaApplicableReferencesAdjustable;

/**
 * Holds instances of CopletData.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: CopletDataManager.java,v 1.5 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public class CopletDataManager 
implements DeltaApplicableReferencesAdjustable {

	/**
	 * The coplet data instances.
	 */
	private Map copletData = new HashMap();
	
	/**
	 * Signals whether a delta has been applied.
	 */
	private boolean deltaApplied = false;
	
	/**
	 * Gets all coplet data.
	 */
	public Map getCopletData() {
		return this.copletData;
	}

	/**
	 * Gets the specified coplet data. 
	 */
	public CopletData getCopletData(String name) {
		return (CopletData)this.copletData.get(name);
	}
	
	/**
	 * Puts the specified coplet data to the manager.
	 */
	public void putCopletData(CopletData data) {
		this.copletData.put(data.getId(), data);
	}
	
	/**
	 * Applies the specified delta.
	 * @throws ClassCastException If the object is not of the expected type.
	 */
	public boolean applyDelta(Object object) {
		CopletDataManager manager = (CopletDataManager)object;
		
		this.deltaApplied = true;

		Iterator iterator = manager.getCopletData().values().iterator();
		CopletData data, delta;
		while (iterator.hasNext()) {
			delta = (CopletData)iterator.next();
			data = this.getCopletData(delta.getId());
			if (data == null) {
				this.putCopletData(delta);
			} else {
				data.applyDelta(delta); 
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
	 * Updates the references to contained DeltaApplicable objects  
	 * if no delta has been applied to them.
	 * @throws ClassCastException If the object is not of the expected type.
	 */
	public void adjustReferences(Object object) {
		CopletDataManager manager = (CopletDataManager)object;
		
		Iterator iterator = this.copletData.values().iterator();
		CopletData data, other;
		while (iterator.hasNext()) {
			data = (CopletData)iterator.next();
			if (!data.deltaApplied()) {
				other = manager.getCopletData(data.getId());
				if (other != null) {
					this.putCopletData(other);
				}
			}
		}
	}
}
