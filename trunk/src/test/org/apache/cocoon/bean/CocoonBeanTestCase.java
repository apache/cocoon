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

import junit.framework.TestCase;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;

import org.apache.cocoon.Constants;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.NullLogger;

/**
 * CocoonBeanTestCase does XYZ
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
public class CocoonBeanTestCase
        extends TestCase
{
    public CocoonBeanTestCase( String name )
    {
        super( name );
    }

    public void testGetterSetter()
    {
        CocoonBean bean = new CocoonBean();
        List forcedLoadList = getForcedClassLoadList();
        String classPath = "claspath";
        String configURI = "config";
        String contextURI = "context";
        String instConfig = "instruments";
        String logConfig = "logger";
        String category = "category";
        ClassLoader loader = new URLClassLoader(new URL[0]);
        Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_DEBUG);
        int threadsPC = 2;
        long timeout = 1000l;
        File workDir = new File("work-dir");

        bean.setClassForceLoadList( forcedLoadList );
        bean.setClassPath(classPath);
        bean.setConfigURI(configURI);
        bean.setContextURI(contextURI);
        bean.setInstrumentConfigURI(instConfig);
        bean.setLogCategory(category);
        bean.setLogConfigURI(logConfig);
        bean.setThreadsPerCPU(threadsPC);
        bean.setThreadTimeOut(timeout);
        bean.setWorkDirectory(workDir);
        bean.setParentClassLoader(loader);
        bean.setInitializationLogger(logger);

        assertEquals( forcedLoadList, bean.getClassForceLoadList() );
        assertEquals( classPath, bean.getClassPath());
        assertEquals( configURI, bean.getConfigURI());
        assertEquals(contextURI, bean.getContextURI());
        assertEquals(instConfig, bean.getInstrumentConfigURI());
        assertEquals(category, bean.getLogCategory());
        assertEquals( logConfig, bean.getLogConfigURI() );
        assertEquals(loader, bean.getParentClassLoader());
        assertEquals(threadsPC, bean.getThreadsPerCPU());
        assertEquals(timeout, bean.getThreadTimeOut());
        assertEquals(workDir, bean.getWorkDirectory());
        assertEquals(logger, bean.getInitializationLogger());
    }

    public void testInitialValues()
    {
        CocoonBean bean = new CocoonBean();
        List forcedLoadList = new LinkedList(); // testing for contents, not type equality
        String classPath = System.getProperty("java.class.path");
        String configURI = Constants.DEFAULT_CONF_FILE;
        String contextURI = Constants.DEFAULT_CONTEXT_DIR;
        String logConfig = contextURI + File.separator + "cocoon.xlog";
        String instConfig = contextURI + File.separator + "cocoon.instruments";
        String category = "cocoon";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        int threadsPC = 1;
        long timeout = 60l * 1000l;
        File workDir = new File( System.getProperty("java.io.tmpdir") );

        assertEquals( forcedLoadList, bean.getClassForceLoadList() );
        assertEquals( classPath, bean.getClassPath() );
        assertEquals( configURI, bean.getConfigURI() );
        assertEquals( contextURI, bean.getContextURI() );
        assertEquals( instConfig, bean.getInstrumentConfigURI() );
        assertEquals( logConfig, bean.getLogConfigURI() );
        assertEquals( category, bean.getLogCategory() );
        assertEquals( loader, bean.getParentClassLoader() );
        assertEquals( threadsPC, bean.getThreadsPerCPU() );
        assertEquals( timeout, bean.getThreadTimeOut() );
        assertEquals( workDir, bean.getWorkDirectory() );
        assertTrue( bean.getInitializationLogger() instanceof NullLogger );
    }

    private List getForcedClassLoadList()
    {
        List list = new ArrayList(1);
        list.add(CocoonBean.class.getName());
        return list;
    }
}