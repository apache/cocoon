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
package org.apache.cocoon.auth.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.commons.lang.ObjectUtils;
import org.apache.cocoon.auth.Application;
import org.apache.cocoon.auth.ApplicationManager;
import org.apache.cocoon.auth.ApplicationUtil;
import org.apache.cocoon.auth.User;

/**
 * This is the default implementation of the
 * {@link org.apache.cocoon.auth.ApplicationManager}.
 *
 * @version $Id$
*/
public class StandardApplicationManager
    extends AbstractLogEnabled
    implements ApplicationManager,
               Contextualizable,
               Serviceable,
               ThreadSafe,
               Disposable {

    /** The key used to store the login information in the session. */
    protected static final String LOGIN_INFO_KEY =
                     StandardApplicationManager.class.getName() + "/logininfo";

    /** The prefix used to store the application data object in the session. */
    protected static final String APPLICATION_KEY_PREFIX =
                     StandardApplicationManager.class.getName() + "/app:";

    /** The component context. */
    protected Context context;

    /** The service manager. */
    protected ServiceManager manager;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(final Context aContext) throws ContextException {
        this.context = aContext;
        try {
            ServletConfig config =
                (ServletConfig)this.context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
            config.getServletContext().setAttribute(StandardApplicationManager.class.getName(),
                                                    this);
        } catch (ContextException ignore) {
            // we ignore this if we are not running inside a servlet environment
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(final ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        this.manager = null;
    }

    /**
     * Get the application with the given name. This method tries to lookup the
     * applicating using the current sitemap service manager.
     * @param appName The name of the application.
     * @return The application object.
     * @throws Exception If the application can't be found.
     */
    protected Application getApplication(final String appName)
    throws Exception {
        final ServiceManager current = (ServiceManager)
                   this.context.get(ContextHelper.CONTEXT_SITEMAP_SERVICE_MANAGER);
        Object o = current.lookup(Application.class.getName() + '/' + appName);
        if ( o == null ) {
            throw new ConfigurationException(
                           "Application '" + appName + "' not found."
                       );
        }
        // to avoid messy release stuff later on, we just release the app now
        // as an application is thread safe this isn't really a problem
        current.release(o);
        return (Application)o;
    }

    /**
     * @see org.apache.cocoon.auth.ApplicationManager#isLoggedIn(java.lang.String)
     */
    public boolean isLoggedIn(final String appName) {
        Object appData = null;
        final Map objectModel = ContextHelper.getObjectModel( this.context );
        final Request req = ObjectModelHelper.getRequest(objectModel);
        final Session session = req.getSession(false);
        if ( session != null ) {
            appData = session.getAttribute(APPLICATION_KEY_PREFIX + appName);

            // if the user is logged in, we set the current application, data and user
            if ( appData != null ) {
                try {
                    final Application application = this.getApplication(appName);
                    final User user = (User)session.getAttribute(USER + '-' + appName);
                    final Application oldApp = (Application)objectModel.get(ApplicationManager.APPLICATION);
                    objectModel.put(ApplicationManager.APPLICATION, application);
                    objectModel.put(ApplicationManager.APPLICATION_DATA, appData);
                    objectModel.put(ApplicationManager.USER, user);
                    // notify application
                    // The application is only notified once per request.
                    if ( oldApp == null || !oldApp.equals(application) ) {
                        application.userIsAccessing(user);
                    }
                } catch (Exception ignore) {
                    throw new CascadingRuntimeException("Unable to get application '"
                                                        + appName + "'", ignore);
                }
            }
        }

        return (appData != null);
    }

    /**
     * @see org.apache.cocoon.auth.ApplicationManager#login(java.lang.String, java.util.Map)
     */
    public User login(final String appName, final Map loginContext) throws Exception {
        User user = null;

        final Map objectModel = ContextHelper.getObjectModel( this.context );

        // first check, if we are already logged in
        if ( this.isLoggedIn(appName) ) {
            user = ApplicationUtil.getUser(objectModel);
        } else {
            final Request req = ObjectModelHelper.getRequest(objectModel);
            Session session = req.getSession(false);

            final Application app = this.getApplication(appName);
            LoginInfo info = null;
            Map loginInfos = null;

            if ( session != null ) {
                // is the user already logged in on the security handler?
                loginInfos = (Map)session.getAttribute(LOGIN_INFO_KEY);
                if ( loginInfos != null
                      && loginInfos.containsKey(app.getSecurityHandler().getId()) ) {
                    info = (LoginInfo)loginInfos.get(app.getSecurityHandler().getId());
                    user = info.user;
                }
            }
            if ( user == null ) {
                user = app.getSecurityHandler().login(loginContext);
                if ( user != null ) {
                    // create new login info
                    session = req.getSession();
                    loginInfos = (Map)session.getAttribute(LOGIN_INFO_KEY);
                    if ( loginInfos == null ) {
                        loginInfos = new HashMap();
                    }
                    info = new LoginInfo(user);
                    loginInfos.put(app.getSecurityHandler().getId(), info);
                }
            }

            // user can be null, if login failed
            if ( user != null ) {
                info.incUsageCounter(appName);
                session.setAttribute(LOGIN_INFO_KEY, loginInfos);

                // set the user in the session
                session.setAttribute(USER + '-' + appName, user);
                objectModel.put(ApplicationManager.USER, user);

                // set the application in the object model
                objectModel.put(ApplicationManager.APPLICATION, app);

                // notify the application
                app.userDidLogin(user, loginContext);

                // set the application data in the session
                Object data = ObjectUtils.NULL;
                if ( app.getApplicationStore() != null ) {
                    data = app.getApplicationStore().loadApplicationData(user, app);
                }
                session.setAttribute(APPLICATION_KEY_PREFIX + appName, data);
                objectModel.put(ApplicationManager.APPLICATION_DATA, data);
                // notify application
                app.userIsAccessing(user);
            }
        }

        return user;
    }

    /**
     * @see org.apache.cocoon.auth.ApplicationManager#logout(java.lang.String, java.util.Map)
     */
    public void logout(final String appName, final Map logoutContext) {
        final Map objectModel = ContextHelper.getObjectModel( this.context );
        final Request req = ObjectModelHelper.getRequest(objectModel);
        final Session session = req.getSession(false);
        if ( session != null ) {
            Application app;

            try {
                app = this.getApplication(appName);
            } catch (Exception ignore) {
                throw new CascadingRuntimeException("Unable to get application '"
                                                    + appName + "'", ignore);
            }

            // remove application data from session
            session.removeAttribute(APPLICATION_KEY_PREFIX + appName);

            // remove application from object model
            if ( app.equals( ApplicationUtil.getApplication(objectModel) ) ) {
                objectModel.remove(ApplicationManager.APPLICATION);
                objectModel.remove(ApplicationManager.APPLICATION_DATA);
                objectModel.remove(ApplicationManager.USER);
            }

            // remove user
            session.removeAttribute(USER + '-' + appName);

            // decrement logininfo counter
            final Map loginInfos = (Map)session.getAttribute(LOGIN_INFO_KEY);
            if ( loginInfos != null ) {
                final LoginInfo info = (LoginInfo)loginInfos.get(app.getSecurityHandler().getId());
                if ( info != null ) {
                    // notify the application
                    app.userWillLogout(info.user, logoutContext);

                    info.decUsageCounter(appName);
                    if ( info.isUsed() ) {
                        session.setAttribute(LOGIN_INFO_KEY, loginInfos);
                    } else {
                        // logout from security handler
                        app.getSecurityHandler().logout(logoutContext, info.user);
                        // remove user info
                        loginInfos.remove(app.getSecurityHandler().getId());
                        if ( loginInfos.size() > 0 ) {
                            session.setAttribute(LOGIN_INFO_KEY, loginInfos);
                        } else {
                            session.removeAttribute(LOGIN_INFO_KEY);
                            // the user has left all applications, test the mode:
                            String mode = null;
                            if ( logoutContext != null ) {
                                mode = (String)logoutContext.get(LOGOUT_CONTEXT_MODE_KEY);
                            }
                            if ( mode == null
                                 || mode.equals(LOGOUT_MODE_TERMINATE_SESSION_IF_UNUSED) ) {
                                session.invalidate();
                            }
                        }
                    }
                }
            }
        }
    }
}
