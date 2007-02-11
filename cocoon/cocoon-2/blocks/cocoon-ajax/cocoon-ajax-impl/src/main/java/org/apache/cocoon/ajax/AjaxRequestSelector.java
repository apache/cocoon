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
package org.apache.cocoon.ajax;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.selection.AbstractSwitchSelector;
import org.apache.commons.lang.BooleanUtils;

/**
 * Choose a select branch depending on if the current request is an Ajax request.
 * The test value can be either "<code>true</code>" or "<code>false</code>".
 * 
 * <pre>
 *   &lt;map:select type="ajax-request"&gt;
 *     &lt;map:when test="true"&gt;
 *       ... ajax request ...
 *     &lt;/map:when&gt;
 *     &lt;map:otherwise&gt;
 *       ... non ajax request ...
 *     &lt;/map:otherwise&gt;
 *   &lt;/map:select&gt;
 * </pre>
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class AjaxRequestSelector extends AbstractSwitchSelector {
    
    public Object getSelectorContext(Map objectModel, Parameters parameters) {
        Request req = ObjectModelHelper.getRequest(objectModel);
        return BooleanUtils.toBooleanObject(AjaxHelper.isAjaxRequest(req));
    }

    public boolean select(String expression, Object selectorContext) {
        boolean test = BooleanUtils.toBoolean(expression);
        return test == ((Boolean)selectorContext).booleanValue();
    }
}
