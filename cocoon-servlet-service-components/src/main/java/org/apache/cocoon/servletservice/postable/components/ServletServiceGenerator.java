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
import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.servletservice.postable.PostableSource;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.cocoon.core.xml.SAXParser;
import org.xml.sax.SAXException;

/**
 * 
 *
 */
public class ServletServiceGenerator extends AbstractGenerator {
	
	private Log logger = LogFactory.getLog(getClass());
	private SAXParser saxParser;
	
	private PostableSource servletSource;
	
    public void init() {
        this.enableLogging(new CLLoggerWrapper(this.logger));
    }

	/* (non-Javadoc)
	 * @see org.apache.cocoon.generation.Generator#generate()
	 */
	public void generate() throws IOException, SAXException, ProcessingException {
        try {
            SourceUtil.parse(saxParser, this.servletSource, super.xmlConsumer);
        } catch (SAXException e) {
            SourceUtil.handleSAXException(this.servletSource.getURI(), e);
        }
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
	 */
	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        String service;
		try {
			service = parameters.getParameter("service");
		} catch (ParameterException e) {
			throw new ProcessingException(e);
		}
		
        Source inputSource;
        try {
        	try {
        		servletSource = (PostableSource)resolver.resolveURI(service);
        	} catch (ClassCastException e) {
        		throw new ProcessingException("Resolved '" + service + "' to source that is not postable. Use servlet: protocol for service calls.");
        	}
            inputSource = super.resolver.resolveURI(src);
            
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
        }
        
        if (logger.isDebugEnabled()) {
        	logger.debug("Source " + service + " resolved to " + servletSource.getURI());
            logger.debug("Source " + super.source + " resolved to " + inputSource.getURI());
        }
        
        IOUtils.copy(inputSource.getInputStream(), servletSource.getOutputStream());
	}

	public SAXParser getSaxParser() {
		return saxParser;
	}

	public void setSaxParser(SAXParser saxParser) {
		this.saxParser = saxParser;
	}

}
