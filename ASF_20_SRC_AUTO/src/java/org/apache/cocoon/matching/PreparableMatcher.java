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
import org.apache.cocoon.sitemap.PatternException;

import java.util.Map;

/**
 * A matcher that can prepare patterns during sitemap setup for faster match at request time.
 * This is also a regular matcher, meaning the sitemap can decide either to prepare the pattern
 * or to match with a request-time evaluated pattern (for {..} substitution).
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: PreparableMatcher.java,v 1.4 2004/03/05 13:02:56 bdelacretaz Exp $
 */
public interface PreparableMatcher extends Matcher {

    /**
     * Prepares a pattern in a form that allows faster match. For example, a regular
     * expression matcher can precompile the expression and return the corresponding
     * object. This method is called once for each pattern used with a particular matcher
     * class. The returned value is then passed back as the <code>preparedPattern</code>
     * parameter of {@link #preparedMatch(Object, Map, Parameters)}.
     *
     * @param pattern The pattern to prepare. Depending on the implementation the pattern
     *                can contain wildcards or regular expressions.
     * @return an optimized representation of the pattern.
     * @throws PatternException if the pattern couldn't be prepared.
     */
    Object preparePattern(String pattern) throws PatternException;

    /**
     * Matches the prepared pattern against some values in the object model (most often the
     * <code>Request</code>) and returns a <code>Map</code> object with replacements
     * for wildcards contained in the pattern.
     *
     * @param preparedPattern The preparedPattern to match against, as returned by {@link #preparePattern(String)}.
     * @param objectModel     The <code>Map</code> with objects of the calling environment
     *                        which can be used to select values this matchers matches against.
     * @return                a <code>Map</code> object with replacements for wildcards/regular-expressions
     *                        contained in the pattern. If the return value is null there was no match.
     */
    Map preparedMatch(Object preparedPattern, Map objectModel, Parameters parameters) throws PatternException;
}



