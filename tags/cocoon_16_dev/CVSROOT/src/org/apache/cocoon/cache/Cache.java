package org.apache.cocoon.cache;

import org.apache.cocoon.framework.*;
import javax.servlet.http.*;

/**
 * The interface that all caching implementations must adhere to.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $Date: 1999/09/13 00:24:03 $
 */
 
public interface Cache extends Actor {

    /**
     * It returns null if no page is available.
     */ 
    public Page getPage(HttpServletRequest request);
    

    /**
     * Sets the page into the cache system using data from the request
     * object for indexing.
     */
    public void setPage(Page page, HttpServletRequest request);
    
}