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
package org.apache.cocoon.portal.aspect.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.aspect.AspectDataHandler;
import org.apache.cocoon.portal.aspect.Aspectalizable;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: AbstractAspectalizable.java,v 1.6 2004/03/05 13:02:09 bdelacretaz Exp $
 */
public abstract class AbstractAspectalizable 
    implements Aspectalizable {

    transient protected AspectDataHandler aspectDataHandler;
    
    transient protected Map persistentDatas;
    
    /**
     * Is this aspect supported
     */
    public boolean isAspectSupported(String aspectName) {
        return this.aspectDataHandler.isAspectSupported( aspectName );
    }

    public Object getAspectData(String aspectName) {
        return this.aspectDataHandler.getAspectData(this, aspectName);
    }
    
    public void setAspectData(String aspectName, Object data) {
        this.aspectDataHandler.setAspectData(this, aspectName, data);
    }
    
    public Map getAspectDatas(){
        return this.aspectDataHandler.getAspectDatas(this);
    }
    
    public Map getPersistentAspectData(){
    	if (this.aspectDataHandler == null) {
	    	return this.persistentDatas;
    	} else {
			return this.aspectDataHandler.getPersistentAspectDatas(this);
    	}
    }

    /**
     * This method is invoked once to set the handler
     */
    public void setAspectDataHandler(AspectDataHandler handler) {
        this.aspectDataHandler = handler;
        if ( this.persistentDatas != null ) {
            Iterator iter = this.persistentDatas.entrySet().iterator();
            Map.Entry entry;
            while (iter.hasNext()) {
                entry = (Map.Entry)iter.next();
                handler.setAspectData(this, (String)entry.getKey(), entry.getValue());
            }
            this.persistentDatas = null;
        }
    }

    public void addPersistentAspectData(String aspectName, Object data) {
        if ( this.persistentDatas == null ) {
            this.persistentDatas = new HashMap();
        }
        this.persistentDatas.put(aspectName, data);
    }

}