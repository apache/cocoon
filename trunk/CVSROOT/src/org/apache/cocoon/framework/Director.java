package org.apache.cocoon.framework;

import java.util.*;

/**
 * A Director is an actor manager. Actors refer to their director to
 * get the actor associated to the respective role. This is useful
 * to decouple the acting role from the actual class performing
 * that part in the play.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:18 $
 */

public interface Director {

    /**
     * Get the actor currently playing the given role.
     */
    Actor getActor(String role);

    /**
     * Set the actor for the role.
     */
    void setRole(String role, Actor actor);
    
    /**
     * Get the roles currently set.
     */
    Enumeration getRoles();

}