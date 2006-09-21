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
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapParameters;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all matchers using a regular expression pattern.
 *
 * @version $Id$
 */

public abstract class AbstractRegexpMatcher extends AbstractPreparableMatcher implements ThreadSafe {

    /**
     * Compile the pattern in a <code>org.apache.regexp.REProgram</code>.
     */
    public Object preparePattern(String pattern) throws PatternException {
        // if pattern is null, return null to allow throwing a located exception in preparedMatch()
        if (pattern == null) {
            return null;
        }

        if (pattern.length() == 0) {
            pattern = "^$";
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("The empty pattern string was rewritten to '^$'"
                                 + " to match for empty strings.  If you intended"
                                 + " to match all strings, please change your"
                                 + " pattern to '.*'");
            }
        }

        try {
            RECompiler compiler = new RECompiler();
            REProgram program = compiler.compile(pattern);
            return program;

        } catch (RESyntaxException rse) {
            getLogger().debug("Failed to compile the pattern '" + pattern + "'", rse);
            throw new PatternException(rse.getMessage(), rse);
        }
    }

    /**
     * Match the prepared pattern against the value returned by {@link #getMatchString(Map, Parameters)}.
     */
    public Map preparedMatch(Object preparedPattern, Map objectModel, Parameters parameters) throws PatternException {

        if(preparedPattern == null) {
            throw new PatternException("A pattern is needed at " + SitemapParameters.getLocation(parameters));
        }

        RE re = new RE((REProgram)preparedPattern);
        String match = getMatchString(objectModel, parameters);

        if (match == null)
            return null;

        if(re.match(match)) {
            /* Handle parenthesised subexpressions. XXX: could be faster if we count
             * parens *outside* the generated code.
             * Note: *ONE* based, not zero, zero contains complete match
             */
            int parenCount = re.getParenCount();
            Map map = new HashMap();
            for (int paren = 0; paren <= parenCount; paren++) {
                map.put(Integer.toString(paren), re.getParen(paren));
            }

            return map;
        }

        return null;
    }

    /**
     * Get the string to test against the regular expression. To be defined
     * by concrete subclasses.
     */
    protected abstract String getMatchString(Map objectModel, Parameters parameters);
}
