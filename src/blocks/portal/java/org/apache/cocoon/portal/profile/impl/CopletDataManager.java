/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Björn Lütkemeier</a>
 * 
 * @version CVS $Id: CopletDataManager.java,v 1.3 2003/05/22 15:19:42 cziegeler Exp $
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
