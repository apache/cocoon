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

/**
 * Flow/Javascript wrapper to Cocoon authentication framework.
 *
 * WARNING   -  THIS IS EXPERIMENTAL STUFF!!! Use it at your own risk
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: auth.js,v 1.7 2003/11/24 03:42:44 antonio Exp $
*/

function auth_checkAuthentication(handler, application) {

    var authMgr = null;
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
    var authMgr = null;
    try {
        // ApplicationName, do we need it?
        authMgr = cocoon.getComponent(Packages.org.apache.cocoon.webapps.authentication.AuthenticationManager.ROLE);
        
        var userHandler = authMgr.isAuthenticated(handler);
        return (userHandler!=null);
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
    
    var authMgr = null;
    try {
        authMgr = cocoon.getComponent(Packages.org.apache.cocoon.webapps.authentication.AuthenticationManager.ROLE);
        return authMgr.login( handler, application, authParams) != null;
    } finally {
        cocoon.releaseComponent(authMgr);
    }
}

function auth_logout(handler, modeString) {
    var mode;
    if ( modeString == null || modeString == "" || modeString == "if-not-authenticated" ) {
        mode = Packages.org.apache.cocoon.webapps.authentication.AuthenticationConstants.LOGOUT_MODE_IF_NOT_AUTHENTICATED;
    } else if ( modeString == "if-unused" ) {
        mode = Packages.org.apache.cocoon.webapps.authentication.AuthenticationConstants.LOGOUT_MODE_IF_UNUSED;
    } else if ( modeString == "immediately" ) {
        mode = Packages.org.apache.cocoon.webapps.authentication.AuthenticationConstants.LOGOUT_MODE_IMMEDIATELY;
    } else {
       throw new Error("Unknown mode"); // " + modeString);
    }
    var authMgr = null;
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