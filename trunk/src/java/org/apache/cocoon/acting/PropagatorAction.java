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
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @author <a href="mailto:Martin.Man@seznam.cz">Martin Man</a>
 * @version CVS $Id: PropagatorAction.java,v 1.1 2004/03/11 09:14:32 cziegeler Exp $
 */
public class PropagatorAction
	extends  ServiceableAction
	implements Configurable, ThreadSafe {

    /** Role name for output modules. */
    private static final String OUTPUT_MODULE_ROLE = OutputModule.ROLE;
    
    /** Selector name for output modules. */
    private static final String OUTPUT_MODULE_SELECTOR = OUTPUT_MODULE_ROLE + "Selector";

    /** Prefix for sitemap parameters targeted at this action. */
	private static final String ACTION_PREFIX = "PropagatorAction:";

    /** Configuration parameter name. */
	private static final String CONFIG_STORE_EMPTY = "store-empty-parameters";

    /** Configuration parameter name. */
	private static final String CONFIG_OUTPUT_MODULE = "output-module";

    /** Should empty parameter values be propagated? */
	private boolean storeEmpty = true;

    /** Configuration object for output module. */
	private Configuration outputConf = null;
    
    /** Name of output module to use. */
	private String outputName = null;

    /** Default output module name. */
	private static final String outputHint = "request-attr"; // default to request attributes
    
    /** List of {@link Entry}s holding default values. */
	private List defaults = null;

    /**
     * A private helper holding default parameter entries.
     * 
     */
	private class Entry {
		public String key = null;
		public String value = null;

		public Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
	 */
	public void configure(Configuration config) throws ConfigurationException {

		this.outputConf = config.getChild(CONFIG_OUTPUT_MODULE);
		this.outputName = this.outputConf.getAttribute("name", outputHint);
		this.storeEmpty =
			config.getChild(CONFIG_STORE_EMPTY).getValueAsBoolean(this.storeEmpty);
		Configuration[] dflts = config.getChild("defaults").getChildren("default");
		if (dflts != null) {
			this.defaults = new ArrayList(dflts.length);
			for (int i = 0; i < dflts.length; i++) {
				this.defaults.add(
					new Entry(
						dflts[i].getAttribute("name"),
						dflts[i].getAttribute("value")));
			}
		} else {
			this.defaults = new ArrayList(0);
		}
	}

	/**
	 * Read parameters and remove configuration for this action.
	 * 
	 * @param param
	 * @return
	 */
	private Object[] readParameters(Parameters param) {
		String outputName =
			param.getParameter(ACTION_PREFIX + CONFIG_OUTPUT_MODULE, null);
		Boolean storeEmpty =
			new Boolean(
				param.getParameterAsBoolean(
					ACTION_PREFIX + CONFIG_STORE_EMPTY,
					this.storeEmpty));
		param.removeParameter(ACTION_PREFIX + CONFIG_OUTPUT_MODULE);
		param.removeParameter(ACTION_PREFIX + CONFIG_STORE_EMPTY);
		return new Object[] { outputName, storeEmpty };
	}

	/*
	 *  (non-Javadoc)
	 * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
	 */
	public Map act(
		Redirector redirector,
		SourceResolver resolver,
		Map objectModel,
		String source,
		Parameters parameters)
		throws Exception {

		// general setup
		OutputModule output = null;
		ServiceSelector outputSelector = null;

		// I don't like this. Have a better idea to return two values.		
		Object[] obj = this.readParameters(parameters);
		String outputName = (String) obj[0];
		boolean storeEmpty = ((Boolean) obj[1]).booleanValue();

		Configuration outputConf = null;
		if (outputName == null) {
			outputName = this.outputName;
			outputConf = this.outputConf;
		}

		Map actionMap = new HashMap();

		try {
			outputSelector =
				(ServiceSelector) this.manager.lookup(OUTPUT_MODULE_SELECTOR);
			if (outputName != null
				&& outputSelector != null
				&& outputSelector.isSelectable(outputName)) {

				output = (OutputModule) outputSelector.select(outputName);

				String[] names = parameters.getNames();

				// parameters
				for (int i = 0; i < names.length; i++) {
					String sessionParamName = names[i];
					String value = parameters.getParameter(sessionParamName);
					if (storeEmpty || (value != null && !value.equals(""))) {
						if (getLogger().isDebugEnabled()) {
							getLogger().debug(
								"Propagating value "
									+ value
									+ " to output module"
									+ sessionParamName);
						}
						output.setAttribute(
							outputConf,
							objectModel,
							sessionParamName,
							value);
						actionMap.put(sessionParamName, value);
					}
				}

				// defaults, that are not overridden
				for (Iterator i = defaults.iterator(); i.hasNext();) {
					Entry entry = (Entry) i.next();
					if (!actionMap.containsKey(entry.key)) {
						if (getLogger().isDebugEnabled()) {
							getLogger().debug(
								"Propagating default value "
									+ entry.value
									+ " to session attribute "
									+ entry.key);
						}
						output.setAttribute(
							outputConf,
							objectModel,
							entry.key,
							entry.value);
						actionMap.put(entry.key, entry.value);
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
			if (outputSelector != null) {
				if (output != null)
					outputSelector.release(output);
				this.manager.release(outputSelector);
			}
		}
		return Collections.unmodifiableMap(actionMap);
	}

}
