package org.apache.cocoon.framework;

/**
 * This interface must be implemented by all <i>acting</i> classes. 
 * These are those classes that must be aware of other actors in
 * in order to complete their jobs.
 * 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:17 $
 */

public interface Actor {
    
    /**
     * Initialize the actor by indicating their director.
     */
    void init(Director director);
    
}