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
package org.apache.cocoon.components.source.impl;

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
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.util.ClassUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.impl.URLSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * This class wraps a Cocoon URLFactory and makes it
 * usable within the Avalon Excalibur source resolving architecure.
 * The main purpose is to avoid recoding existing factories.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: URLFactoryWrapper.java,v 1.2 2004/03/05 13:02:41 bdelacretaz Exp $
 */

public final class URLFactoryWrapper
    extends AbstractLogEnabled
    implements SourceFactory,
               ThreadSafe,
               Configurable,
               Disposable,
               Composable,
               Contextualizable
{
    /** The <code>ComponentManager</code> */
    private ComponentManager manager;

    /** The special Source factories */
    private URLFactory urlFactory;

    /** The context */
    private Context context;

    /**
     * Configure the SourceFactories
     */
    public void configure(final Configuration conf)
    throws ConfigurationException {

        try {
            final Configuration factoryConf = conf.getChild("url-factory");
            final String className = factoryConf.getAttribute("class");
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Getting the URLFactory " + className);
            }
            this.urlFactory = (URLFactory)ClassUtils.newInstance(className);
            this.init(this.urlFactory, factoryConf);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Could not get parameters because: " +
                                           e.getMessage(), e);
        }
    }

    /**
     * Get the context
     */
    public void contextualize(Context context)
    throws ContextException {
        this.context = context;
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
        if (this.urlFactory != null) {
            this.deinit(this.urlFactory);
        }
        this.urlFactory = null;
    }


    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource( String location, Map parameters )
        throws MalformedURLException, IOException
    {
        if( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "Creating source object for " + location );
        }

        final int protocolPos = location.indexOf("://");
        final URL url = this.urlFactory.getURL(location.substring(protocolPos+3));
        final URLSource source = new org.apache.excalibur.source.impl.URLSource();
        source.init(url, parameters);
        return source;
    }

    /**
     * Init a url factory
     */
    private void init(URLFactory factory,
                      Configuration config)
    throws ContextException, ComponentException, ConfigurationException {
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
    }

    /**
     * Deinit a url factory
     */
    private void deinit(URLFactory factory) {
        if (factory instanceof Disposable) {
            ((Disposable) factory).dispose();
        }
    }

    /**
     * Release a {@link Source} object.
     */
    public void release( Source source ) {
        if ( null != source ) {
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Releasing source " + source.getURI());
            }
            // do simply nothing
        }
    }

}
