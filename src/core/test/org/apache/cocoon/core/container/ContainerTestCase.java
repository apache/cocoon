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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestResult;

import org.apache.avalon.excalibur.logger.DefaultLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * JUnit TestCase for Avalon Components.
 * <p>
 *   This class extends the JUnit TestCase class to setup an environment which
 *   makes it possible to easily test Avalon Components. The following methods
 *   and instance variables are exposed for convenience testing:
 * </p>
 * <dl>
 *   <dt>manager</dt>
 *   <dd>
 *     This instance variable contains an initialized ComponentLocator which
 *     can be used to lookup Components configured in the test configuration
 *     file. (see below)
 *   </dd>
 *   <dt>getLogger()</dt>
 *   <dd>
 *     This method returns the default logger for this test case
 *   </dd>
 * </dl>
 * <p>
 *   The following test case configuration can be used as a basis for new tests.
 *   Detailed are explanations of the configuration elements can be found after
 *   the example.  The example will log all logger output to the console and to
 *   a log file.
 * </p>
 * <pre>
 *   &lt;testcase&gt;
 *     &lt;annotation&gt;
 *       &lt;![CDATA[
 *         &lt;title&gt;{Name of test}&lt;/title&gt;
 *         &lt;para&gt;
 *           {Description of test}
 *           The configuration is specified in the file located in
 *           &lt;parameter&gt;avalon-excalibur/src/test/{path and name of conf file}.xtext&lt;/parameter&gt;.
 *         &lt;/para&gt;
 *       ]]&gt;
 *     &lt;/annotation&gt;
 *
 *     &lt;logkit log-level="INFO"&gt;
 *       &lt;factories&gt;
 *         &lt;factory type="stream" class="org.apache.avalon.excalibur.logger.factory.StreamTargetFactory"/&gt;
 *         &lt;factory type="file" class="org.apache.avalon.excalibur.logger.factory.FileTargetFactory"/&gt;
 *       &lt;/factories&gt;
 *
 *       &lt;targets&gt;
 *         &lt;stream id="console"&gt;
 *           &lt;stream&gt;System.out&lt;/stream&gt;
 *           &lt;format type="avalon"&gt;
 *             %7.7{priority} %23.23{time:yyyy-MM-dd' 'HH:mm:ss.SSS} [%30.30{category}] (%{context}): %{message}\n%{throwable}
 *           &lt;/format&gt;
 *         &lt;/stream&gt;
 *         &lt;file id="log-file"&gt;
 *           &lt;filename&gt;TEST-{full test class name}.log&lt;/filename&gt;
 *           &lt;format type="avalon"&gt;
 *             %7.7{priority} %23.23{time:yyyy-MM-dd' 'HH:mm:ss.SSS} [%30.30{category}] (%{context}): %{message}\n%{throwable}
 *           &lt;/format&gt;
 *         &lt;/file&gt;
 *       &lt;/targets&gt;
 *
 *       &lt;categories&gt;
 *         &lt;category name="test" log-level="INFO"&gt;
 *           &lt;log-target id-ref="console"/&gt;
 *           &lt;log-target id-ref="log-file"/&gt;
 *         &lt;/category&gt;
 *         &lt;category name="jdbc" log-level="INFO"&gt;
 *           &lt;log-target id-ref="console"/&gt;
 *           &lt;log-target id-ref="log-file"/&gt;
 *         &lt;/category&gt;
 *       &lt;/categories&gt;
 *     &lt;/logkit&gt;
 *
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
 *         &lt;jdbc name="personell" logger="jdbc"&gt;
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
 *  following elements: <code>annotation</code>, <code>logkit</code>,
 *  <code>context</code>, <code>roles</code>, and <code>components</code>
 *  </dd>.
 *
 * <dt>annotation</dt>
 * <dd>Defines a test annotation.  This element should define a block of
 *  XML enclosed within a CDATA element.  The XML should be made up of a
 *  <code>title</code> element, naming the test, and a <code>para</code>
 *  element which is used to describe the test.</dd>
 *
 * <dt>logkit</dt>
 * <dd>Configures the logger used by the test cases and the components used
 *  by the tests.  The <code>logkit</code> element takes two optional
 *  attributes:
 *      <dl>
 *      <dt>logger</dt><dd>Uses to name the logger which is used to bootstrap
 *       the LogKit logger.  (Defaults to <code>"lm"</code>)</dd>
 *      <dt>log-level</dt><dd>Because the logger used by the LogKit must be
 *       created before the Log Kit Manager is initialized, it must be fully
 *       configured before the <code>logkit</code> element is parsed.  This
 *       attribute allows the Log Kit's log priority to be set.  This log
 *       level will also become the default for the Role Manager, Component
 *       Manager, and all components if they do not have <code>category</code>
 *       elements declated in the <code>logkit</code> element.
 *       (Defaults to "INFO")</dd>
 *      </dl>
 *  The loggers used by test cases and components can be easily configured
 *  from within this file.  The default test configuration, shown above,
 *  includes a "test" category.  This category is used to configure the
 *  default logger for all test cases.  If it is set to "DEBUG", then all
 *  test debug logging will be enabled.  To enalble debug logging for a
 *  single test case, a child category must be defined for the
 *  "testCheckTotals" test case as follows:
 *  <pre>
 *       &lt;categories&gt;
 *         &lt;category name="test" log-level="INFO"&gt;
 *           &lt;log-target id-ref="console"/&gt;
 *           &lt;log-target id-ref="log-file"/&gt;
 *
 *           &lt;category name="testCheckTotals" log-level="DEBUG"&gt;
 *             &lt;log-target id-ref="console"/&gt;
 *             &lt;log-target id-ref="log-file"/&gt;
 *           &lt;/category&gt;
 *         &lt;/category&gt;
 *       &lt;/categories&gt;
 *  </pre>
 *  For general information on how to configure the LogKit Manager, please
 *  refer to the Log Kit documentation.
 * </dd>
 *
 * <dt>context</dt>
 * <dd>Allows context properties to be set in the context passed to any
 *  Contextualizable components.</dd>
 *
 * <dt>roles</dt>
 * <dd>Roles configuration for the Components configured in the
 *  <code>components</code> element.  The logger used by the RoleManager
 *  can be configured using a <code>logger</code> attribute, which defaults
 *  to "rm".  By default this logger will have the same log level and
 *  formatting as the LogKit logger.  It can be configured by adding a
 *  <code>category</code> within the <code>logkit</code> element.</dd>
 *
 * <dt>components</dt>
 * <dd>Used to configure any Components used by the test cases.  The logger
 *  used by the ComponentLocator can be configured using a <code>logger</code>
 *  attribute, which defaults to "cm".  By default this logger will have the
 *  same log level and formatting as the LogKit logger.  It can be configured
 *  by adding a <code>category</code> within the <code>logkit</code> element.
 *  </dd>
 *
 * </dl>
 *
 * @version $Id: ExcaliburTestCase.java,v 1.6 2004/02/28 11:47:27 cziegeler Exp $
 */
public class ContainerTestCase extends TestCase {
    
    ///Format of default formatter
    private static final String FORMAT =
        "%7.7{priority} %23.23{time:yyyy-MM-dd' 'HH:mm:ss.SSS} [%30.30{category}] (%{context}): %{message}\n%{throwable}";

    //The default logger
    private Logger m_logger;
    private CocoonServiceManager m_manager;
    private LoggerManager m_logKitManager;
    private static HashMap m_tests = new HashMap();

    protected ServiceManager manager;

    public ContainerTestCase( final String name ) {
        super( name );

        ArrayList methodList = (ArrayList)ContainerTestCase.m_tests.get( getClass() );

        if( null == methodList )
        {
            Method[] methods = getClass().getMethods();
            methodList = new ArrayList( methods.length );

            for( int i = 0; i < methods.length; i++ )
            {
                String methodName = methods[ i ].getName();
                if( methodName.startsWith( "test" ) &&
                    ( Modifier.isPublic( methods[ i ].getModifiers() ) ) &&
                    ( methods[ i ].getReturnType().equals( Void.TYPE ) ) &&
                    ( methods[ i ].getParameterTypes().length == 0 ) )
                {
                    methodList.add( methodName );
                }
            }

            ContainerTestCase.m_tests.put( getClass(), methodList );
        }
    }

    /** Return the logger */
    protected Logger getLogger()
    {
        return m_logger;
    }

    /**
     * Initializes the ComponentLocator
     *
     * The configuration file is determined by the class name plus .xtest appended,
     * all '.' replaced by '/' and loaded as a resource via classpath
     */
    protected void prepare()
        throws Exception
    {
        final String resourceName = getClass().getName().replace( '.', '/' ) + ".xtest";
        URL resource = getClass().getClassLoader().getResource( resourceName );

        if( resource != null )
        {
            getLogger().debug( "Loading resource " + resourceName );
            prepare( resource.openStream() );
        }
        else
        {
            getLogger().debug( "Resource not found " + resourceName );
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
        throws Exception
    {
        getLogger().debug( "ExcaliburTestCase.initialize" );

        final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        final Configuration conf = builder.build( testconf );

        String annotation = conf.getChild( "annotation" ).getValue( null );

        if( ( null != annotation ) && !( "".equals( annotation ) ) )
        {
            m_logger.info( annotation );
        }

        Context context = setupContext( conf.getChild( "context" ) );

        setupManagers( conf.getChild( "components" ),
                       conf.getChild( "roles" ),
                       conf.getChild( "logkit" ),
                       context );
        manager = m_manager;

        setCurrentLogger( "prepare" );
    }

    /**
     * Disposes the <code>ComponentLocator</code>
     */
    final private void done()
    {
        if( null != m_manager )
        {
            m_manager.dispose();
        }

        m_manager = null;
    }

    /**
     * Exctract the base class name of a class.
     */
    private String getBaseClassName( Class clazz )
    {
        String name = clazz.getName();
        int pos = name.lastIndexOf( '.' );
        if( pos >= 0 )
        {
            name = name.substring( pos + 1 );
        }
        return name;
    }

    /**
     * Override <code>run</code> so that we can have code that is run once.
     */
    final public void run( TestResult result )
    {
        ArrayList methodList = (ArrayList)ContainerTestCase.m_tests.get( getClass() );

        if( null == methodList || methodList.isEmpty() )
        {
            return; // The test was already run!  NOTE: this is a hack.
        }

        // Set the logger for the initialization phase.
        setCurrentLogger( getBaseClassName( getClass() ) );

        try
        {
            if( this instanceof Initializable )
            {
                ( (Initializable)this ).initialize();
            }

            prepare();

            Iterator tests = methodList.iterator();

            while( tests.hasNext() )
            {
                String methodName = (String)tests.next();
                setName( methodName );
                setCurrentLogger( methodName );

                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "" );
                    getLogger().debug( "========================================" );
                    getLogger().debug( "  begin test: " + methodName );
                    getLogger().debug( "========================================" );
                }

                super.run( result );

                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "========================================" );
                    getLogger().debug( "  end test: " + methodName );
                    getLogger().debug( "========================================" );
                    getLogger().debug( "" );
                }
            }

        }
        catch( Exception e )
        {
            System.out.println( e );
            e.printStackTrace();
            result.addError( this, e );
        }
        finally
        {
            done();

            if( this instanceof Disposable )
            {
                try
                {
                    ( (Disposable)this ).dispose();
                }
                catch( Exception e )
                {
                    result.addFailure( this, new AssertionFailedError( "Disposal Error" ) );
                }
            }
        }

        methodList.clear();
        ContainerTestCase.m_tests.put( getClass(), methodList );
    }

    /**
     * Sets the logger which will be returned by getLogger and getLogEnabledLogger
     */
    final private void setCurrentLogger( String name ) {
        m_logger = m_logKitManager.getLoggerForCategory( "test." + name );;
    }

    /**
     * set up a context according to the xtest configuration specifications context
     * element.
     *
     * A method addContext(DefaultContext context) is called here to enable subclasses
     * to put additional objects into the context programmatically.
     */
    final private Context setupContext( final Configuration conf )
        throws Exception
    {
        //FIXME(GP): This method should setup the Context object according to the
        //           configuration spec. not yet completed
        final DefaultContext context = new DefaultContext();
        final Configuration[] confs = conf.getChildren( "entry" );
        for( int i = 0; i < confs.length; i++ )
        {
            final String key = confs[ i ].getAttribute( "name" );
            final String value = confs[ i ].getAttribute( "value", null );
            if( value == null )
            {
                String clazz = confs[ i ].getAttribute( "class" );
                Object obj = getClass().getClassLoader().loadClass( clazz ).newInstance();
                context.put( key, obj );
                if( getLogger().isInfoEnabled() )
                    getLogger().info( "ExcaliburTestCase: added an instance of class " + clazz + " to context entry " + key );
            }
            else
            {
                context.put( key, value );
                if( getLogger().isInfoEnabled() )
                    getLogger().info( "ExcaliburTestCase: added value \"" + value + "\" to context entry " + key );
            }
        }
        addContext( context );
        return ( context );
    }

    /**
     * This method may be overwritten by subclasses to put additional objects
     * into the context programmatically.
     */
    protected void addContext( DefaultContext context )
    {
    }

    final private void setupManagers( final Configuration confCM,
                                      final Configuration confRM,
                                      final Configuration confLM,
                                      final Context context )
        throws Exception
    {
        // Setup the log manager.  Get the logger name and log level from attributes
        //  in the <logkit> node
        String lmLoggerName = confLM.getAttribute( "logger", "lm" );
        String lmLogLevel = confLM.getAttribute( "log-level", "INFO" );
        DefaultLoggerManager logKitManager = new DefaultLoggerManager();
        logKitManager.enableLogging( new ConsoleLogger() );
        logKitManager.contextualize( context );
        logKitManager.configure( confLM );
        m_logKitManager = logKitManager;

        // Setup the RoleManager
        String rmLoggerName = confRM.getAttribute( "logger", "rm" );
        RoleManager roleManager = new RoleManager();
        roleManager.enableLogging( logKitManager.getLoggerForCategory( rmLoggerName ) );
        roleManager.configure( confRM );

        // Set up the ComponentLocator
        String cmLoggerName = confCM.getAttribute( "logger", "cm" );
        CocoonServiceManager ecManager = new CocoonServiceManager(null, null);
        ecManager.enableLogging( logKitManager.getLoggerForCategory( cmLoggerName ) );
        ecManager.setLoggerManager( logKitManager );
        ecManager.contextualize( context );
        ecManager.setRoleManager( roleManager );
        ecManager.configure( confCM );
        ecManager.initialize();
        m_manager = ecManager;
    }

    protected final Object lookup( final String key )
    throws ServiceException {
        return manager.lookup( key );
    }

    protected final void release( final Object object ) {
        manager.release( object );
    }
}

