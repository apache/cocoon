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
package org.apache.cocoon.components;

import org.apache.avalon.fortress.ContainerManagerConstants;
import org.apache.avalon.fortress.MetaInfoManager;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.lifecycle.Creator;

import java.util.HashMap;
import java.util.Map;

/**
 * SitemapConfigurableAccessor does XYZ
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
public class SitemapConfigurableAccessor 
implements Creator {

    /** 
     * The {@link SitemapConfigurationHolder}s 
     */
    private Map m_sitemapConfigurationHolders = new HashMap( 15 );

    /* (non-Javadoc)
     * @see org.apache.avalon.lifecycle.Creator#create(java.lang.Object, org.apache.avalon.framework.context.Context)
     */
    public void create(Object object, Context context) 
    throws Exception {
        if ( object instanceof SitemapConfigurable ) {
            ServiceManager manager = (ServiceManager) context.get(ContainerManagerConstants.SERVICE_MANAGER);
            MetaInfoManager metaInfoManager = (MetaInfoManager)manager.lookup(MetaInfoManager.ROLE);
            try {
                String role = metaInfoManager.getMetaInfoForClassname(object.getClass().getName()).getConfigurationName();
                SitemapConfigurationHolder holder;
                
                holder = (SitemapConfigurationHolder) m_sitemapConfigurationHolders.get( role );
                if ( null == holder ) {
                    // create new holder
                    holder = new DefaultSitemapConfigurationHolder( role );
                    m_sitemapConfigurationHolders.put( role, holder );
                }
                
                ( (SitemapConfigurable) object ).configure( holder );
            } finally {
                manager.release(metaInfoManager);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.lifecycle.Creator#destroy(java.lang.Object, org.apache.avalon.framework.context.Context)
     */
    public void destroy(Object object, Context context) {
    }

}
