/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.samples.flow.java;

import java.util.*;

import javax.jdo.PersistenceManager;

import org.apache.cocoon.components.flow.java.*;
import org.apache.cocoon.forms.binding.*;
import org.apache.cocoon.forms.flow.java.*;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.ojb.broker.components.PBFactory;
import org.apache.cocoon.ojb.samples.EmployeeDAO;
import org.apache.cocoon.ojb.samples.bean.Employee;
import org.apache.ojb.broker.*;
import org.apache.ojb.broker.query.*;

public class PersistenceFlow extends AbstractContinuable {

    private transient PersistenceBroker broker = null;

    public PersistenceFlow() {
        PBFactory factory = (PBFactory)getComponent(PBFactory.ROLE);
        broker = factory.defaultPersistenceBroker();
        releaseComponent(factory);
    }

    public void doInsertEmployee() throws BindingException {

        // Create a empty Bean
        Employee employee = new Employee();
        // Fill some initial data to the bean
        employee.setId(1);
        // Load form descriptor
        FormInstance form = new FormInstance("forms/employee.xml");
        // Load form binding
        form.createBinding("forms/employee-binding.xml");
        // Load the Bean to the form
        form.load(employee);
        // Let Cocoon Forms handle the form
        form.show("form/employee");
        // Update the Bean based on user input
        form.save(employee);
        // Update Bean in Database
        broker.store(employee);
        // Send response to the user
        doShowEmployee();
    }

    public void doUpdateEmployee() throws BindingException {

        // Get id as parameter
        int id = 1;
        if (getRequest().getParameter("id")!=null)
            id = Integer.parseInt(getRequest().getParameter("id")); 
        else
            throw new IllegalStateException("No parameter 'id'");

        // Create a empty Bean
        Employee employee = new Employee();
        // Fill some initial data to the bean
        employee.setId(id);
        // Load bean based on the given PrimaryKey
        employee = (Employee) broker.getObjectByIdentity(new Identity(employee, broker));
        // Load form descriptor
        FormInstance form = new FormInstance("forms/employee.xml");
        // Load form binding
        form.createBinding("forms/employee-binding.xml");
        // Load the Bean to the form
        form.load(employee);
        // Let Cocoon Forms handle the form
        form.show("form/employee");
        // Update the Bean based on user input
        form.save(employee);

        // Update Bean in Database
        broker.store(employee);

        // Send response to the user
        doShowEmployee();
    }

    public void doRemoveEmployee() {

        // Get id as parameter
        int id = 1;
        if (getRequest().getParameter("id")!=null)
            id = Integer.parseInt(getRequest().getParameter("id"));
        else
            throw new IllegalStateException("No parameter 'id'");

        // Create a empty Bean
        Employee employee = new Employee();
        // Fill some initial data to the bean
        employee.setId(id);
        // Load bean based on the given PrimaryKey
        employee = (Employee) broker.getObjectByIdentity(new Identity(employee, broker));
        // Remove bean
        broker.delete(employee);
        // Send response to the user
        doShowEmployee();
    }

    public void doShowEmployee() {

        // Query all objects
        ArrayList results = new ArrayList();
        QueryByCriteria query = new QueryByCriteria(Employee.class, new Criteria());
        for(Iterator i=broker.getCollectionByQuery(query).iterator(); i.hasNext();) {
            results.add(i.next());
        }
        // Sort result
        Collections.sort(results, new EmployeeComparator());
        // Send response to the user
        sendPage("page/employee-result", new VarMap().add("employee", results));
    }

    public class EmployeeComparator implements Comparator, Continuable {
        public int compare(Object o1, Object o2) {
            return ((Employee)o1).getId()-((Employee)o2).getId();
        }
      
        public boolean equals(Object obj) {
            return true;
        }
    }
}
