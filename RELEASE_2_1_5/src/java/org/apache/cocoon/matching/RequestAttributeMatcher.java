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

import java.util.HashMap;
import java.util.Map;

/**
 * This class allows for matching based on a request attribute.
 * If the specified request attribute exists, its string representation
 * is retrieved for later sitemap substitution.
 *
 * <p><b>Example:</b></p>
 * <pre>
 * &lt;map:match type="request-attribute" pattern="next"&gt;
 *     &lt;map:redirect-to src="{1}"/&gt;
 * &lt;/map:match&gt;
 * </pre>
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: RequestAttributeMatcher.java,v 1.2 2004/03/05 13:02:56 bdelacretaz Exp $
 */
public class RequestAttributeMatcher implements Matcher, ThreadSafe
{
    /**
     * Match method to see if the request attribute exists. If it does
     * have a value the string represenation of attribute is added to
     * the array list for later sitemap substitution.
     *
     * @param pattern name of request attribute to find
     * @param objectModel environment passed through via cocoon
     * @return null or map containing value of request attribute 'pattern'
     */
    public Map match(String pattern, Map objectModel, Parameters parameters) {

        Object attribute = ObjectModelHelper.getRequest(objectModel).getAttribute(pattern);

        if (attribute == null) {
            return null;
        } else {
            Map map = new HashMap();
            map.put("1", attribute.toString());
            return map;
        }
    }
}
