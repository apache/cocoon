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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.IncrementGenerator;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Increment context attribute "count" for testing purposes.
 *
 * This transformer always returns a VALID validity, and the cache key is given as
 * a sitemap parameter.
 *
 * @version $Id$
 */
public class IncrementTransformer extends AbstractTransformer implements CacheableProcessingComponent {
	XMLConsumer consumer;
	Map objectModel;
	String key;

	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
		this.objectModel = objectModel;
		try {
			this.key = par.getParameter("key");
		} catch (ParameterException e) {
			throw new CascadingRuntimeException("Could not find parameter key", e);
		}
	}

	public Serializable getKey() {
		return key;
	}

	public SourceValidity getValidity() {
		return NOPValidity.SHARED_INSTANCE;
	}

	public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
		IncrementGenerator.increment(objectModel, "count");
		super.startElement(uri, loc, raw, a);
	}
}
