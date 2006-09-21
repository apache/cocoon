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
import org.apache.cocoon.sitemap.PatternException;

import java.util.Map;

/**
 *
 * @version $Id$
 */
public interface Matcher {

    String ROLE = Matcher.class.getName();

    /**
     * Matches the pattern against some <code>Request</code> values
     * and returns a <code>Map</code> object with replacements
     * for wildcards contained in the pattern.
     * @param pattern     The pattern to match against. Depending on the
     *                    implementation the pattern can contain wildcards
     *                    or regular expressions.
     * @param objectModel The <code>Map</code> with object of the
     *                    calling environment which can be used
     *                    to select values this matchers matches against.
     * @return Map        The returned <code>Map</code> object with
     *                    replacements for wildcards/regular-expressions
     *                    contained in the pattern.
     *                    If the return value is null there was no match.
     */
    Map match (String pattern, Map objectModel, Parameters parameters) throws PatternException;
}



