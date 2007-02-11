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
package org.apache.cocoon.components.url;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.ClassUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @deprecated by the new source resolving of avalon excalibur
 *
 * @author <a href="mailto:giacomo@apache.org">Giacomo Pati</a>
 * @version CVS $Id: URLFactoryImpl.java,v 1.3 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public class URLFactoryImpl extends AbstractLogEnabled
    implements ThreadSafe, Configurable, Disposable, Composable, Contextualizable, URLFactory {

    /**
     * The context
     */
    protected Context context;

    /**
     * The special URL factories
     */
    protected Map factories;

    /** The component manager */
    private ComponentManager manager;

    /**
     * Create a URL from a location. This method supports specific
     * pseudo-protocol as defined in its configuration
     *
     * @param location The location
     * @return The URL pointed to by the location
     * @exception MalformedURLException If the location is malformed
     */
    public URL getURL(String location) throws MalformedURLException {
        Iterator iter = factories.keySet().iterator();
        String protocol = null;
        while (iter.hasNext()) {
            protocol = (String)iter.next();
            if (location.startsWith(protocol + "://")) {
                return ((URLFactory)factories.get(protocol)).getURL(location.substring(protocol.length() + 3));
            }
        }
        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Making URL from " + location);
            }
            return new URL(location);
        } catch (MalformedURLException mue) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Making URL - MalformedURLException in getURL:" , mue);
            }

            org.apache.cocoon.environment.Context envContext = null;
            try {
                envContext = (org.apache.cocoon.environment.Context)
                                context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
            } catch (ContextException e){
                getLogger().error("Making URL - ContextException in getURL",e);
            }

            final String path = envContext.getRealPath(location);
            if (path != null)
                return (new File(path)).toURL();

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Making URL a Resource:" + location);
            }
            URL url = envContext.getResource(location);
            if(url != null)
                return url;

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Making URL a File (assuming that it is full path):" + location);
            }
            return (new File(location)).toURL();
        }
    }

    public URL getURL(URL base, String location) throws MalformedURLException {
        if ( base != null ) {
            if (base.getProtocol().equals("file")) {
                File temp = new File(base.toExternalForm().substring("file:".length()), location);
                String path = temp.getAbsolutePath();
                // VG: M$ paths starts with drive letter
                if (path.charAt(0) != File.separator.charAt(0)) {
                    return getURL("file:/" + path);
                } else {
                    return getURL("file:" + path);
                }
            }

            return getURL(new URL(base, location).toExternalForm());
        } else {
            return getURL(location);
        }
    }

    /**
     * Get the context
     */
    public void contextualize(Context context) throws ContextException {
        if (this.context == null) {
            this.context = context;
        }
    }

    /**
     * Configure the URLFactories
     */
    public void configure(final Configuration conf)
    throws ConfigurationException {
        try {
            getLogger().debug("Getting the URLFactories");
            factories = new HashMap();
            Configuration[] configs = conf.getChildren("protocol");
            URLFactory urlFactory = null;
            String protocol = null;
            for (int i = 0; i < configs.length; i++) {
                protocol = configs[i].getAttribute("name");
                if (factories.containsKey(protocol)) {
                    throw new ConfigurationException("URLFactory defined twice for protocol: " + protocol);
                }
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("\tfor protocol: " + protocol + " " + configs[i].getAttribute("class"));
                }
                urlFactory = (URLFactory) ClassUtils.newInstance(configs[i].getAttribute("class"));
                this.init(urlFactory, configs[i]);
                factories.put(protocol, urlFactory);
            }
        } catch (Exception e) {
            getLogger().error("Could not get URLFactories", e);
            throw new ConfigurationException("Could not get parameters because: " +
                                           e.getMessage());
        }
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void compose(ComponentManager manager)
    throws ComponentException {
        this.manager = manager;
    }

    /**
     * Dispose
     */
    public void dispose() {
        Iterator iter = this.factories.values().iterator();
        URLFactory current;
        while (iter.hasNext()) {
            current = (URLFactory) iter.next();
            this.deinit(current);
        }
        this.factories = null;
    }

    /**
     * Init a url factory
     */
    private void init(URLFactory factory, Configuration config)
    throws ContextException, ComponentException, ConfigurationException, ParameterException {
        if (factory instanceof LogEnabled) {
            ((LogEnabled) factory).enableLogging(getLogger());
        }
        if (factory instanceof Contextualizable) {
            ((Contextualizable) factory).contextualize (this.context);
        }
        if (factory instanceof Composable) {
            ((Composable) factory).compose(this.manager);
        }
        if (config != null && factory instanceof Configurable) {
            ((Configurable) factory).configure(config);
        }
        if (config != null && factory instanceof Parameterizable) {
            ((Parameterizable) factory).parameterize(Parameters.fromConfiguration(config));
        }
    }

    /**
     * Deinit a url factory
     */
    private void deinit(URLFactory factory) {
        if (factory instanceof Disposable) {
            ((Disposable) factory).dispose();
        }
    }
}
