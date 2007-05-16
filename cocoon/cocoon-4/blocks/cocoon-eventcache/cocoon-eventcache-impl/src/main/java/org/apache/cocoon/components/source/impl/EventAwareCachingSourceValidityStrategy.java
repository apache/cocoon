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
