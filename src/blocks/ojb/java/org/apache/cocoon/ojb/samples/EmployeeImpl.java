/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.ojb.samples;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.apache.cocoon.ojb.jdo.components.JdoPMF;
import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;
/**
 *  Department's Impl
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id: EmployeeImpl.java,v 1.3 2003/10/10 06:10:00 antonio Exp $
*/
public class EmployeeImpl {

	public EmployeeImpl(){}
	
	
	public void retrieve(Employee bean, JdoPMF pmf) {
		
		/* 1. Get the PersistenceManager */
		PersistenceManager persistenceManager = pmf.getPersistenceManager();
		
		Employee e = new Employee();
		e.setId(bean.getId());
		PersistenceBroker broker = PersistenceBrokerFactory.defaultPersistenceBroker();
		Identity oid = new Identity(e, broker);
		
		Employee b = new Employee();
		//	2. start transaction
		persistenceManager.currentTransaction().begin();
		// 3. Get the Object based on the primary key
		b = (Employee) persistenceManager.getObjectById(oid, false);
		// 4. Copy data to bean
		copyData(b, bean);
		// 5. End transaction
		persistenceManager.currentTransaction().commit();
	}
	
	public void insert(Employee e, JdoPMF pmf) {
		/* 1. Get the PersistenceManager */
		PersistenceManager persistenceManager = pmf.getPersistenceManager();
		// 2. Get current transaction
		Transaction tx = persistenceManager.currentTransaction();
		// 3. Start a Transaction
		tx.begin();
		// 4. now perform persistence operations. Store the Employee
		persistenceManager.makePersistent(e);
		// 5. Commit the transaction
		tx.commit();
	}
	
	private void copyData(Employee from, Employee to)
	{
		to.setId(from.getId());
		to.setDepartmentId(from.getDepartmentId());
		to.setName(from.getName());
	}
}
