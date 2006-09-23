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
package org.apache.cocoon.jcr;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
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
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.sitemap.PatternException;

import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.FileSource;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.util.Map;

/**
 * Base class for JCR (aka <a
 * href="http://www.jcp.org/en/jsr/detail?id=170">JSR-170</a>) repository as
 * a Cocoon component. The main purpose of this class is to allow repository
 * credentials to be specified in the component's configuration, so that the
 * application code just has to call <code>repository.login()</code>.
 *
 * <p>
 * There is no Cocoon-specific role for this component: "<code>javax.jcr.Repository</code>"
 * should be used.
 *
 * <p>
 * The configuration of this class, inherited by its subclasses, is as follows:
 *
 * <pre>
 *    &lt;jcr-repository&gt;
 *      &lt;jaas src="context://samples/jaas.config"/&gt;
 *      &lt;credentials login="<i>expression</i>" password="<i>expression</i>"/&gt;
 *      ... other specific configuration...
 *    &lt;/jcr-repository&gt;
 * </pre>
 *
 * Login and password can be specified using the sitemap expression language,
 * thus allowing the use of input modules to compute their values, e.g.
 * <code>password="{session-attr:jcr-password}"</code>.
 *
 * <p>
 * <code>&lt;credentials&gt;</code> is optional. If not specified, the
 * application must explicitely supply credentials when calling
 * <code>Repository.login()</code>.
 *
 * @version $Id$
 */
public abstract class AbstractRepository extends AbstractLogEnabled
                                         implements Repository, ThreadSafe, Contextualizable, Serviceable,
                                                    Configurable, Disposable, Component {

    /**
     * Role which shall be used for JCR repository implementations.
     */
    public static final String ROLE = "javax.jcr.Repository";

    /**
     * The request attribute in which the JCR session is stored
     */
    public static final String JCR_SESSION_REQUEST_ATTRIBUTE = "jcr-session";

    protected ServiceManager manager;

    protected Context context;

    protected Repository delegate;

    // Defined by the portal block :-(
    // protected VariableResolverFactory variableFactory;

    protected VariableResolver loginResolver;

    protected VariableResolver passwordResolver;


    // =============================================================================================
    // Avalon lifecycle
    // =============================================================================================

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public void configure(Configuration config) throws ConfigurationException {
        // FIXME FIXME FIXME Hack setting system jaas property
        Configuration jaas = config.getChild("jaas", false);
        if (jaas != null) {
            String jaasURI = jaas.getAttribute("src");
            SourceResolver resolver = null;
            FileSource jaasSrc = null;
            try {
                resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
                jaasSrc = (FileSource) resolver.resolveURI(jaasURI);
                if (System.getProperty("java.security.auth.login.config") == null) {
                    System.setProperty("java.security.auth.login.config", jaasSrc.getFile().getAbsolutePath());
                } else {
                    // WARNING: java.security.auth.login.config has already been set
                }
            } catch (Exception e) {
                throw new ConfigurationException("Cannot resolve jaas URI: " + jaasURI + " at " + config.getLocation());
            } finally {
                if (jaasSrc != null) {
                    resolver.release(jaasSrc);
                }
                this.manager.release(resolver);
            }
        }

        Configuration credentials = config.getChild("credentials", false);
        if (credentials != null) {
            String login = credentials.getAttribute("login");
            String password = credentials.getAttribute("password");

            try {
                this.loginResolver = VariableResolverFactory.getResolver(login, this.manager);
            } catch (PatternException e) {
                throw new ConfigurationException("Invalid expression for 'login' at " + credentials.getLocation(), e);
            }
            try {
                this.passwordResolver = VariableResolverFactory.getResolver(password, this.manager);
            } catch (PatternException e) {
                if (this.loginResolver instanceof Disposable) {
                    ((Disposable) this.loginResolver).dispose();
                }
                this.loginResolver = null;
                throw new ConfigurationException("Invalid expression for 'password' at " + credentials.getLocation(), e);
            }
        }
    }

    public void dispose() {
        this.context = null;
        this.delegate = null;

        if (this.loginResolver instanceof Disposable) {
            ((Disposable) this.loginResolver).dispose();
        }
        this.loginResolver = null;
        if (this.passwordResolver instanceof Disposable) {
            ((Disposable) this.passwordResolver).dispose();
        }
        this.passwordResolver = null;
        this.manager = null;
    }

    // =============================================================================================
    // Repository interface
    // =============================================================================================

    public String getDescriptor(String key) {
        return delegate.getDescriptor(key);
    }

    public String[] getDescriptorKeys() {
        return delegate.getDescriptorKeys();
    }

    public Session login()
    throws LoginException, NoSuchWorkspaceException, RepositoryException {
        Session session = getCachedSession(null);

        if (session == null) {
            Credentials creds = getCredentials();
            session = creds == null ? delegate.login() : delegate.login(creds);
            cacheSession(session, null);
        }

        return session;
    }

    public Session login(Credentials creds)
    throws LoginException, NoSuchWorkspaceException, RepositoryException {
        Session session = getCachedSession(null);

        if (session == null) {
            session = delegate.login(creds);
            cacheSession(session, null);
        }

        return session;
    }

    public Session login(Credentials creds, String workspace)
    throws LoginException, NoSuchWorkspaceException, RepositoryException {
        Session session = getCachedSession(workspace);

        if (session == null) {
            session = delegate.login(creds, workspace);
            cacheSession(session, workspace);
        }

        return session;
    }

    public Session login(String workspace)
    throws LoginException, NoSuchWorkspaceException, RepositoryException {
        Session session = getCachedSession(workspace);

        if (session == null) {
            Credentials creds = getCredentials();
            session = creds == null ? delegate.login(workspace) : delegate.login(creds, workspace);
            cacheSession(session, workspace);
        }

        return session;
    }

    // TODO: When logout should be called?

    // =============================================================================================
    // Implementation methods
    // =============================================================================================

    private Session getCachedSession(String workspace) {
        Map objectModel;
        try {
            objectModel = ContextHelper.getObjectModel(context);
        } catch(Exception e) {
            // We don't have an object model (happens e.g. at init time or in a cron job)
            return null;
        }

        String attributeName = workspace == null ?
                JCR_SESSION_REQUEST_ATTRIBUTE : JCR_SESSION_REQUEST_ATTRIBUTE + "/" + workspace;

        Request request = ObjectModelHelper.getRequest(objectModel);
        //FIXME: request is null when running in a testcase
        if (request == null) return null;
        Session session = (Session) request.getAttribute(attributeName);

        return (session != null && session.isLive()) ? session : null;
    }

    private void cacheSession(Session session, String workspace) {
        Map objectModel;
        try {
            objectModel = ContextHelper.getObjectModel(context);
        } catch(Exception e) {
            // We don't have an object model (happens e.g. at init time or in a cron job)
            return;
        }

        String attributeName = workspace == null ?
                JCR_SESSION_REQUEST_ATTRIBUTE : JCR_SESSION_REQUEST_ATTRIBUTE + "/" + workspace;

        Request request = ObjectModelHelper.getRequest(objectModel);
        //FIXME: request is null when running in a testcase
        if (request == null) return;
        request.setAttribute(attributeName, session);
    }

    private Credentials getCredentials() throws LoginException {
        if (this.loginResolver != null) {

            Map objectModel;
            try {
                objectModel = ContextHelper.getObjectModel(context);
            } catch(Exception e) {
                // We don't have an object model (happens e.g. at init time or in a cron job)
                throw new LoginException("No objectModel to evaluate credentials", e);
            }

            try {
                String login = this.loginResolver.resolve(objectModel);
                String password = this.loginResolver.resolve(objectModel);
                return new SimpleCredentials(login, password.toCharArray());
            } catch (PatternException e) {
                throw new LoginException("Failed to evaluate credentials", e);
            }
        } else {
            return null;
        }
    }
}
