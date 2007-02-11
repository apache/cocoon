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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.sitemap.PatternException;

import java.util.HashMap;
import java.util.Map;

/**
 * Matches cookies agains given name. Returns value of the matched cookie.
 *
 * @author <a href="mailto:maciejka@tiger.com.pl">Maciek Kaminski</a>
 * @version CVS $Id: CookieMatcher.java,v 1.4 2004/03/05 13:02:56 bdelacretaz Exp $
 */
public class CookieMatcher extends AbstractLogEnabled implements Matcher, ThreadSafe {

    public Map match(String pattern, Map objectModel, Parameters parameters)
            throws PatternException {

        if (pattern == null) {
            throw new PatternException("No cookie name given.");
        }

        Request request = ObjectModelHelper.getRequest(objectModel);
        Cookie[] cookies = request.getCookies();
        HashMap result = null;

        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(pattern)) {
                    result = new HashMap();
                    result.put("1", cookie.getValue());
                    break;
                }
            }
        }

        return result;
    }
}
