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
package org.apache.cocoon.template.environment;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.environment.TemplateObjectModelHelper;


/**
 * Creation of an Expression context from the TemplateObjectModelHelper
 * 
 * @version SVN $Id$
 */
public class FlowObjectModelHelper {

    /**
     * Create an expression context that contains the object model
     */
    public static ExpressionContext getFOMExpressionContext(final Map objectModel, 
                                                            final Parameters parameters) {
        ExpressionContext context = new ExpressionContext();
        Map expressionContext = (Map)TemplateObjectModelHelper.getTemplateObjectModel(objectModel, parameters);
        expressionContext = (Map) TemplateObjectModelHelper.addJavaPackages( expressionContext );
        context.setVars( expressionContext );
        context.setContextBean(FlowHelper.getContextObject(objectModel));

        return context;
    }
}
