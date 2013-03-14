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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.HashMap;

import java.util.Map;

/**
 * AbstractOutputModule gives you the infrastructure for easily
 * deploying more output modules.
 *
 * <p>In order to get at the logger, use <code>getLogger()</code>.</p>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id$
 */
public abstract class AbstractOutputModule extends AbstractLogEnabled
    implements OutputModule, Configurable, Disposable {

    /**
     * Stores (global) configuration parameters as <code>key</code> /
     * <code>value</code> pairs.
     */
    protected HashMap settings;

    /**
     * Configures the module.
     *
     * <p>Takes all elements nested in component declaration and stores
     * them as key-value pairs in <code>settings</code>. Nested
     * configuration option are not catered for. This way global
     * configuration options can be used.</p>
     *
     * <p>For nested configurations override this function.</p>
     */
    public void configure(Configuration conf) throws ConfigurationException {
        Configuration[] parameters = conf.getChildren();
        // Ideally here should be length * 1.333(3) but simple +1 will do for lengths up to 3
        this.settings = new HashMap(parameters.length + 1);
        for (int i = 0; i < parameters.length; i++) {
            String key = parameters[i].getName();
            String val = parameters[i].getValue("");
            this.settings.put(key, val);
        }
    }

    /**
     * Dispose
     */
    public void dispose() {
        // Implemeted so that we don't need to implement it in every subclass
        this.settings = null;
    }

    /**
     * Utility method to store parameters in a map as request attribute until
     * either {@link #rollback(Map, String)} or {@link #prepareCommit(Map, String)}
     * is called.
     * @param objectModel - the objectModel
     * @param trans_place - request attribute name used for the transient data
     * @param name - name of the attribute to set
     * @param value - attribute value
     */
    protected void transientSetAttribute(Map objectModel, String trans_place, String name, Object value) {
        final Request request = ObjectModelHelper.getRequest(objectModel);

        Map map = (Map) request.getAttribute(trans_place);
        if (map == null) {
            // Need java.util.HashMap here since JXPath does not like the extended version...
            map = new java.util.HashMap();
        }

        map.put(name, value);
        request.setAttribute(trans_place, map);
    }

    /**
     * Clears all uncommitted transient attributes.
     *
     * @param objectModel - the objectModel
     * @param trans_place - request attribute name used for the transient data
     */
    protected void rollback(Map objectModel, String trans_place) {
        ObjectModelHelper.getRequest(objectModel).removeAttribute(trans_place);
    }

    /**
     * Returns a whether an transient attribute already exists.
     * {@link #transientSetAttribute(Map, String, String, Object)} since the last call to
     * {@link #rollback(Map, String)} or {@link #prepareCommit(Map, String)}
     *
     * @param objectModel - the objectModel
     * @param trans_place - request attribute name used for the transient data
     */
    protected boolean attributeExists(Map objectModel, String trans_place, String name) {
        final Request request = ObjectModelHelper.getRequest(objectModel);

        Map map = (Map) request.getAttribute(trans_place);
        if (map == null) {
            return false;
        }

        return map.containsKey(name);
    }

    /**
     * Returns a map containing all transient attributes and remove them i.e. attributes set with
     * {@link #transientSetAttribute(Map, String, String, Object)} since the last call to
     * {@link #rollback(Map, String)} or {@link #prepareCommit(Map, String)}
     *
     * @param objectModel - the objectModel
     * @param trans_place - request attribute name used for the transient data
     */
    protected Map prepareCommit(Map objectModel, String trans_place) {
        final Request request = ObjectModelHelper.getRequest(objectModel);

        Map data = (Map) request.getAttribute(trans_place);
        request.removeAttribute(trans_place);
        return data;
    }
}
