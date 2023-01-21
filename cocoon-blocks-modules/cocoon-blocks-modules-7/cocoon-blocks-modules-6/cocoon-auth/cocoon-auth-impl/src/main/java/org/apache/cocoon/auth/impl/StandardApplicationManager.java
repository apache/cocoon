/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.auth.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.cocoon.auth.Application;
import org.apache.cocoon.auth.ApplicationManager;
import org.apache.cocoon.auth.ApplicationUtil;
import org.apache.cocoon.auth.SecurityHandler;
import org.apache.cocoon.auth.User;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * This is the default implementation of the
 * {@link org.apache.cocoon.auth.ApplicationManager}.
 *
 * This implementation is heavily tied to Spring. This has been done to make
 * the configuration of the application manager easier.
 *
 * @version $Id$
 */
public class StandardApplicationManager
    implements ApplicationManager, BeanFactoryAware {

    /** The key used to store the login information in the session. */
    protected static final String LOGIN_INFO_KEY =
                     StandardApplicationManager.class.getName() + "/logininfo";

    /** The prefix used to store the application data object in the session. */
    protected static final String APPLICATION_KEY_PREFIX =
                     StandardApplicationManager.class.getName() + "/app:";

    /** The prefix used to register applications. */
    protected static final String APPLICATION_BEAN_NAME_PREFIX = Application.class.getName() + '/';

    /** The prefix used to register security handler. */
    protected static final String SECURITYHANDLER_BEAN_NAME_PREFIX = SecurityHandler.class.getName() + '/';

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    /** The process info provider. */
    protected ProcessInfoProvider processInfoProvider;

    /** A map containing all applications. */
    protected Map applications = Collections.synchronizedMap(new HashMap());

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.processInfoProvider = (ProcessInfoProvider)factory.getBean(ProcessInfoProvider.ROLE);
        // put map with applications into servlet context
        final ServletContext servletContext = (ServletContext)factory.getBean(ServletContext.class.getName());
        servletContext.setAttribute(StandardApplicationManager.class.getName(), this.applications);
    }

    /**
     * Return the application with the name.
     */
    protected Application getApplication(final String appName) {
        final Application app = (Application) WebAppContextUtils.getCurrentWebApplicationContext().getBean(Application.class.getName() + '/' + appName);
        if ( !this.applications.containsKey(appName) ) {
            this.applications.put(appName, app);
        }
        return app;
    }

    protected String getKey(SecurityHandler handler) {
        return handler.getId();
    }

    /**
     * @see org.apache.cocoon.auth.ApplicationManager#isLoggedIn(String)
     */
    public boolean isLoggedIn(final String appName) {
        Object appData = null;
        final Map objectModel = this.processInfoProvider.getObjectModel();
        final Request req = ObjectModelHelper.getRequest(objectModel);
        final HttpSession session = req.getSession(false);
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
                    if ( oldApp == null || oldApp != application ) {
                        application.userIsAccessing(user);
                    }
                } catch (Exception ignore) {
                    throw new RuntimeException("Unable to get application '"
                                               + appName + "'", ignore);
                }
            }
        }

        return (appData != null);
    }

    /**
     * @see org.apache.cocoon.auth.ApplicationManager#login(String, java.util.Map)
     */
    public User login(final String appName, final Map loginContext) throws Exception {
        final Map objectModel = this.processInfoProvider.getObjectModel();
        final Application application = this.getApplication(appName);
        final String securityHandlerKey = this.getKey(application.getSecurityHandler());

        User user = null;

        // first check, if we are already logged in
        if ( this.isLoggedIn(appName) ) {
            user = ApplicationUtil.getUser(objectModel);
        } else {
            final Request req = ObjectModelHelper.getRequest(objectModel);
            HttpSession session = req.getSession(false);

            LoginInfo info = null;
            Map loginInfos = null;

            if ( session != null ) {
                // is the user already logged in on the security handler?
                loginInfos = (Map)session.getAttribute(LOGIN_INFO_KEY);
                if ( loginInfos != null && loginInfos.containsKey(securityHandlerKey)) {
                    info = (LoginInfo)loginInfos.get(securityHandlerKey);
                    user = info.user;
                }
            }
            if ( user == null ) {
                user = application.getSecurityHandler().login(loginContext);
                if ( user != null ) {
                    // create new login info
                    session = req.getSession();
                    loginInfos = (Map)session.getAttribute(LOGIN_INFO_KEY);
                    if ( loginInfos == null ) {
                        loginInfos = new HashMap();
                    }
                    info = new LoginInfo(user);
                    loginInfos.put(securityHandlerKey, info);
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
                objectModel.put(ApplicationManager.APPLICATION, application);

                // set the application data in the session
                Object data = ObjectUtils.NULL;
                if ( application.getApplicationStore() != null ) {
                    data = application.getApplicationStore().loadApplicationData(user, application);
                }
                session.setAttribute(APPLICATION_KEY_PREFIX + appName, data);
                objectModel.put(ApplicationManager.APPLICATION_DATA, data);

                // notify the application about successful login
                application.userDidLogin(user, loginContext);

                // notify the application about accessing
                application.userIsAccessing(user);
            }
        }

        return user;
    }

    /**
     * @see org.apache.cocoon.auth.ApplicationManager#logout(String, java.util.Map)
     */
    public void logout(final String appName, final Map logoutContext) {
        final Map objectModel = this.processInfoProvider.getObjectModel();
        final Request req = ObjectModelHelper.getRequest(objectModel);
        final HttpSession session = req.getSession(false);
        if ( session != null ) {
            // remove application data from session
            session.removeAttribute(APPLICATION_KEY_PREFIX + appName);

            final Application application = this.getApplication(appName);
            final String securityHandlerKey = this.getKey(application.getSecurityHandler());

            // remove application from object model
            if ( application.equals( ApplicationUtil.getApplication(objectModel) ) ) {
                objectModel.remove(ApplicationManager.APPLICATION);
                objectModel.remove(ApplicationManager.APPLICATION_DATA);
                objectModel.remove(ApplicationManager.USER);
            }

            // remove user
            session.removeAttribute(USER + '-' + appName);

            // decrement logininfo counter
            final Map loginInfos = (Map)session.getAttribute(LOGIN_INFO_KEY);
            if ( loginInfos != null ) {
                final LoginInfo info = (LoginInfo)loginInfos.get(securityHandlerKey);
                if ( info != null ) {
                    // notify the application
                    application.userWillLogout(info.user, logoutContext);

                    info.decUsageCounter(appName);
                    if ( info.isUsed() ) {
                        session.setAttribute(LOGIN_INFO_KEY, loginInfos);
                    } else {
                        // logout from security handler
                        application.getSecurityHandler().logout(logoutContext, info.user);
                        // remove user info
                        loginInfos.remove(securityHandlerKey);
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

    /**
     * Logs the user out of all applications.
     * This method has not been tested yet.
     * @param session The corresponding session
     */
    public static void logoutFromAllApplications(final HttpSession session) {
        final Map loginInfos = (Map)session.getAttribute(LOGIN_INFO_KEY);
        if ( loginInfos != null ) {
            final Map applications = (Map)session.getServletContext().getAttribute(StandardApplicationManager.class.getName());
            final Iterator i = loginInfos.values().iterator();
            while ( i.hasNext() ) {
                final LoginInfo info = (LoginInfo)i.next();
                if ( info.isUsed() ) {
                    final Iterator appIter = info.getApplications().iterator();
                    SecurityHandler handler = null;
                    while ( appIter.hasNext() ) {
                        final String appName = (String)appIter.next();
                        try {
                            final Application app = (Application)applications.get(appName);
                            app.userWillLogout(info.getUser(), Collections.EMPTY_MAP);
                            handler = app.getSecurityHandler();
                        } catch (Exception ignore) {
                            // we ignore this
                        }
                    }
                    if ( handler != null ) {
                        handler.logout(Collections.EMPTY_MAP, info.getUser());
                    }
                }
            }
        }
    }
}
