/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.tools.generation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.portal.tools.PortalToolCatalogue;
import org.apache.cocoon.portal.tools.PortalToolManager;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @version CVS $Id$
 */
public class I18nCatalogueGenerator extends ServiceableGenerator {
    
    private SAXParser parser;

    private String catalogueStartTag = "<?xml version=\"1.0\"?><catalogue>";
    private String catalogueEndTag = "</catalogue>";
    private String lang = ".xml";
    private PortalToolManager ptm = null;
    private StringBuffer i18n = new StringBuffer();
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate() throws IOException, SAXException,
            ProcessingException {
        StringBuffer catalogue = new StringBuffer();
        catalogue.append(catalogueStartTag).append(i18n.toString()).append(catalogueEndTag); //add pi and catalogue tags
        final InputSource inputSource = new InputSource(new StringReader(catalogue.toString()));
        try {
            parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        parser.parse(inputSource, super.xmlConsumer);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) 
    					throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        if(src.indexOf("_") != -1) {
            lang = src.substring(src.indexOf("_"), src.length());
            lang = lang.toLowerCase();
        }
        try {
            ptm = (PortalToolManager) this.manager.lookup(PortalToolManager.ROLE);
            List cats = ptm.getI18n();
            for(Iterator it = cats.iterator(); it.hasNext();) {
                PortalToolCatalogue ptc = (PortalToolCatalogue) it.next();
                try {
                    Source cat;
	                cat = resolver.resolveURI(ptc.getLocation() + ptc.getName() + lang);
	                // if(!cat.exists()) {
	                //    cat = resolver.resolveURI(ptc.getLocation() + ptc.getName() + ".xml"); // default file
	                // }
	                BufferedReader br = new BufferedReader(new InputStreamReader(cat.getInputStream()));
	                String tmp = new String();
	                while(br.ready()) {
	                    tmp = tmp + br.readLine();
	                }
                    tmp = tmp.replaceAll("<\\?(.+)\\?>",""); // remove processing instr.
                    tmp = tmp.replaceAll("<catalogue([^>]+)?>{1}" ,""); // remove catalogue tags
                    tmp = tmp.replaceAll("</catalogue>", "");
                    tmp = tmp.replaceAll("<!--(.+)-->", "");
                    i18n.append(tmp);
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                }
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } 
        
    }

}
