/*
 * Copyright 2004,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.pluto.om;

import java.util.Collection;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.apache.pluto.om.servlet.ServletDefinition;
import org.apache.pluto.om.servlet.ServletDefinitionCtrl;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.apache.cocoon.portal.pluto.om.common.ParameterSetImpl;
import org.apache.cocoon.portal.pluto.om.common.SecurityRoleRefSetImpl;
import org.apache.cocoon.portal.pluto.om.common.Support;
import org.apache.cocoon.portal.pluto.om.common.DescriptionSetImpl;
import org.apache.cocoon.portal.pluto.om.common.DisplayNameSetImpl;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ServletDefinitionImpl.java,v 1.2 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class ServletDefinitionImpl
implements ServletDefinition, ServletDefinitionCtrl, java.io.Serializable, Support {

    private DescriptionSet descriptions = new DescriptionSetImpl();
    private DisplayNameSet displayNames = new DisplayNameSetImpl();

    // not used variables - only for castor
    public String icon = null;
    private String id = "";
    private ParameterSet initParams = new ParameterSetImpl();
    private SecurityRoleRefSet initSecurityRoleRefs = new SecurityRoleRefSetImpl();
    public String jspFile = null;
    public String loadOnStartup = null;
    private ObjectID objectId = null;
    public String securityRoleRef = null;
    private String servletClass = null;
    private ServletMapping servletMapping = null;
    private String servletName = null;
    private long available = 0;

    private WebApplicationDefinition webApplication = null;

    // ServletDefinition implementation.

    public ObjectID getId()
    {
        if (objectId==null) {
            objectId = org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString(id);
        }
        return objectId;
    }

    public String getServletName()
    {
        return servletName;
    }

    public DisplayName getDisplayName(Locale locale)
    {
        return displayNames.get(locale);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.ServletDefinition#getDescription(Locale)
     */
    public Description getDescription(Locale locale) {
        return descriptions.get(locale);
    }

    public String getServletClass()
    {
        return servletClass;
    }

    public ParameterSet getInitParameterSet()
    {
        return initParams;
    }

    public WebApplicationDefinition getWebApplicationDefinition()
    {
        return webApplication;
    }

    public RequestDispatcher getRequestDispatcher(ServletContext servletContext)
    {
        ServletContext newContext = webApplication.getServletContext(servletContext);
        if (newContext==null) {
            return null;
        }
        return newContext.getRequestDispatcher(servletMapping.getUrlPattern());
    }

    public long getAvailable() {
        return available;
    }

    public boolean isUnavailable() {
        if (available == 0) {
            return false;
        } else if (available <= System.currentTimeMillis()) {
            available = 0;
            return false;
        } else {
            return true;
        }
    }

    // Support implementation.

    public void postBuild(Object parameter) throws Exception
    {
        setServletMapping((ServletMapping)parameter);
    }


    public void postLoad(Object parameter) throws Exception
    {
        ((Support)descriptions).postLoad(parameter);
        ((Support)displayNames).postLoad(parameter);
    }
    public void postStore(Object parameter) throws Exception
    {
    }
    public void preBuild(Object parameter) throws Exception
    {
        setWebApplicationDefinition((WebApplicationDefinition)parameter);
    }
    public void preStore(Object parameter) throws Exception
    {
    }

    // additional methods.
    
    public String getCastorId() {                
        if (id.length() > 0)
            return getId().toString();
        else
            return null;
    }

    public Collection getCastorInitParams()
    {
        return(ParameterSetImpl)initParams;
    }

    public SecurityRoleRefSet getCastorInitSecurityRoleRefs()
    {
        return initSecurityRoleRefs;
    }

    public Collection getCastorDisplayNames()
    {
        return(DisplayNameSetImpl)displayNames;
    }

    public Collection getCastorDescriptions()
    {
        return(DescriptionSetImpl)descriptions;
    }

    public Collection getDescriptions()
    {
        return(DescriptionSetImpl)descriptions;
    }



    public SecurityRoleRefSet getInitSecurityRoleRefSet()
    {
        return initSecurityRoleRefs;
    }



    public String getJspFile()
    {
        return jspFile;
    }


    public void setCastorId(String id) {        
        setId(id);
    }

    public void setCastorInitSecurityRoleRefs(SecurityRoleRefSet castorInitSecurityRoleRefs)
    {
        this.initSecurityRoleRefs = castorInitSecurityRoleRefs;
    }

    public void setCastorDisplayNames(DisplayNameSet castorDisplayNames)
    {
        this.displayNames = castorDisplayNames;
    }

    public void setCastorDescriptions(DescriptionSet castorDescriptions)
    {
        this.descriptions = castorDescriptions;
    }

    public void setDisplayNames(DisplayNameSet displayNames)
    {
        this.displayNames = displayNames;
    }

    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }

    public void setId(String id)
    {
        this.id = id;
        objectId = null;
    }

    public void setServletClass(String servletClass)
    {
        this.servletClass = servletClass;
    }

    public void setAvailable(long available) {
        if (available > System.currentTimeMillis()) {
            this.available = available;
        } else {
            this.available = 0;
        }
    }

    public void setJspFile(String jspFile)
    {
        this.jspFile = jspFile;
    }

    protected void setServletMapping(ServletMapping servletMapping)
    {
        this.servletMapping = servletMapping;
    }

    public void setServletName(String servletName)
    {
        this.servletName = servletName;
    }

    protected void setWebApplicationDefinition(WebApplicationDefinition webApplication)
    {
        this.webApplication = webApplication;
    }

}
