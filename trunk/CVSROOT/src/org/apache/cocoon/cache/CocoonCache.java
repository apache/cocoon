package org.apache.cocoon.cache;

import java.util.*;
import javax.servlet.http.*;
import org.apache.cocoon.*;
import org.apache.cocoon.store.*;
import org.apache.cocoon.framework.*;

/**
 * This is the dynamic cocoon cache implementation which is
 * able to cache all instances of generated documents, both
 * statically and dynamically generated.
 *
 * @author <a href="stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $Date: 1999/10/26 16:20:36 $
 */
public class CocoonCache implements Cache, Status {

    private Store store;
    
    public void init(Director director) {
        this.store = (Store) director.getActor("store");
    }
    
    /**
     * This method retrieves a page from the store
     * and checks if its changeable points have changed.
     * Only if all the changeable points haven't changed
     * the page is returned, otherwise null is returned.
     */
    public Page getPage(HttpServletRequest request) {
        Page page = (Page) store.get(Utils.encode(request));

        if (page == null) {
            return null;
        }
        
        boolean changed = false;
        Enumeration e = page.getChangeables();
        while (e.hasMoreElements()) {
            Changeable c = (Changeable) e.nextElement();
            changed = c.hasChanged(request);
            if (changed) {
                break;
            }
        }
        
        return (changed) ? null : page;
    }
    
    /**
     * This method inserts the page in cache and associates it
     * with the given request.
     */
    public void setPage(Page page, HttpServletRequest request) {
        if (!page.isCached()) {         
            page.setCached(true);
            this.store.hold(Utils.encode(request), page);
        }
    }

    public String getStatus() {
        return "<b>Cocoon Dynamic Cache System</b>";
    }
}