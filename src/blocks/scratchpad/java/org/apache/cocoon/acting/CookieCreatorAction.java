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
package org.apache.cocoon.acting;


import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
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
 * @version CVS $Id: CookieCreatorAction.java,v 1.3 2004/03/05 10:07:25 bdelacretaz Exp $
 */

public class CookieCreatorAction extends ServiceableAction implements ThreadSafe {

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

