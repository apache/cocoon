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

import java.util.Collections;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.sitemap.PatternException;

/**
 * A matcher that tests if the current request is an Ajax request. This matcher
 * provides no sitemap variables.
 * 
 * <p>Example:
 * <pre>
 *   &lt;map:match type="ajax-request"&gt;
 *     ... ajax request ...
 *   &lt;/map:match&gt;
 * </pre>
 * 
 * @cocoon.sitemap.component.documentation
 * A matcher that tests if the current request is an Ajax request. This matcher
 * provides no sitemap variables.
 *
 * @since 2.1.8
 * @version $Id$
 */
public class AjaxRequestMatcher implements Matcher, ThreadSafe {

    public Map match(String pattern, Map objectModel, Parameters parameters) throws PatternException {
        Request req = ObjectModelHelper.getRequest(objectModel);
        
        return AjaxHelper.isAjaxRequest(req) ? Collections.EMPTY_MAP : null;
    }
}
