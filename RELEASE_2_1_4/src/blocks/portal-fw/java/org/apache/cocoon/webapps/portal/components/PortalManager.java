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
 * @version CVS $Id: PortalManager.java,v 1.12 2004/01/09 11:20:22 cziegeler Exp $
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
    throws ProcessingException, IOException, SAXException;

    /**
     * Include Portal URI into stream
     */
    void streamConfiguration(XMLConsumer consumer,
                             String      requestURI,
                             String      profileID,
                             String      media,
                             String      contextID)
    throws IOException, SAXException, ProcessingException;

    /**
     * Show the admin configuration page.
     */
    void showAdminConf(XMLConsumer consumer)
    throws SAXException, ProcessingException, IOException;

    /**
     * Get the status profile
     */
    Element getStatusProfile()
    throws SAXException, IOException, ProcessingException;

    /**
     * Show the portal.
     * The portal is included in the current stream.
     */
    void showPortal(XMLConsumer consumer,
                    boolean configMode,
                    boolean adminProfile)
    throws SAXException, ProcessingException, IOException;

    /**
     * Check the authentication for the coplet. If it is not available do a redirect
     */
    boolean checkAuthentication(Redirector redirector, String copletID)
    throws SAXException, IOException, ProcessingException;
        
    /**
     * Get the current media type
     */
    String getMediaType() 
    throws ProcessingException;

    /**
     * Get the portal context of the current application
     */
    SessionContext getContext(boolean create)
    throws ProcessingException, IOException, SAXException;

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
