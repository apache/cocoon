/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.auth.impl;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This session listener keeps track of expired sessions. It can be used in
 * conjunction with the {@link org.apache.cocoon.auth.impl.StandardApplicationManager}
 *
 * This listener has not been tested yet.
 *
 * @version $Id$
*/
public class ServletSessionListener implements HttpSessionListener {

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionCreated(final HttpSessionEvent event) {
        // we don't care about a new session
    }

    /**
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    public void sessionDestroyed(final HttpSessionEvent event) {
        final HttpSession session = event.getSession();
        StandardApplicationManager.logoutFromAllApplications(session);
    }
}
