package javax.jdo;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: PersistenceManagerFactory.java,v 1.1 2003/09/28 04:31:11 antonio Exp $
 */

import javax.jdo.PersistenceManager;

abstract public interface PersistenceManagerFactory {

    public PersistenceManager getPersistenceManager();
}
 
