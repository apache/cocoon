/*
 * Created on 08-oct-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.cocoon.ojb.samples;

import javax.jdo.PersistenceManager;
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
	
	public Employee loadEmployee() {
		return emp;
	}
	
	public void saveEmployee(Employee e, JdoPMF pmf) {
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
