/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version $Id: AbstractDatatypeBuilder.java,v 1.9 2004/02/11 09:53:44 antonio Exp $
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
        if (type == null || type.equals(""))
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
                if (!rule.supportsType(datatype.getTypeClass(), datatype.isArrayType())) {
                    throw new Exception("Validation rule \"" + validationElements[i].getLocalName() + "\" cannot be used with strings, error at " + DomHelper.getLocation(validationElements[i]));
                }
                datatype.addValidationRule(rule);
            }
        }
    }
}
