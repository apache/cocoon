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
package org.apache.cocoon.portal.aspect.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.portal.aspect.AspectDataHandler;
import org.apache.cocoon.portal.aspect.AspectDataStore;
import org.apache.cocoon.portal.aspect.AspectDescription;
import org.apache.cocoon.portal.aspect.Aspectalizable;
import org.apache.cocoon.portal.aspect.AspectalizableDescription;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaultAspectDataHandler.java,v 1.4 2003/12/11 09:56:58 cziegeler Exp $
 */
public class DefaultAspectDataHandler 
    implements AspectDataHandler {

    protected AspectalizableDescription description;
    
    protected ServiceSelector storeSelector;
    
    /**
     * Constructor
     */
    public DefaultAspectDataHandler(AspectalizableDescription desc,
                                    ServiceSelector storeSelector) {
        this.description = desc;
        this.storeSelector = storeSelector;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataHandler#getAspectData(org.apache.cocoon.portal.aspect.Aspectalizable, java.lang.String)
     */
    public Object getAspectData(Aspectalizable owner, String aspectName) {
        // is this aspect allowed?
        AspectDescription aspectDesc = this.description.getAspectDescription( aspectName );
        if ( aspectDesc == null ) return null;
        
        // lookup storage
        AspectDataStore store = null;
        Object data = null;
        try {
            store = (AspectDataStore)this.storeSelector.select(aspectDesc.getStoreName());
            data = store.getAspectData(owner, aspectName);

            if ( data == null && aspectDesc.isAutoCreate() ) {
                data = AspectUtil.createNewInstance(aspectDesc);
                store.setAspectData( owner, aspectName, data );
            }

        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup aspect data store " + aspectDesc.getStoreName(), ce);
        } finally {
            this.storeSelector.release( store );
        }        

        return data;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataHandler#getAspectDatas(org.apache.cocoon.portal.aspect.Aspectalizable)
     */
    public Map getAspectDatas(Aspectalizable owner)  {
        Map datas = new HashMap();
        Iterator iter = this.description.getAspectDescriptions().iterator();
        while ( iter.hasNext() ) {
            AspectDescription current = (AspectDescription)iter.next();
            Object data = this.getAspectData(owner, current.getName());
            if ( data != null ) {
                datas.put( current.getName(), data );
            }
        }
        return datas;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataHandler#getPersistentAspectDatas(org.apache.cocoon.portal.aspect.Aspectalizable)
     */
    public Map getPersistentAspectDatas(Aspectalizable owner)  {
        Map datas = new HashMap();
        Iterator iter = this.description.getAspectDescriptions().iterator();
        while ( iter.hasNext() ) {
            AspectDescription current = (AspectDescription)iter.next();

            // lookup storage
            AspectDataStore store = null;
            Object data = null;
            try {
                store = (AspectDataStore)this.storeSelector.select(current.getStoreName());
                if ( store.isPersistent() ) {
                    data = store.getAspectData(owner, current.getName());

                    if ( data == null && current.isAutoCreate() ) {
                        data = AspectUtil.createNewInstance(current);
                        store.setAspectData( owner, current.getName(), data );
                    }

                    if ( data != null ) {
                        datas.put( current.getName(), data );
                    }
                }

            } catch (ServiceException ce) {
                throw new CascadingRuntimeException("Unable to lookup aspect data store " + current.getStoreName(), ce);
            } finally {
                this.storeSelector.release( store );
            }        

        }
        return datas;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.aspect.AspectDataHandler#setAspectData(org.apache.cocoon.portal.aspect.Aspectalizable, java.lang.String, java.lang.Object)
     */
    public void setAspectData(Aspectalizable owner,
                               String aspectName,
                               Object data) {
        // is this aspect allowed?
        AspectDescription aspectDesc = this.description.getAspectDescription( aspectName );
        if ( aspectDesc == null ) return;

        // lookup storage
        AspectDataStore store = null;
        try {
            store = (AspectDataStore)this.storeSelector.select(aspectDesc.getStoreName());
            store.setAspectData(owner, aspectName, AspectUtil.convert(aspectDesc, data));
        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup aspect data store " + aspectDesc.getStoreName(), ce);
        } finally {
            this.storeSelector.release( store );
        }        
    }

    /**
     * Is this supported
     */
    public boolean isAspectSupported(String aspectName) {
        return (this.description.getAspectDescription(aspectName) != null);
    }
}
