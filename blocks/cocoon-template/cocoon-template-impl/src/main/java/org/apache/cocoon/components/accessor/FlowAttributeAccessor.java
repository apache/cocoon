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
package org.apache.cocoon.components.accessor;

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.environment.TemplateObjectModelHelper;

/**
 * @version $Id$
 */
public class FlowAttributeAccessor extends ObjectModelAccessor {

    /**
     * @see org.apache.cocoon.components.accessor.ObjectModelAccessor#getObject()
     */
    public Object getObject() {
        Map map = new HashMap();
        Object contextObject = FlowHelper.getContextObject(getObjectModel());

        if (contextObject instanceof Map) {
            map.putAll((Map)contextObject);
        } else if (contextObject != null) {
            TemplateObjectModelHelper.fillContext(contextObject, map);
        }

        return map;
    }
}
