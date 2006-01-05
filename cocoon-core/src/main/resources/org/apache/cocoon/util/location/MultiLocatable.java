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
 * @since 2.1.8
 * @version $Id$
 */
public interface MultiLocatable extends Locatable {
    
    /**
     * Return the list of locations.
     * 
     * @return a list of locations, possibly empty but never null.
     */
    public List getLocations();
    
    /**
     * Add a location to the current list of locations.
     * <p>
     * Implementations are free to filter locations that can be added (e.g. {@link Location#UNKNOWN}),
     * and there is therefore no guarantee that the given location will actually be added to the list.
     * Filtered locations are silently ignored.
     * 
     * @param location the location to be added.
     */
    public void addLocation(Location location);

}
