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
package org.apache.cocoon.components.pipeline.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.components.pipeline.AbstractProcessingPipeline;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;

/**
 * This is the base class for all caching pipeline implementations.
 * The pipeline can be configured with the {@link Cache} to use
 * by specifying the <code>cache-role</code> parameter.
 *
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: BaseCachingProcessingPipeline.java,v 1.3 2003/12/06 21:22:10 cziegeler Exp $
 */
public abstract class BaseCachingProcessingPipeline
    extends AbstractProcessingPipeline
    implements Disposable {

    /** This is the Cache holding cached responses */
    protected Cache  cache;

    /** The deserializer */
    protected XMLDeserializer xmlDeserializer;
    /** The serializer */
    protected XMLSerializer xmlSerializer;

    /**
     * Parameterizable Interface - Configuration
     */
    public void parameterize(Parameters params)
    throws ParameterException {
        super.parameterize(params);
        
        String cacheRole = params.getParameter("cache-role", Cache.ROLE);
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Using cache " + cacheRole);
        }
        
        try {
            this.cache = (Cache)this.manager.lookup(cacheRole);
        } catch (ServiceException ce) {
            throw new ParameterException("Unable to lookup cache: " + cacheRole, ce);
        }
    }
    
    /**
     * Recyclable Interface
     */
    public void recycle() {
        this.manager.release( this.xmlDeserializer );
        this.xmlDeserializer = null;

        this.manager.release( this.xmlSerializer );
        this.xmlSerializer = null;

        super.recycle();
    }

    /**
     * Disposable Interface
     */
    public void dispose() {
        if ( null != this.manager ) {
            this.manager.release(this.cache);
        }
        this.cache = null;
        this.manager = null;
    }
}
