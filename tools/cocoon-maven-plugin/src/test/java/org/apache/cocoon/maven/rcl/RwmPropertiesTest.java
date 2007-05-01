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
package org.apache.cocoon.maven.rcl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

public class RwmPropertiesTest extends TestCase {
    
    public void testLoadingSpringProps() throws Exception {
        RwmProperties p = createTestProperties();
        Properties springProps = p.getSpringProperties();
        assertEquals(9, springProps.size());
        // test setting the correct context URL if a *%classes-dir property was set
        assertTrue(springProps.containsKey("org.apache.cocoon.cocoon-rcl-plugin-demo.block/contextPath"));
        if(File.separatorChar == '\\') {
            assertEquals("file:/F:/blocks/myBlock1/src/main/resources/COB-INF", 
                    springProps.getProperty("org.apache.cocoon.cocoon-rcl-plugin-demo.block1/contextPath"));
            assertEquals("file:/F:/blocks/myBlock2/some-other-dir/src/main/resources/COB-INF", 
                    springProps.getProperty("org.apache.cocoon.cocoon-rcl-plugin-demo.block2/contextPath"));     
        } else {
            assertEquals("file:/home/test/myBlock4/src/main/resources/COB-INF", 
                    springProps.getProperty("org.apache.cocoon.cocoon-rcl-plugin-demo.block4/contextPath"));
            assertEquals("file:/home/test/myBlock5/some-other-dir/src/main/resources/COB-INF", 
                    springProps.getProperty("org.apache.cocoon.cocoon-rcl-plugin-demo.block5/contextPath"));            
        }
        assertTrue(springProps.getProperty("org.apache.cocoon.cocoon-rcl-plugin-demo.block3/contextPath").endsWith("src/main/resources/COB-INF"));
        // other property overrider configurer properties
        assertEquals("5", springProps.getProperty("org.apache.cocoon.someBean/someProperty"));
    }
    
    public void testLoadingCocoonProps() throws Exception {
        RwmProperties p = createTestProperties();
        Properties cocooonProps = p.getCocoonProperties();
        assertEquals(2, cocooonProps.size());        
        // test variable interpolation
        assertEquals("interpolatedValue:A", cocooonProps.getProperty("b"));        
    }

    public void testLoadExcludedLibs() throws Exception {
        RwmProperties p = createTestProperties();
        Set as = p.getExcludedLibProps();
        assertEquals(2, as.size());  
        assertTrue(as.contains("com.mygroup:myartifact"));
    }
    
    public void testLoadingBasedirs() throws Exception {
        RwmProperties p = createTestProperties();
        Set as = p.getClassesDirs();
        assertEquals(9, as.size());
        if(File.separatorChar == '\\') {
            assertTrue(as.contains("file:/F:/blocks/myBlock/target/classes"));
            assertTrue(as.contains("file:/F:/blocks/myBlock1/target/classes"));
            assertTrue(as.contains("file:/C:/blah"));            
        } else {
            assertTrue(as.contains("file:/nix/foo"));
        }
    }      
    
    protected RwmProperties createTestProperties() throws Exception {    
        return new RwmProperties(getResourcesFromClassLoaderAsFile("rcl.properties"), new File(""));
    }
    
    protected File getResourcesFromClassLoaderAsFile(String fileName) throws IOException {
        File tempFile = File.createTempFile(
                fileName, ".conf");
        FileOutputStream tempFileFos = new FileOutputStream(tempFile);
        IOUtils.copy(readResourceFromClassloader(fileName), tempFileFos);
        tempFileFos.close();
        tempFile.deleteOnExit();
        return tempFile;
    }
    
    protected InputStream readResourceFromClassloader(String fileName) {
        String resource = RwmPropertiesTest.class.getPackage().getName().replace('.', '/') + "/" + fileName;
        return ReloadingWebappMojo.class.getClassLoader().getResourceAsStream(resource);
    }    
    
}
