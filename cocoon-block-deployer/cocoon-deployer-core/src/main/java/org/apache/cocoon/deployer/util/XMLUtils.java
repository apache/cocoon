/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.deployer.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils {

	/**
	 * Get the namespace of the root element
	 * 
	 * @param blockDescriptor - XML as InputStream
	 * @return the namespace URI
	 * @throws SAXException
	 * @throws IOException
	 */
	public static String getDocumentNamespace(InputStream blockDescriptor) throws SAXException, IOException {
	    DOMParser parser = new DOMParser();
	    parser.parse(new InputSource(blockDescriptor));
	    return parser.getDocument().getDocumentElement().getNamespaceURI();		
	}

	
}
