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
package org.apache.cocoon.serialization;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.elementprocessor.ElementProcessorFactory;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.HSSFElementProcessorFactory;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.xml.sax.SAXException;

/**
 * Serializer to produce an HSSF stream.
 * 
 * @version $Id$
 */
public class HSSFSerializer extends POIFSSerializer
                            implements SitemapModelComponent {

    private ElementProcessorFactory _element_processor_factory;
    private String locale;

    /**
     * Setup the component. Setup includes allocating any resources required
     * throughout the components lifecycle. Sitemap parameters will overwrite
     * configuration parameters.
     * 
     * @exception ProcessingException
     *                if an error occurs
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        try {
            if (par.isParameter("locale")) {
                setLocale(par.getParameter("locale"));
            }
        } catch (ParameterException e) {
            throw new ProcessingException(e);
        }
        _element_processor_factory = new HSSFElementProcessorFactory(locale);
    }

    /**
     * get the ElementProcessorFactory
     * 
     * @return the ElementProcessorFactory
     */
    protected ElementProcessorFactory getElementProcessorFactory() {
        return _element_processor_factory;
    }

    /**
     * post-processing for endDocument
     */
    protected void doLocalPostEndDocument() {
    }

    /**
     * pre-processing for endDocument
     */
    protected void doLocalPreEndDocument() {
    }

    /**
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }
}
