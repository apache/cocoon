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
package org.apache.cocoon.matching;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

import java.util.HashMap;
import java.util.Map;

/**
 * This class allows for matching based on a request parameter.
 * If the specified request parameter exists, its value is retrieved for later
 * sitemap substitution.
 *
 * <p><b>Example:</b></p>
 * <pre>
 * &lt;map:match type="request" pattern="dest"&gt;
 *     &lt;map:redirect-to uri="{1}"/&gt;
 * &lt;/map:match&gt;
 * </pre>
 *
 * @author <a href="mailto:Marcus.Crafter@osa.de">Marcus Crafter</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: RequestParameterMatcher.java,v 1.2 2004/03/05 13:02:56 bdelacretaz Exp $
 */
public class RequestParameterMatcher implements Matcher, ThreadSafe
{
    /**
     * Match method to see if the request parameter exists. If it does
     * have a value the parameter is added to the array list for later
     * sitemap substitution.
     *
     * @param pattern name of request parameter to find
     * @param objectModel environment passed through via cocoon
     * @return null or map containing value of request parameter 'pattern'
     */
    public Map match(String pattern, Map objectModel, Parameters parameters) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        String parameter = request.getParameter(pattern);
        if (parameter == null) {
            return null; // no parameter defined
        } else {
            Map map = new HashMap();
            map.put("1", parameter);
            return map; // parameter defined, return map
        }
    }
}
