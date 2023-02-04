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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Set a number of constants. To override the values with input from
 * another module, combine this one with the ChainMetaModule and an
 * arbitrary number of other modules.
 *
 * <pre>
 * &lt;values&gt;
 *  &lt;skin&gt;myskin&lt;/skin&gt;
 *  &lt;base&gt;baseurl&lt;/base&gt;
 *  ...
 * &lt;/values&gt;
 * </pre>
 * 
 * @version $Id$
 */
public class DefaultsModule extends AbstractLogEnabled
                            implements InputModule, Configurable, ThreadSafe {

    private Map constants;


    public void configure(Configuration config) throws ConfigurationException {
        
        this.constants = new HashMap();
        Configuration[] consts = config.getChild("values").getChildren();
        for (int i = 0; i < consts.length; i++) {
            this.constants.put(consts[i].getName(), consts[i].getValue(""));
        }
    }

    public Object[] getAttributeValues( String name, Configuration modeConf, Map objectModel )
    throws ConfigurationException {

        String parameter = name;
        Configuration mConf = null;
        if (modeConf != null) {
            mConf = modeConf.getChild("values");
        }

        Object[] values = new Object[1];
        values[0] = mConf != null ? mConf.getChild(parameter).getValue((String) this.constants.get(parameter))
                : this.constants.get(parameter);
        return values;
    }


    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {

        SortedSet matchset = new TreeSet(this.constants.keySet());
        if (modeConf != null) {
            Configuration[] consts = modeConf.getChild("values").getChildren();
            for (int i = 0; i < consts.length; i++)
                matchset.add(consts[i].getName());
        }

        return matchset.iterator();
    }


    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {

        Object[] values = this.getAttributeValues(name, modeConf, objectModel);
        return values[0];
    }
}
