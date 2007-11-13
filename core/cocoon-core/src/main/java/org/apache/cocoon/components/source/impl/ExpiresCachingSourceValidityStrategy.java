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
package org.apache.cocoon.components.source.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;

/**
 * We need to store both the cache expiration and the original source validity
 * the former is to determine whether to recheck the latter (see checkValidity)
 *
 * @version $Id$
 */
public class ExpiresCachingSourceValidityStrategy implements CachingSourceValidityStrategy {

    private Log logger = LogFactory.getLog(getClass());

    public SourceValidity[] getCacheValidities(CachingSource cachingSource, Source source) {
        return new SourceValidity[] { 
              new ExpiresValidity(cachingSource.getExpiration()), source.getValidity() };
    }

    public boolean checkValidity(CachedSourceResponse response, Source source, long expires) {
        final SourceValidity[] validities = response.getValidityObjects();
        boolean valid = true;

        final ExpiresValidity expiresValidity = (ExpiresValidity) validities[0];
        final SourceValidity sourceValidity = validities[1];

        if (expiresValidity.isValid() != SourceValidity.VALID) {
            int validity = sourceValidity != null? sourceValidity.isValid() : SourceValidity.INVALID;
            if (validity == SourceValidity.INVALID ||
                    validity == SourceValidity.UNKNOWN &&
                            sourceValidity.isValid(source.getValidity()) != SourceValidity.VALID) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Response expired, invalid for " + logger);
                }
                valid = false;
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Response expired, still valid for " + logger);
                }
                // set new expiration period
                validities[0] = new ExpiresValidity(expires);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Response not expired for " + logger);
            }
        }

        return valid;
    }

}
