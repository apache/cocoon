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

/**
 * Generates index of available packages 
 * 
 * @version CVS $Revision: 1.1 $ $Date: 2004/05/25 12:53:43 $
 */
public class PackageOverviewXMLWriter extends AbstractXMLWriter{

    public PackageOverviewXMLWriter(Resolver resolver) {
        super(resolver);
    }

    public void writePackageOverview()
    throws SAXException, IOException, TransformerConfigurationException {

        File file = this.resolver.getOutputFileForPackageOverview();
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        TransformerHandler handler = this.getHandler(output);

        // Output SAX 'header':
        handler.startDocument();
        handler.startPrefixMapping(NS_PREFIX, NS_URI);
        outputPackagesStartElement(handler);

        Iterator iter = this.resolver.getJavaPackages().keySet().iterator();
        while(iter.hasNext()) {
            String packageName = (String)iter.next();
            String packagePath = packageName.replace('.', '/');
            saxStartElement(handler, PACKAGE_ELEMENT, new String[][] {{PACKAGENAME_ATTRIBUTE, packageName},
                                                                      {PACKAGEPATH_ATTRIBUTE, packagePath}});
            saxEndElement(handler, PACKAGE_ELEMENT);
        }
        
        // Close package-level element:
        outputPackagesEndElement(handler);
        
        // Output SAX 'footer':
        handler.endPrefixMapping(NS_PREFIX);
        handler.endDocument();

        output.close();
    }
}
