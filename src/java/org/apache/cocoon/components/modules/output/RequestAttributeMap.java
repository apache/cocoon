/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.cocoon.components.modules.output;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

/**
 * Abstraction layer to encapsulate different output
 * destinations. This module outputs to a request attribute
 * java.util.Map object that contains all the attributes that were
 * set. Configuration option &lt;key-prefix&gt; defaults to
 * "org.apache.cocoon.components.modules.output.OutputModule"
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: RequestAttributeMap.java,v 1.3 2004/03/08 13:58:32 cziegeler Exp $
 */
public class RequestAttributeMap extends AbstractOutputModule implements OutputModule {
    
    public final String PREFIX = "org.apache.cocoon.components.modules.output.OutputModule";
    public final String TRANS_PREFIX = "org.apache.cocoon.components.modules.output.OutputModule.RequestAttributeMap.transient";
    
    /**
     * communicate an attribute value to further processing logic.
     * @param modeConf column's mode configuration from resource
     * description. This argument is optional.
     * @param objectModel The objectModel
     * @param name The attribute's label, consisting of "table.column"
     * or "table.column[index]" in case of multiple attributes of the
     * same spec.
     * @param value The attriute's value.
     * */
    public void setAttribute( Configuration modeConf, Map objectModel, String name, Object value ) {
        if (getLogger().isDebugEnabled())
            getLogger().debug("setting transient ['"+name+"'] to ['"+value+"']");
        super.transientSetAttribute(objectModel, TRANS_PREFIX, name, value );
    }
    
    
    /**
     * If a database transaction needs to rollback, this is called to
     * inform the further processing logic about this fact. All
     * already set attribute values are invalidated. <em>This is difficult
     * because only the request object can be used to synchronize this
     * and build some kind of transaction object. Beaware that sending
     * your data straight to some beans or other entities could result
     * in data corruption!</em>
     * */
    public void rollback( Configuration modeConf, Map objectModel, Exception e ) {
        if (getLogger().isDebugEnabled())
            getLogger().debug("rolling back");
        super.rollback(objectModel, TRANS_PREFIX);
    }
    
    
    /**
     * Signal that the database transaction completed
     * successfully. See notes on @link{rollback}.
     * */
    public void commit( Configuration modeConf, Map objectModel ) {
        if (getLogger().isDebugEnabled())
            getLogger().debug("prepare commit");
        Map aMap = super.prepareCommit(objectModel,TRANS_PREFIX);
        if (aMap == null) {
            // nothing to do
            return;
        }
        
        String prefix = (String) this.settings.get("key-prefix", PREFIX);
        Request request = ObjectModelHelper.getRequest(objectModel);
        Object temp = request.getAttribute(prefix);
        Map old = null;
        if (temp == null) {
            old = aMap;
        } else {
            old = (Map) temp;
            old.putAll(aMap);
        }
        request.setAttribute(prefix, old);
        if (getLogger().isDebugEnabled())
            getLogger().debug("done commit to ['"+prefix+"']");
    }
    
}
