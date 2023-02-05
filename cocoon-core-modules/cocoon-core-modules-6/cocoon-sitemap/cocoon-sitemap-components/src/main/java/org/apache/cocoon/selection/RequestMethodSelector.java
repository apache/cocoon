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
package org.apache.cocoon.selection;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * A <code>Selector</code> that matches a getMethod() of the HTTP request.
 *
 * @cocoon.sitemap.component.documentation
 * A <code>Selector</code> that matches a getMethod() of the HTTP request.
 *
 * @version $Id$
 */
public class RequestMethodSelector extends AbstractLogEnabled
                                   implements ThreadSafe, Selector {

    public boolean select(String expression, Map objectModel, Parameters parameters) {
        String method = ObjectModelHelper.getRequest(objectModel).getMethod();
        return method.equals(expression);
    }
}
