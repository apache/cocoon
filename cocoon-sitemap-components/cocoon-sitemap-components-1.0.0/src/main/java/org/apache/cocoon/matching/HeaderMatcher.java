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
package org.apache.cocoon.matching;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * This class allows for matching based on a request header.
 * If the specified request header parameter exists, its value is
 * retrieved for later sitemap substitution.
 *
 * <p><b>Example:</b></p>
 * <pre>
 * &lt;map:match type="header" pattern="referer"&gt;
 *     &lt;map:redirect-to uri="{1}"/&gt;
 * &lt;/map:match&gt;
 * </pre>
 *
 * @cocoon.sitemap.component.documentation
 * This class allows for matching based on a request header.
 * If the specified request header parameter exists, its value is
 * retrieved for later sitemap substitution.
 *
 * @version $Id$
 */
public class HeaderMatcher implements Matcher, ThreadSafe
{
    /**
     * Match method to see if the request header exists. If it does
     * have a value the header added to the array list for later
     * sitemap substitution.
     *
     * @param pattern name of request header to find
     * @param objectModel environment passed through via cocoon
     * @return null or map containing value of request header 'pattern'
     */
    public Map match(String pattern, Map objectModel, Parameters parameters) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        String value = request.getHeader(pattern);
        if (value == null) {
            return null; // no request header defined
        } else {
            Map map = new HashMap();
            map.put("1", value);
            return map; // request header defined, return map
        }
    }
}
