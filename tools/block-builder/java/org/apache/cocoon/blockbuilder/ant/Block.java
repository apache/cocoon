/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.cocoon.blockbuilder.ant;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @since 0.1
 */
public class Block {
    
    private String name;
    private String relativeJardir;
    private String blockPath;
    
    public Block() {
        
    }
    
    public void setName(String name) {
    	this.name = name;
    }

    public String getName() {
    	return this.name;
    }
    
    public void setPath(String path) {
    	this.blockPath = path;
    }
    
    public void setJardir(String dir) {
    	this.relativeJardir = dir;
    }
    
    public File[] getJarFile(File basedir) {
    	File jarDir = new File(basedir, blockPath + File.separator + this.relativeJardir);
        return jarDir.listFiles(new FilenameFilter() {
            public boolean accept(File f, String name) {
                return name.toLowerCase().endsWith("jar");
            }
        });
    }
}
