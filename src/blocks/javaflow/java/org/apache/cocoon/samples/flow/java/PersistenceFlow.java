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

import org.apache.cocoon.components.flow.java.VarMap;
import org.apache.cocoon.forms.binding.*;
import org.apache.cocoon.forms.flow.java.AbstractFormFlow;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.ojb.broker.components.PBFactory;
import org.apache.cocoon.ojb.samples.EmployeeDAO;
import org.apache.cocoon.ojb.samples.bean.Employee;
import org.apache.ojb.broker.*;
import org.apache.ojb.broker.query.*;

public class PersistenceFlow extends AbstractFormFlow {

    public void doInsertEmployee() throws BindingException {

				PersistenceBroker broker = getPersistenceBroker();

        // Create a empty Bean
        Employee employee = new Employee();
        // Fill some initial data to the bean
        employee.setId(1);
        // Load form descriptor
        Form form = loadForm("forms/employee.xml");
        // Load form binding
        Binding binding = loadBinding("forms/employee-binding.xml");
        // Load the Bean to the form
        binding.loadFormFromModel(form, employee);
        // Let Cocoon Forms handle the form
        showForm(form, "form/employee");
        // Update the Bean based on user input
        binding.saveFormToModel(form, employee);
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

				PersistenceBroker broker = getPersistenceBroker();
				
        // Create a empty Bean
        Employee employee = new Employee();
        // Fill some initial data to the bean
        employee.setId(id);
        // Load bean based on the given PrimaryKey
				employee = (Employee) broker.getObjectByIdentity(new Identity(employee, broker));
        // Load form descriptor
        Form form = loadForm("forms/employee.xml");
        // Load form binding
        Binding binding = loadBinding("forms/employee-binding.xml");
        // Load the Bean to the form
        binding.loadFormFromModel(form, employee);
        // Let Cocoon Forms handle the form
        showForm(form, "form/employee");
        // Update the Bean based on user input
        binding.saveFormToModel(form, employee);

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

        PersistenceBroker broker = getPersistenceBroker();

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

        PersistenceBroker broker = getPersistenceBroker();

        // Query all objects
        Set results = new HashSet();
				QueryByCriteria query = new QueryByCriteria(Employee.class, new Criteria());
        for(Iterator i=broker.getCollectionByQuery(query).iterator(); i.hasNext();) {
            results.add(i.next());
				}
        // Send response to the user
        sendPage("page/employee-result", new VarMap().add("employee", results));
    }

    public PersistenceBroker getPersistenceBroker() {
        return ((PBFactory)getComponent(PBFactory.ROLE)).defaultPersistenceBroker();
		}
}
