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
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.regexp.RE;
import org.apache.regexp.REProgram;

/**
 * Matches the request URIs against a regular expression pattern.
 *
 * @author     <a href="mailto:huber@apache.org">Bernhard Huber</a>
 * @since 03 January 2003
 * @version CVS $Id: RegexpURIDefaultsMatcher.java,v 1.2 2004/03/05 10:07:26 bdelacretaz Exp $
 */
public class RegexpURIDefaultsMatcher extends AbstractRegexpMatcher {

    /**
     * Match the prepared pattern against the value returned by {@link #getMatchString(Map, Parameters)}.
     *
     * @param  preparedPattern  Description of the Parameter
     * @param  objectModel      Description of the Parameter
     * @param  parameters       Description of the Parameter
     * @return                  Description of the Return Value
     */
    public Map preparedMatch(Object preparedPattern, Map objectModel, Parameters parameters) {

        RE re = new RE((REProgram) preparedPattern);
        String match = getMatchString(objectModel, parameters);

        if (match == null) {
            return null;
        }

        if (re.match(match)) {
            /* Handle parenthesised subexpressions. XXX: could be faster if we count
             * parens *outside* the generated code.
             * Note: *ONE* based, not zero, zero contains complete match
             */
            int parenCount = re.getParenCount();
            Map map = new HashMap();

            // set defaults
            mapDefaults(parameters, map);

            // override default by matching values
            for (int paren = 0; paren <= parenCount; paren++) {
                getLogger().debug("Matched " +
                        String.valueOf(Integer.toString(paren)) + " " + String.valueOf(re.getParen(paren)));

                map.put(Integer.toString(paren), re.getParen(paren));
            }

            return map;
        }

        return null;
    }

    /**
     * Return the request URI.
     *
     * @param  objectModel  Description of the Parameter
     * @param  parameters   Description of the Parameter
     * @return              The matchString value
     */
    protected String getMatchString(Map objectModel, Parameters parameters) {
        String uri = ObjectModelHelper.getRequest(objectModel).getSitemapURI();

        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        return uri;
    }


    /*
      pattern: ([a-z]+)(\(([0-9]+)+\))?
      map-default:2 "(1)"
      map-default:3 "1"
    */
    /**
     *  Description of the Method
     *
     *@param  parameters  Description of the Parameter
     *@param  map         Description of the Parameter
     */
    protected void mapDefaults(Parameters parameters, Map map) {
        String[] parameterNames = parameters.getNames();
        final String MAP_DEFAULT_PREFIX = "map-default:";
        final int MAP_DEFAULT_PREFIX_LENGTH = MAP_DEFAULT_PREFIX.length();

        for (int i = 0; i < parameterNames.length; i++) {
            if (parameterNames[i].startsWith(MAP_DEFAULT_PREFIX)) {
                String entry = parameterNames[i];
                String defaultEntry = entry.substring(MAP_DEFAULT_PREFIX_LENGTH);
                String defaultValue = parameters.getParameter(entry, null);

                if (defaultValue != null) {
                    getLogger().debug("Setting " +
                            "defaultEntry " + String.valueOf(defaultEntry) + ", " +
                            "defaultValue " + String.valueOf(defaultValue));

                    map.put(defaultEntry, defaultValue);
                }
            }
        }
    }
}

