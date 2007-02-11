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
package org.apache.cocoon.woody.datatype.validationruleimpl;

import org.apache.cocoon.woody.datatype.ValidationRuleBuilder;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.expression.ExpressionManager;
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
 * @version $Id: AbstractValidationRuleBuilder.java,v 1.4 2004/02/11 09:53:44 antonio Exp $
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
        Element failMessageElement = DomHelper.getChildElement(validationRuleElement, Constants.WD_NS, "failmessage");
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
