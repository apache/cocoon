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
 * @version CVS $Id: URLFactoryWrapper.java,v 1.1 2003/03/09 00:07:05 pier Exp $
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
