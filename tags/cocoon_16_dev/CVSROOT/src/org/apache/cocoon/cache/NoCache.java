package org.apache.cocoon.cache;

import javax.servlet.http.*;
import org.apache.cocoon.framework.*;

/**
 * A caching implementation that doesn't cache anything :).
 *
 * @author <a href="stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $Date: 1999/09/13 00:24:03 $
 */
public class NoCache extends AbstractActor implements Cache, Status {

    public Page getPage(HttpServletRequest request) {
    	return null;
    }
    
    public void setPage(Page page, HttpServletRequest request) {
    	// do nothing
    }
    
    public String getStatus() {
        return "<b>No cache</b>";
    }
}