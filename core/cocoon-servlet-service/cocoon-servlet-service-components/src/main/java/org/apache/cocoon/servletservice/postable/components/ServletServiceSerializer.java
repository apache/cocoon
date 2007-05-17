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
package org.apache.cocoon.servletservice.postable.components;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.serialization.AbstractSerializer;
import org.apache.cocoon.servletservice.postable.PostableSource;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.SourceException;
import org.xml.sax.SAXException;

public class ServletServiceSerializer extends AbstractSerializer implements SitemapModelComponent {

	private Log logger = LogFactory.getLog(getClass());

	private PostableSource servletSource;
	private Response response;

	private SaxBuffer saxBuffer;
	
	public void init() {
        this.enableLogging(new CLLoggerWrapper(this.logger));
    }

	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
		String service;
		try {
			service = par.getParameter("service");
		} catch (ParameterException e) {
			throw new ProcessingException(e);
		}

		try {
			servletSource = (PostableSource) resolver.resolveURI(service);
		} catch (ClassCastException e) {
			throw new ProcessingException("Resolved '" + service + "' to source that is not postable. Use servlet: protocol for service calls.");
		} catch (SourceException se) {
			throw SourceUtil.handle("Error during resolving of '" + service + "'.", se);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Source " + service + " resolved to " + servletSource.getURI());
		}

		saxBuffer = new SaxBuffer();
		setConsumer(saxBuffer);
		
		response = ObjectModelHelper.getResponse(objectModel);
	}
	
	/**
	 * This method returns dummy mime type to satisfy pipeline's requirement to have mime type determined at setup phase.
	 * In this serializer case it's not possible to satisfy this requirement so dummy value is returned and real is set in the 
	 * method {@link #endDocument()}.
	 * @see http://article.gmane.org/gmane.text.xml.cocoon.devel/73261 for post explaining current (hacky) solution
	 */
	public String getMimeType() {
		return "application/dummy-mime-type";
	}

	public void endDocument() throws SAXException {
		super.endDocument();

		String serializedXML;
		try {
			serializedXML = XMLUtils.serialize(saxBuffer, XMLUtils.createPropertiesForXML(false));
		} catch (ProcessingException e) {
			throw new SAXException("Exception occured while serializing content of sax buffer", e);
		}
		try {
			IOUtils.copy(new StringReader(serializedXML), servletSource.getOutputStream());
		} catch (IOException e) {
			throw new SAXException("Exception occured while writing to the output stream of source '" + servletSource.getURI() + "'", e);
		}
		try {
			//here real mime type is set, see getMimeType() method's comment
			response.setHeader("Content-Type", servletSource.getMimeType());
			
			IOUtils.copy(servletSource.getInputStream(), super.output);
		} catch (Exception e) {
			throw new SAXException("Exception occured while copying response from the service to the output stream", e);
		}
	}

}
