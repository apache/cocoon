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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.apache.cocoon.portal.pluto.om.common.AbstractSupportSet;
import org.apache.cocoon.portal.pluto.om.common.Support;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletApplicationDefinitionImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
 */
public class PortletApplicationDefinitionImpl 
extends AbstractSupportSet
implements PortletApplicationDefinition {

    protected String GUID;

    protected String appId = null;
    protected String version = null;

    
    private ArrayList customPortletMode = new ArrayList();
    private ArrayList customPortletState = new ArrayList();
    private ArrayList userAttribute = new ArrayList();
    private ArrayList securityConstraint = new ArrayList();

    private PortletDefinitionListImpl portlets = new PortletDefinitionListImpl();

    private WebApplicationDefinition webApplication = null;

    private ObjectID objectId = null;

    private String contextPath = null;

    // PortletApplicationDefinition implementation.

    /** PUBLIC*/
    public ObjectID getId() {
        if ( this.objectId == null ) {
            this.objectId = org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString(getGUID());                        
        }
        return this.objectId;
    }

    /** PUBLIC*/
    public String getVersion() {
        return this.version;
    }

    /** TODO PUBLIC*/
    public PortletDefinitionList getPortletDefinitionList() {
        return this.portlets;
    }

    /** PUBLIC*/
    public WebApplicationDefinition getWebApplicationDefinition() {
        return this.webApplication;
    }

    // Support implementation.

    public void postLoad(Object parameter) throws Exception
    {
        ((Support)portlets).postLoad(parameter);
    }

    public void preBuild(Object parameter) throws Exception
    {
        Vector structure = (Vector)parameter;
        String contextRoot = (String)structure.get(0);
        WebApplicationDefinition webApplication = (WebApplicationDefinition)structure.get(1);
        Map servletMap = (Map)structure.get(2);

        setContextRoot(contextRoot);

        setWebApplicationDefinition(webApplication);       

        Vector structure2 = new Vector();
        structure2.add(this);
        structure2.add(servletMap);

        ((Support)portlets).preBuild(structure2);

    }
    
    public void postBuild(Object parameter) throws Exception
    {
    }

    public void preStore(Object parameter) throws Exception
    {
        ((Support)portlets).preStore(parameter);
    }

    public void postStore(Object parameter) throws Exception
    {
        ((Support)portlets).postStore(parameter);
    }

    // internal methods.

    protected String getGUID() {
        if (GUID == null) {
            GUID = "";            
            String id = "";

            if (webApplication != null) {
                id = webApplication.getContextRoot();
            } else {
                id = contextPath;
            }

            if (id!=null) {
                if (id.startsWith("/")) {
                    id = id.substring(id.indexOf("/")+1);
                }

                GUID += id;
            }
        }

        return GUID;
    }

    private void setContextRoot(String contextRoot)
    {
        this.contextPath = contextRoot;                
    }

    // additional methods.

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }


    // not yet fully supported:
    public Collection getCustomPortletMode()
    {
        return customPortletMode;
    }

    public void setCustomPortletMode(Collection customPortletMode)
    {
        this.customPortletMode = (ArrayList)customPortletMode;
    }

    public Collection getCustomPortletState()
    {
        return customPortletState;
    }

    public void setCustomPortletState(Collection customPortletState)
    {
        this.customPortletState = (ArrayList)customPortletState;
    }

    public Collection getUserAttribute()
    {
        return userAttribute;
    }

    public void setUserAttribute(Collection userAttribute)
    {
        this.userAttribute = (ArrayList)userAttribute;
    }

    public Collection getSecurityConstraint()
    {
        return securityConstraint;
    }

    public void setSecurityConstraint(Collection securityConstraint)
    {
        this.securityConstraint = (ArrayList)securityConstraint;
    }

    // additional internal methods

    public Collection getCastorPortlets()
    {
        return portlets;
    }

    protected void setWebApplicationDefinition(WebApplicationDefinition webApplication)
    {
        this.webApplication = webApplication;
    }

}
