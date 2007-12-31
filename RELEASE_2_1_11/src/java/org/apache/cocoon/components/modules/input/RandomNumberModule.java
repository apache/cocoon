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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * RandomNumberModule returns a random number as a string.
 * Configuration through child elements: "min", "max" setting
 * range of random number. Defaults to "0" and "9999999999"
 * respectively.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version $Id$
 */
public class RandomNumberModule extends AbstractInputModule implements ThreadSafe {

    final static Vector returnNames;
    static {
        Vector tmp = new Vector();
        tmp.add("randomNumber");
        returnNames = tmp;
    }

    public Object getAttribute(String name, Configuration modeConf, Map objectModel) throws ConfigurationException {
        long min = Long.parseLong((String)this.settings.get("min", "0"));
        long max = Long.parseLong((String)this.settings.get("max", String.valueOf(Long.MAX_VALUE)));
        if (modeConf != null) {
            min = modeConf.getAttributeAsLong("max", min);
            max = modeConf.getAttributeAsLong("max", max);

            //preferred
            min = modeConf.getChild("min").getValueAsLong(min);
            max = modeConf.getChild("max").getValueAsLong(max);
        }
        return Long.toString(min + Math.round(Math.random()*(max-min)));
    }

    public Iterator getAttributeNames(Configuration modeConf, Map objectModel) throws ConfigurationException {
        return RandomNumberModule.returnNames.iterator();
    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {
            List values = new LinkedList();
            values.add(this.getAttribute(name, modeConf, objectModel));
            return values.toArray();
    }
}
