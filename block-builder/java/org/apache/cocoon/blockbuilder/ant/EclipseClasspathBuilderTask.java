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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @since 0.1
 */
public class EclipseClasspathBuilderTask extends Task {
    
    private static final String EL_JARS = "jars";
    private static final String EL_FILE = "file";
    private static final String ATTR_ID = "id";    
    private static final String EL_LIB  = "lib";
    private static final String EL_CLASSPATH = "classpath";
    private static final String EL_CLASSPATHENTRY = "classpathentry";
    private static final String ATTR_PATH = "path";
    private static final String ATTR_KIND = "kind";
    private static final String ATTR_OUT = "output";    
    private static final String LIT_ATTR_KIND_SRC = "src";
    private static final String LIT_ATTR_KIND_CON = "con";
    private static final String LIT_ATTR_KIND_LIB = "lib";
    // private static final String LIT_ATTR_KIND_OUT = "output";        
    private static final String CORE_LIB = "core";
    private static final String DEFAULT_ECLIPSE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER";
    
    private List blocks = new ArrayList();
    private Map coreJarMap = new HashMap();
	private List libs = new ArrayList();
	private List sources = new ArrayList();
    private List cocoonJars = new ArrayList();
    private File corejarsFile;
    private File outFile;
    private File coreJarDir;
    private String eclipseContainer = DEFAULT_ECLIPSE_CONTAINER;


	public void execute() throws BuildException {
        try {
            
            // create a new document
            Document doc= new DocumentImpl();
            Element root = doc.createElement(EL_CLASSPATH);
            
            // append all source directories
            Iterator sourceIterator = this.sources.iterator();
            while(sourceIterator.hasNext()) {
                Source source = ((Source) sourceIterator.next());
                Element entry = doc.createElement(EL_CLASSPATHENTRY);
                entry.setAttribute(ATTR_KIND, LIT_ATTR_KIND_SRC);
                entry.setAttribute(ATTR_PATH, source.getDir());
                entry.setAttribute(ATTR_OUT, source.getOut());                
                root.appendChild(entry);            	
            }
            
            // append cocoon libraries
            Iterator cocoonIterator = this.cocoonJars.iterator();
            while(cocoonIterator.hasNext()) {
                Cocoon cocoonEntry = (Cocoon) cocoonIterator.next();
            	File jar = new File(this.getProject().getBaseDir(), cocoonEntry.getJar());
                Element entry = doc.createElement(EL_CLASSPATHENTRY);
                entry.setAttribute(ATTR_KIND, LIT_ATTR_KIND_LIB);
                entry.setAttribute(ATTR_PATH, jar.getCanonicalPath());
                root.appendChild(entry);                
            }
               
            // append all public block jars
    		Iterator blockIterator = this.blocks.iterator();
            while(blockIterator.hasNext()) {
                Block block = (Block)blockIterator.next();
                if(!block.isDynamicEclipseReference()) {
                	File[] f = block.getJarFile(this.getProject().getBaseDir());
                    if(f!=null) for(int i = 0; i < f.length; i++) {
                        Element entry = doc.createElement(EL_CLASSPATHENTRY);
                        entry.setAttribute(ATTR_KIND, LIT_ATTR_KIND_LIB);
                        entry.setAttribute(ATTR_PATH, f[i].getCanonicalPath());
                        root.appendChild(entry);
                    }
                } else {
                    Element entry = doc.createElement(EL_CLASSPATHENTRY);
                    entry.setAttribute(ATTR_KIND, LIT_ATTR_KIND_SRC);
                    entry.setAttribute(ATTR_PATH, "/" + block.getEclipseProjectName());
                    root.appendChild(entry);                    
                }
            }
            
            // append all libraries
            Iterator libIterator = this.libs.iterator();
            while(libIterator.hasNext()) {
                Library lib = (Library) libIterator.next();
                if(CORE_LIB.equals(lib.getLocation())) {
                    String libFromRepository = (String) this.coreJarMap.get(lib.getId());
                    if(libFromRepository == null) {
                        throw new BuildException("Library '" + lib.getId() + "' can't be found! " + 
                                "Make sure it is available in " + corejarsFile.getCanonicalPath() + ".");
                    }
                    String jar = (new File(this.coreJarDir, libFromRepository)).getCanonicalPath();
                    Element entry = doc.createElement(EL_CLASSPATHENTRY);
                    entry.setAttribute(ATTR_KIND, LIT_ATTR_KIND_LIB);
                    entry.setAttribute(ATTR_PATH, jar);
                    root.appendChild(entry);              
                }
            }
            
            // append default output dir
            /* RP: is it really necessary?
            Element outputEntry = doc.createElement(EL_CLASSPATHENTRY);
            outputEntry.setAttribute(ATTR_KIND, LIT_ATTR_KIND_OUT);
            outputEntry.setAttribute(ATTR_PATH, "");
            root.appendChild(outputEntry);               
            */
            
            // append container
            Element containerEntry = doc.createElement(EL_CLASSPATHENTRY);
            containerEntry.setAttribute(ATTR_KIND, LIT_ATTR_KIND_CON);
            containerEntry.setAttribute(ATTR_PATH, this.eclipseContainer);
            root.appendChild(containerEntry);    
                        
            // append root element to document
            doc.appendChild(root);
            
            // serialize document
            FileOutputStream fos = new FileOutputStream(outFile);
            OutputFormat of = new OutputFormat("XML","UTF-8",true);
            of.setIndent(1);
            of.setIndenting(true);
            XMLSerializer serializer = new XMLSerializer(fos,of);

            serializer.asDOMSerializer();
            serializer.serialize( doc.getDocumentElement() );
            fos.close();    
            this.log("Wrote Eclipse .classpath file sucessfully.");
            
        } catch(BuildException ex) {
        	throw ex;
         }catch(Exception ex) {
            throw new BuildException("error while serializing dom tree: " + ex.getMessage());
        } 
        
        
    }
    
    
    /**
     * Parse the jars.xml file and use addCoreJar() method to create an ArrayList with
     * all available jars.
     * 
     * @param file
     * @throws Exception
     */
    private void parseJarXML(File file) throws Exception {
    	DOMParser parser = new DOMParser();
        parser.parse(new InputSource(new FileInputStream(file)));
        Document doc = parser.getDocument();
        
        // read in all available libraries
        NodeList rootNodeList = doc.getChildNodes();
        for(int i = 0; i <= rootNodeList.getLength(); i++ ) {
            Node rootChildNode = rootNodeList.item(i);
            if(rootChildNode != null && EL_JARS.equals(rootChildNode.getLocalName())) {
            	NodeList jarsNodeList = rootChildNode.getChildNodes();
                for(int x = 0; x <= jarsNodeList.getLength(); x++) {
                    Node fileNode = jarsNodeList.item(x);
                    if(fileNode != null && EL_FILE.equals(fileNode.getLocalName())) {
                        addCoreJar(fileNode, this.coreJarMap);
                    }
                }
            }
        }       
        
    }
    
	private void addCoreJar(Node fileNode, Map jarMap) {
        String id = fileNode.getAttributes().getNamedItem(ATTR_ID).getNodeValue();
        NodeList fileNodeList = fileNode.getChildNodes();
        for(int x = 0; x <= fileNodeList.getLength(); x++) {
            Node childNode = fileNodeList.item(x);
            if(childNode != null && EL_LIB.equals(childNode.getLocalName())) {
                String libValue = childNode.getFirstChild().getNodeValue();
                jarMap.put(id, libValue);             
            }
        }        
	}

    public void setCorejardir(File dir) {
    	this.coreJarDir = dir;
    }

	public void setCorejars(File corejars) throws Exception {
    	this.corejarsFile = corejars;
        if(!this.corejarsFile.exists()) {
            throw new BuildException(this.corejarsFile.getCanonicalPath() + " can't be found.");
        }
        parseJarXML(this.corejarsFile);
    }
    
    public void setContainer(String container) {
    	this.eclipseContainer = container;
    }
    
    public void setOutfile(File outfile) {
    	this.outFile = outfile;
    }
    
    public void addLib(Library lib) throws BuildException {
        this.libs.add(lib);
    }
    
    public void addBlock(Block block) {
    	blocks.add(block);
    }
    
    public void addSource(Source source) {
        sources.add(source);
    }
    
    public void addCocoon(Cocoon cocoon) {
    	this.cocoonJars.add(cocoon);
    }
    
}
