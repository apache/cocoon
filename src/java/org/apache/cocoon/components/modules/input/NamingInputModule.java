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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * NamingInputModule accesses values stored in the JNDI context.
 *
 * <p>This module accept any configuration parameters and passes them as
 * properties to the InitialContext. When connecting to the Naming context
 * of the server Cocoon is running in, no parameters are required.</p>
 *
 * <p>Example module configuration when connecting to external WebLogic server:
 * <pre>
 *   &lt;java.naming.factory.initial&gt;weblogic.jndi.WLInitialContextFactory&lt;/java.naming.factory.initial&gt;
 *   &lt;java.naming.provider.url&gt;t3://localhost:7001&lt;/java.naming.provider.url&gt;
 * </pre>
 *
 * <p>Example usage:
 * <pre>
 *   &lt;map:generate src="{naming:java:comp/env/greeting}"/&gt;
 * </pre>
 * This lookups <code>greeting</code> entry from the environment of the webapp.
 * Webapp's web.xml should define this entry:
 * <pre>
 *   &lt;env-entry&gt;
 *     &lt;env-entry-name&gt;greeting&lt;/env-entry-name&gt;
 *     &lt;env-entry-value&gt;Hello, World&lt;/env-entry-value&gt;
 *     &lt;env-entry-type&gt;java.lang.String&lt;/env-entry-type&gt;
 *   &lt;/env-entry&gt;
 * </pre>
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: NamingInputModule.java,v 1.2 2004/06/16 20:22:10 vgritsenko Exp $
 */
public class NamingInputModule extends AbstractInputModule implements ThreadSafe, Initializable {

    /**
     * Initial context properties.
     */
    private Properties properties;

    /**
     * Initial context.
     */
    private InitialContext context;

    /**
     * Fill in InitialContext properties from passed configuration.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        Configuration[] parameters = conf.getChildren();
        this.properties = new Properties();
        for (int i = 0; i < parameters.length; i++) {
            String key = parameters[i].getName();
            String val = parameters[i].getValue("");
            this.properties.put(key, val);
        }
    }

    /**
     * Creates InitialContext with configured properties.
     */
    public void initialize() throws Exception {
        this.context = new InitialContext(this.properties);
    }

    /**
     * Close InitialContext.
     */
    public void dispose() {
        super.dispose();
        if (this.context != null) {
            try {
                this.context.close();
            } catch (NamingException ignored) {
            }
        }
    }

    /**
     * Look up <code>name</code> from the InitialContext.
     */
    public Object getAttribute(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {

        // Why properties can override passed name parameter? See RequestParameterModule
        String pname = (String) this.properties.get("path");
        if (pname == null) {
            pname = name;
        }

        if (modeConf != null) {
            pname = modeConf.getAttribute("path", pname);
            // preferred
            pname = modeConf.getChild("path").getValue(pname);
        }

        try {
            return this.context.lookup(pname);
        } catch (NamingException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Can't get parameter " + pname, e);
            }
            return null;
        }
    }

    /**
     * Returns empty iterator
     */
    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
    throws ConfigurationException {

        return Collections.EMPTY_LIST.iterator();
    }
}
