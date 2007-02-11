/*
 * Created on 08-oct-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package javax.jdo;

/**
 * @author agallardo
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
abstract public interface Transaction {
	abstract public void begin();
	abstract public void commit();
	abstract public boolean getNonTransactionlaRead();
	abstract public boolean getNonTransactionlaWrite();
	abstract public boolean getOptimistic();
	abstract public PersistenceManager getPersistenceManager();
	abstract public boolean getRestoreValues();
	abstract public boolean getRetainValues();
//	abstract public Synchronization getSynchronization();
	abstract public boolean isActive();
	abstract public void rollback();
	abstract public void setNontransactionalRead(boolean b);
	abstract public void setNontransactionalWrite(boolean b);
	abstract public void setOptimistic(boolean b);
	abstract public void setRestoreValues(boolean b);
	abstract public void setRetainValues(boolean b);
//	abstract public void setSynchronization(Synchronization s);
}

