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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.objectmodel.ObjectModel;
import org.apache.cocoon.objectmodel.helper.ParametersMap;


/**
 * Creation of an Expression context from the TemplateObjectModelHelper
 * 
 * @version SVN $Id$
 */
public class FlowObjectModelHelper {

    /** Avoid instantiation. */
    private FlowObjectModelHelper() {}

    /**
     * Create an expression context that contains the object model
     * @param newObjectModel TODO
     */
    public static void fillNewObjectModelWithFOM(ObjectModel newObjectModel, 
                                                            final Map objectModel, final Parameters parameters) {
        
        ((Map)newObjectModel.get("cocoon")).put("parameters", new ParametersMap(parameters));
    }

}
