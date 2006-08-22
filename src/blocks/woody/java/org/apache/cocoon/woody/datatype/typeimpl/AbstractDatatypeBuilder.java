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
package org.apache.cocoon.woody.datatype.typeimpl;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.DatatypeBuilder;
import org.apache.cocoon.woody.datatype.DatatypeManager;
import org.apache.cocoon.woody.datatype.ValidationRule;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
import org.apache.cocoon.woody.datatype.convertor.ConvertorBuilder;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.util.SimpleServiceSelector;
import org.w3c.dom.Element;

/**
 * Abstract base class for datatype builders, most concrete datatype builders
 * will derive from this class.
 * @version $Id$
 */
public abstract class AbstractDatatypeBuilder implements DatatypeBuilder, Serviceable, Configurable {
    protected ServiceManager serviceManager;
    private SimpleServiceSelector convertorBuilders;
    private String defaultConvertorHint;
    private Convertor plainConvertor;

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        convertorBuilders = new SimpleServiceSelector("convertor", ConvertorBuilder.class);
        Configuration convertorsConf = configuration.getChild("convertors");
        convertorBuilders.configure(convertorsConf);
        defaultConvertorHint = convertorsConf.getAttribute("default");

        String plainConvertorHint = convertorsConf.getAttribute("plain");
        ConvertorBuilder plainConvertorBuilder;
        try {
            plainConvertorBuilder = (ConvertorBuilder)convertorBuilders.select(plainConvertorHint);
        } catch (ServiceException e) {
            throw new ConfigurationException("Convertor defined in plain attribute unavailable.", e);
        }

        try {
            plainConvertor = plainConvertorBuilder.build(null);
        } catch (Exception e) {
            throw new ConfigurationException("Error create plain convertor.", e);
        }
    }

    public void buildConvertor(Element datatypeEl, AbstractDatatype datatype) throws Exception {
        Element convertorEl = DomHelper.getChildElement(datatypeEl, Constants.WD_NS, "convertor", false);
        Convertor convertor = buildConvertor(convertorEl);
        datatype.setConvertor(convertor);
    }

    public Convertor buildConvertor(Element convertorEl) throws Exception {
        String type = null;
        // convertor configuration is allowed to be null, so check that it is not null
        if (convertorEl != null)
            type = convertorEl.getAttribute("type");
        if (type == null || type.length() == 0)
            type = defaultConvertorHint;
        ConvertorBuilder convertorBuilder = (ConvertorBuilder)convertorBuilders.select(type);
        return convertorBuilder.build(convertorEl);
    }

    public Convertor getPlainConvertor() {
        return plainConvertor;
    }

    protected void buildValidationRules(Element datatypeElement, AbstractDatatype datatype, DatatypeManager datatypeManager) throws Exception {
        Element validationElement = DomHelper.getChildElement(datatypeElement, Constants.WD_NS, "validation");
        if (validationElement != null) {
            Element[] validationElements = DomHelper.getChildElements(validationElement, Constants.WD_NS);
            for (int i = 0; i < validationElements.length; i++) {
                ValidationRule rule = datatypeManager.createValidationRule(validationElements[i]);
                if (rule.supportsType(datatype.getTypeClass(), datatype.isArrayType())) {
                    datatype.addValidationRule(rule);
                } else {
                    throw new Exception("Validation rule \"" + validationElements[i].getLocalName() + "\" cannot be used with strings, error at " + DomHelper.getLocation(validationElements[i]));
                }
            }
        }
    }
}
