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
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.aspect.AspectDataStore;
import org.apache.cocoon.portal.aspect.Aspectalizable;

/**
 * An aspect data store that holds the aspects in memory.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: MemoryAspectDataStore.java,v 1.2 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public class MemoryAspectDataStore 
    extends AbstractLogEnabled
    implements Component, ThreadSafe, AspectDataStore {
    
    protected final Map objectMap = new HashMap();
    
    /**
     * Get the aspect map for an object
     */
    protected Map getMap(Aspectalizable owner) {
        Map result = (Map)this.objectMap.get(owner);
        if ( result == null ) {
            result = new HashMap();
            this.objectMap.put(owner, result);
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataStore#getAspectData(org.apache.cocoon.portal.aspect.Aspectalizable, java.lang.String)
     */
    public Object getAspectData(Aspectalizable owner, String aspectName) {
        return this.getMap(owner).get( aspectName );
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataStore#setAspectData(org.apache.cocoon.portal.aspect.Aspectalizable, java.lang.String, java.lang.Object)
     */
    public void setAspectData(Aspectalizable owner, String aspectName, Object data) {
        this.getMap(owner).put(aspectName, data);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataStore#isPersistent()
     */
    public boolean isPersistent() {
        return false;
    }
    
}
