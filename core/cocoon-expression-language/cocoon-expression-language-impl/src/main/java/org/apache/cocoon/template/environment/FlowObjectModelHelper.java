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
package org.apache.cocoon.template.environment;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.objectmodel.ObjectModel;
import org.apache.cocoon.objectmodel.helper.TemplateObjectModelHelper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


/**
 * Creation of an Expression context from the TemplateObjectModelHelper
 * 
 * @version SVN $Id$
 */
public class FlowObjectModelHelper {

    /** Avoid instantiation. */
    private FlowObjectModelHelper() {}

    public static Scriptable getScope(Scriptable rootScope) {
        Scriptable scope;
        Context ctx = Context.enter();
        try {
            scope = ctx.newObject(rootScope);
            scope.setPrototype(rootScope);
            scope.setParentScope(null);
        } catch (Exception e) {
            throw new RuntimeException("Exception", e);
        } finally {
            Context.exit();
        }
        return scope;
    }

    /**
     * Create an expression context that contains the object model
     * @param newObjectModel TODO
     */
    public static void fillNewObjectModelWithFOM(ObjectModel newObjectModel, 
                                                            final Map objectModel, final Parameters parameters) {
        Map expressionContext = TemplateObjectModelHelper.getTemplateObjectModel(objectModel, parameters);
        
        //FIXME: It's a temporary code!
        ((Map)newObjectModel.get("cocoon")).putAll((Map)expressionContext.get("cocoon"));
        for (Iterator keysIterator = expressionContext.keySet().iterator(); keysIterator.hasNext(); ) {
            Object key = keysIterator.next();
            if ("cocoon".equals(key))
                continue;
            newObjectModel.put(key, expressionContext.get(key));
        }
        newObjectModel.put(org.apache.cocoon.objectmodel.ObjectModel.CONTEXTBEAN, FlowHelper.getContextObject(objectModel));
    }

}
