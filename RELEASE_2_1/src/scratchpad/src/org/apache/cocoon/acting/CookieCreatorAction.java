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
package org.apache.cocoon.acting;


import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;

import java.util.Map;

/**
 *  The CookieCreatorAction class create or remove cookies. The action needs
 *  these parameters: <dl> <dt>name</dt> <dd>the cookie name</dd> <dt>value</dt>
 *  <dd>the cookie value</dd> <dt>comment</dt> <dd>a comment to the cookie</dd>
 *  <dt>domain</dt> <dd>the domain the cookie is sent to</dd> <dt>path</dt> <dd>
 *  the path of the domain the cookie is sent to</dd> <dt>secure</dt> <dd>use a
 *  secure transport protocol (default is false)</dd> <dt>maxage</dt> <dd>Age in
 *  seconds. Use -1 to remove cookie. (default is 0; cookie lives within the
 *  session and it is not stored)</dd> <dt>version</dt> <dd>version of
 *  cookie(default is 0)</dd> </dl> If you want to set a cookie you only need to
 *  specify the cookie name. Its value is an empty string as default. The maxage
 *  is 0 that means the cookie will live until the session is invalidated. If
 *  you want to remove a cookie set its maxage to -1.
 *
 * @author <a href="mailto:paolo@arsenio.net">Paolo Scaffardi</a>
 * @version CVS $Id: CookieCreatorAction.java,v 1.1 2003/03/09 00:10:08 pier Exp $
 */

public class CookieCreatorAction extends ComposerAction {

    /**
     *  Description of the Method
     *
     * @param  redirector     Description of Parameter
     * @param  resolver       Description of Parameter
     * @param  objectModel    Description of Parameter
     * @param  src            Description of Parameter
     * @param  parameters     Description of Parameter
     * @return                Description of the Returned Value
     * @exception  Exception  Description of Exception
     */
    public Map act(Redirector redirector,
            SourceResolver resolver,
            Map objectModel,
            String src,
            Parameters parameters)
        throws Exception {

        Response res = ObjectModelHelper.getResponse(objectModel);

        /*
         *  check response validity
         */
        if (res == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("COOKIECREATOR: no response object");
            }
            return null;
        }

        String cookieName = null;

        String
                cookieValue = null;

        String
                cookieComment = null;

        String
                cookieDomain = null;

        String
                cookiePath = null;

        try {
            cookieName = parameters.getParameter("name");
        } catch (Exception e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("COOKIECREATOR: cannot retrieve cookie name attribute");
            }
            return null;
        }

        cookieValue = parameters.getParameter("value", "");

        Cookie cookie = res.createCookie(cookieName, cookieValue);

        try {
            if ((cookieComment = parameters.getParameter("comment")) != null) {
                cookie.setComment(cookieComment);
            }
        } catch (ParameterException ignore) {
        }

        try {
            if ((cookieDomain = parameters.getParameter("domain")) != null) {
                cookie.setDomain(cookieDomain);
            }
        } catch (ParameterException ignore) {
        }

        try {
            if ((cookiePath = parameters.getParameter("path")) != null) {
                cookie.setPath(cookiePath);
            }
        } catch (ParameterException ignore) {
        }

        cookie.setSecure(parameters.getParameterAsBoolean("secure", false));
        cookie.setMaxAge(parameters.getParameterAsInteger("maxage", 0));
        cookie.setVersion(parameters.getParameterAsInteger("version", 0));

        res.addCookie(cookie);

        if (getLogger().isDebugEnabled()) {
            if (cookie.getMaxAge() == 0) {
                getLogger().debug("COOKIECREATOR: '" + cookieName
                         + "' cookie has been removed");
            } else {
                getLogger().debug("COOKIECREATOR: '" + cookieName
                         + "' cookie created with value '"
                         + cookieValue + "' (version " + cookie.getVersion()
                         + "; it will expire in "
                         + cookie.getMaxAge() + " seconds)");
            }
        }
        return null;
    }
}

