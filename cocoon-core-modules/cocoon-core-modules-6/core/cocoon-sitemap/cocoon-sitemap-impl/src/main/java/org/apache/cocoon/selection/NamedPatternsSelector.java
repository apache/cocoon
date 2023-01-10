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
package org.apache.cocoon.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Abstract class for selectors that select a value when it matches
 * some patterns associated to the select expression.
 *
 * <p>Known implementations of this abstract class include <code>BrowserSelector</code>,
 * <code>HostSelector</code> from <code>cocoon-sitemap-components</code> maven module.
 *
 * @version $Id$
 */
public abstract class NamedPatternsSelector extends AbstractLogEnabled
                                            implements Configurable, ThreadSafe, Selector {

    /**
     * Association of names to String[] of values.
     */
    private Map strings;

    /**
     * Setup the association from expressions to a list of patterns. The configuration
     * should look like :
     * &lt;pre&gt;
     *  &lt;map:selector name="foo" src="..."&gt;
     *    &lt;confName nameAttr="expression" valueAttr="pattern"/&gt;
     *    ... others (expression, pattern) associations ...
     *  &lt;/map:selector&gt;
     * &lt;/pre&gt;
     *
     * @param conf the configuration
     * @param confName the name of children of <code>conf</code> that will be used to
     *            build associations
     * @param nameAttr the name of the attribute that holds the expression
     * @param valueAttr the name of the attribute that holds the pattern
     */
    protected void configure(Configuration conf, String confName, String nameAttr, String valueAttr)
      throws ConfigurationException {
        Configuration confs[] = conf.getChildren(confName);
        Map configMap = new HashMap();

        // Build a list of strings for each name
        for (int i = 0; i < confs.length; i++) {
            String name = confs[i].getAttribute(nameAttr);
            String value = confs[i].getAttribute(valueAttr);

            // Get value list for this name
            List nameList = (List)configMap.get(name);
            if (nameList == null) {
                nameList = new ArrayList();
                configMap.put(name, nameList);
            }

            // Add the current value
            nameList.add(value);
        }

        // Convert lists to arrays for faster lookup
        Iterator entries = configMap.entrySet().iterator();
        while(entries.hasNext()) {
            Map.Entry entry = (Map.Entry)entries.next();
            List nameList = (List)entry.getValue();
            entry.setValue(nameList.toArray(new String[nameList.size()]));
        }

        this.strings = configMap;
    }

    /**
     * Checks if <code>value</code> is a (case-sensitive) substring of one of the patterns associated
     * to <code>expression</code>
     *
     * @param expression the expression that is selected
     * @param value the value to check
     * @return true if <code>value</code> matches one of the patterns
     */
    protected boolean checkPatterns(String expression, String value) {
        return checkPatterns(expression, value, true);
    }

    /**
     * Checks if <code>value</code> is a substring of one of the patterns associated
     * to <code>expression</code>
     *
     * @param expression the expression that is selected
     * @param value the value to check
     * @param caseSensitive boolean switch whether comparison is done case-sensitive  
     * @return true if <code>value</code> matches one of the patterns
     */
    protected boolean checkPatterns(String expression, String value, boolean caseSensitive) {
        if (value == null) {
            getLogger().debug("No value given -- failing.");
            return false;
        }
        // Get patterns for 'expression'
        String[] patterns = (String[])this.strings.get(expression);
        if (patterns == null) {
            getLogger().warn("No configuration for expression '" + expression + "' -- failing.");
            return false;
        }

        if (!caseSensitive) {
            value = value.toLowerCase();
        }

        // Does a pattern match 'value' ?
        for (int i = 0; i < patterns.length; i++) {
            if ((caseSensitive && value.indexOf(patterns[i]) != -1)
                || (!caseSensitive && value.indexOf(patterns[i].toLowerCase()) != -1)) {
                getLogger().debug(expression + " selected value " + value);
                return true;
            }
        }

        // No match
        return false;
    }

}
