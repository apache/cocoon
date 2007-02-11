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
 * @version CVS $Id: ServletDefinitionImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
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
