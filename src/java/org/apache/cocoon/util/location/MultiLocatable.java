package org.apache.cocoon.util.location;

import java.util.List;

/**
 * An extension of {@link Location} for classes that can hold a list of locations.
 * It will typically be used to build location stacks.
 * <p>
 * The <em>first</em> location of the collection returned by {@link #getLocations()} should be
 * be identical to the result of {@link org.apache.cocoon.util.location.Locatable#getLocation()}.
 * <p>
 * If the list of locations designates a call stack, then its first element should be the deepmost
 * location of this stack. This is consistent with the need for <code>getLocation()</code> to
 * return the most precise location.
 * 
 * @version $Id$
 */
public interface MultiLocatable extends Locatable {
    
    /**
     * Return the list of locations.
     * 
     * @return a list of locations, or <code>null</code> if there are no locations.
     */
    public List getLocations();

}
