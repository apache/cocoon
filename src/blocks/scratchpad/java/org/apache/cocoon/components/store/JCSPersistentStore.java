/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.components.store;

import java.io.File;
import java.io.IOException;

import org.apache.jcs.access.exception.CacheException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.store.Store;

/**
 * This store is based on the JCS Caching library
 * (http://jakarta.apache.org/turbine/jcs/). This store can be configured
 * to use any of the caching types available through the JCS.
 *
 * TODO - This store implementation should  perhaps be moved to excalibur store
 *
 * @author <a href="mailto:cmoss@tvnz.co.nz">Corin Moss</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 */
public class JCSPersistentStore extends AbstractJCSStore
    implements Store,
               ThreadSafe,
               Parameterizable,
               Disposable,
               Serviceable {
    
    protected ServiceManager manager;
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }
    
    /**
     *  Configure the Component.<br>
     *  A few options can be used
     *  <UL>
     *    <LI> configFile = the name of the file which specifies the configuration 
     *         parameters
     *    </LI>
     *    <LI> region = the region to be used as defined in the config file
     *    </LI>
     *  </UL>
     *
     * @param params the configuration paramters
     * @exception  ParameterException
     */
    public void parameterize(Parameters params) throws ParameterException {
        // TODO - These are only values for testing:
        final String configFileName = params.getParameter("config-file", "context://WEB-INF/TestDiskCache.ccf");     
        final String regionName = params.getParameter("region-name", "indexedRegion1");        
        final String groupName = params.getParameter("group-name", "indexedDiskCache");        
        
        SourceResolver resolver = null;
        Source source = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(configFileName);
            
            // get the config file to use
            final File configFile = SourceUtil.getFile(source);
            
            //if(!configFile.exists()){
            //   throw new ParameterException(
            //      "JCS Config file does not exist: " + configFileName
            //   );
            //}
            try {
               this.setup(configFile, regionName, groupName);
            } catch (CacheException ce) {
               throw new ParameterException(
                  "JCS unable to run setup with region: " + regionName
               );
            }
        } catch (ServiceException se) {
            throw new ParameterException("Unable to get source resolver.", se);
        } catch (IOException ioe) {
            throw new ParameterException("Unable to get handle on JCS Config file: " + configFileName , ioe);
        } finally {
            if ( resolver != null ) {
                resolver.release(source);
                this.manager.release(resolver);
            }
        }

    }

    public void dispose() {
        
        try {
            getLogger().debug("Disposing");

            if (super.m_JCS != null) {
                
                super.m_JCS = null;
                //protected - what is the best way to do this?
                //super.m_JCS.dispose();
            }

        } catch (Exception e) {
            getLogger().error("dispose(..) Exception", e);
        }
    }
}
