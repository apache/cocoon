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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.URIAbsolutizer;

/**
 * This class implements a proxy like source that uses another source
 * to get the content. This implementation can cache the content for
 * a given period of time and can refresh the content async in the
 * background.
 *    
 * <component-instance class="org.apache.cocoon.components.source.impl.CachingSourceFactory" name="cached"/>
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CachingSourceFactory.java,v 1.2 2003/10/25 18:06:19 joerg Exp $
 * @since 2.1.1
 */
public final class CachingSourceFactory
    extends AbstractLogEnabled
    implements SourceFactory, ThreadSafe, Serviceable, URIAbsolutizer, Disposable, Parameterizable
{
    /** The <code>ServiceManager</code> */
    protected ServiceManager manager;

    /** The {@link SourceResolver} */
    protected SourceResolver   resolver;

    /** The store */
    protected Cache cache;
    
    /** Async ? */
    protected boolean async;
    
    /** The role of the cache */
    protected String cacheRole;
    
    /** The refresher */
    protected Refresher refresher;
    
    /**
     * Serviceable
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        // due to cyclic dependencies we can't lookup the resolver or the refresher here
    }

    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource( String location, Map parameters )
    throws MalformedURLException, IOException {
        if(  this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "Creating source object for " + location );
        }

        // we must do a lazy lookup because of cyclic dependencies
        if (this.resolver == null) {
            try {
                this.resolver = (SourceResolver)this.manager.lookup( SourceResolver.ROLE );
            } catch (ServiceException se) {
                throw new SourceException("SourceResolver is not available.", se);
            }
        }
        if ( this.refresher == null ) {
            try {
                this.refresher = (Refresher)this.manager.lookup(Refresher.ROLE);
            } catch (ServiceException se) {
                throw new SourceException("Refesher is not available.", se);
            }
        }

        CachingSource source;
        if ( this.async ) {
            source = new AsyncCachingSource( location, parameters);
            final long expires = source.getExpiration();
            
            CachedResponse response = this.cache.get( source.getCacheKey() );
            if ( response == null ) {
                
                // call the target the first time
                this.refresher.refresh(source.getCacheKey(),
                                       source.getSourceURI(),
                                       expires,
                                       this.cacheRole);

                response = this.cache.get( source.getCacheKey() );
            }
            ((AsyncCachingSource)source).setResponse(response);

            this.refresher.refreshPeriodically(source.getCacheKey(),
                                   source.getSourceURI(),
                                   expires,
                                   this.cacheRole);
        } else {
            source = new CachingSource( location, parameters);
        }
        ContainerUtil.enableLogging(source, this.getLogger());
        try {
            ContainerUtil.service(source, this.manager);
            // we pass the components for performance reasons
            source.init(this.resolver, this.cache);
            ContainerUtil.initialize(source);                                  
        } catch (IOException ioe) {
            throw ioe;
        } catch (ServiceException se) {
            throw new SourceException("Unable to initialize source.", se);
        } catch (Exception e) {
            throw new SourceException("Unable to initialize source.", e);
        }
        return source;
    }
    
    /**
     * Release a {@link Source} object.
     */
    public void release( Source source ) {
        if ( source instanceof CachingSource) {
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Releasing source " + source.getURI());
            }
            ContainerUtil.dispose(source);
        }
    }

    public String absolutize(String baseURI, String location) {
        return SourceUtil.absolutize(baseURI, location, true);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.resolver);
            this.manager.release(this.cache);
            this.manager.release(this.refresher);
            this.refresher = null;
            this.cache = null;
            this.manager = null;
            this.resolver = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.cacheRole = parameters.getParameter("cache-role", Cache.ROLE);
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Using cache " + this.cacheRole);
        }
        
        try {
            this.cache = (Cache)this.manager.lookup(this.cacheRole);
        } catch (ServiceException se) {
            throw new ParameterException("Unable to lookup cache: " + this.cacheRole, se);
        }

        this.async = parameters.getParameterAsBoolean("async", false);
    }

}
