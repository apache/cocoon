package javax.jdo;

import java.util.Collection;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: PersistenceManager.java,v 1.2 2003/10/08 16:42:43 antonio Exp $
 */
abstract public interface PersistenceManager {
	abstract void close();
	abstract public Transaction currentTransaction();
	abstract public void deletePersistent(Object o);
	abstract public void deletePersistentAll(Object[] o);
	abstract public void deletePersistentAll(Collection c);
	abstract public void evict(Object o);
	abstract public void evictAll();
	abstract public void evictAll(Object[] o);
	abstract public void evict(Collection o);
	
	
	abstract public void makePersistent(Object O);
}

