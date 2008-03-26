/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.components.source.impl;

import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;

/**
 * Use event caching strategy, the associated event is the source uri
 * 
 * @version $Id$
 */
public class EventAwareCachingSourceValidityStrategy implements CachingSourceValidityStrategy {

    private Log logger = LogFactory.getLog(getClass());        
    
    public SourceValidity[] getCacheValidities(CachingSource cachingSource, Source source) {
        return new SourceValidity[] { new EventValidity(new NamedEvent(source.getURI())) };
    }

    public boolean checkValidity(CachedSourceResponse response, Source source, long expires) {
        if (logger.isDebugEnabled()) {
            logger.debug("Cached response of source does not expire");
        }
        return true;
    }

}
