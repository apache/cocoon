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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * Match the request URI against a wildcard expression.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: WildcardURIMatcher.java,v 1.5 2004/03/08 14:02:42 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=Matcher
 * @x-avalon.lifestyle type=singleton
 */
public class WildcardURIMatcher extends AbstractWildcardMatcher
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
