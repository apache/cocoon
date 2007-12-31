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

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

import java.util.Iterator;
import java.util.Map;

/**
 * Abstraction layer to encapsulate different output
 * destinations. Configuration option &lt;key-prefix&gt; defaults to
 * <code>"org.apache.cocoon.components.modules.output.OutputModule" + ":"</code>
 *
 * <p>Can be used with different isolation-level: default is "0" being
 * no isolation at all, values are immediately visible but are removed
 * on a rollback; "1" keeps the values at a safe place until either
 * rollback or commit is called. Then values are either discarded or
 * copied to the final destination.</p>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id$
 */
public class RequestAttributeOutputModule extends AbstractOutputModule {

    public static final String PREFIX = OutputModule.ROLE;
    public static final String TRANS_PREFIX = PREFIX + ".RequestAttributeOutputModule.transient";
    public static final String ROLLBACK_LIST = PREFIX + ".RequestAttributeOutputModule.rollback";

    /**
     * communicate an attribute value to further processing logic.
     * @param modeConf column's mode configuration from resource
     *                 description. This argument is optional.
     * @param objectModel The objectModel
     * @param name The attribute's label, consisting of "table.column"
     *             or "table.column[index]" in case of multiple attributes
     *             of the same spec.
     * @param value The attriute's value.
     */
    public void setAttribute(Configuration modeConf, Map objectModel, String name, Object value) {
        if (this.settings.get("isolation-level", "0").equals("1")) {
            // Read committed isolation level
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Setting transient ['" + name + "'] to ['" + value + "']");
            }
            transientSetAttribute(objectModel, TRANS_PREFIX, name, value);
        } else {
            // Read uncommitted isolation level
            final Request request = ObjectModelHelper.getRequest(objectModel);

            name = getName(name);

            if (!attributeExists(objectModel, ROLLBACK_LIST, name)) {
                Object tmp = request.getAttribute(name);
                transientSetAttribute(objectModel, ROLLBACK_LIST, name, tmp);
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Setting ['" + name + "'] to ['" + value + "']");
            }
            request.setAttribute(name, value);
        }
    }

    /**
     * If a database transaction needs to rollback, this is called to
     * inform the further processing logic about this fact. All
     * already set attribute values are invalidated.
     *
     * <em>This is difficult
     * because only the request object can be used to synchronize this
     * and build some kind of transaction object. Beware that sending
     * your data straight to some beans or other entities could result
     * in data corruption!</em>
     */
    public void rollback(Configuration modeConf, Map objectModel, Exception e) {
        getLogger().debug("Rollback");
        final Request request = ObjectModelHelper.getRequest(objectModel);

        if (this.settings.get("isolation-level", "0").equals("1")) {
            rollback(objectModel, TRANS_PREFIX);
        } else {
            Map rollbackList = prepareCommit(objectModel, ROLLBACK_LIST);
            if (rollbackList != null) {
                for (Iterator i = rollbackList.entrySet().iterator(); i.hasNext();) {
                    final Map.Entry me = (Map.Entry) i.next();
                    String key = (String) me.getKey();
                    Object val = me.getValue();
                    if (val != null) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Rolling back ['" + key + "'] to ['" + val + "']");
                        }
                        request.setAttribute(key, val);
                    } else {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Rolling back ['" + key + "']");
                        }
                        request.removeAttribute(key);
                    }
                }
            }
        }

        String prefix = (String) this.settings.get("key-prefix", PREFIX);
        if (prefix.length() == 0) {
            request.setAttribute("errorMessage", e.getMessage());
        } else {
            request.setAttribute(prefix + ':' + "errorMessage", e.getMessage());
        }
    }

    /**
     * Signal that the database transaction completed
     * successfully. See notes on @link{rollback}.
     */
    public void commit(Configuration modeConf, Map objectModel) {
        getLogger().debug("Commit");
        if (this.settings.get("isolation-level", "0").equals("1")) {
            Map data = prepareCommit(objectModel, TRANS_PREFIX);
            if (data == null || data.isEmpty()) {
                return;
            }

            String prefix = (String) this.settings.get("key-prefix", PREFIX);
            if (prefix.length() == 0) {
                prefix = null;
            }

            Request request = ObjectModelHelper.getRequest(objectModel);
            for (Iterator i = data.entrySet().iterator(); i.hasNext();) {
                final Map.Entry me = (Map.Entry) i.next();
                String key = (String) me.getKey();
                Object value = me.getValue();
                if (prefix != null) {
                    key = prefix + ':' + key;
                }
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Committing ['" + key + "'] to ['" + value + "']");
                }
                request.setAttribute(key, value);
            }
        } else {
            prepareCommit(objectModel, ROLLBACK_LIST);
        }
    }

    protected String getName(String name) {
        String prefix = (String) this.settings.get("key-prefix", PREFIX);
        return prefix.length() == 0 ? name : prefix + ':' + name;
    }
}
