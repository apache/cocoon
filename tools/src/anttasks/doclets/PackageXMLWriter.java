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
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;

import com.thoughtworks.qdox.model.JavaClass;

/**
 * Generates class index for a given package 
 * 
 * @version CVS $Revision: 1.1 $ $Date: 2004/05/25 12:53:43 $
 */
public class PackageXMLWriter extends AbstractXMLWriter{

    public PackageXMLWriter(Resolver resolver) {
        super(resolver);
    }

    public void writePackage(String javaPackage)
    throws SAXException, IOException, TransformerConfigurationException {

        File file = this.resolver.getOutputFileForPackage(javaPackage);
        Set classes = (Set)this.resolver.getJavaPackages().get(javaPackage);
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        TransformerHandler handler = this.getHandler(output);

        // Output SAX 'header':
        handler.startDocument();
        handler.startPrefixMapping(NS_PREFIX, NS_URI);
        outputPackageStartElement(handler, javaPackage);

        Set javaInterfaces = new TreeSet();
        Set javaClasses = new TreeSet();
        Iterator iter = classes.iterator();
        while(iter.hasNext()) {
            String className = (String)iter.next();
            JavaClass clazz = (JavaClass)this.resolver.getJavaClasses().get(javaPackage + "." + className);
            if (clazz.isInterface()) javaInterfaces.add(className);
            else javaClasses.add(className);
        }

        Iterator interfaceIter = javaInterfaces.iterator();
        saxStartElement(handler, INTERFACES_ELEMENT, new String[][] {{}});
        while (interfaceIter.hasNext()) {
            String interfaceName = (String)interfaceIter.next();
            saxStartElement(handler, INTERFACE_ELEMENT, new String[][] {{CLASSNAME_ATTRIBUTE, interfaceName}});
            saxEndElement(handler, INTERFACE_ELEMENT);
        }
        saxEndElement(handler, INTERFACES_ELEMENT);

        Iterator classesIter = javaClasses.iterator();
        saxStartElement(handler, CLASSES_ELEMENT, new String[][] {{}});
        while (classesIter.hasNext()) {
            String classesName = (String)classesIter.next();
            saxStartElement(handler, CLASS_ELEMENT, new String[][] {{CLASSNAME_ATTRIBUTE, classesName}});
            saxEndElement(handler, CLASS_ELEMENT);
        }
        saxEndElement(handler, CLASSES_ELEMENT);
        
        // Close package-level element:
        outputPackageEndElement(handler);
        
        // Output SAX 'footer':
        handler.endPrefixMapping(NS_PREFIX);
        handler.endDocument();

        output.close();
    }
}
