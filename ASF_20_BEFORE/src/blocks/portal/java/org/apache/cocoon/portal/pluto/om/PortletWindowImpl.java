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

*/
package org.apache.cocoon.portal.pluto.om;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowCtrl;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletWindowImpl.java,v 1.3 2004/03/01 03:50:57 antonio Exp $
 */
public class PortletWindowImpl implements PortletWindow, PortletWindowCtrl {
        
    
    private ObjectID objectId;
    private String id;
    private PortletEntity portletEntity;

    public PortletWindowImpl(String id) {
        this.id = id;
    }

    // PortletWindow implementation.

     /**
     * Returns the identifier of this portlet instance window as object id
     *
     * @return the object identifier
     **/
    public ObjectID getId()
    {
        if (objectId==null)
        {
            objectId = org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString(id);
        }
        return objectId;
    }
    /**
     * Returns the portlet entity
     *
     * @return the portlet entity
     **/
    public PortletEntity getPortletEntity()
    {
        return portletEntity;
    }

    // PortletWindowCtrl implementation.
    /**
     * binds an identifier to this portlet window
     *
     * @param id the new identifier
     */
    public void setId(String id)
    {
        this.id = id;
        objectId = null;
    }
    
    /**
     * binds a portlet instance to this portlet window
     * 
     * @param portletEntity a portlet entity object
     **/
    public void setPortletEntity(PortletEntity portletEntity) {
        this.portletEntity = portletEntity;
    }

}