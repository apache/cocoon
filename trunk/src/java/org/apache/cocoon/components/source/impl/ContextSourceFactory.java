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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.URIAbsolutizer;

/**
 * A factory for the context protocol using the context of the servlet api. It builds the
 * source by asking the environment context for the real URL
 * (see {@link org.apache.cocoon.environment.Context#getResource(String)}) and then resolving this real URL.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="http://www.apache.org/~sylvain">Sylvain Wallez</a>
 * @version CVS $Id: ContextSourceFactory.java,v 1.6 2003/12/26 18:43:39 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=SourceFactory
 * @x-avalon.info name=context-source
 * @x-avalon.lifestyle type=singleton
 */
public class ContextSourceFactory
    extends AbstractLogEnabled
    implements SourceFactory, Serviceable, Disposable, Contextualizable, URIAbsolutizer
{

    /** The context */
    protected Context envContext;

    /** The ServiceManager */
    protected ServiceManager manager;

    /** The Source Resolver */
    protected SourceResolver resolver;

    /**
     * Serviceable Interface
     * 
     * @avalon.dependency type="SourceResolver"
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        // FIXME : Looking up the resolver here leads to an infinite loop
        // (is this because of Avalon or CocoonComponentManager ??)
        // So we delay this for to the first call to getSource().
        //this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Disposable Interface
     */
    public void dispose() {
        if (this.resolver != null) {
            this.manager.release( this.resolver );
            this.resolver = null;
        }
    }

    /**
     * Get the context
     */
    public void contextualize(org.apache.avalon.framework.context.Context context)
    throws ContextException {
        this.envContext = (Context)context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource( String location, Map parameters )
        throws SourceException, MalformedURLException, IOException
    {
        if( this.getLogger().isDebugEnabled() )
        {
            this.getLogger().debug( "Creating source object for " + location );
        }

        // Lookup resolver if needed
        if (this.resolver == null) {
            try {
                this.resolver = (SourceResolver)this.manager.lookup( SourceResolver.ROLE );
            } catch (ServiceException se) {
            	throw new SourceException("Unable to lookup source resolver.", se);
            }
        }
                
        // Remove the protocol and the first '/'
        int pos = location.indexOf(":/");
        String path = location.substring(pos+1);
        
        URL u;
        
        // Try to get a file first and fall back to a resource URL
        String actualPath = envContext.getRealPath(path);
        if (actualPath != null) {
            u = new File(actualPath).toURL();
        } else {
            u = envContext.getResource(path);
        }

        if (u != null) {
            return this.resolver.resolveURI(u.toExternalForm());
            
        } else {
            String message = location + " could not be found. (possible context problem)";
            getLogger().info(message);
            throw new MalformedURLException(message);
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
            this.resolver.release( source );
        }
    }

    public String absolutize(String baseURI, String location) {
        return SourceUtil.absolutize(baseURI, location, true);
    }
}
