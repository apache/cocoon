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

import java.io.File;
import java.io.IOException;
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
import org.xml.sax.SAXException;

/**
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.3 $ $Date: 2004/05/01 16:12:05 $
 */
public final class DocumentCache {

    /** Cache the read configuration files (Documents) */
    protected static Map fileCache = new HashMap();
    
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
            task.log("Using file from cache: " + fileName, Project.MSG_DEBUG);
            fileCache.remove(fileName);
        } else {
            try {
                // load xml
                task.log("Reading: " + fileName, Project.MSG_DEBUG);
                document = builder.parse(fileName);
            } catch (IOException e) {
                throw new BuildException("IOException: "+e);
            }                
        }
        return document;
    }
    
    public static void storeDocument(File file, Document document, Task task) 
    throws IOException {  
        task.log("Storing file in cache: " + file, Project.MSG_DEBUG);
        final String fileName = file.toURL().toExternalForm();
        fileCache.put(fileName, document);
    }

    public static void writeDocument(File file, Document document, Task task) {
        task.log("Writing: " + file);
        // Set the DOCTYPE output option on the transformer 
        // if we have any DOCTYPE declaration in the input xml document
        final DocumentType doctype = document.getDoctype();
        Properties props = new Properties();
        if (null != doctype && null != doctype.getPublicId()) {
            props.put(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
            props.put(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
        }
        transformer.setOutputProperties(props);
        
        try {
            transformer.transform(new DOMSource(document),
                                  new StreamResult(file));
        } catch (TransformerException e) {
            throw new BuildException("TransformerException: "+e);
        }
    }
}
