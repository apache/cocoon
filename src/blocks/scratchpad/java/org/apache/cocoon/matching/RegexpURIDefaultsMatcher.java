/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: RegexpURIDefaultsMatcher.java,v 1.1 2003/09/04 12:42:46 cziegeler Exp $
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

