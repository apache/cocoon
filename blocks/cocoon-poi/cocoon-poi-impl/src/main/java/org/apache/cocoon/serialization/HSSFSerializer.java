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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.cocoon.components.elementprocessor.ElementProcessorFactory;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.HSSFElementProcessorFactory;

/**
 * Serializer to produce an HSSF stream.
 *
 * @version $Id$
 */
public class HSSFSerializer extends POIFSSerializer
                            implements Initializable, Configurable {

    private ElementProcessorFactory _element_processor_factory;
    private final static String _mime_type = "application/vnd.ms-excel";
    String locale;

    /**
     * Initialialize the component. Initialization includes allocating any
     * resources required throughout the components lifecycle.
     *
     * @exception Exception if an error occurs
     */
    public void initialize() throws Exception {
        _element_processor_factory = new HSSFElementProcessorFactory(locale);
        setupLogger(_element_processor_factory);
    }

    public void configure(Configuration conf) throws ConfigurationException {
        Configuration[] parameters = conf.getChildren("parameter");
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getAttribute("name");
            if (name.trim().equals("locale")) {
                locale = parameters[i].getAttribute("value");
            }
        }
    }

    /**
     * get the mime type
     *
     * @return application/vnd.ms-excel
     */
    public String getMimeType() {
        return _mime_type;
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
}
