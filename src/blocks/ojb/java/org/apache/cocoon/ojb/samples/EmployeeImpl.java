/*
 * Created on 08-oct-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.cocoon.ojb.samples;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.cocoon.ojb.jdo.components.JdoPMF;

/**
 * @author agallardo
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EmployeeImpl {
	private Employee emp;
	
	public EmployeeImpl(){}
	
	
	public Employee load(int id, JdoPMF pmf) {
		String filter = "employee.id ==" + id;
		/* 1. Get the PersistenceManager */
		PersistenceManager persistenceManager = pmf.getPersistenceManager();
		Extent extent = persistenceManager.getExtent(Employee.class, true);
		Query query = persistenceManager.newQuery(extent, filter);
		query.setOrdering("employee.id ascending");
		Collection result = (Collection)query.execute();
		Iterator iter = result.iterator();
		while(iter.hasNext()) {
			Employee e = (Employee) iter.next();
			if (id == e.getId())
				return e;
		}
		return null;
	}
	
//	public Employee load(int id, JdoPMF pmf) {
//		
//		/* 1. Get the PersistenceManager */
//		PersistenceManager persistenceManager = pmf.getPersistenceManager();
//		
//		Employee e = new Employee();
//		e.setId(id);
//		PersistenceBroker broker = PersistenceBrokerFactory.defaultPersistenceBroker();
//		Identity oid = new Identity(e, broker);
//		
//		Employee toBeReturned = new Employee();
//		//	2. start transaction
//		persistenceManager.currentTransaction().begin();
//		// 3. Get the Object based on the primary key
//		toBeReturned = (Employee) persistenceManager.getObjectById(oid, false);
//		// 4. End transaction
//		persistenceManager.currentTransaction().commit();
//		return toBeReturned;
//	}
	
	public void save(Employee e, JdoPMF pmf) {
		// Setting up the Bean 
		emp = e;
		/* 1. Get the PersistenceManager */
		PersistenceManager persistenceManager = pmf.getPersistenceManager();
		// 2. Get current transaction
		Transaction tx = persistenceManager.currentTransaction();
		// 3. Start a Transaction
		tx.begin();
		// 4. now perform persistence operations. Store the Employee
		persistenceManager.makePersistent(emp);
		// 5. Commit the transaction
		tx.commit();
	}
}
