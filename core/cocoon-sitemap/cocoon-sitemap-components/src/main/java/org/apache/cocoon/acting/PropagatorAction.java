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
package org.apache.cocoon.acting;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.modules.output.OutputModule;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is the action used to propagate parameters into a store using an
 * {@link org.apache.cocoon.components.modules.output.OutputModule}. It
 * simply propagates given expression. Additionaly it will make all propagated values
 * available via returned Map.
 *
 * <p>Example configuration:</p>
 * <pre>
 * &lt;map:action type="...." name="...." logger="..."&gt;
 *   &lt;output-module name="session-attr"&gt;
 *      &lt;!-- optional configuration for output module --&gt;
 *   &lt;/output-module&gt;
 *   &lt;store-empty-parameters&gt;true&lt;/store-empty-parameters&gt;
 *   &lt;defaults&gt;
 *     &lt;default name="..." value="...."/&gt;
 *     &lt;default name="..." value="..."/&gt;
 *   &lt;/defaults&gt;
 * &lt;/map:action&gt;
 * </pre>
 *
 * <p>Example use:</p>
 * <pre>
 * &lt;map:act type="session-propagator"&gt;
 *      &lt;paramater name="example" value="{example}"/&gt;
 *      &lt;paramater name="example1" value="xxx"/&gt;
 *      &lt;parameter name="PropagatorAction:store-empty-parameters" value="true"/&gt;
 *      &lt;parameter name="PropagatorAction:output-module" value="session-attr"/&gt;
 * &lt;/map:act&gt;
 * </pre>
 *
 * <h3>Configuration</h3>
 * <table><tbody>
 * <tr>
 *  <th>output-module</th>
 *  <td>Nested element configuring output to use. Name attribute holds
 *      output module hint.</td>
 *  <td></td><td>XML</td><td><code>request-attr</code></td>
 * </tr>
 * <tr>
 *  <th>store-empty-parameters</th>
 *  <td>Propagate parameters with empty values.</td>
 *  <td></td><td>boolean</td><td><code>true</code></td>
 * </tr>
 * <tr>
 *  <th>defaults</th>
 *  <td>Parent for default parameters to propagate.</td>
 *  <td></td><td>XML</td><td></td>
 * </tr>
 * <tr>
 *  <th>defaults/default</th>
 *  <td>Name attribute holds parameter name, value attribute holds
 *      parameter value. Will be used when not set on use.</td>
 *  <td></td><td>parameter</td><td></td>
 * </tr>
 * </tbody></table>
 *
 *<h3>Parameters</h3>
 * <table><tbody>
 * <tr>
 *  <th>PropagatorAction:output-module</th>
 *  <td>Alternative output module hint to use. A <code>null</code> configuration
 *      will be passed to a module selected this way.</td>
 *  <td></td><td>String</td><td>as determined by configuration</td>
 * </tr>
 * <tr>
 *  <th>PropagatorAction:store-empty-parameters</th>
 *  <td>Propagate parameters with empty values.</td>
 *  <td></td><td>boolean</td><td>as determined by configuration</td>
 * </tr>
 * <tr>
 *  <th>any other</th>
 *  <td>Any other parameter will be propagated.</td>
 *  <td></td><td>String</td><td></td>
 * </tr>
 * </tbody></table>
 *
 * @cocoon.sitemap.component.documentation
 * This is the action used to propagate parameters into a store using an
 * {@link org.apache.cocoon.components.modules.output.OutputModule}. It
 * simply propagates given expression. Additionaly it will make all propagated values
 * available via returned Map.
 *
 * @version $Id$
 */
public class PropagatorAction extends ServiceableAction
                              implements Configurable, ThreadSafe {

    /** Prefix for sitemap parameters targeted at this action. */
    private static final String ACTION_PREFIX = "PropagatorAction:";

    /** Configuration parameter name. */
    private static final String CONFIG_STORE_EMPTY = "store-empty-parameters";

    /** Configuration parameter name. */
    private static final String CONFIG_OUTPUT_MODULE = "output-module";

    /** Default output module name. */
    private static final String OUTPUT_HINT = "request-attr"; // defaults to request attributes


    /** Should empty parameter values be propagated? */
    private boolean storeEmpty = true;

    /** Configuration object for output module. */
    private Configuration outputConf;

    /** Name of output module to use. */
    private String outputName;

    /** List of {@link Entry}s holding default values. */
    private List defaults;

    /**
     * A private helper holding default parameter entries.
     *
     */
    private static class Entry {
        public String key;
        public String value;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.outputConf = config.getChild(CONFIG_OUTPUT_MODULE);
        this.outputName = this.outputConf.getAttribute("name", OUTPUT_HINT);
        this.storeEmpty =
            config.getChild(CONFIG_STORE_EMPTY).getValueAsBoolean(this.storeEmpty);

        Configuration[] dflts = config.getChild("defaults").getChildren("default");
        if (dflts != null) {
            this.defaults = new ArrayList(dflts.length);
            for (int i = 0; i < dflts.length; i++) {
                this.defaults.add(
                        new Entry(dflts[i].getAttribute("name"),
                                  dflts[i].getAttribute("value")));
            }
        } else {
            this.defaults = new ArrayList(0);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.acting.Action#act(Redirector, SourceResolver, Map, String, Parameters)
     */
    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters parameters)
    throws Exception {
        // Read action parameters
        String outputName = parameters.getParameter(ACTION_PREFIX + CONFIG_OUTPUT_MODULE,
                                                    null);
        boolean storeEmpty = parameters.getParameterAsBoolean(ACTION_PREFIX + CONFIG_STORE_EMPTY,
                                                              this.storeEmpty);
        parameters.removeParameter(ACTION_PREFIX + CONFIG_OUTPUT_MODULE);
        parameters.removeParameter(ACTION_PREFIX + CONFIG_STORE_EMPTY);

        Configuration outputConf = null;
        if (outputName == null) {
            outputName = this.outputName;
            outputConf = this.outputConf;
        }

        // Action results map
        final Map results = new HashMap();

        OutputModule output = null;
        ServiceSelector selector = null;
        try {
            selector = (ServiceSelector) this.manager.lookup(OutputModule.ROLE + "Selector");
            if (outputName != null
                && selector != null
                && selector.isSelectable(outputName)) {

                output = (OutputModule) selector.select(outputName);

                String[] names = parameters.getNames();
                for (int i = 0; i < names.length; i++) {
                    String name = names[i];
                    String value = parameters.getParameter(name);
                    if (storeEmpty || (value != null && !value.equals(""))) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Propagating <" + name + "> value <" + value + ">");
                        }
                        output.setAttribute(outputConf,
                                            objectModel,
                                            name,
                                            value);
                        results.put(name, value);
                    }
                }

                // Defaults, that are not overridden
                for (Iterator i = defaults.iterator(); i.hasNext();) {
                    Entry entry = (Entry) i.next();
                    if (!results.containsKey(entry.key)) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Propagating default <" + entry.key + "> value <" + entry.value + ">");
                        }
                        output.setAttribute(outputConf,
                                            objectModel,
                                            entry.key,
                                            entry.value);
                        results.put(entry.key, entry.value);
                    }
                }

                output.commit(outputConf, objectModel);
            }
        } catch (Exception e) {
            if (output != null) {
                output.rollback(outputConf, objectModel, e);
            }
            throw e;
        } finally {
            if (selector != null) {
                if (output != null) {
                    selector.release(output);
                }
                this.manager.release(selector);
            }
        }

        return Collections.unmodifiableMap(results);
    }
}
