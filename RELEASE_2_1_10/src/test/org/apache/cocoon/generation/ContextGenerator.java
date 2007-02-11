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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Outputs an XML representation of the set of attributes contained in the
 * Cocoon environment's context for testing purposes.
 *
 * @version $Id$
 */
public class ContextGenerator implements Generator {
	XMLConsumer consumer;
	Map objectModel;

	public void setConsumer(XMLConsumer consumer) {
		this.consumer = consumer;
	}

	public void generate() throws IOException, SAXException, ProcessingException {
		consumer.startDocument();
		consumer.startElement("", "context", "context", new AttributesImpl());
		Context context = ObjectModelHelper.getContext(objectModel);
		Enumeration keys = context.getAttributeNames();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			Object value = context.getAttribute(key);
			AttributesImpl attrs = new AttributesImpl();
			attrs.addAttribute("", "name", "name", "CDATA", key);
			consumer.startElement("", "key", "key", attrs);
			String str = value.toString();
			consumer.characters(str.toCharArray(), 0, str.length());
			consumer.endElement("", "key", "key");
		}
		consumer.endElement("", "context", "context");
		consumer.endDocument();
	}

	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
		this.objectModel = objectModel;
	}
}
