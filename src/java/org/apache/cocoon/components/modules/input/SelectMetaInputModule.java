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
import java.util.TreeMap;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 *
 * <h2>Configuration</h2>
 * <table><tbody>
 * <tr><th>input-module</th>
 *  <td>Configuration and name of input module used for the selection.</td>
 *  <td>req</td>
 *  <td>String</td><td><code>null</code></td>
 * </tr>
 * <tr><th>when</th>
 *  <td>Selection case, condition in test attribute, input module name
 *      in name attribute. Optional configuration as nested content.</td>
 *  <td>req</td><td>String</td><td><code>null</code></td>
 * </tr>
 * <tr><th>otherwise</th>
 *  <td>Default selection case. If not present and no case matches, <code>null</code>
 *      is returned.</td>
 *  <td></td><td>String</td><td><code>null</code></td>
 * </tr>
 * </tbody></table>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SelectMetaInputModule.java,v 1.4 2004/06/16 14:57:54 vgritsenko Exp $
 */
public class SelectMetaInputModule extends AbstractMetaModule implements ThreadSafe {

    private Map whenTest = null;
    private ModuleHolder expression = null;
    private ModuleHolder otherwise = null;
    private String parameter = null;

    public SelectMetaInputModule() {
        super();
        this.defaultInput = null; // not needed
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {

        Configuration[] expr = config.getChildren("input-module");
        if (expr == null || expr.length != 1) {
            throw new ConfigurationException("Need to have exactly one input-module element.");
        }
        this.parameter = config.getChild("parameter").getValue();
        Configuration[] whens = config.getChildren("when");
        Configuration[] others = config.getChildren("otherwise");
        if ((whens == null && others == null)
            || ((whens == null || whens.length == 0) && (others == null || others.length == 0))) {
            throw new ConfigurationException("Need to have at least one when or otherwise element.");
        }
        if (others != null && others.length > 1) {
            throw new ConfigurationException("Need to have at most one otherwise element.");
        }
        this.whenTest = new TreeMap();
        for (int i = 0; i < expr.length; i++) {
            String name = expr[i].getAttribute("name");
            this.expression = new ModuleHolder(name, expr[i], null);
        }

        if (others != null) {
            for (int i = 0; i < others.length; i++) {
                String name = others[i].getAttribute("name");
                this.otherwise = new ModuleHolder(name, others[i], null);
            }
        }

        if (whens != null) {
            for (int i = 0; i < whens.length; i++) {
                String name = whens[i].getAttribute("name");
                this.whenTest.put(
                    whens[i].getAttribute("test"),
                    new ModuleHolder(name, whens[i], null));
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(String, Configuration, Map)
     */
    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        Object result = this.getAttribute(name, modeConf, objectModel, false);
        return result;
    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        Object result = this.getAttribute(name, modeConf, objectModel, true);
        return (result != null ? (Object[]) result : null );
    }

    private Object getAttribute(String name, Configuration modeConf, Map objectModel, boolean getValues)
    throws ConfigurationException {
        if (!this.initialized) {
            this.lazy_initialize();
        }
        ModuleHolder expression = this.expression;
        ModuleHolder otherwise = this.otherwise;
        ModuleHolder module = null;
        String parameter = this.parameter;
        boolean needRelease = false;
        boolean dynamicConfig = (modeConf != null && modeConf.getChildren().length > 0);

        if (dynamicConfig) {
            // clear all configured values so that they
            // don't get mixed up
            expression = null;
            otherwise = null;
            needRelease = true;

            Configuration[] expr = modeConf.getChildren("input-module");
            Configuration[] other = modeConf.getChildren("otherwise");
            if (expr != null && expr.length == 1) {
                expression = new ModuleHolder(expr[0].getAttribute("name"), expr[0]);
            }
            if (other != null && other.length == 1) {
                otherwise = new ModuleHolder(other[0].getAttribute("name"), other[0]);
            }
            parameter = modeConf.getChild("parameter").getValue();
        }

        String value =
            (String) this.getValue(parameter, objectModel, expression.input, expression.name, expression.config);
        if (needRelease) {
            this.releaseModule(expression.input);
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug(
                (dynamicConfig ? "(dyn)" : "(static)")
                    + " select ("
                    + value
                    + ") from "
                    + expression.name
                    + ":"
                    + parameter);
        }

        if (dynamicConfig && value != null) {
            Configuration[] whens = modeConf.getChildren("when");
            if (whens != null && whens.length > 0) {
                int i = 0;
                boolean found = false;
                while (!found && i < whens.length) {
                    if (whens[i].getAttribute("test").equals(value)) {
                        found = true;
                        break;
                    }
                    i++;
                }
                if (found) {
                    module = new ModuleHolder(whens[i].getAttribute("name"), whens[i]);
                }
            }
        } else if (value != null) {
            module = (ModuleHolder) this.whenTest.get(value);
        }
        if (module != null) {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("found matching when : "+module.name);
            }
        } else {
            module = otherwise;
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("using otherwise : "+module.name);
            }
        }

        Object result;
        if (getValues){
            result = (module == null ? null : this.getValues(name, objectModel, module));
        } else {
            result = (module == null ? null : this.getValue(name, objectModel, module));
        }

        if (needRelease && module != null) {
            this.releaseModule(module.input);
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Obtained value : "+result);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        super.compose(manager);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        this.releaseModule(this.expression.input);
        this.expression = null;

        if (this.otherwise != null) {
            this.releaseModule(this.otherwise.input);
            this.otherwise = null;
        }

        for (Iterator i = this.whenTest.values().iterator(); i.hasNext();) {
            ModuleHolder holder = (ModuleHolder) i.next();
            this.releaseModule(holder.input);
        }
        this.whenTest = null;

        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.AbstractMetaModule#lazy_initialize()
     */
    public synchronized void lazy_initialize() {
        if (this.initialized) {
            return;
        }

        super.lazy_initialize();

        if (this.expression != null) {
            this.expression.input = this.obtainModule(this.expression.name);
        }
        if (this.otherwise != null){
            this.otherwise.input = this.obtainModule(this.otherwise.name);
        }
        if (this.whenTest != null){
            for (Iterator i = this.whenTest.values().iterator(); i.hasNext(); ){
                ModuleHolder moduleHolder = (ModuleHolder) i.next();
                moduleHolder.input = this.obtainModule(moduleHolder.name);
            }
        }
    }
}
