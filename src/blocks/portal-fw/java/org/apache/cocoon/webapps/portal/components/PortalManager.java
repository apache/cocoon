/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.webapps.portal.components;

import java.io.IOException;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.xml.XMLConsumer;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *  This is the basis portal component
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: PortalManager.java,v 1.14 2004/05/26 08:39:49 cziegeler Exp $
*/
public interface PortalManager {

    /** The avalon role */
    String ROLE  = PortalManager.class.getName();

    /** Values for the buildprofile type element */
    String BUILDTYPE_VALUE_BASIC  = "basic";
    String BUILDTYPE_VALUE_GLOBAL = "global";
    String BUILDTYPE_VALUE_ROLE   = "role";
    String BUILDTYPE_VALUE_ID     = "user";

    /**
     * The request parameters for customizing coplets
     * The request parameter is <code>REQ_PARAMETER_CMD</code> and the
     * the value is one from <code>REQ_CMD_</code> followed by
     * '_<copletID>_<copletNR>'.
     */
    String REQ_PARAMETER_CMD = "portalcmd";

    String REQ_CMD_MAXIMIZE = "maximize";
    String REQ_CMD_MINIMIZE = "minimize";
    String REQ_CMD_CLOSE    = "close";
    String REQ_CMD_OPEN    = "open";
    String REQ_CMD_HIDE    = "hide"; // synonym to close
    String REQ_CMD_SHOW    = "show"; // synonym to open
    String REQ_CMD_CUSTOMIZE= "customize";
    String REQ_CMD_UPDATE   = "update";
    String REQ_CMD_DELETE   = "delete";
    String REQ_CMD_MOVE     = "move";
    String REQ_CMD_NEW      = "new";
    String REQ_CMD_MOVEROW  = "row";   // 1.1
    String REQ_CMD_SAVEPROFILE = "save";

    /** This parameter is used for changing of profile value */
    String REQ_PARAMETER_CONF = "portalconf";

    /** This parameter denotes the profile to be used */
    String REQ_PARAMETER_PROFILE = "portalprofile";

    /** These parameter characterize the role and id */
    String REQ_PARAMETER_ROLE = "portalrole";
    String REQ_PARAMETER_ID   = "portalid";
    String REQ_PARAMETER_STATE= "portaladmin";
    String REQ_PARAMETER_COPLET= "portalcoplet";
    String REQ_PARAMETER_ADMIN_COPLETS = "portaladmin_coplets";

    /** This is the current role/id which is set in the context */
    String ATTRIBUTE_PORTAL_ROLE = "role";
    String ATTRIBUTE_PORTAL_ID   = "ID";

    /** Some states for the admin configuration */
    String ATTRIBUTE_ADMIN_STATE = "adminstate";
    String ATTRIBUTE_ADMIN_ROLE  = "adminrole";
    String ATTRIBUTE_ADMIN_ID    = "adminid";
    String ATTRIBUTE_ADMIN_COPLETS = "admincoplets";

    /**
     * Configure portal and check if it is allowed to see this coplet (if it is one).
     * This is only a public wrapper for the getConfiguration method.
     */
    void configurationTest()
    throws ProcessingException;

    /**
     * Include Portal URI into stream
     */
    void streamConfiguration(XMLConsumer consumer,
                             String      requestURI,
                             String      profileID,
                             String      media,
                             String      contextID)
    throws SAXException, ProcessingException;

    /**
     * Show the admin configuration page.
     */
    void showAdminConf(XMLConsumer consumer)
    throws SAXException, ProcessingException, IOException;

    /**
     * Get the status profile
     */
    Element getStatusProfile()
    throws ProcessingException;

    /**
     * Show the portal.
     * The portal is included in the current stream.
     */
    void showPortal(XMLConsumer consumer,
                    boolean configMode,
                    boolean adminProfile)
    throws SAXException, ProcessingException;

    /**
     * Check the authentication for the coplet. If it is not available do a redirect
     */
    boolean checkAuthentication(Redirector redirector, String copletID)
    throws IOException, ProcessingException;
        
    /**
     * Get the current media type
     */
    String getMediaType() 
    throws ProcessingException;

    /**
     * Get the portal context of the current application
     */
    SessionContext getContext(boolean create)
    throws ProcessingException;

    /**
     * Builds the key for caching
     */
    String getProfileID(String type,
                        String role,
                        String id,
                        boolean adminProfile) 
    throws ProcessingException;
    
    /**
     * Retrieve the profil
     */
    Map retrieveProfile(String profileID)
    throws ProcessingException;
        
}
