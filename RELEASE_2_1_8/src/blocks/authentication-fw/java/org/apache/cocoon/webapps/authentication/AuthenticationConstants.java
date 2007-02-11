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
package org.apache.cocoon.webapps.authentication;

/**
 * The <code>Constants</code> used throughout the core of the authentication
 * framework.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: AuthenticationConstants.java,v 1.5 2004/03/05 13:01:40 bdelacretaz Exp $
*/
public interface AuthenticationConstants {

    /** The name of the authentication context. */
    String SESSION_CONTEXT_NAME = "authentication";

    /** Logout mode: session is terminated immediately */
    int LOGOUT_MODE_IMMEDIATELY = 0;
    
    /** Logout mode: session is terminated if not used anymore (by the 
     * session or the authentication framework */
    int LOGOUT_MODE_IF_UNUSED = 1;

    /** Logout mode: session is terminated if the user is not authenticated
     * to any handler anymore. */
    int LOGOUT_MODE_IF_NOT_AUTHENTICATED = 2;
}


