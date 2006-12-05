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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class RwmProperties {

    private static final String COB_INF_DIR = "/COB-INF";
    private static final String BLOCK_CONTEXT_URL_PARAM = "/blockContextURL";
    private static final String ARTIFACT = "%artifact";
    private static final String CLASSES_DIR = "%classes-dir";    
    
    private Properties rclProps = new Properties();

    public RwmProperties(InputStream propsIs) throws IOException {
        this.rclProps.load(propsIs);
    }
    
    public RwmProperties(File propsFile) throws IOException {
        this(new FileInputStream(propsFile));
    }

    public Set getArtifacts() {
        return getFilteredPropertiesValuesAsSet(ARTIFACT);
    }
    
    public Set getClassesDirs() {
        return getFilteredPropertiesValuesAsSet(CLASSES_DIR);
    }
    
    public Properties getSpringProperties() {
        Properties springProps = new Properties();
        for(Enumeration rclEnum = rclProps.keys(); rclEnum.hasMoreElements();) {
            String key = (String) rclEnum.nextElement();
            if(!key.endsWith(ARTIFACT) && !key.endsWith(CLASSES_DIR)) {
                springProps.put(key, this.rclProps.getProperty(key));
            }
            if(key.endsWith(CLASSES_DIR)) {
                String newKey = key.substring(0, key.length() - CLASSES_DIR.length()) + BLOCK_CONTEXT_URL_PARAM;
                springProps.put(newKey, this.rclProps.getProperty(key) + COB_INF_DIR);
            }
        } 
        return springProps;
    }    
    
    private Set getFilteredPropertiesValuesAsSet(String filter) {
        Set returnSet = new HashSet();
        for(Enumeration rclEnum = rclProps.keys(); rclEnum.hasMoreElements();) {
            String key = (String) rclEnum.nextElement();
            if(key.endsWith(filter)) {
                returnSet.add(this.rclProps.getProperty(key));
            }
        }        
        return returnSet;
    }
    
}

    
