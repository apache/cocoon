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

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * Generates javadoc for a given class 
 * 
 * @version CVS $Revision: 1.1 $ $Date: 2004/05/25 12:53:43 $
 */
public class ClassXMLWriter extends AbstractXMLWriter{
    
    public ClassXMLWriter(Resolver resolver) {
        super(resolver);
    }
    
    public void writeClass(JavaClass clazz)
    throws SAXException, IOException, TransformerConfigurationException {

        this.javadocClass = clazz;
        File file = this.resolver.getOutputFileForClass(clazz.getFullyQualifiedName());
        file.getParentFile().mkdirs();
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        TransformerHandler handler = this.getHandler(output);
        
        // Output SAX 'header':
        handler.startDocument();
        handler.startPrefixMapping(NS_PREFIX, NS_URI);

        outputClassStartElement(handler, clazz);

        // Modifiers:
        outputModifiers(handler, clazz);

        // Imports:
        JavaSource parent = clazz.getParentSource();
        // Add two implicit imports:
        parent.addImport("java.lang.*");
        if (parent.getPackage().length() > 0) {
            parent.addImport(parent.getPackage() + ".*");
        } else {
            parent.addImport("*");
        }
        String[] imports = parent.getImports();

        saxStartElement(handler, IMPORTS_ELEMENT);
        for (int i = 0; i < imports.length; i++) {
            if (imports[i].endsWith("*")) {
                // package import:
                saxStartElement(handler, IMPORT_ELEMENT, new String[][] {{IMPORT_ATTRIBUTE, "package"}});
                String imp = imports[i];
                while (imp.endsWith("*") || imp.endsWith(".")) {
                    imp = imp.substring(0, imp.length() - 1);
                }
                saxCharacters(handler, imp);
            } else {
                saxStartElement(handler, IMPORT_ELEMENT, new String[][] {{IMPORT_ATTRIBUTE, "class"}});
                saxCharacters(handler, imports[i]);
            }
            saxEndElement(handler, IMPORT_ELEMENT);
        }
        saxEndElement(handler, IMPORTS_ELEMENT);

        // Superclass:
        if (!clazz.isInterface()) {
            outputSuperClassInheritance(handler, clazz, CLASS_INHERITANCE);
        }

        // Implements:
        outputImplements(handler, clazz, true);

        // Comment:
        outputComment(handler, clazz.getComment());

        // Tags:
        outputTags(handler, clazz);

        // Inner classes:
        outputInnerClasses(handler, clazz, true);

        // Fields:
        outputFields(handler, clazz, true);

        // Constructors:
        outputMethods(handler, clazz, CONSTRUCTOR_MODE);

        // Methods:
        outputMethods(handler, clazz, METHOD_MODE);

        // Close class-level element:
        outputClassEndElement(handler, clazz);

        // Output SAX 'footer':
        handler.endPrefixMapping(NS_PREFIX);
        handler.endDocument();

        output.close();
    }

}
