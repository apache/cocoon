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
package org.apache.cocoon.portal.pluto;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.pluto.services.information.PortalContextProvider;

/**
 * Information about the portal
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortalContextProviderImpl.java,v 1.1 2004/01/22 14:01:21 cziegeler Exp $
 */
public class PortalContextProviderImpl 
implements PortalContextProvider {

    /** Portal information */
    protected String info;

    /** supported portlet modes by this portal */
    protected Vector modes;

    /** supported window states by this portal */
    protected Vector states;

    /** portal properties */
    protected HashMap properties;

    /** The host name */
    protected String hostNameHTTP;
    
    /** The host name */
    protected String hostNameHTTPS;
    
    /** The host name */
    protected String contextHTTP;
    
    /** The host name */
    protected String contextHTTPS;

    /**
     * Constructor
     */
    public PortalContextProviderImpl(Map objectModel) {        // these are the minimum modes that the portal needs to support        this.modes = this.getDefaultModes();
        // these are the minimum states that the portal needs to support        this.states = this.getDefaultStates(); 
        // set info        this.info = "Apache Cocoon/1.0";
        this.properties = new HashMap();
        this.init(objectModel);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortalContextProvider#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Property name == null");
        }

        return(String) properties.get(name);
    }


    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortalContextProvider#getPropertyNames()
     */
    public Collection getPropertyNames() {
        return properties.keySet();
    }


    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortalContextProvider#getSupportedPortletModes()
     */
    public Collection getSupportedPortletModes() {
        return this.modes;
    }


    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortalContextProvider#getSupportedWindowStates()
     */
    public Collection getSupportedWindowStates() {
        return this.states;
    }


    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortalContextProvider#getPortalInfo()
     */
    public String getPortalInfo() {
        return this.info;
    }

    /**
     * Return all default modes
     */
    protected Vector getDefaultModes() {
        Vector m = new Vector();

        m.add(new PortletMode("view"));
        m.add(new PortletMode("edit"));
        m.add(new PortletMode("help"));
        m.add(new PortletMode("config"));
        
        return m;
    }

    /**
     * Return all default states
     */
    protected Vector getDefaultStates() {
        Vector s = new Vector();

        s.add(new WindowState("normal"));
        s.add(new WindowState("minimized"));
        s.add(new WindowState("maximized"));
        
        return s;
    }

    /**
     * Initialize some infos
     */
    protected void init(Map objectModel) {
        final Request request = ObjectModelHelper.getRequest(objectModel);
        final String hostName   = request.getServerName();
        final String contextRoot = request.getContextPath();
        final int hostPortHTTP  = request.getServerPort();
        final int hostPortHTTPS = 443;
        
        StringBuffer hostHTTP = new StringBuffer("http://");
        hostHTTP.append(hostName);
        if (hostPortHTTP != 80) {
            hostHTTP.append(":");
            hostHTTP.append(hostPortHTTP);
        }
        this.hostNameHTTP = hostHTTP.toString();
        hostHTTP.append('/');
        hostHTTP.append(contextRoot);
        this.contextHTTP = hostHTTP.toString();
        
        StringBuffer hostHTTPS = new StringBuffer("https://");
        hostHTTPS.append(hostName);
        if (hostPortHTTPS != 443) {
            hostHTTPS.append(":");
            hostHTTPS.append(hostPortHTTPS);
        }
        this.hostNameHTTPS = hostHTTPS.toString();
        hostHTTPS.append('/');
        hostHTTPS.append(contextRoot);
        this.contextHTTPS = hostHTTPS.toString();
    }
    
    public String getBaseURLexcludeContext(boolean secure) {
        return (secure?this.hostNameHTTPS : this.hostNameHTTP);
    }

    public String getBaseURL(boolean secure) {
        return (secure?this.contextHTTPS : this.contextHTTP);
    }
    
}
