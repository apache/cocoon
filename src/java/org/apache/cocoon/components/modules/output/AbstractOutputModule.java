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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.HashMap;
//import java.util.HashMap;

/**
 * AbstractOutputModule gives you the infrastructure for easily
 * deploying more output modules.  In order to get at the
 * Logger, use getLogger().
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: AbstractOutputModule.java,v 1.4 2004/03/05 13:02:49 bdelacretaz Exp $
 */
public abstract class AbstractOutputModule extends AbstractLogEnabled
    implements OutputModule, Configurable, Disposable {

    /**
     * Stores (global) configuration parameters as <code>key</code> /
     * <code>value</code> pairs.
     */
    protected HashMap settings = null;

    /**
     * Configures the module.
     *
     * Takes all elements nested in component declaration and stores
     * them as key-value pairs in <code>settings</code>. Nested
     * configuration option are not catered for. This way global
     * configuration options can be used.
     *
     * For nested configurations override this function.
     * */
    public void configure(Configuration conf) throws ConfigurationException {
        Configuration[] parameters = conf.getChildren();
        this.settings = new HashMap(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            String key = parameters[i].getName();
            String val = parameters[i].getValue("");
            this.settings.put(key, val);
        }
    }

    /**
     *  dispose
     */
    public void dispose() {
        // Purposely empty so that we don't need to implement it in every
        // class.
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
    protected void transientSetAttribute( Map objectModel, String trans_place, String name, Object value ) {

        Request request = ObjectModelHelper.getRequest(objectModel);
        Object temp = request.getAttribute(trans_place);
        Map aMap = null;

        if (temp == null) {           
            aMap = new java.util.HashMap();
            // need java.util.HashMap here since JXPath does not like the extended version...
        } else {
            aMap = (Map) temp;
        }

        aMap.put(name,value);

        request.setAttribute(trans_place, aMap);
    }

    /**
     * Clears all uncommitted transient attributes.
     * @param objectModel - the objectModel
     * @param trans_place - request attribute name used for the transient data
     */    
    protected void rollback( Map objectModel, String trans_place) {
        ObjectModelHelper.getRequest(objectModel).setAttribute(trans_place, null);
    }

    /**
     * Returns a whether an transient attribute already exists.
     * {@link #transientSetAttribute(Map, String, String, Object)} since the last call to 
     * {@link #rollback(Map, String)} or {@link #prepareCommit(Map, String)}
     * @param objectModel - the objectModel
     * @param trans_place - request attribute name used for the transient data
     */    
    protected boolean attributeExists( Map objectModel, String trans_place, String name )
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Object temp = request.getAttribute(trans_place);
        if (temp == null) {
            return false;
        } else {
            return ((Map) temp).containsKey(name);
        }
    }

    /**
     * Returns a map containing all transient attributes and remove them i.e. attributes set with 
     * {@link #transientSetAttribute(Map, String, String, Object)} since the last call to 
     * {@link #rollback(Map, String)} or {@link #prepareCommit(Map, String)}
     * @param objectModel - the objectModel
     * @param trans_place - request attribute name used for the transient data
     */    
    protected Map prepareCommit( Map objectModel, String trans_place )
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        Object temp = request.getAttribute(trans_place);
        request.setAttribute(trans_place, null);
        if (temp == null) {
            return null;
        } else {
            return (Map) temp;
        }
    }

}
