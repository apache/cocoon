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
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Map;

/**
 * Matches the request URIs against a regular expression pattern.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: RegexpURIMatcher.java,v 1.2 2004/03/05 13:02:56 bdelacretaz Exp $
 */
public class RegexpURIMatcher extends AbstractRegexpMatcher
{
    /**
     * Return the request URI.
     */
    protected String getMatchString(Map objectModel, Parameters parameters) {
        String uri = ObjectModelHelper.getRequest(objectModel).getSitemapURI();

        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }

        return uri;
    }
}
