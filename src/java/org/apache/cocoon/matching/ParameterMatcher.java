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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

/**
 * This class allows for matching based on a parameter provided from the sitemap.
 * If the specified sitemap parameter exists, its value is retrieved for later
 * sitemap substitution.
 *
 * <p><b>Example:</b></p>
 * <pre>
 * &lt;map:match type="parameter" pattern="dest"&gt;
 *     &lt;map:redirect-to uri="{1}"/&gt;
 * &lt;/map:match&gt;
 * </pre>
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: ParameterMatcher.java,v 1.4 2004/03/08 14:02:41 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=Matcher
 * @x-avalon.lifestyle type=singleton
 */
public class ParameterMatcher implements Matcher

{
    /**
     * Match method to see if the sitemap parameter exists. If it does
     * have a value the parameter added to the array list for later
     * sitemap substitution.
     *
     * @param pattern name of sitemap parameter to find
     * @param objectModel environment passed through via cocoon
     * @return null or map containing value of sitemap parameter 'pattern'
     */
    public Map match(String pattern, Map objectModel, Parameters parameters) {

        String parameter = parameters.getParameter(pattern, null);
        if (parameter == null) {
            return null; // no parameter defined
        } else {
            Map map = new HashMap();
            map.put("1", parameter);
            return map; // parameter defined, return map
        }
    }
}
