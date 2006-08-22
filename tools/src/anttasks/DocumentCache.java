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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id$
 */
public final class DocumentCache {

    /** Cache the read configuration files (Documents) */
    protected final static Map fileCache = new HashMap();
    
    /** The document builder */
    private static DocumentBuilder builder;
    private static Transformer transformer;

    /**
     * Initialize internal instance of XMLCatalog
     */
    static {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            builderFactory.setExpandEntityReferences(false);
            builderFactory.setNamespaceAware(false);
            builderFactory.setAttribute(
               "http://apache.org/xml/features/nonvalidating/load-external-dtd",
               Boolean.FALSE);
            builder = builderFactory.newDocumentBuilder();
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerException e) {
            throw new BuildException("TransformerException: "+e);
        } catch (ParserConfigurationException e) {
            throw new BuildException("ParserConfigurationException: "+e);
        }  
    }

    public static Document getDocument(File file, Task task) 
    throws SAXException, IOException {
        final String fileName = file.toURL().toExternalForm();
        Document document = (Document)fileCache.get(fileName);
        if ( document != null ) {
            if ( task != null ) {
                task.log("Using file from cache: " + fileName, Project.MSG_DEBUG);
            }
            fileCache.remove(fileName);
        } else {
            try {
                // load xml
                if ( task != null ) {
                    task.log("Reading: " + fileName, Project.MSG_DEBUG);
                }
                document = builder.parse(fileName);
            } catch (IOException e) {
                throw new BuildException("IOException: "+e);
            }                
        }
        return document;
    }
    
    public static Document getDocument(String string, String systemURI) {
        try {
            final InputSource is = new InputSource(new StringReader(string));
            if ( systemURI != null ) {
                is.setSystemId(systemURI);
            }
            return builder.parse(is);
        } catch (Exception e) {
            throw new BuildException("Unable to parse string.", e);
        }
    }
    
    public static void storeDocument(File file, Document document, Task task) 
    throws IOException {
        task.log("Storing file in cache: " + file, Project.MSG_DEBUG);
        final String fileName = file.toURL().toExternalForm();
        fileCache.put(fileName, document);
    }

    public static void writeDocument(File file, Document document, Task task) {
        if ( task != null ) {
            task.log("Writing: " + file);
        }
        // Set the DOCTYPE output option on the transformer 
        // if we have any DOCTYPE declaration in the input xml document
        final DocumentType doctype = document.getDoctype();
        Properties props = new Properties();
        if (null != doctype) {
            if (null != doctype.getPublicId()) {
                props.put(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
            }
            if (null != doctype.getSystemId()) {
                props.put(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
            }
        }
        transformer.setOutputProperties(props);
        
        try {
            StreamResult s = new StreamResult(file);
            // for JDK 5.0 we explicitly have to set the output stream
            // otherwise we get FileNotFoundExceptions (at least on
            // windows)
            s.setOutputStream(new FileOutputStream(file));
            transformer.transform(new DOMSource(document),
                                  s);
        } catch (FileNotFoundException e) {
            throw new BuildException("FileNotFoundException: "+e);
        } catch (TransformerException e) {
            throw new BuildException("TransformerException: "+e);
        }
    }
}
