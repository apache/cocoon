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
package org.apache.cocoon.portal.tools.generation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.portal.tools.PortalToolCatalogue;
import org.apache.cocoon.portal.tools.PortalToolManager;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @version $Id$
 */
public class I18nCatalogueGenerator extends ServiceableGenerator {

    private SAXParser parser;
    private PortalToolManager ptm;
    private final String CATALOGUE_TAG = "catalogue";
    private String lang = ".xml";

    /* (non-Javadoc)
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
    	this.xmlConsumer.startDocument();
    	this.xmlConsumer.startElement("", CATALOGUE_TAG, CATALOGUE_TAG, XMLUtils.EMPTY_ATTRIBUTES);
            List cats = ptm.getI18n();
            for(Iterator it = cats.iterator(); it.hasNext();) {
                PortalToolCatalogue ptc = (PortalToolCatalogue) it.next();
                try {
                    Source cat;
	                cat = resolver.resolveURI(ptc.getLocation() + ptc.getName() + lang);
	                IncludeXMLConsumer ixc = new IncludeXMLConsumer(this.xmlConsumer);
	                ixc.setIgnoreRootElement(true);
	                this.parser.parse(new InputSource(cat.getInputStream()),  ixc);
                } catch (MalformedURLException e) {
                	// ignore
                } catch (IOException e) {
                	// ignore
                }
            }
        this.xmlConsumer.endElement("", CATALOGUE_TAG, CATALOGUE_TAG);
        this.xmlConsumer.endDocument();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    					throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        if(src.indexOf("_") != -1) {
            this.lang = src.substring(src.indexOf("_"), src.length());
            this.lang = this.lang.toLowerCase();
        }

    }

    /* (non-Javadoc)
	 * @see org.apache.cocoon.generation.ServiceableGenerator#service(org.apache.avalon.framework.service.ServiceManager)
	 */
	public void service(ServiceManager manager) throws ServiceException {
		super.service(manager);
		this.parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
		ptm = (PortalToolManager) this.manager.lookup(PortalToolManager.ROLE);
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.generation.ServiceableGenerator#dispose()
	 */
	public void dispose() {
		super.dispose();
		this.manager.release(this.parser);
		this.manager.release(this.ptm);
	}
}
