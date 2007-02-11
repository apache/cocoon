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

package org.apache.cocoon.components.modules.input;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.cocoon.util.HashMap;

/**
 * AbstractInputModule gives you the infrastructure for easily
 * deploying more InputModules.  In order to get at the Logger, use
 * getLogger().
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: AbstractInputModule.java,v 1.4 2004/03/05 13:02:48 bdelacretaz Exp $
 */
public abstract class AbstractInputModule extends AbstractLogEnabled
    implements InputModule, Configurable, Disposable {

    /**
     * For those modules that access only one attribute, have a 
     * fixed collection we can return an iterator for.
     */
    final static Vector returnNames;
    static {
        Vector tmp = new Vector();
        tmp.add("attribute");
        returnNames = tmp;
    }



    /**
     * Stores (global) configuration parameters as <code>key</code> /
     * <code>value</code> pairs.
     */
    protected HashMap settings = null;

    /**
     * Configures the database access helper.
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
            this.settings.put (key, val);
        }
    }

    /**
     *  dispose
     */
    public void dispose() {
        // Purposely empty so that we don't need to implement it in every
        // class.
    }
    
    //
    // you need to implement at least one of the following two methods
    // since the ones below have a cyclic dependency!
    // 
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object getAttribute(String name, Configuration modeConf, Map objectModel) throws ConfigurationException {
        Object[] result = this.getAttributeValues(name, modeConf, objectModel);
        return (result == null ? null : result[0]);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {
        Object result = this.getAttribute(name, modeConf, objectModel);
        return (result == null ? null : new Object[] {result});
    }


    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Iterator getAttributeNames(Configuration modeConf, Map objectModel) throws ConfigurationException {
        return AbstractInputModule.returnNames.iterator();
    }
}
