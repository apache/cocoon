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

/**
 * Flow/Javascript wrapper to Cocoon authentication framework.
 *
 * WARNING   -  THIS IS EXPERIMENTAL STUFF!!! Use it at your own risk
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: auth.js,v 1.9 2004/03/05 13:01:41 bdelacretaz Exp $
*/

function auth_checkAuthentication(handler, application) {

    var authMgr;
    var authenticated = false;
    try {
        authMgr = cocoon.getComponent(Packages.org.apache.cocoon.webapps.authentication.AuthenticationManager.ROLE);

        // do authentication
        authenticated = authMgr.checkAuthentication(null, handler, application);
        if (!authenticated) {
            cocoon.redirectTo(authMgr.getForwardingURI(handler));
        }
    } finally {
        cocoon.releaseComponent(authMgr);
    }
    return authenticated;
}

function auth_isAuthenticated(handler) {
    var authMgr;
    try {
        // ApplicationName, do we need it?
        authMgr = cocoon.getComponent(Packages.org.apache.cocoon.webapps.authentication.AuthenticationManager.ROLE);

        var userHandler = authMgr.isAuthenticated(handler);
        return userHandler != null;
    } finally {
        cocoon.releaseComponent(authMgr);
    }
}

function auth_login(handler, application, params) {

    var authParams = new Packages.org.apache.excalibur.source.SourceParameters();
    for (var name in params) {
      if (name.substring(0,10).equals("parameter_")) {
        authParams.setParameter(name.substring(10), params[name]);
      }
    }
    var authMgr;
    try {
        authMgr = cocoon.getComponent(Packages.org.apache.cocoon.webapps.authentication.AuthenticationManager.ROLE);
        return authMgr.login( handler, application, authParams) != null;
    } finally {
        cocoon.releaseComponent(authMgr);
    }
}

function auth_logout(handler, modeString) {
    var mode;
    if (modeString == null || modeString == "" || modeString == "if-not-authenticated") {
        mode = Packages.org.apache.cocoon.webapps.authentication.AuthenticationConstants.LOGOUT_MODE_IF_NOT_AUTHENTICATED;
    } else if (modeString == "if-unused") {
        mode = Packages.org.apache.cocoon.webapps.authentication.AuthenticationConstants.LOGOUT_MODE_IF_UNUSED;
    } else if (modeString == "immediately") {
        mode = Packages.org.apache.cocoon.webapps.authentication.AuthenticationConstants.LOGOUT_MODE_IMMEDIATELY;
    } else {
       throw new Error("Unknown mode"); // " + modeString);
    }
    var authMgr;
    try {
        authMgr = cocoon.getComponent(Packages.org.apache.cocoon.webapps.authentication.AuthenticationManager.ROLE);
        var state = authMgr.getState();

        if (handler == null && state != null) {
            handler = state.getHandlerName();
        }
        if (handler == null)
            throw new Error("LogoutAction requires at least the handler parameter.");
        authMgr.logout( handler, mode );
    } finally {
        cocoon.releaseComponent(authMgr);
    }
}
