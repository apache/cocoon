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

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @deprecated Renamed to RequestParameterMatcher
 * @version CVS $Id: RequestParamMatcher.java,v 1.2 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public class RequestParamMatcher extends RequestParameterMatcher
    implements LogEnabled
{
    public void enableLogging(Logger logger) {
        logger.warn("RequestParamMatcher is deprecated. Please use RequestParameterMatcher");
    }
}
