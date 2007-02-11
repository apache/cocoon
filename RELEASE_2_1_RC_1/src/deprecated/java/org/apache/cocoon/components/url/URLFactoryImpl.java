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
 * @version CVS $Id: URLFactoryImpl.java,v 1.2 2003/03/16 17:49:11 vgritsenko Exp $
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
