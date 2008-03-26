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
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.SourceException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.serialization.AbstractSerializer;
import org.apache.cocoon.servletservice.postable.PostableSource;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLUtils;

import org.xml.sax.SAXException;

/**
 * <p>The serializer takes only <code>service</code> parameter that should
 * contain the URL of the called service.</p>
 *
 * <p>Use <code>servlet:</code> source for that purpose.</p>
 *
 * <p>FIXME: Provide a link to the documents discussing servlet (and sitemap) services.</p>
 *
 * @cocoon.sitemap.component.documentation
 * The <code>ServletServiceSerializer</code> POSTs its input data to a called
 * service. Result of the serialization is a data returned by the called
 * service.
 * @cocoon.sitemap.component.name servletService
 * @cocoon.sitemap.component.documentation.caching Not Implemented
 *
 * @version $Id$
 * @since 1.0.0
 */
public class ServletServiceSerializer extends AbstractSerializer
                                      implements SitemapModelComponent {

	private PostableSource servletSource;
	private Response response;

	private SaxBuffer saxBuffer;


    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
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

		if (getLogger().isDebugEnabled()) {
			getLogger().debug("Source " + service + " resolved to " + servletSource.getURI());
		}

		saxBuffer = new SaxBuffer();
		setConsumer(saxBuffer);

		response = ObjectModelHelper.getResponse(objectModel);
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
            // TODO Improve this quick fix; it is better for Postable to provide Writer instead.
            // TODO Need to specify
            // Convert output stream to writer to specify UTF-8 encoding.
            Writer out = new OutputStreamWriter(servletSource.getOutputStream(), "UTF-8");
            IOUtils.copy(new StringReader(serializedXML), out);
            out.flush();
        } catch (IOException e) {
			throw new SAXException("Exception occured while writing to the output stream of source '" + servletSource.getURI() + "'", e);
        }

        try {
			//here real mime type is set, see Spring bean's configuration comment
			response.setHeader("Content-Type", servletSource.getMimeType());

			IOUtils.copy(servletSource.getInputStream(), super.output);
		} catch (Exception e) {
			throw new SAXException("Exception occured while copying response from the service to the output stream", e);
		}
	}
}
