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

package org.apache.cocoon.components.modules.output;

import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * Abstraction layer to encapsulate different output
 * destinations. Configuration option &lt;key-prefix&gt; defaults to
 * "org.apache.cocoon.components.modules.output.OutputModule"+":"
 *
 * Can be used with different isolation-level: default is "0" being
 * no isolation at all, values are immediately visible but are removed
 * on a rollback; "1" keeps the values at a save place until either
 * rollback or commit is called. Then values are either discarded or
 * copied to the final destination.
 *
 * @version $Id$
 */
public class SessionAttributeOutputModule extends AbstractOutputModule
                                          implements ThreadSafe {
    
    public static final String PREFIX = "org.apache.cocoon.components.modules.output.OutputModule";
    public static final String TRANS_PREFIX = "org.apache.cocoon.components.modules.output.OutputModule.SessionAttributeOutputModule.transient";
    public static final String ROLLBACK_LIST = "org.apache.cocoon.components.modules.output.OutputModule.SessionAttributeOutputModule.rollback";
    
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
        if (this.settings.get("isolation-level","0").equals("1")) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("setting transient ['"+name+"'] to ['"+value+"']");
            this.transientSetAttribute(objectModel, TRANS_PREFIX, name, value);
        } else {
            // use read uncommitted isolation level

            HttpSession session = ObjectModelHelper.getRequest(objectModel).getSession();

            name = getName(name);

            if (!this.attributeExists(objectModel, ROLLBACK_LIST, name)) {
                Object tmp = session.getAttribute(name);
                this.transientSetAttribute(objectModel, ROLLBACK_LIST, name, tmp);
            }

            if (getLogger().isDebugEnabled())
                getLogger().debug("setting ['"+name+"'] to ['"+value+"']");
            session.setAttribute(name, value);
        }

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
        if (this.settings.get("isolation-level","0").equals("1")) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("rolling back");
            this.rollback(objectModel, TRANS_PREFIX);
        } else {

            if (getLogger().isDebugEnabled())
                getLogger().debug("start rolling back");
            
            HttpSession session = ObjectModelHelper.getRequest(objectModel).getSession();
            Object tmp = this.prepareCommit(objectModel,ROLLBACK_LIST);
            if (tmp != null) {
                Map rollbackList = (Map) tmp;
                Iterator iter = rollbackList.keySet().iterator();
                while(iter.hasNext()) {
                    String key = (String) iter.next();
                    Object val = rollbackList.get(key);
                    if (val != null) {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug("rolling back ['"+key+"'] to ['"+val+"']");
                        session.setAttribute(key, val);
                    } else {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug("rolling back ['"+key+"']");
                        session.removeAttribute(key);
                    }
                }
            }
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug("done rolling back");

        String prefix = (String) this.settings.get("key-prefix", PREFIX );
        if (!(prefix.equals(""))) {
            ObjectModelHelper.getRequest(objectModel).getSession().setAttribute(prefix+":",e.getMessage());
        } else {
            ObjectModelHelper.getRequest(objectModel).getSession().setAttribute("errorMessage",e.getMessage());
        }
    }
    
    
    /**
     * Signal that the database transaction completed
     * successfully. See notes on @link{rollback}.
     * */
    public void commit( Configuration modeConf, Map objectModel ) {
        if (this.settings.get("isolation-level","0").equals("1")) {

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("prepare commit");
            }

            Map aMap = this.prepareCommit(objectModel, TRANS_PREFIX);
            if (aMap == null) {
                return;
            }
            
            Iterator iter = aMap.keySet().iterator();
            if (!iter.hasNext()){
                return;
            }
            
            String prefix = (String) this.settings.get("key-prefix", PREFIX );
            if (prefix.length() > 0) {
                prefix = prefix + ":";
            } else {
                prefix = null;
            }
            HttpSession session = ObjectModelHelper.getRequest(objectModel).getSession();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                Object value = aMap.get(key);
                if (prefix != null) { key = prefix + key; }
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("committing ['"+key+"'] to ['"+value+"']");
                }
                session.setAttribute(key, value);
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("done commit");
            }

        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("commit");
            }

            prepareCommit(objectModel, ROLLBACK_LIST);
        }
    }
    
    protected String getName( String name ) {
        String prefix = (String) this.settings.get("key-prefix", PREFIX);
        return prefix.length() == 0 ? name : prefix + ":" + name;
    }

}
