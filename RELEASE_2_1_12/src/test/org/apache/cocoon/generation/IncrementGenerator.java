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
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Increment context attribute "count" for testing purposes.
 *
 * This generator always returns a VALID validity, and the cache key is given as
 * a sitemap parameter.
 *
 * @version $Id$
 */
public class IncrementGenerator implements Contextualizable, Generator, CacheableProcessingComponent {

    XMLConsumer consumer;
	Map objectModel;
	String key;
	Context context;

	public void generate() throws IOException, SAXException, ProcessingException {
		increment(objectModel, "count");

		consumer.startDocument();
		consumer.startElement("", "node", "node", new AttributesImpl());
		consumer.endElement("", "node", "node");
		consumer.endDocument();
	}
	
	public Context getAvalonContext() {
		return context;
	}

	public void setConsumer(XMLConsumer consumer) {
		this.consumer = consumer;
	}

	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
		this.objectModel = objectModel;
		try {
			this.key = par.getParameter("key");
		} catch (ParameterException e) {
			throw new CascadingRuntimeException("Could not find parameter key", e);
		}
	}
	
	public static void increment(Map objectModel, String key) {
		org.apache.cocoon.environment.Context context = ObjectModelHelper.getContext(objectModel);
		Integer count = (Integer) context.getAttribute(key);
		if (count == null) {
			count = new Integer(0);
		}
		count = new Integer(count.intValue() + 1);
		context.setAttribute(key, count);
	}

	public Serializable getKey() {
		return key;
	}

	public SourceValidity getValidity() {
		return NOPValidity.SHARED_INSTANCE;
	}

	public void contextualize(Context context) throws ContextException {
		this.context = context;
	}
}
