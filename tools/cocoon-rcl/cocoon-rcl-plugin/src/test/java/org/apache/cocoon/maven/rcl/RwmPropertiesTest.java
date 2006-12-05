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

import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

public class RwmPropertiesTest extends TestCase {

    public void testLoadingSpringProps() throws Exception {
        RwmProperties p = createTestProperties();
        Properties springProps = p.getSpringProperties();
        assertEquals(5, springProps.size());
        assertTrue(springProps.containsKey("org.apache.cocoon.cocoon-rcl-plugin-demo.block/blockContextURL"));
        assertEquals("file:/F:/blocks/myBlock1/target/classes/COB-INF", 
            springProps.getProperty("org.apache.cocoon.cocoon-rcl-plugin-demo.block1/blockContextURL"));
    }
    
    public void testLoadingArtifactValues() throws Exception {
        RwmProperties p = createTestProperties();
        Set as = p.getArtifacts();
        assertEquals(2, as.size());
        assertTrue(as.contains("org.apache.cocoon:cocoon-myBlock"));
        assertTrue(as.contains("org.apache.cocoon:cocoon-myBlock1"));    
    }   

    public void testLoadingBasedirs() throws Exception {
        RwmProperties p = createTestProperties();
        Set as = p.getClassesDirs();
        assertEquals(3, as.size());
        assertTrue(as.contains("file:/F:/blocks/myBlock/target/classes"));
        assertTrue(as.contains("file:/F:/blocks/myBlock1/target/classes"));        
    }      
    
    protected RwmProperties createTestProperties() throws Exception {    
        return new RwmProperties(readResourceFromClassloader("rcl.properties"));
    }
    
    protected InputStream readResourceFromClassloader(String fileName) {
        String resource = RwmPropertiesTest.class.getPackage().getName().replace('.', '/') + "/" + fileName;
        return ReloadingWebappMojo.class.getClassLoader().getResourceAsStream(resource);
    }    
    
}
