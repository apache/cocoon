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
 * @version CVS $Id: CopletData.java,v 1.9 2003/09/02 08:34:18 cziegeler Exp $
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
