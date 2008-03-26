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
package org.apache.cocoon.forms.datatype;

import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Implementation of the {@link DatatypeManager} component.
 *
 * <p>It supports an extensible number of datatype and validation rule implementations
 * by delegating the creation of them to {@link DatatypeBuilder}s and {@link ValidationRuleBuilder}s.
 * Currently the list of datatype and validationrule builders is hardcoded, but this will
 * become externally configurable in the future.
 *
 * @version $Id$
 *
 */
public class DefaultDatatypeManager implements DatatypeManager {
    private Map datatypeBuilders;
    private Map validatorRuleBuilders;

    public Datatype createDatatype(Element datatypeElement, boolean arrayType) throws Exception {
        String typeName = DomHelper.getAttribute(datatypeElement, "base");
        DatatypeBuilder builder = (DatatypeBuilder) datatypeBuilders.get(typeName);
        if (builder == null) {
            throw new Exception("Unknown datatype '" + typeName + "' specified at " + DomHelper.getLocation(datatypeElement));
        }
        return builder.build(datatypeElement, arrayType, this);
    }

    public ValidationRule createValidationRule(Element validationRuleElement) throws Exception {
        String name  = validationRuleElement.getLocalName();
        ValidationRuleBuilder builder = (ValidationRuleBuilder) validatorRuleBuilders.get(name);
        if (builder == null) {
            throw new Exception("Unknown validation rule \"" + name + "\" specified at " + DomHelper.getLocation(validationRuleElement));
        }
        return builder.build(validationRuleElement);
    }

    public Convertor createConvertor(String dataTypeName, Element convertorElement) throws Exception {
        DatatypeBuilder builder = (DatatypeBuilder)datatypeBuilders.get(dataTypeName);
        if (builder == null) {
            throw new Exception("Unknown datatype '" + dataTypeName + "' specified for " + DomHelper.getLocation(convertorElement));
        }
        return builder.buildConvertor(convertorElement);
    }

    public void setDatatypeBuilders( Map datatypeBuilders )
    {
        this.datatypeBuilders = datatypeBuilders;
    }

    public void setValidatorRuleBuilders( Map validatorRuleBuilders )
    {
        this.validatorRuleBuilders = validatorRuleBuilders;
    }
}
