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
package org.apache.cocoon.forms.datatype.validationruleimpl;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.ValidationRuleBuilder;
import org.apache.cocoon.forms.expression.ExpressionManager;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Element;
import org.outerj.expression.Expression;
import org.outerj.expression.TokenMgrError;

/**
 * Abstract base class for ValidationRuleBuilder implementations.
 * 
 * @version $Id: AbstractValidationRuleBuilder.java,v 1.2 2004/03/09 13:08:47 cziegeler Exp $
 */
public abstract class AbstractValidationRuleBuilder implements ValidationRuleBuilder, Serviceable, Disposable {
    protected ExpressionManager expressionManager;
    protected ServiceManager serviceManager;

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
        expressionManager = (ExpressionManager)serviceManager.lookup(ExpressionManager.ROLE);
    }

    /**
     * Checks if the validation rule configuration contains a custom failmessage, and if so,
     * sets it one the ValidationRule.
     */
    protected void buildFailMessage(Element validationRuleElement, AbstractValidationRule rule) {
        Element failMessageElement = DomHelper.getChildElement(validationRuleElement, Constants.DEFINITION_NS, "failmessage");
        if (failMessageElement != null) {
            XMLizable failMessage = DomHelper.compileElementContent(failMessageElement);
            rule.setFailMessage(failMessage);
        }
    }

    /**
     * Parses an expression and throws a nice error message if this fails.
     */
    protected Expression parseExpression(String exprString, Element element, String attrName) throws Exception {
        try {
            return expressionManager.parse(exprString);
        } catch (TokenMgrError e) {
            throw new CascadingException("Error in expression \"" + exprString + "\" in attribute \"" + attrName + "\" at " + DomHelper.getLocation(element), e);
        } catch (Exception e) {
            throw new CascadingException("Error in expression \"" + exprString + "\" in attribute \"" + attrName + "\" at " + DomHelper.getLocation(element), e);
        }
    }

    public void dispose() {
        serviceManager.release(expressionManager);
    }
}
