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
package org.apache.cocoon.components.source;

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
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Source;
import org.apache.cocoon.util.ClassUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @deprecated The Avalon Excalibur Source Resolving is now used.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SourceHandlerImpl.java,v 1.2 2003/03/16 17:49:10 vgritsenko Exp $
 */
public final class SourceHandlerImpl extends AbstractLogEnabled
    implements Configurable, Disposable, Composable, Contextualizable, SourceHandler {

    /** The component manager */
    private ComponentManager manager;

    /** The url factory */
    private URLFactory urlFactory;

    /** The special Source factories */
    private Map sourceFactories;

    /** The context */
    private Context context;

    /**
     * Configure the SourceFactories
     */
    public void configure(final Configuration conf)
    throws ConfigurationException {
        try {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("Getting the SourceFactories");
            }
            HashMap factories = new HashMap();
            Configuration[] configs = conf.getChildren("protocol");
            SourceFactory sourceFactory = null;
            String protocol = null;
            for (int i = 0; i < configs.length; i++) {
                protocol = configs[i].getAttribute("name");
                if (factories.containsKey(protocol)) {
                    throw new ConfigurationException("SourceFactory defined twice for protocol: " + protocol);
                }

                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("\tfor protocol: " + protocol + " " + configs[i].getAttribute("class"));
                }
                sourceFactory = (SourceFactory) ClassUtils.newInstance(configs[i].getAttribute("class"));
                this.init(sourceFactory, configs[i]);
                factories.put(protocol, sourceFactory);
            }

            this.sourceFactories = java.util.Collections.synchronizedMap(factories);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Could not get parameters because: " +
                                           e.getMessage());
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
        this.urlFactory = (URLFactory)this.manager.lookup(URLFactory.ROLE);
    }

    /**
     * Dispose
     */
    public void dispose() {
        this.manager.release(this.urlFactory);

        final Iterator iter = this.sourceFactories.values().iterator();
        SourceFactory current;
        while (iter.hasNext()) {
            current = (SourceFactory) iter.next();
            this.deinit(current);
        }
        this.sourceFactories = null;
    }

    /**
     * Get a <code>Source</code> object.
     */
    public Source getSource(Environment environment, String location)
    throws ProcessingException, MalformedURLException, IOException {
        final int protocolEnd = location.indexOf(':');
        if (protocolEnd != -1) {
            final String protocol = location.substring(0, protocolEnd);
            final SourceFactory sourceFactory = (SourceFactory)this.sourceFactories.get(protocol);
            if (sourceFactory != null) {
                return sourceFactory.getSource(environment, location);
            }
        }

        // default implementation
        Source result = new URLSource(this.urlFactory.getURL(location), this.manager);
        if (result instanceof LogEnabled) {
            ((LogEnabled)result).enableLogging(getLogger());
        }
        return result;
    }

    /**
     * Get a <code>Source</code> object.
     */
    public Source getSource(Environment environment, URL base, String location)
    throws ProcessingException, MalformedURLException, IOException {
        final String protocol = base.getProtocol();
        final SourceFactory sourceFactory = (SourceFactory)this.sourceFactories.get(protocol);
        if (sourceFactory != null) {
            return sourceFactory.getSource(environment, base, location);
        }

        // default implementation
        return new URLSource(this.urlFactory.getURL(base, location), this.manager);
    }

    /**
     * Add a factory
     */
    public void addFactory(String protocol, SourceFactory factory)
    throws ProcessingException {
        try {
            this.init(factory, null);
            SourceFactory oldFactory = (SourceFactory)this.sourceFactories.put(protocol, factory);
            if (oldFactory != null) {
                deinit(oldFactory);
            }
        } catch (ComponentException e) {
            throw new ProcessingException("cannot initialize factory: " + factory, e);
        } catch (ContextException e) {
            throw new ProcessingException("cannot initialize factory: " + factory, e);
        } catch (ConfigurationException e) {
            throw new ProcessingException("cannot configure factory: " + factory, e);
        }
    }

    /**
     * Init a source factory
     */
    private void init(SourceFactory factory, Configuration config)
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
     * Deinit a source factory
     */
    private void deinit(SourceFactory factory) {
        if (factory instanceof Disposable) {
            ((Disposable) factory).dispose();
        }
    }

}
