package org.apache.cocoon.framework;

/**
 * This class implements the usual method to store and keep
 * the director reference for later use.
 * 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:17 $
 */

public abstract class AbstractActor implements Actor {

    protected Director director;
        
    /**
     * Initialize the actor by indicating their director.
     */
    public void init(Director director) {
        this.director = director;
    }
}