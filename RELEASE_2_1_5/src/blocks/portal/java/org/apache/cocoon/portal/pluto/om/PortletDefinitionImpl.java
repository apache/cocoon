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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.cocoon.portal.pluto.om.common.ContentTypeSetImpl;
import org.apache.cocoon.portal.pluto.om.common.DescriptionSetImpl;
import org.apache.cocoon.portal.pluto.om.common.DisplayNameSetImpl;
import org.apache.cocoon.portal.pluto.om.common.LanguageSetImpl;
import org.apache.cocoon.portal.pluto.om.common.ParameterSetImpl;
import org.apache.cocoon.portal.pluto.om.common.PreferenceSetImpl;
import org.apache.cocoon.portal.pluto.om.common.SecurityRoleRefSetImpl;
import org.apache.cocoon.portal.pluto.om.common.Support;
import org.apache.pluto.invoker.PortletInvoker;
import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.om.common.DisplayNameSet;
import org.apache.pluto.om.common.LanguageSet;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.apache.pluto.om.portlet.ContentTypeSet;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionCtrl;
import org.apache.pluto.om.servlet.ServletDefinition;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletDefinitionImpl.java,v 1.3 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class PortletDefinitionImpl 
implements PortletDefinition, PortletDefinitionCtrl, java.io.Serializable, Support {


    private PortletApplicationDefinition application = null;
    private LanguageSet castorResources = null;

    // contains String objects
    private ArrayList castorSupportedLocales = new ArrayList();
    private ClassLoader classLoader = null;
    private String className = null;
    private ContentTypeSet contentTypes = new ContentTypeSetImpl();
    private DescriptionSet descriptions = new DescriptionSetImpl();

    private DisplayNameSet displayNames = new DisplayNameSetImpl();
    private String expirationCache = null;
    public  String id = "";    
    private ParameterSet initParams = new ParameterSetImpl();
    private SecurityRoleRefSet initSecurityRoleRefs = new SecurityRoleRefSetImpl();
    private String name = null;

    private ObjectID objectId = null;
    private PreferenceSet preferences = new PreferenceSetImpl();

    //    private PortletInfoImpl portletInfo = null;
    private String resourceBundle = null;
    private ServletDefinition servlet = null;

    // contains Locale objects
    private ArrayList supportedLocales = new ArrayList();

    /** is this a local portlet? */
    protected boolean localPortlet = false;
    
    /** The local portlet invoker */
    protected PortletInvoker localPortletInvoker;
    
    // PortletDefinition implementation.

    public ObjectID getId() {        
        if (objectId==null) {
            objectId = org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl.createFromString(getGUID());            
        }

        return objectId;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinition#getDescription(Locale)
     */
    public Description getDescription(Locale locale) {
        return descriptions.get(locale);
    }

    public LanguageSet getLanguageSet() {
        ((LanguageSetImpl)castorResources).setClassLoader(this.getPortletClassLoader());
        return castorResources; 
    }

    public ParameterSet getInitParameterSet() {
        return initParams;
    }

    public SecurityRoleRefSet getInitSecurityRoleRefSet() {
        return initSecurityRoleRefs;
    }

    public PreferenceSet getPreferenceSet() {
        ((PreferenceSetImpl)preferences).setClassLoader(this.getPortletClassLoader());
        return preferences;
    }

    public ContentTypeSet getContentTypeSet() {
        return contentTypes;
    }

    public PortletApplicationDefinition getPortletApplicationDefinition() {
        return application;
    }

    public ServletDefinition getServletDefinition() {
        return servlet;
    }

    public DisplayName getDisplayName(Locale locale) {
        return displayNames.get(locale);
    }

    public String getExpirationCache() {
        return expirationCache;
    }

    // PortletDefinitionCtrl implementation.

    public void setId(String id) {
        // todo excep
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.portlet.PortletDefinitionCtrl#setDescriptions(DescriptionSet)
     */
    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }

    public void setDisplayNames(DisplayNameSet displayNames) {
        this.displayNames = displayNames;
    }

    public void setPortletClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }
    
    public void store() throws java.io.IOException {
        // not supported
    }


    public void postBuild(Object parameter) throws Exception {
        setServletDefinition((ServletDefinition)parameter);
        ((Support)contentTypes).postBuild(this);
        if (castorResources!=null) {
            ((Support)castorResources).postBuild(this);
        }
    }

    public void postLoad(Object parameter) throws Exception {
        ((Support)contentTypes).postLoad(this);        

        // create Locale objects for given locale entries
        Iterator iterator = castorSupportedLocales.iterator();
        while (iterator.hasNext()) {

            String locale = (String)iterator.next();

            // parse locale String
            StringTokenizer tokenizer = new StringTokenizer(locale,"_");
            String[] localeDef = new String[3];
            for (int i=0; i<3 ;i++) {
                if (tokenizer.hasMoreTokens()) {
                    localeDef[i] = tokenizer.nextToken();
                } else {
                    localeDef[i] = "";
                }
            }
            supportedLocales.add(new java.util.Locale(localeDef[0], localeDef[1], localeDef[2]));

        }

        if (castorResources==null) {
            castorResources = new LanguageSetImpl();
        }
        if (resourceBundle!=null) {
            ((LanguageSetImpl)castorResources).setResources(resourceBundle);    
        }
        ((Support)castorResources).postLoad(this.supportedLocales);
        ((Support)descriptions).postLoad(parameter);
        ((Support)displayNames).postLoad(parameter);

    }
    public void postStore(Object parameter) throws Exception {
        ((Support)contentTypes).postStore(this);
        if (castorResources!=null) {
            ((Support)castorResources).postStore(this);
        }
    }
    public void preBuild(Object parameter) throws Exception {
        setPortletApplicationDefinition((PortletApplicationDefinition)parameter);
        ((Support)contentTypes).preBuild(this);
        if (castorResources!=null) {
            ((Support)castorResources).preBuild(this);
        }
    }
    public void preStore(Object parameter) throws Exception {
        ((Support)contentTypes).preStore(this);
        if (castorResources!=null) {
            ((Support)castorResources).preStore(this);
        }
    }

    // additional methods.

    public Collection getCastorContentTypes() {
        return(ContentTypeSetImpl)contentTypes;
    }

    public Collection getCastorDisplayNames() {
        return(DisplayNameSetImpl)displayNames;
    }

    public Collection getCastorDescriptions() {
        return(DescriptionSetImpl)descriptions;
    }

    public Collection getDescriptions() {
        return(DescriptionSetImpl)descriptions;
    }

    public Collection getCastorInitParams() {
        return(ParameterSetImpl)initParams;        
    }

    public SecurityRoleRefSet getCastorInitSecurityRoleRefs() {
        return initSecurityRoleRefs;
    }

    public PreferenceSet getCastorPreferences() {
        return preferences;
    }

    public LanguageSet getCastorResources() {
        return castorResources;
    } 

    // not yet fully supported
    // public Collection getSecurityRoleRef()
    // {
    //     return securityRoleRef;
    // }

    // public void setSecurityRoleRef(Collection securityRoleRef)
    // {
    //     this.securityRoleRef = (ArrayList)securityRoleRef;
    // }

    // additional internal methods

    public Collection getCastorSupportedLocales() {
        return castorSupportedLocales;
    }


    private String getGUID() {
        String portletID = "";
        if (getName()!=null) portletID += getName();

        portletID =  application.getId().toString() + "."+portletID;        
        return portletID;
    }

    public ClassLoader getPortletClassLoader() {
        return classLoader;
    }

    public String getResourceBundle() {
        return this.resourceBundle;
    }    

    public Collection getSupportedLocales() {
        return supportedLocales;
    }

    public void setCastorContentTypes(ContentTypeSet castorContentTypes) {
        this.contentTypes = castorContentTypes;
    }    

    public void setCastorInitParams(ParameterSet castorInitParams) {
        this.initParams = castorInitParams;
    }

    public void setCastorInitSecurityRoleRefs(SecurityRoleRefSet castorInitSecurityRoleRefs) {
        this.initSecurityRoleRefs = castorInitSecurityRoleRefs;
    }

    public void setCastorDisplayNames(DisplayNameSet castorDisplayNames) {
        this.displayNames = castorDisplayNames;
    }

    public void setCastorDescriptions(DescriptionSet castorDescriptions) {
        this.descriptions = castorDescriptions;
    }

    public void setCastorPreferences(PreferenceSet castorPreferences) {
        this.preferences = castorPreferences;
    }

    public void setCastorResources(LanguageSet resources) {
        this.castorResources = resources;
    } 

    public void setCastorSupportedLocales(Collection castorSupportedLocales) {
        this.castorSupportedLocales = (ArrayList)castorSupportedLocales;
    }

    public void setExpirationCache(String expirationCache) {
        this.expirationCache = expirationCache;
    }

    protected void setPortletApplicationDefinition(PortletApplicationDefinition application) {
        this.application = application;
    }

    public void setResourceBundle(String resourceBundle) {
        this.resourceBundle = resourceBundle;
    }    

    protected void setServletDefinition(ServletDefinition servlet) {
        this.servlet = servlet;
    }

    /**
     * @return Returns the localPortlet.
     */
    public boolean isLocalPortlet() {
        return this.localPortlet;
    }

    /**
     * @param localPortlet The localPortlet to set.
     */
    public void setLocalPortlet(boolean localPortlet) {
        this.localPortlet = localPortlet;
    }

    /**
     * @return Returns the localPortletInvoker.
     */
    public PortletInvoker getLocalPortletInvoker() {
        return this.localPortletInvoker;
    }

    /**
     * Set a local portlet invoker for caching
     * @param localPortletInvoker The localPortletInvoker to set.
     */
    public void setLocalPortletInvoker(PortletInvoker localPortletInvoker) {
        this.localPortletInvoker = localPortletInvoker;
    }
}
