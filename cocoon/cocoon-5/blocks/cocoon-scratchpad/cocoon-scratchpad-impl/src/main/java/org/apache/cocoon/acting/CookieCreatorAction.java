/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;

/**
 * The CookieCreatorAction action creates or removes cookies. The action needs
 * these parameters:
 * <dl>
 * <dt>name</dt><dd>the cookie name</dd>
 * <dt>value</dt><dd>the cookie value</dd>
 * <dt>comment</dt><dd>a comment to the cookie</dd>
 * <dt>domain</dt><dd>the domain the cookie is sent to</dd>
 * <dt>path</dt><dd>the path of the domain the cookie is sent to</dd>
 * <dt>secure</dt><dd>use a secure transport protocol (default is false)</dd>
 * <dt>maxage</dt> <dd>Age in seconds. Use 0 to remove cookie. Defaults to -1
 * (cookie lives for the duration of the session and is not persisted)</dd>
 * <dt>version</dt> <dd>version of cookie(default is 0)</dd>
 * </dl>
 *
 * <p>If you want to set a cookie you only need to
 * specify the cookie name. Its value is an empty string by default. The default
 * <code>maxage</code> is set to -1, that means the cookie will live until the
 * session is invalidated. If you want to remove a cookie, set its maxage to 0.
 *
 * @version $Id$
 */
public class CookieCreatorAction extends ServiceableAction
                                 implements ThreadSafe {

    /**
     * Creates a cookie.
     * @return Empty map on success, null on failure.
     */
    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters)
    throws Exception {
        final Response response = ObjectModelHelper.getResponse(objectModel);
        if (response == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("No response object");
            }
            return null;
        }

        String name = parameters.getParameter("name", null);
        if (name == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("No cookie name parameter");
            }
            return null;
        }

        String value = parameters.getParameter("value", "");
        String comment = parameters.getParameter("comment", null);
        String domain = parameters.getParameter("domain", null);
        String path = parameters.getParameter("path", null);

        final Cookie cookie = response.createCookie(name, value);
        if (comment != null) {
            cookie.setComment(comment);
        }
        if (domain != null) {
            cookie.setDomain(domain);
        }
        if (path != null) {
            cookie.setPath(path);
        }
        cookie.setSecure(parameters.getParameterAsBoolean("secure", false));
        cookie.setMaxAge(parameters.getParameterAsInteger("maxage", -1));
        cookie.setVersion(parameters.getParameterAsInteger("version", 0));
        response.addCookie(cookie);

        if (getLogger().isDebugEnabled()) {
            if (cookie.getMaxAge() == 0) {
                getLogger().debug("Cookie '" + name + "' has been removed");
            } else if (cookie.getMaxAge() < 0) {
                getLogger().debug("Cookie '" + name + "' created with value '" +
                                  value + "' (version " + cookie.getVersion() +
                                  "; will be stored for session duration)");
            } else {
                getLogger().debug("Cookie '" + name + "' created with value '" +
                                  value + "' (version " + cookie.getVersion() +
                                  "; will expire in " +
                                  cookie.getMaxAge() + " seconds)");
            }
        }

        return Collections.EMPTY_MAP;
    }
}
