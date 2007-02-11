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
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.SecurityRoleSet;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.servlet.ServletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl;
import org.apache.cocoon.portal.pluto.om.common.ParameterSetImpl;
import org.apache.cocoon.portal.pluto.om.common.SecurityRoleSetImpl;
import org.apache.cocoon.portal.pluto.om.common.Support;
import org.apache.cocoon.portal.pluto.om.common.DescriptionSetImpl;
import org.apache.cocoon.portal.pluto.om.common.DisplayNameSetImpl;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: WebApplicationDefinitionImpl.java,v 1.4 2004/01/27 10:37:35 cziegeler Exp $
 */
public class WebApplicationDefinitionImpl 
implements WebApplicationDefinition, Support {


    private String contextPath;        
    private DescriptionSet descriptions = new DescriptionSetImpl();
    private DisplayNameSet displayNames =  new DisplayNameSetImpl();
    private String id = "";
    private ParameterSet initParams = new ParameterSetImpl();
    /** The object id */
    private ObjectID objectId;
    private Collection servletMappings = new ArrayList();
    private ServletDefinitionList servlets = new ServletDefinitionListImpl();
    private SecurityRoleSet securityRoles = new SecurityRoleSetImpl();

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getId()
     */
    public ObjectID getId() {
        if (this.objectId == null) {
            this.objectId = ObjectIDImpl.createFromString(id);
        }
        return this.objectId;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getDisplayName(Locale)
     */
    public DisplayName getDisplayName(Locale locale) {
        return displayNames.get(locale);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getDescription(java.util.Locale)
     */
    public Description getDescription(Locale locale) {
        return descriptions.get(locale);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getInitParameterSet()
     */
    public ParameterSet getInitParameterSet() {
        return initParams;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getServletDefinitionList()
     */
    public ServletDefinitionList getServletDefinitionList() {
        return servlets;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getServletContext(javax.servlet.ServletContext)
     */
    public ServletContext getServletContext(ServletContext servletContext) {
        return servletContext.getContext(contextPath);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.servlet.WebApplicationDefinition#getContextRoot()
     */
    public String getContextRoot() {
        return contextPath;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postBuild(java.lang.Object)
     */
    public void postBuild(Object parameter) throws Exception {
        // not needed in this implementation
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postLoad(java.lang.Object)
     */
    public void postLoad(Object parameter) throws Exception {
        Vector structure = (Vector)parameter;
        PortletApplicationDefinition portletApplication = (PortletApplicationDefinition)structure.get(0);

        ((Support)portletApplication).postLoad(this);

        ((Support)servlets).postLoad(this);

        ((Support)descriptions).postLoad(parameter);
        ((Support)displayNames).postLoad(parameter);

    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postStore(java.lang.Object)
     */
    public void postStore(Object parameter) throws Exception {
        ((Support)servlets).postStore(this);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#preBuild(java.lang.Object)
     */
    public void preBuild(Object parameter) throws Exception {
        Vector structure = (Vector)parameter;
        PortletApplicationDefinition portletApplication = (PortletApplicationDefinition)structure.get(0);
        String contextString = (String)structure.get(1);

        setContextRoot(contextString);

        HashMap servletMap = new HashMap(1);
        Vector structure2 = new Vector();
        structure2.add(this);
        structure2.add(servletMappings);
        structure2.add(servletMap);

        ((Support)servlets).preBuild(structure2);

        Vector structure3 = new Vector();
        structure3.add(contextString);
        structure3.add(this);
        structure3.add(servletMap);
        ((Support)portletApplication).preBuild(structure3);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.om.common.Support#preStore(java.lang.Object)
     */
    public void preStore(Object parameter) throws Exception {
        Vector structure = (Vector)parameter;
        PortletApplicationDefinition portletApplication = (PortletApplicationDefinition)structure.get(0);

        ((Support)portletApplication).preStore(null);

        ((Support)servlets).preStore(this);
    }
    
    // additional methods.
    
    public void setCastorId(String id) {        
        this.id = id;
        objectId = null;
    }
    
    public String getCastorId() {                
        if (id.length() > 0) {
            return getId().toString();
        } else {
            return null;
        }
    }

    public Collection getCastorInitParams() {
        return(ParameterSetImpl)initParams;
    }

    public Collection getCastorServlets() {
        return(ServletDefinitionListImpl)servlets;
    }

    public Collection getCastorDisplayNames() {
        return(DisplayNameSetImpl)displayNames;
    }

    public Collection getCastorDescriptions() {
        return(DescriptionSetImpl)descriptions;
    }

    public SecurityRoleSet getSecurityRoles() {
        return securityRoles;
    }

    public Collection getServletMappings() {
        return servletMappings;
    }
    
    protected void setContextRoot(String contextPath) {
        this.contextPath = contextPath;
    }    

    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }

    public void setDisplayNames(DisplayNameSet displayNames) {
        this.displayNames = displayNames;
    }

    public void setCastorDescriptions(DescriptionSet castorDescriptions) {
        this.descriptions = castorDescriptions;
    }

    public void setCastorDisplayNames(DisplayNameSet castorDisplayNames) {
        this.displayNames = castorDisplayNames;
    }

}
