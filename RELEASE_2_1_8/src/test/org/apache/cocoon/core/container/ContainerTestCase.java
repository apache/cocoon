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
package org.apache.cocoon.core.container;

import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.avalon.excalibur.component.DefaultRoleManager;
import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.cocoon.util.Deprecation;

/**
 * JUnit TestCase for Cocoon Components.
 * <p>
 *   This class extends the JUnit TestCase class to setup an environment which
 *   makes it possible to easily test Cocoon Components. The following methods
 *   and instance variables are exposed for convenience testing:
 * </p>
 * <dl>
 *   <dt>getManager()</dt>
 *   <dd>
 *     This instance variable contains an initialized service manager which
 *     can be used to lookup components configured in the test configuration
 *     file. (see below)
 *   </dd>
 *   <dt>getLogger()</dt>
 *   <dd>
 *     This method returns a logger for this test case. By default this
 *     logger logs with log level DEBUG.
 *   </dd>
 * </dl>
 * <p>
 *   The following test case configuration can be used as a basis for new tests.
 *   Detailed explanations of the configuration elements can be found after
 *   the example.
 * </p>
 * <pre>
 *   &lt;testcase&gt;
 *     &lt;context&gt;
 *       &lt;entry name="foo" value="bar"/&gt;
 *       &lt;entry name="baz" class="my.context.Class"/&gt;
 *     &lt;/context&gt;
 *
 *     &lt;roles&gt;
 *       &lt;role name="org.apache.avalon.excalibur.datasource.DataSourceComponentSelector"
 *             shorthand="datasources"
 *             default-class="org.apache.avalon.excalibur.component.ExcaliburComponentSelector"&gt;
 *          &lt;hint shorthand="jdbc" class="org.apache.avalon.excalibur.datasource.JdbcDataSource"/&gt;
 *       &lt;/role&gt;
 *     &lt;/roles&gt;
 *
 *     &lt;components&gt;
 *       &lt;datasources&gt;
 *         &lt;jdbc name="personell"&gt;
 *           &lt;pool-controller min="5" max="10"/&gt;
 *           &lt;jdbc name="personnel"/&gt;
 *           &lt;dburl&gt;jdbc:odbc:test&lt;/dburl&gt;
 *           &lt;user&gt;test&lt;/user&gt;
 *           &lt;password&gt;test&lt;/password&gt;
 *           &lt;driver&gt;sun.jdbc.odbc.JdbcOdbcDriver&lt;/driver&gt;
 *         &lt;/jdbc&gt;
 *       &lt;/datasources&gt;
 *     &lt;/components&gt;
 *   &lt;/testcase&gt;
 * </pre>
 * <p>
 * Element Explanation:
 * <dl>
 * <dt>testcase</dt>
 * <dd>Defines a test case configuration.  Must contain one each of the
 *  following elements: <code>annotation</code>,
 *  <code>context</code>, <code>roles</code>, and <code>components</code>
 *  </dd>.
 *
 * <dt>context</dt>
 * <dd>Allows context properties to be set in the context passed to any
 *  Contextualizable components.</dd>
 *
 * <dt>roles</dt>
 * <dd>Roles configuration for the components configured in the
 *  <code>components</code> element.
 * </dd>
 *
 * <dt>components</dt>
 * <dd>Used to configure any Components used by the test cases.
 * </dd>
 *
 * </dl>
 *
 * @version $Id: $
 */
public class ContainerTestCase extends TestCase {

    /** The default logger */
    private Logger logger;

    /** The service manager to use */
    private ServiceManager manager;

    /** Return the logger */
    protected Logger getLogger() {
        return logger;
    }

    /** Return the service manager */
    protected ServiceManager getManager() {
        return this.manager;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        String level = System.getProperty("junit.test.loglevel", "" + ConsoleLogger.LEVEL_WARN);
        this.logger = new ConsoleLogger(Integer.parseInt(level));
        Deprecation.setLogger(this.logger);
        prepare();
    }

    /**
     * Initializes the ComponentLocator
     *
     * The configuration file is determined by the class name plus .xtest appended,
     * all '.' replaced by '/' and loaded as a resource via classpath
     */
    protected void prepare()
    throws Exception {
        final String resourceName = getClass().getName().replace( '.', '/' ) + ".xtest";
        URL resource = getClass().getClassLoader().getResource( resourceName );

        if (resource != null) {
            getLogger().debug("Loading resource " + resourceName);
            prepare(resource.openStream());
        } else {
            getLogger().debug("Resource not found " + resourceName);
        }
    }

    /**
     * Initializes the ComponentLocator
     *
     * @param testconf The configuration file is passed as a <code>InputStream</code>
     *
     * A common way to supply a InputStream is to overwrite the initialize() method
     * in the sub class, do there whatever is needed to get the right InputStream object
     * supplying a conformant xtest configuartion and pass it to this initialize method.
     * the mentioned initialize method is also the place to set a different logging priority
     * to the member variable m_logPriority.
     */
    protected final void prepare(final InputStream testconf)
    throws Exception {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("ContainerTestCase.initialize");
        }

        final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        final Configuration conf = builder.build(testconf);

        Context context = this.setupContext(conf.getChild("context"));

        setupManagers(conf.getChild("components"),
                      conf.getChild("roles"),
                      context);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        done();
        super.tearDown();
    }

    /**
     * Disposes the <code>ComponentLocator</code>
     */
    final private void done() {
        if (manager != null) {
            ContainerUtil.dispose(manager);
            manager = null;
        }
    }

    /**
     * set up a context according to the xtest configuration specifications context
     * element.
     *
     * A method addContext(DefaultContext context) is called here to enable subclasses
     * to put additional objects into the context programmatically.
     */
    final private Context setupContext( final Configuration conf )
    throws Exception {
        final DefaultContext context = new DefaultContext();
        final Configuration[] confs = conf.getChildren( "entry" );
        for (int i = 0; i < confs.length; i++) {
            final String key = confs[i].getAttribute("name");
            final String value = confs[i].getAttribute("value", null);
            if (value == null) {
                String clazz = confs[i].getAttribute("class");
                Object obj = getClass().getClassLoader().loadClass(clazz).newInstance();
                context.put(key, obj);
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("ContainerTestCase: added an instance of class " + clazz + " to context entry " + key);
                }
            } else {
                context.put(key, value);
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("ContainerTestCase: added value \"" + value + "\" to context entry " + key);
                }
            }
        }
        addContext(context);
        return context ;
    }

    /**
     * This method may be overwritten by subclasses to put additional objects
     * into the context programmatically.
     */
    protected void addContext(DefaultContext context) {
    }

    final private void setupManagers(final Configuration confCM,
                                     final Configuration confRM,
                                     final Context context)
    throws Exception {
        // Setup the RoleManager
        DefaultRoleManager roleManager = new DefaultRoleManager();
        roleManager.enableLogging(getLogger());
        roleManager.configure(confRM);

        // Set up the ComponentLocator
        ExcaliburComponentManager ecManager = new ExcaliburComponentManager();
        ecManager.enableLogging(getLogger());
        ecManager.contextualize(context);
        ecManager.setRoleManager(roleManager);
        ecManager.setLoggerManager(new DefaultLoggerManager(getLogger()));
        ecManager.configure(confCM);
        ecManager.initialize();
        this.manager = new WrapperServiceManager(ecManager);
    }

    protected final Object lookup(final String key)
    throws ServiceException {
        return manager.lookup(key);
    }

    protected final void release(final Object object) {
        manager.release(object);
    }

    protected static class DefaultLoggerManager implements LoggerManager {
        private Logger logger;

        public DefaultLoggerManager(Logger logger) {
            this.logger = logger;
        }
        /* (non-Javadoc)
         * @see org.apache.avalon.excalibur.logger.LoggerManager#getDefaultLogger()
         */
        public Logger getDefaultLogger() {
            return this.logger;
        }
        /* (non-Javadoc)
         * @see org.apache.avalon.excalibur.logger.LoggerManager#getLoggerForCategory(java.lang.String)
         */
        public Logger getLoggerForCategory(String arg0) {
            return this.logger;
        }
    }
}
