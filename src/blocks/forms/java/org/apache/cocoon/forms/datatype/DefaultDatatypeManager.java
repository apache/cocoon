/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.forms.datatype;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.util.SimpleServiceSelector;
import org.w3c.dom.Element;

/**
 * Implementation of the {@link DatatypeManager} component.
 *
 * <p>It supports an extensible number of datatype and validation rule implementations
 * by delegating the creation of them to {@link DatatypeBuilder}s and {@link ValidationRuleBuilder}s.
 * Currently the list of datatype and validationrule builders is hardcoded, but this will
 * become externally configurable in the future.
 * 
 * @version $Id: DefaultDatatypeManager.java,v 1.3 2004/04/21 13:20:27 bruno Exp $
 *
 */
public class DefaultDatatypeManager extends AbstractLogEnabled implements DatatypeManager, ThreadSafe, Serviceable,
        Configurable, Initializable, Disposable, Contextualizable {
    private SimpleServiceSelector typeBuilderSelector;
    private SimpleServiceSelector validationRuleBuilderSelector;
    private ServiceManager serviceManager;
    private Configuration configuration;
    private Context context;

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
    }

    public void initialize() throws Exception {
        typeBuilderSelector = new SimpleServiceSelector("datatype", DatatypeBuilder.class);
        typeBuilderSelector.enableLogging(getLogger());
        typeBuilderSelector.contextualize(context);
        typeBuilderSelector.service(serviceManager);
        typeBuilderSelector.configure(configuration.getChild("datatypes"));

        validationRuleBuilderSelector = new SimpleServiceSelector("validation-rule", ValidationRuleBuilder.class);
        validationRuleBuilderSelector.enableLogging(getLogger());
        validationRuleBuilderSelector.contextualize(context);
        validationRuleBuilderSelector.service(serviceManager);
        validationRuleBuilderSelector.configure(configuration.getChild("validation-rules"));

        configuration = null;
    }

    public Datatype createDatatype(Element datatypeElement, boolean arrayType) throws Exception {
        String typeName = DomHelper.getAttribute(datatypeElement, "base");
        DatatypeBuilder builder = null;
        try {
            builder = (DatatypeBuilder)typeBuilderSelector.select(typeName);
        } catch (ServiceException e) {
            throw new CascadingException("Unknown datatype '" + typeName + "' specified at " + DomHelper.getLocation(datatypeElement), e);
        }
        return builder.build(datatypeElement, arrayType, this);
    }

    public ValidationRule createValidationRule(Element validationRuleElement) throws Exception {
        String name  = validationRuleElement.getLocalName();
        ValidationRuleBuilder builder = null;
        try {
            builder = (ValidationRuleBuilder)validationRuleBuilderSelector.select(name);
        } catch (ServiceException e) {
            throw new CascadingException("Unknown validation rule \"" + name + "\" specified at " + DomHelper.getLocation(validationRuleElement), e);
        }
        return builder.build(validationRuleElement);
    }

    public Convertor createConvertor(String dataTypeName, Element convertorElement) throws Exception {
        DatatypeBuilder datatypeBulder = (DatatypeBuilder)typeBuilderSelector.select(dataTypeName);
        return datatypeBulder.buildConvertor(convertorElement);
    }

    public void dispose() {
        typeBuilderSelector.dispose();
        validationRuleBuilderSelector.dispose();
    }
}
