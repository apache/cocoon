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
package org.apache.cocoon.webapps.portal;

/**
 * Some constants for the portal
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: PortalConstants.java,v 1.2 2004/03/05 13:02:18 bdelacretaz Exp $
*/
public interface PortalConstants {

    /** The name of the portal context. */
    String SESSION_CONTEXT_NAME = "portal";

    /** The private context of the portal profile. Some more information
     *  is appended by the PortalManager to this key.
     */
    String PRIVATE_SESSION_CONTEXT_NAME = "org.apache.cocoon.webapps.portal.context.SessionContext";

    /** The Module name of the authentication module */
    String AUTHENTICATION_MODULE_NAME = "portal";

    /** If a coplet is loaded the <code>SessionInfo.copletInfo</code> map
     *  contains in this key the parameters for the coplet.
     */
    String COPLETINFO_PARAMETERS = "COPLETINFO_PARAMETERS";

    /** If a coplet is loaded the <code>SessionInfo.copletInfo</code> map
     *  contains in this key the portal URI
     */
    String COPLETINFO_PORTALURI = "COPLETINFO_PORTALURI";

    /** If a coplet is loaded the <code>SessionInfo.copletInfo</code> map
     *  contains in this key the status profile
     */
    String COPLETINFO_STATUSPROFILE = "COPLETINFO_STATUSPROFILE";

    // XML Elements
    String ELEMENT_CONFIGURATION="configuration";
    String ELEMENT_ID        = "id";
    String ELEMENT_ADMINCONF = "portaladminconf";
    String ELEMENT_LAYOUT    = "layout";
    String ELEMENT_PORTAL    = "portal";
    String ELEMENT_PORTALCONF= "portalconf";
    String ELEMENT_PROFILE   = "profile";
    String ELEMENT_ROLE      = "role";
    String ELEMENT_STATE     = "state";
    String ELEMENT_COPLET    = "coplet";
    String ELEMENT_COPLETS   = "coplets";

    // admin conf states
    final String STATE_USER    = "user";
    final String STATE_ROLE    = "role";
    final String STATE_MAIN    = "main";
    final String STATE_COPLETS = "coplets";
    final String STATE_GLOBAL  = "global";
    final String STATE_COPLET  = "coplet";
    final String STATE_MAIN_ROLE= "mainrole";

    /** The name of the attribute holding the portal configuration */
    final String ATTRIBUTE_CONFIGURATION  = "portalConf";

    /** The name of the attribute holding the url rewritten portal uri */
    final String ATTRIBUTE_PORTAL_URI  = "portalURI";

    final String ATTRIBUTE_COPLET_REPOSITORY = "portalRep";

    final String PROFILE_PROFILE        = "profile";   // DocumentFragment
    final String PROFILE_TYPE_PATHS     = "typePaths"; // List
    final String PROFILE_TYPE_CONF_PATHS= "typeConfPaths"; // List
    final String PROFILE_PORTAL_LAYOUTS = "portalLayouts"; // Map
    final String PROFILE_COPLET_LAYOUTS = "copletLayouts"; // Map
    final String PROFILE_MISC_POINTER   = "misc"; // Node[] with the values from below
        final int  PROFILE_MISC_HEADER_NODE = 0;
        final int  PROFILE_MISC_FOOTER_NODE = 1;
        final int  PROFILE_MISC_HEADER_CONTENT_NODE = 2;
        final int  PROFILE_MISC_FOOTER_CONTENT_NODE = 3;
        final int  PROFILE_MISC_COLUMNS_NODE= 4;
        final int  PROFILE_MISC_LAST_COPLET_NODE = 5;
        final int  PROFILE_MISC_MESSAGES_NODE = 6;
        // starting with 8 the columns follow (by now max: 5)

    final String PROFILE_DEFAULT_COPLETS= "defCoplets"; // Map
    final String PROFILE_MEDIA_COPLETS  = "mediaCoplets"; // Map
    final String PROFILE_SAVE_STATUS_FLAG= "saveStatus"; // Value not used

    /** Configuration Map */
    final String CONF_BUILD_RESOURCE           = "A";
    final String CONF_AUTH_REDIRECT            = "B";
    final String CONF_LAYOUTBASE_RESOURCE      = "C";
    final String CONF_COPLETBASE_RESOURCE      = "D";
    final String CONF_COPLETBASE_SAVE_RESOURCE = "E";
    final String CONF_TYPEBASE_RESOURCE        = "F";
    final String CONF_GLOBALDELTA_LOADRESOURCE = "G";
    final String CONF_GLOBALDELTA_SAVERESOURCE = "H";
    final String CONF_GLOBALDELTA_TYPERESOURCE = "I";
    final String CONF_ROLEDELTA_LOADRESOURCE   = "J";
    final String CONF_ROLEDELTA_SAVERESOURCE   = "K";
    final String CONF_ROLEDELTA_TYPERESOURCE   = "L";
    final String CONF_USERDELTA_LOADRESOURCE   = "M";
    final String CONF_USERDELTA_SAVERESOURCE   = "N";
    final String CONF_USERDELTA_TYPERESOURCE   = "O";
    final String CONF_STATUS_LOADRESOURCE      = "P";
    final String CONF_STATUS_SAVERESOURCE      = "Q";
    final String CONF_ADMIN_TYPE_BASE          = "R";
    final String CONF_PORTAL_URI               = "S";
    final String CONF_PROFILE_CACHE            = "T";
    final String CONF_PARALLEL_COPLETS         = "U";
    final String CONF_COPLET_TIMEOUT           = "V";

    final int MAX_COLUMNS = 5;

    /* The Parameters for the coplet */
    final String PARAMETER_MEDIA = "media";
    final String PARAMETER_ID    = "id";
    final String PARAMETER_NUMBER= "number";
    final String PARAMETER_CUSTOMIZE = "customize";
    final String PARAMETER_SIZE  = "size";
    final String PARAMETER_VISIBLE= "visible";
    final String PARAMETER_PERSISTENT= "persistent";
}
