package javax.jdo;

import java.util.Collection;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: PersistenceManager.java,v 1.3 2003/10/09 18:30:53 antonio Exp $
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
	abstract public void evictAll(Collection o);
	abstract public Extent getExtent(Class c, boolean b);
	abstract public boolean getIgnoreCache();
	abstract public boolean getMultiThreaded();
	abstract public Object getObjectById(Object o, boolean b);
	abstract public Object getObjectId(Object o);
	abstract public Class getObjectIdClass(Class c);
	abstract public PersistenceManagerFactory getPersistenceManagerFactory();
	abstract public Object getTransactionlaObjectId(Object o);
	abstract public Object getUserObjec();
	abstract public boolean isClosed();
	abstract public void makeNontransactional(Object o);
	abstract public void makeNontransactionalAll(Object[] o);
	abstract public void makeNontransactionalAll(Collection c);
	abstract public void makePersistent(Object O);
	abstract public void makePersistentAll(Object[] O);
	abstract public void makePersistentAll(Collection O);
	abstract public void makeTransactional(Object O);
	abstract public void makeTransactionalAll(Object[] O);
	abstract public void makeTransactionalAll(Collection O);
	abstract public void makeTransient(Object O);
	abstract public void makeTransientAll(Object[] O);
	abstract public void makeTransientAll(Collection O);
	abstract public Object newObjectIdInstance(Class c, String s);
	abstract public Query newQuery();
	abstract public Query newQuery(Class c);
	abstract public Query newQuery(Class c, String s);
	abstract public Query newQuery(Class c, Collection co);
	abstract public Query newQuery(Class c, Collection co, String s);
	abstract public Query newQuery(Object o);
	abstract public Query newQuery(String s, Object o);
	abstract public Query newQuery(Extent e);
	abstract public Query newQuery(Extent e, String s);
	abstract public void refresh(Object o);
	abstract public void refreshAll();
	abstract public void refreshAll(Object[] o);
	abstract public void refreshAll(Collection c);
	abstract public void retrieve(Object o);
	abstract public void retrieveAll(Object[] o);
	abstract public void retrieveAll(Collection c);
	abstract public void setIgnoreCache(boolean b);
	abstract public void setMultithreaded(boolean b);
	abstract public void setUserObject(Object o);
	
	
	
	
	
	
}

