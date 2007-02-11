/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.selection;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for selectors that select a value when it matches
 * some patterns associated to the select expression.
 *
 * @see BrowserSelector
 * @see HostSelector
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: NamedPatternsSelector.java,v 1.1 2003/03/09 00:09:35 pier Exp $
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
     * Checks if <code>value</code> is a substring of one of the patterns associated
     * to <code>expression</code>
     *
     * @param expression the expression that is selected
     * @param value the value to check
     * @return true if <code>value</code> matches one of the patterns
     */
    protected boolean checkPatterns(String expression, String value) {
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

        // Does a pattern match 'value' ?
        for (int i = 0; i < patterns.length; i++) {
            if (value.indexOf(patterns[i]) != -1) {
                getLogger().debug(expression + " selected value " + value);
                return true;
            }
        }

        // No match
        return false;
    }

}
