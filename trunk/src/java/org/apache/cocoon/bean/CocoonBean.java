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

 4. The names "Jakarta", "Avalon", "Excalibur" and "Apache Software Foundation"
    must not be used to endorse or promote products derived from this  software
    without  prior written permission. For written permission, please contact
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
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.fortress.util.LifecycleExtensionManager;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.CompilingProcessor;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.CocoonContainer;
import org.apache.cocoon.components.SitemapConfigurableAccessor;

/**
 * CocoonBean does XYZ
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
public class CocoonBean
{
    private final FortressConfig m_confBuilder;
    private ContainerManager m_contManager;
    private Logger m_initializationLogger;
    private String m_contextURI = Constants.DEFAULT_CONTEXT_DIR;
    private File m_workDirectory = new File( System.getProperty( "java.io.tmpdir" ) );
    private String m_logConfigURI = m_contextURI + File.separator + "cocoon.xlog";
    private String m_logCategory = "";
    private String m_configURI = Constants.DEFAULT_CONF_FILE;
    private String m_roleConfigURI = "resource://org/apache/cocoon/cocoon.roles";
    private List m_classForceLoadList = new ArrayList();
    private String m_classPath = System.getProperty( "java.class.path" );
    private int m_threadsPerCPU = 1;
    private String m_instrumentConfigURI = m_contextURI + File.separator + "cocoon.instruments";
    private long m_threadTimeOut = 60l * 1000l;
    private ClassLoader m_parentClassLoader;
    private boolean m_alreadyLoaded;
    private Map m_properties;

    public CocoonBean()
    {
        m_confBuilder = new FortressConfig();
        m_parentClassLoader = Thread.currentThread().getContextClassLoader();
        m_initializationLogger = new NullLogger();
        m_contManager = null;
        m_alreadyLoaded = false;
        m_properties = new HashMap();
    }

    public CompilingProcessor getRootProcessor()
    {
        ServiceManager manager = getServiceManager();

        CompilingProcessor rootProcessor = null;
        try
        {
            rootProcessor = (CompilingProcessor)manager.lookup( CompilingProcessor.ROLE);
        }
        catch ( ServiceException e )
        {
            throw new CascadingRuntimeException("Error retrieving root processor", e);
        }

        return rootProcessor;
    }

    protected ServiceManager getServiceManager(){
        if (null == m_contManager)
        {
            try
            {
                initialize();
            }
            catch ( Exception e )
            {
                throw new CascadingRuntimeException("Error starting up container", e);
            }
        }

        CocoonContainer container = (CocoonContainer)m_contManager.getContainer();
        ServiceManager manager = container.getServiceManager();
        return manager;
    }


    public Logger getInitializationLogger()
    {
        return m_initializationLogger;
    }

    public void setInitializationLogger( Logger initializationLogger )
    {
        m_initializationLogger = initializationLogger;
    }

    public File getWorkDirectory()
    {
        return m_workDirectory;
    }

    public void setWorkDirectory( File workDirectory )
    {
        m_workDirectory = workDirectory;
    }

    public int getThreadsPerCPU()
    {
        return m_threadsPerCPU;
    }

    public void setThreadsPerCPU( int threadsPerCPU )
    {
        m_threadsPerCPU = threadsPerCPU;
    }

    public ClassLoader getParentClassLoader()
    {
        return m_parentClassLoader;
    }

    public void setParentClassLoader( ClassLoader parentClassLoader )
    {
        m_parentClassLoader = parentClassLoader;
    }

    public long getThreadTimeOut()
    {
        return m_threadTimeOut;
    }

    public void setThreadTimeOut( long threadTimeOut )
    {
        m_threadTimeOut = threadTimeOut;
    }

    public String getContextURI()
    {
        return m_contextURI;
    }

    public void setContextURI( String contextURI )
    {
        m_contextURI = contextURI;
    }

    public String getInstrumentConfigURI()
    {
        return m_instrumentConfigURI;
    }

    public void setInstrumentConfigURI( String instrumentConfigURI )
    {
        m_instrumentConfigURI = instrumentConfigURI;
    }

    public String getLogConfigURI()
    {
        return m_logConfigURI;
    }

    public void setLogConfigURI( String logConfigURI )
    {
        m_logConfigURI = logConfigURI;
    }

    public String getLogCategory()
    {
        return m_logCategory;
    }

    public void setLogCategory( String logCategory )
    {
        m_logCategory = logCategory;
    }

    public String getConfigURI()
    {
        return m_configURI;
    }

    public void setConfigURI( String configURI )
    {
        m_configURI = configURI;
    }

    public List getClassForceLoadList()
    {
        return m_classForceLoadList;
    }

    public void setClassForceLoadList( List classForceLoadList )
    {
        m_classForceLoadList = classForceLoadList;
    }

    public String getClassPath()
    {
        return m_classPath;
    }

    public void setClassPath( String classPath )
    {
        m_classPath = classPath;
    }

    public void setProperty( String key, Object value )
    {
        if ( null == value )
        {
            m_properties.remove( key );
        }
        else
        {
            m_properties.put( key, value );
        }
    }

    public Object getProperty( String key )
    {
        return m_properties.get( key );
    }

    public void clearAllProperties()
    {
        m_properties.clear();
    }

    public void initialize() throws Exception
    {
        // restart....
        if (null != m_contManager) dispose();

        forceLoadClasses();

        m_initializationLogger.debug("Starting up Cocoon");

        m_confBuilder.setContextDirectory( m_contextURI );
        m_confBuilder.setContainerConfiguration( m_configURI );
        m_confBuilder.setLoggerCategory( m_logCategory );
        m_confBuilder.setLoggerManagerConfiguration( m_logConfigURI );
        m_confBuilder.setNumberOfThreadsPerCPU( m_threadsPerCPU );
        m_confBuilder.setThreadTimeout( m_threadTimeOut );
        m_confBuilder.setWorkDirectory( m_workDirectory );
        m_confBuilder.setInstrumentManagerConfiguration( m_instrumentConfigURI );
        m_confBuilder.setContextClassLoader( m_parentClassLoader );
        m_confBuilder.setCommandFailureHandlerClass( CocoonCommandFailureHandler.class );
        m_confBuilder.setContainerClass(CocoonContainer.class);
        m_confBuilder.setRoleManagerConfiguration(m_roleConfigURI);
        
        m_confBuilder.setLifecycleExtensionManager( getLifecycleExtensionManager() );

        DefaultContext initContext = new DefaultContext( m_confBuilder.getContext() );
        Iterator it = m_properties.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry)it.next();
            initContext.put(entry.getKey(), entry.getValue());
        }

        m_contManager = new DefaultContainerManager( initContext, m_initializationLogger );
        ContainerUtil.initialize( m_contManager );
    }

    private LifecycleExtensionManager getLifecycleExtensionManager()
    {
        LifecycleExtensionManager manager = new LifecycleExtensionManager();
        manager.addAccessorExtension(new SitemapConfigurableAccessor());

        return manager;
    }

    private void forceLoadClasses()
    {
        if ( m_alreadyLoaded ) return;

        m_initializationLogger.debug("Loading classes");

        Iterator it = m_classForceLoadList.iterator();
        while (it.hasNext())
        {
            String className = (String) it.next();
            m_initializationLogger.debug("Loading class: " + className);

            try
            {
                m_parentClassLoader.loadClass(className);
            }
            catch (Exception e)
            {
                m_initializationLogger.warn("Could not load class: " + className, e);
            }
        }

        m_alreadyLoaded = true;
    }

    public void dispose()
    {
        m_initializationLogger.debug("Shutting down Cocoon");
        ContainerUtil.dispose( m_contManager );
        m_contManager = null;
    }

    protected void finalize() throws Throwable
    {
        dispose();
        super.finalize();
    }
    
}
