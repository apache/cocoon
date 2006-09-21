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
package org.apache.cocoon.components.modules.input;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Iterator;
import java.util.Map;

/** 
 * Meta module that obtains an Object from another module, assumes
 * that this Object implements the java.util.Map interface, and gives
 * access to the map contents. Possible use is to propagate data from
 * flow through request attributes to database actions.
 * The same can be achieved by using the {@link JXPathMetaModule}.
 *
 * <p>Configuration: "input-module", "object", "parameter"</p>
 *
 * @version $Id$
 */
public class MapMetaModule extends AbstractMetaModule implements ThreadSafe {

    protected String objectName;
    protected String parameter;

    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name", this.defaultInput);
        this.objectName = this.inputConf.getAttribute("object",this.objectName);
        this.parameter = this.inputConf.getAttribute("parameter",this.parameter);

        // preferred
        this.objectName = config.getChild("object").getValue(this.objectName);
        this.parameter = config.getChild("parameter").getValue(this.parameter);
    }


    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object getAttribute( String name, Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        if (!this.initialized) {
            this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        // obtain correct configuration objects
        // default vs dynamic
        String inputName=null;
        String objectName = this.objectName;
        String parameter = this.parameter;
        if (modeConf != null) {
            inputName  = modeConf.getChild("input-module").getAttribute("name",null);
            objectName = modeConf.getAttribute("object",objectName);
            parameter  = modeConf.getAttribute("parameter",parameter);

            // preferred
            objectName = modeConf.getChild("object").getValue(objectName);
            parameter  = modeConf.getChild("parameter").getValue(parameter);
        }
        parameter = (parameter != null? parameter : name);

        Object value = getValue(objectName, objectModel, 
                                this.input, this.defaultInput, this.inputConf, 
                                null, inputName, modeConf);

        value = (value!=null? ((Map) value).get(parameter) : null);

        return value;        
    }

    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        if (!this.initialized) {
             this.lazy_initialize();
        }
        if (this.defaultInput == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration inputConfig = null;
        String inputName=null;
        String objectName = this.objectName;
        if (modeConf!=null) {
            inputName   = modeConf.getChild("input-module").getAttribute("name",null);
            objectName = modeConf.getAttribute("object",this.objectName);

            // preferred
            objectName = modeConf.getChild("object").getValue(objectName);
            if (inputName != null) {
                inputConfig = modeConf.getChild("input-module");
            }
        }

        Iterator keys = ((Map) getValue(objectName, objectModel, 
                                        this.input, this.defaultInput, this.inputConf,
                                        null, inputName, inputConfig)).keySet().iterator();

        return keys;        
    }

    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        Object[] values = new Object[1];
        values[0] = this.getAttribute(name, modeConf, objectModel);
        return (values[0]!=null?values:null);
    }
}
