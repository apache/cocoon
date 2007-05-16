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

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.servletservice.postable.PostableSource;
import org.apache.cocoon.sitemap.DisposableSitemapComponent;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.SourceException;
import org.xml.sax.SAXException;

public class ServletServiceTransformer extends AbstractSAXTransformer implements DisposableSitemapComponent {
	private Log logger = LogFactory.getLog(getClass());
	
	private SAXParser saxParser;
	
	private PostableSource servletSource;
	
    public void init() {
        this.enableLogging(new CLLoggerWrapper(this.logger));
    }
	
	public void setupTransforming() throws IOException, ProcessingException, SAXException {
		super.setupTransforming();
		
		String service;
		try {
			service = parameters.getParameter("service");
		} catch (ParameterException e) {
			throw new ProcessingException(e);
		}
		
        try {
        	servletSource = (PostableSource)resolver.resolveURI(service);
        } catch (ClassCastException e) {
        	throw new ProcessingException("Resolved '" + service + "' to source that is not postable. Use servlet: protocol for service calls.");            
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + service + "'.", se);
        }
        
        if (logger.isDebugEnabled()) {
        	logger.debug("Source " + service + " resolved to " + servletSource.getURI());
        }
		
		startSerializedXMLRecording(null);
	}
	
	public void endDocument() throws SAXException {
		super.endDocument();
		try {
			String xml = endSerializedXMLRecording();
			IOUtils.copy(new StringReader(xml), servletSource.getOutputStream());
			SourceUtil.parse(saxParser, servletSource, contentHandler);
		}
		catch (Exception e) {
			throw new SAXException("Exception occured while calling servlet service", e);
		}
	}
	
	public void dispose() {
		if (servletSource != null)
			resolver.release(servletSource);
		super.dispose();
	}

	public SAXParser getSaxParser() {
		return saxParser;
	}

	public void setSaxParser(SAXParser saxParser) {
		this.saxParser = saxParser;
	}

}
