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

import junit.framework.TestCase;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;

import org.apache.cocoon.Constants;

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
    }

    private List getForcedClassLoadList()
    {
        List list = new ArrayList(1);
        list.add(CocoonBean.class.getName());
        return list;
    }
}