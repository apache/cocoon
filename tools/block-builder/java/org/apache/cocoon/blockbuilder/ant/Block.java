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
import java.io.FileInputStream;
import java.io.FilenameFilter;

import org.apache.tools.ant.BuildException;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @since 0.1
 */
public class Block {
    
    private String name;
    private String relativeJardir;
    private String blockPath;
    private boolean dynamicEclipseReference = false;
    
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
    
    public void setDynamicEclipseReference(boolean dynamicEclipseReference) {
        this.dynamicEclipseReference = dynamicEclipseReference;
    }
    
    public boolean isDynamicEclipseReference() {
        return this.dynamicEclipseReference;
    }
    
    public String getEclipseProjectName() throws Exception {
        return getProjectName(new File(this.blockPath, ".project"));
    }
    
    public File[] getJarFile(File basedir) {
    	File jarDir = new File(basedir, blockPath + File.separator + this.relativeJardir);
        return jarDir.listFiles(new FilenameFilter() {
            public boolean accept(File f, String name) {
                return name.toLowerCase().endsWith("jar");
            }
        });
    }
    
    private String getProjectName(File eclipseProjectFile) throws Exception {
        String projectName = "";
        try {
            String EL_PROJECTDESCRIPTION = "projectDescription";
            String EL_NAME = "name";
            
            DOMParser parser = new DOMParser();
            parser.parse(new InputSource(new FileInputStream(eclipseProjectFile)));
            Document doc = parser.getDocument();
            
            // read in all available libraries
            NodeList rootNodeList = doc.getChildNodes();
            for(int i = 0; i <= rootNodeList.getLength(); i++ ) {
                Node rootChildNode = rootNodeList.item(i);
                if(rootChildNode != null && EL_PROJECTDESCRIPTION.equals(rootChildNode.getLocalName())) {
                    NodeList projectDescriptor = rootChildNode.getChildNodes();
                    for(int x = 0; x <= projectDescriptor.getLength(); x++) {
                        Node nameNode = projectDescriptor.item(x);
                        if(nameNode != null && EL_NAME.equals(nameNode.getLocalName())) {
                            projectName = nameNode.getFirstChild().getNodeValue();
                        }
                    }
                }
            }       
        } catch(Exception e) {
            throw new BuildException("Make sure that a valid Eclipse project file can be found at " 
                    + eclipseProjectFile.getCanonicalPath());
        }
        return projectName;
    }
}
