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
package doclets;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;

import com.thoughtworks.qdox.model.JavaClass;

/**
 * Generates index of available classes 
 * 
 * @version CVS $Revision: 1.1 $ $Date: 2004/05/25 12:53:43 $
 */
public class ClassOverviewXMLWriter extends AbstractXMLWriter {
    
    public ClassOverviewXMLWriter(Resolver resolver) {
        super(resolver);
    }

    public void writeClassOverview()
    throws SAXException, IOException, TransformerConfigurationException {

        File file = this.resolver.getOutputFileForClassOverview();
        
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        TransformerHandler handler = this.getHandler(output);

        // Output SAX 'header':
        handler.startDocument();
        handler.startPrefixMapping(NS_PREFIX, NS_URI);
        outputClassesStartElement(handler);

        Iterator iter = this.resolver.getJavaClasses().keySet().iterator();
        while(iter.hasNext()) {
            String qualifiedName = (String)iter.next();
            JavaClass clazz = (JavaClass)this.resolver.getJavaClasses().get(qualifiedName);
            String className = clazz.getName();
            String packageName = clazz.getPackage();
            String packagePath = packageName.replace('.', '/');
            if (clazz.isInterface()) {
                saxStartElement(handler, INTERFACE_ELEMENT, new String[][] {{CLASSNAME_ATTRIBUTE, className},
                                                                            {PACKAGEPATH_ATTRIBUTE, packagePath},
                                                                            {PACKAGENAME_ATTRIBUTE, packageName}});
                saxEndElement(handler, INTERFACE_ELEMENT);
            } else {
                saxStartElement(handler, CLASS_ELEMENT, new String[][] {{CLASSNAME_ATTRIBUTE, className},
                                                                        {PACKAGEPATH_ATTRIBUTE, packagePath},
                                                                        {PACKAGENAME_ATTRIBUTE, packageName}});
                saxEndElement(handler, CLASS_ELEMENT);
            }
        }
        
        // Close package-level element:
        outputClassesEndElement(handler);
        
        // Output SAX 'footer':
        handler.endPrefixMapping(NS_PREFIX);
        handler.endDocument();

        output.close();
    }
}
