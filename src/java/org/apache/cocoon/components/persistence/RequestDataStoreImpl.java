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
package org.apache.cocoon.components.persistence;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * The default implementation
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: RequestDataStoreImpl.java,v 1.3 2004/01/08 09:23:14 cziegeler Exp $
 * @since 2.1.1
 */
public class RequestDataStoreImpl
    extends AbstractLogEnabled
    implements Component, ThreadSafe, RequestDataStore, Contextualizable {
        
    protected Context context;

    protected final String requestDataKey = this.getClass().getName() + "/RD";
    
    protected final String globalRequestDataKey = this.getClass().getName() + "/GRD";
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#getGlobalRequestData(java.lang.String)
     */
    public Object getGlobalRequestData(String key) {
        Object value = null;
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.globalRequestDataKey);
        if ( m != null ) {
            value = m.get( key );
        }
        return value;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#getRequestData(java.lang.String)
     */
    public Object getRequestData(String key) {
        Object value = null;
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.requestDataKey + ObjectModelHelper.getRequest(objectModel).hashCode());
        if ( m != null ) {
            value = m.get( key );
        }
        return value;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#removeGlobalRequestData(java.lang.String)
     */
    public void removeGlobalRequestData(String key) {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.globalRequestDataKey);
        if ( m != null ) {
            objectModel.remove( key );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#removeRequestData(java.lang.String)
     */
    public void removeRequestData(String key) {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.requestDataKey + ObjectModelHelper.getRequest(objectModel).hashCode());
        if ( m != null ) {
            objectModel.remove( key );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#setGlobalRequestData(java.lang.String, java.lang.Object)
     */
    public void setGlobalRequestData(String key, Object value) {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.globalRequestDataKey);
        if ( m == null ) {
            m = new HashMap();
            objectModel.put(this.globalRequestDataKey, m);
        }
        m.put(key, value);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.persistance.RequestDataStore#setRequestData(java.lang.String, java.lang.Object)
     */
    public void setRequestData(String key, Object value) {
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        Map m = (Map)objectModel.get(this.requestDataKey + ObjectModelHelper.getRequest(objectModel).hashCode());
        if ( m == null ) {
            m = new HashMap();
            objectModel.put(this.requestDataKey + ObjectModelHelper.getRequest(objectModel).hashCode(), m);
        }
        m.put(key, value);
    }

}
