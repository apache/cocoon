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
 * @version CVS $Id$
 */
public abstract class AbstractAspectalizable 
    implements Aspectalizable, Cloneable {

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
    	}
        return this.aspectDataHandler.getPersistentAspectDatas(this);
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
            this.persistentDatas = new HashMap(3);
        }
        this.persistentDatas.put(aspectName, data);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        AbstractAspectalizable clone = (AbstractAspectalizable)super.clone();
        
        clone.aspectDataHandler = this.aspectDataHandler;
        final Map datas = this.aspectDataHandler.getAspectDatas(this);
        final Iterator i = datas.entrySet().iterator();
        while ( i.hasNext() ) {
            final Map.Entry e = (Map.Entry)i.next();
            clone.aspectDataHandler.setAspectData(clone, e.getKey().toString(), e.getValue());
        }
        clone.persistentDatas = this.persistentDatas;
        
        return clone;
    }
}