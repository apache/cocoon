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
import org.apache.cocoon.Constants;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.cocoon.sitemap.PatternException;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for wildcard matchers
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractWildcardMatcher.java,v 1.3 2004/03/05 13:02:56 bdelacretaz Exp $
 */

public abstract class AbstractWildcardMatcher extends AbstractPreparableMatcher implements ThreadSafe {

    /**
     * Compile the pattern in an <code>int[]</code>.
     */
    public Object preparePattern(String pattern) {
        // if pattern is null, return null to allow throwing a located exception in preparedMatch()
        return pattern == null ? null : WildcardHelper.compilePattern(pattern);
    }

    /**
     * Match the prepared pattern against the result of {@link #getMatchString(Map, Parameters)}.
     */
    public Map preparedMatch(Object preparedPattern, Map objectModel, Parameters parameters) throws PatternException {

        if(preparedPattern == null) {
            throw new PatternException("A pattern is needed at " +
                parameters.getParameter(Constants.SITEMAP_PARAMETERS_LOCATION, "unknown location"));
        }

        String match = getMatchString(objectModel, parameters);

        if (match == null) {
            return null;
        }

        HashMap map = new HashMap();

        if (WildcardHelper.match(map, match, (int[])preparedPattern)) {
            return map;
        } else {
            return null;
        }
    }

    /**
     * Get the string to test against the wildcard expression. To be defined
     * by concrete subclasses.
     */
    protected abstract String getMatchString(Map objectModel, Parameters parameters);
}
