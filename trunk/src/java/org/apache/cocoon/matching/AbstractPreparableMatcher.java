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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.sitemap.PatternException;

/**
 * A matcher that can prepare patterns during sitemap setup for faster match at request time.
 * This is also a regular matcher, meaning the sitemap can decide either to prepare the pattern
 * or to match with a request-time evaluated pattern (for {..} substitution).
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractPreparableMatcher.java,v 1.3 2004/03/08 14:02:41 cziegeler Exp $
 */
public abstract class AbstractPreparableMatcher extends AbstractLogEnabled implements PreparableMatcher {

    /**
     * Match the pattern by preparing it and matching the prepared pattern.
     */
    public Map match (String pattern, Map objectModel, Parameters parameters)
      throws PatternException {
        return preparedMatch(preparePattern(pattern), objectModel, parameters);
    }
}
