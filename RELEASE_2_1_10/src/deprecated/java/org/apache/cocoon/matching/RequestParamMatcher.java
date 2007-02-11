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

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

/**
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @deprecated Renamed to RequestParameterMatcher
 * @version CVS $Id$
 */
public class RequestParamMatcher extends RequestParameterMatcher
    implements LogEnabled
{
    public void enableLogging(Logger logger) {
        logger.warn("RequestParamMatcher is deprecated. Please use RequestParameterMatcher");
    }
}
