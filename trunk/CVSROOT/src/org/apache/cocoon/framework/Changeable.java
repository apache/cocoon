package org.apache.cocoon.framework;

import javax.servlet.http.*;

/**
 * This interface must be implemented by all those classes that represent
 * a changeable point in the document processing chain. Each changeable point
 * is then queried by the cache system to determine the validity of the
 * cached respose.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:17 $
 */

public interface Changeable {
    
    /**
     * Returns false if the requested resource hasn't changed, true
     * otherwise. This method is called by the cache system to 
     * ensure the validity of the cached response. It is the 
     * producer responsibility to provide the fastest possible 
     * implementation of this method or, whether this is not 
     * possible and the costs of the change evaluation is
     * comparable to the production costs, to return
     * true directly with no further delay, thus reducing
     * the evaluation overhead to a minimum.
     * 
     * This method is guaranteed to be called after at least
     * a single call to any production methods getStream or getDocument.
     *
     * The context is the trigger of the changeable point and may
     * differ between implementations.
     */
    boolean hasChanged(Object context);
    
}