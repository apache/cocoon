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
import java.util.Map;

import org.apache.cocoon.portal.factory.impl.AbstractProducible;


/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: CopletInstanceData.java,v 1.10 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public final class CopletInstanceData 
    extends AbstractProducible {

	protected CopletData copletData;

    protected Map attributes = new HashMap();

    /** Temporary attributes are not persisted */
    protected Map temporaryAttributes = new HashMap();
    
	/**
	 * Constructor
	 */
	public CopletInstanceData() {
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
    
    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }
    
    public Map getAttributes() {
        return this.attributes;
    }

    public Object getTemporaryAttribute(String key) {
        return this.temporaryAttributes.get(key);
    }

    public void setTemporaryAttribute(String key, Object value) {
        this.temporaryAttributes.put(key, value);
    }
    
    public void removeTemporaryAttribute(String key) {
        this.temporaryAttributes.remove(key);
    }
    
    public Map getTemporaryAttributes() {
        return this.temporaryAttributes;
    }
}