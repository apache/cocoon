/*
 * Created on Jun 28, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.cocoon.generation;

import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.excalibur.source.SourceValidity;

/**
 * @author ghoward
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EventCachedFileGenerator extends FileGenerator {

    private EventValidity validity = null;
	/* (non-Javadoc)
	 * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
	 */
	public SourceValidity getValidity() {
        if (validity == null) {
            validity = new EventValidity(new NamedEvent("test"));
        }
        return validity;
	}

}
