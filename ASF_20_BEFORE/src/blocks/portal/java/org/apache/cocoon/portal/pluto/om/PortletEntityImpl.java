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

import java.io.IOException;
import java.util.Locale;

import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.entity.PortletEntityCtrl;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindowList;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletEntityImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
 */
public class PortletEntityImpl implements PortletEntity, PortletEntityCtrl {

    protected PortletWindowList portletWindows;
    protected ObjectID objectId;
    protected PortletDefinition definition;
    protected CopletInstanceData coplet;
    protected PortletApplicationEntity applicationEntity;

    /**
     * Constructor
     */
    PortletEntityImpl(PortletApplicationEntity pae,
                      CopletInstanceData cid, 
                       PortletDefinition pd) {
        this.objectId = org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString("CID" + cid.hashCode());
        this.portletWindows = new PortletWindowListImpl();
        this.coplet = cid;
        this.definition = pd;
        this.applicationEntity = pae;
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getDescription(java.util.Locale)
     */
    public Description getDescription(Locale locale) {
        return this.definition.getDescription(locale);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getId()
     */
    public ObjectID getId() {
        return this.objectId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletApplicationEntity()
     */
    public PortletApplicationEntity getPortletApplicationEntity() {
        return this.applicationEntity;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletDefinition()
     */
    public PortletDefinition getPortletDefinition() {
        return this.definition;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getPortletWindowList()
     */
    public PortletWindowList getPortletWindowList() {
        return this.portletWindows;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntity#getPreferenceSet()
     */
    public PreferenceSet getPreferenceSet() {
        return this.definition.getPreferenceSet();
    }

    /**
     * Return the coplet instance data
     */
    public CopletInstanceData getCopletInstanceData() {
        return this.coplet;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#reset()
     */
    public void reset() throws IOException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#setId(java.lang.String)
     */
    public void setId(String id) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#setPortletDefinition(org.apache.pluto.om.portlet.PortletDefinition)
     */
    public void setPortletDefinition(PortletDefinition portletDefinition) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.entity.PortletEntityCtrl#store()
     */
    public void store() throws IOException {
        // TODO Auto-generated method stub
    }

}
