/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.ojb.samples;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.apache.cocoon.ojb.jdo.components.JDO;
import org.apache.cocoon.ojb.samples.bean.Employee;
import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;

/**
 *  Employee's Impl
 *
 * @version $Id$
*/
public class EmployeeDAO {

    public EmployeeDAO(){}

    public void retrieve(Employee bean, JDO jdo) {
        // 1. Get the PersistenceManager
        PersistenceManager persistenceManager = jdo.getPersistenceManager();
        
        Employee e = new Employee();
        e.setId(bean.getId());
        PersistenceBroker broker = PersistenceBrokerFactory.defaultPersistenceBroker();
        Identity oid = new Identity(e, broker);
        
        Employee b = new Employee();
        // 2. start transaction
        persistenceManager.currentTransaction().begin();
        // 3. Get the Object based on the primary key
        b = (Employee) persistenceManager.getObjectById(oid, false);
        // 4. Copy data to bean
        copyData(b, bean);
        // 5. End transaction
        persistenceManager.currentTransaction().commit();
    }

    public void insert(Employee e, JDO jdo) {
        // 1. Get the PersistenceManager
        PersistenceManager persistenceManager = jdo.getPersistenceManager();
        // 2. Get current transaction
        Transaction tx = persistenceManager.currentTransaction();
        // 3. Start a Transaction
        tx.begin();
        // 4. now perform persistence operations. Store the Employee
        persistenceManager.makePersistent(e);
        // 5. Commit the transaction
        tx.commit();
    }

    public void update(Employee bean, JDO jdo) {
        // 1. Get the PersistenceManager
        PersistenceManager persistenceManager = jdo.getPersistenceManager();
        
        Employee e = new Employee();
        e.setId(bean.getId());
        PersistenceBroker broker = PersistenceBrokerFactory.defaultPersistenceBroker();
        Identity oid = new Identity(e, broker);
        
        Employee b = new Employee();
        // 2. start transaction
        persistenceManager.currentTransaction().begin();
        // 3. Get the Object based on the primary key
        b = (Employee) persistenceManager.getObjectById(oid, false);
        // 4. Copy data from bean
        copyData(bean, b);
        // Store to database
        // persistenceManager.makePersistent(b);
        // 5. End transaction
        persistenceManager.currentTransaction().commit();
    }

    public void remove(Employee bean, JDO jdo) {
        // 1. Get the PersistenceManager
        PersistenceManager persistenceManager = jdo.getPersistenceManager();
        
        Employee e = new Employee();
        e.setId(bean.getId());
        PersistenceBroker broker = PersistenceBrokerFactory.defaultPersistenceBroker();
        Identity oid = new Identity(e, broker);
        
        Employee b = new Employee();
        // 2. start transaction
        persistenceManager.currentTransaction().begin();
        // 3. Get the Object based on the primary key
        b = (Employee) persistenceManager.getObjectById(oid, false);
        // Delete in the database
        persistenceManager.deletePersistent(b);
        // 5. End transaction
        persistenceManager.currentTransaction().commit();
    }

    private void copyData(Employee from, Employee to) {
        to.setId(from.getId());
        to.setDepartmentId(from.getDepartmentId());
        to.setName(from.getName());
    }
}
