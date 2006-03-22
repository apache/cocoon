/* 
 * Copyright 2002-2004 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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
package org.apache.cocoon.core.container;

import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.core.MutableSettings;
import org.apache.cocoon.core.container.spring.BeanFactoryUtil;
import org.apache.cocoon.core.container.spring.AvalonEnvironment;
import org.apache.cocoon.core.container.spring.ConfigReader;
import org.apache.cocoon.core.container.spring.ConfigurationInfo;
import org.apache.cocoon.environment.mock.MockContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

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
 *  following elements: 
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
 * @version $Id$
 */
public class ContainerTestCase extends TestCase {
    
    /** The default logger */
    private Logger logger;
    
    /** The service manager to use */
    private ServiceManager manager;

    /** The context */
    private Context context;

    /** The root bean factory. */
    private ConfigurableListableBeanFactory rootBeanFactory;

    /** Return the logger */
    protected Logger getLogger() {
        return logger;
    }

    /** Return the service manager. */
    protected ServiceManager getManager() {
        return this.manager;
    }

    /** Return the bean factory. */
    protected ConfigurableListableBeanFactory getBeanFactory() {
        return this.rootBeanFactory;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        String level = System.getProperty("junit.test.loglevel", "" + ConsoleLogger.LEVEL_WARN);
        this.logger = new ConsoleLogger(Integer.parseInt(level));
        this.prepare();
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

        if( resource != null ) {
            getLogger().debug( "Loading resource " + resourceName );
            this.prepare( resource.openStream() );
        } else {
            getLogger().debug( "Resource not found " + resourceName );
            this.prepare( null );
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
    protected final void prepare( final InputStream testconf )
    throws Exception {
        if ( getLogger().isDebugEnabled() ) {
            getLogger().debug( "Initializing " + this.getName() );
        }

        final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        final Configuration conf;
        if ( testconf != null ) {
            conf = builder.build( testconf );
        } else {
            conf = new DefaultConfiguration("", "-");
        }

        // setup context
        this.context = this.setupContext( conf.getChild( "context" ) );

        this.setupManagers( conf.getChild( "components" ),  conf.getChild( "roles" ) );
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        this.done();
        super.tearDown();
    }

    /**
     * Disposes the <code>ComponentLocator</code>
     */
    final private void done() {
        if( this.rootBeanFactory != null ) {
            this.rootBeanFactory.destroySingletons();
            this.rootBeanFactory = null;
        }
        this.manager = null;
        this.context = null;
        this.logger = null;
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
        for( int i = 0; i < confs.length; i++ ) {
            final String key = confs[ i ].getAttribute( "name" );
            final String value = confs[ i ].getAttribute( "value", null );
            if( value == null ) {
                String clazz = confs[ i ].getAttribute( "class" );
                Object obj = getClass().getClassLoader().loadClass( clazz ).newInstance();
                context.put( key, obj );
                if( getLogger().isInfoEnabled() ) {
                    getLogger().info( "ContainerTestCase: added an instance of class " + clazz + " to context entry " + key );
                }
            } else {
                context.put( key, value );
                if( getLogger().isInfoEnabled() ) {
                    getLogger().info( "ContainerTestCase: added value \"" + value + "\" to context entry " + key );
                }
            }
        }
        this.addContext( context );
        context.makeReadOnly();
        return context ;
    }

    /**
     * This method may be overwritten by subclasses to put additional objects
     * into the context programmatically.
     */
    protected void addContext( DefaultContext context ) {
        // nothing to add here
    }

    /**
     * This method may be overwritten by subclasses to add aditional
     * components.
     */
    protected void addComponents(ConfigurationInfo info) 
    throws Exception {
        // subclasses can add components here
    }
    
    final private void setupManagers( final Configuration confCM,
                                      final Configuration confRM)
    throws Exception {
        final AvalonEnvironment avalonEnv = new AvalonEnvironment();
        avalonEnv.logger = this.logger;
        avalonEnv.context = this.context;
        avalonEnv.settings = new MutableSettings();
        avalonEnv.servletContext = new MockContext();

        this.rootBeanFactory = BeanFactoryUtil.createRootBeanFactory(avalonEnv);
        // read roles
        ConfigurationInfo rolesInfo = ConfigReader.readConfiguration(confRM, null, avalonEnv, null);
        ConfigurableListableBeanFactory rolesContext = BeanFactoryUtil.createBeanFactory(avalonEnv, rolesInfo, this.rootBeanFactory, true);

        // read components
        ConfigurationInfo componentsInfo = ConfigReader.readConfiguration(confCM, rolesInfo, avalonEnv, null);
        this.addComponents( componentsInfo );
        ConfigurableListableBeanFactory componentsContext = BeanFactoryUtil.createBeanFactory(avalonEnv, componentsInfo, rolesContext, false);

        this.manager = (ServiceManager)componentsContext.getBean(ServiceManager.class.getName());
    }

    protected final Object lookup( final String key )
    throws ServiceException {
        return manager.lookup( key );
    }

    protected final void release( final Object object ) {
        manager.release( object );
    }
    
    private Object getComponent(String classname,
                                Configuration conf,
                                Parameters p) 
    throws Exception {
        final Object instance = Class.forName(classname).newInstance();
        ContainerUtil.enableLogging(instance, getLogger());
        ContainerUtil.contextualize(instance, this.context);
        ContainerUtil.service(instance, getManager());
        if ( instance instanceof Configurable ) {
            // default configuration to invoke method!
            if ( conf == null ) {
                conf = new DefaultConfiguration("", "-");
            }
            ContainerUtil.configure(instance, conf);
        }
        if ( instance instanceof Parameterizable ) {
            // default configuration to invoke method!
            if ( p == null ) {
                p = new Parameters();
            }
            ContainerUtil.parameterize(instance, p);                       
        }
        ContainerUtil.initialize(instance);
        return instance;
    }
    
    protected Object getComponent(String classname,
                                  Configuration conf) 
    throws Exception {
        return this.getComponent(classname, conf, null);
    }

    protected Object getComponent(String classname,
                                  Parameters p) 
    throws Exception {
        return this.getComponent(classname, null, p);
    }
    
    protected Object getComponent(String classname) 
    throws Exception {
        return this.getComponent(classname, null, null);
    }
}
