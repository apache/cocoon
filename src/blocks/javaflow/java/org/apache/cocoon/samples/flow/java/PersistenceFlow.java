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
import org.apache.cocoon.ojb.jdo.components.JdoPMF;
import org.apache.cocoon.ojb.samples.EmployeeDAO;
import org.apache.cocoon.ojb.samples.bean.Employee;
import org.apache.ojb.broker.*;
import org.apache.ojb.broker.query.*;

public class PersistenceFlow extends AbstractFormFlow {

    public void doInsertEmployee() throws BindingException {

			  JdoPMF factory = (JdoPMF)getComponent(JdoPMF.ROLE);

        // Create a empty Bean
        Employee employee = new Employee();
        EmployeeDAO dao = new EmployeeDAO();
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
        dao.insert(employee, factory);
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

        JdoPMF factory = (JdoPMF)getComponent(JdoPMF.ROLE);
				
        // Create a empty Bean
        Employee employee = new Employee();
        EmployeeDAO dao = new EmployeeDAO();
        // Fill some initial data to the bean
        employee.setId(id);
        // Load bean based on the given PrimaryKey
        dao.retrieve(employee, factory);
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
        dao.update(employee, factory);

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

				JdoPMF factory = (JdoPMF)getComponent(JdoPMF.ROLE);

        // Create a empty Bean
        Employee employee = new Employee();
        EmployeeDAO dao = new EmployeeDAO();
        // Fill some initial data to the bean
        employee.setId(id);
        // Remove bean
        dao.remove(employee, factory);
        // Send response to the user
        doShowEmployee();
    }

    public void doShowEmployee() {

			  JdoPMF factory = (JdoPMF)getComponent(JdoPMF.ROLE);

        // Query all objects
        Set results = query(new Criteria(), factory);
        // Send response to the user
        sendPage("page/employee-result", new VarMap().add("employee", results));
    }

    public Set query(Criteria criteria, JdoPMF pmf) {

        // 1. Get the PersistenceManager 
        PersistenceManager persistenceManager = pmf.getPersistenceManager();
        PersistenceBroker broker = PersistenceBrokerFactory.defaultPersistenceBroker();
        // 2. start transaction
        persistenceManager.currentTransaction().begin();
        // 3. Get objects based on query
        HashSet results = new HashSet();        
        QueryByCriteria query = new QueryByCriteria(Employee.class, criteria);
        for(Iterator i=broker.getCollectionByQuery(query).iterator(); i.hasNext();) {
            Employee b = (Employee)i.next();
            
            Employee e = new Employee();
            // 4. Copy data to bean
            copyData(b, e);

            results.add(e);
        }
        // 5. End transaction
        persistenceManager.currentTransaction().commit();

        return results;
    }

    private void copyData(Employee from, Employee to) {
        to.setId(from.getId());
        to.setDepartmentId(from.getDepartmentId());
        to.setName(from.getName());
    }

/*    public void dispose() {
        if (this.manager != null) {
            // Release the factory
            manager.release(factory);
        }
        super.dispose();
    }*/
}
