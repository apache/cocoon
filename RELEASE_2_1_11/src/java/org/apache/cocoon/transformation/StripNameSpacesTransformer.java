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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @cocoon.sitemap.component.documentation
 * The <code>StripNameSpacesTransformer</code> is a class that can be plugged into a pipeline
 * to strip all namespaces from a SAX stream. It is much faster (certainly for larger 
 * streams, but easily factor 100) then the conventional stripnamespaces.xsl
 * 
 * @cocoon.sitemap.component.name  stripnamespaces
 * @cocoon.sitemap.component.logger sitemap.transformer.stripnamespaces
 * 
 * @cocoon.sitemap.component.pooling.max  32
 * 
 * @version $Id$
 * @author ard schrijvers
 *  
 */
public class StripNameSpacesTransformer extends AbstractTransformer implements
		CacheableProcessingComponent {
	
	private static final String EMPTY_NS = "";
	
	public void setup(SourceResolver resolver, Map objectModel, String src,
			Parameters params) throws ProcessingException, SAXException,
			IOException {
           // nothing needed
	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// no prefix
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		// no prefix
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attr) throws SAXException {
		
		super.startElement(EMPTY_NS, localName, localName, attr);

	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(EMPTY_NS, localName, localName);
	}

	public Serializable getKey() {
		return "1";
	}

	public SourceValidity getValidity() {
		return NOPValidity.SHARED_INSTANCE;
	}
}