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
import org.apache.cocoon.components.container.CocoonContainer;
import org.apache.cocoon.components.container.ComponentContext;
import org.apache.cocoon.components.container.SitemapConfigurableCreator;

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

    public CocoonBean() {
        m_confBuilder = new FortressConfig();
        m_parentClassLoader = Thread.currentThread().getContextClassLoader();
        m_initializationLogger = new NullLogger();
        m_alreadyLoaded = false;
        m_properties = new HashMap();
    }

    public CompilingProcessor getRootProcessor() {
        // TODO - the rootProcessor is never released
        ServiceManager manager = getServiceManager();

        CompilingProcessor rootProcessor = null;
        try {
            rootProcessor = (CompilingProcessor)manager.lookup( CompilingProcessor.ROLE);
        } catch ( ServiceException e ) {
            throw new CascadingRuntimeException("Error retrieving root processor", e);
        }

        return rootProcessor;
    }

    protected ServiceManager getServiceManager(){
        if (null == m_contManager) {
            try {
                initialize();
            } catch ( Exception e ) {
                throw new CascadingRuntimeException("Error starting up container", e);
            }
        }

        CocoonContainer container = (CocoonContainer)m_contManager.getContainer();
        ServiceManager manager = container.getServiceManager();
        return manager;
    }


    public Logger getInitializationLogger() {
        return m_initializationLogger;
    }

    public void setInitializationLogger( Logger initializationLogger ) {
        m_initializationLogger = initializationLogger;
    }

    public File getWorkDirectory() {
        return m_workDirectory;
    }

    public void setWorkDirectory( File workDirectory ) {
        m_workDirectory = workDirectory;
    }

    public int getThreadsPerCPU() {
        return m_threadsPerCPU;
    }

    public void setThreadsPerCPU( int threadsPerCPU ) {
        m_threadsPerCPU = threadsPerCPU;
    }

    public ClassLoader getParentClassLoader() {
        return m_parentClassLoader;
    }

    public void setParentClassLoader( ClassLoader parentClassLoader ) {
        m_parentClassLoader = parentClassLoader;
    }

    public long getThreadTimeOut() {
        return m_threadTimeOut;
    }

    public void setThreadTimeOut( long threadTimeOut ) {
        m_threadTimeOut = threadTimeOut;
    }

    public String getContextURI() {
        return m_contextURI;
    }

    public void setContextURI( String contextURI ) {
        m_contextURI = contextURI;
    }

    public String getInstrumentConfigURI() {
        return m_instrumentConfigURI;
    }

    public void setInstrumentConfigURI( String instrumentConfigURI ) {
        m_instrumentConfigURI = instrumentConfigURI;
    }

    public String getLogConfigURI() {
        return m_logConfigURI;
    }

    public void setLogConfigURI( String logConfigURI ) {
        m_logConfigURI = logConfigURI;
    }

    public String getLogCategory() {
        return m_logCategory;
    }

    public void setLogCategory( String logCategory ) {
        m_logCategory = logCategory;
    }

    public String getConfigURI() {
        return m_configURI;
    }

    public void setConfigURI( String configURI ) {
        m_configURI = configURI;
    }

    public List getClassForceLoadList() {
        return m_classForceLoadList;
    }

    public void setClassForceLoadList( List classForceLoadList ) {
        m_classForceLoadList = classForceLoadList;
    }

    public String getClassPath() {
        return m_classPath;
    }

    public void setClassPath( String classPath ) {
        m_classPath = classPath;
    }

    public void setProperty( String key, Object value ) {
        if ( null == value ) {
            m_properties.remove( key );
        } else {
            m_properties.put( key, value );
        }
    }

    public Object getProperty( String key ) {
        return m_properties.get( key );
    }

    public void clearAllProperties() {
        m_properties.clear();
    }

    public void initialize() throws Exception {
        // restart....
        if (null != m_contManager) dispose();

        forceLoadClasses();

        m_initializationLogger.info("Starting up Cocoon " + Constants.VERSION);

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

        DefaultContext initContext = new ComponentContext( m_confBuilder.getContext() );
        Iterator it = m_properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            initContext.put(entry.getKey(), entry.getValue());
        }
        initContext.put(Constants.CONTEXT_WORK_DIR,m_workDirectory);

        m_contManager = new DefaultContainerManager( initContext, m_initializationLogger );
        ContainerUtil.initialize( m_contManager );
    }

    private LifecycleExtensionManager getLifecycleExtensionManager() {
        LifecycleExtensionManager manager = new LifecycleExtensionManager();
        manager.addCreatorExtension(new SitemapConfigurableCreator());

        return manager;
    }

    private void forceLoadClasses() {
        if ( m_alreadyLoaded ) return;

        m_initializationLogger.debug("Loading classes");

        Iterator it = m_classForceLoadList.iterator();
        while (it.hasNext()) {
            String className = (String) it.next();
            m_initializationLogger.debug("Loading class: " + className);

            try {
                m_parentClassLoader.loadClass(className);
            } catch (Exception e) {
                m_initializationLogger.warn("Could not load class: " + className, e);
            }
        }

        m_alreadyLoaded = true;
    }

    public void dispose() {
        m_initializationLogger.debug("Shutting down Cocoon");
        ContainerUtil.dispose( m_contManager );
        m_contManager = null;
    }

    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }
    
}
