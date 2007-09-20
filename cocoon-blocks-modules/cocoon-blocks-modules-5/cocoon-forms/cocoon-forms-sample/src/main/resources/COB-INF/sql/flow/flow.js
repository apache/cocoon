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

/**
 * CRUD operations on the "personnel" database that comes with Cocoon samples,
 * showing how a few conventions, Map and List wrappers for forms and JDBC allow
 * to easily connect forms and a database.
 *
 * @version $Id$
 */
cocoon.load("servlet:forms:/resource/internal/flow/javascript/Form.js");
cocoon.load("flow/jdbi.js");
importClass(org.apache.cocoon.forms.formmodel.WidgetState);

// Add asMap() and asList() methods to Form as a syntactic convenience
Form.prototype.asMap = function(widget) {
    if (!widget) widget = this.form;
    return new org.apache.cocoon.forms.util.ContainerWidgetAsMap(widget, true);
}

Form.prototype.asList = function(repeater) {
    return new org.apache.cocoon.forms.util.RepeaterAsList(repeater, true);
}

// The DAO object for our application
dao = function() {};

// The database interface for our DAO
dao.dbi = new JDBI("personnel");

// Factory for new IDs
dao.newId = function(table, column) {
    // WARNING: very dummy approach what can produce duplicate entries under load
    //     --> use another way in production (sequences, high/low generator, etc)
    var id = dao.dbi.first("select max(id)+1 as new_id from " + table).get("new_id");
    if (id == null) id = new java.lang.Integer(1);
    return id;
}

// Get the contents of a table as a selectionList (see dao.department below)
function selection_list() {
    var table = dao[cocoon.parameters.table];
    if (!table) throw "No table named " + table;
    cocoon.sendPage("selection-list.xml", { items: table.selectionList() });
}

//-------------------------------------------------------------------------------------------------
// The Data Access Object for the employees table.
// Defining DAOs is not mandatory, and SQL statements can be written directly in the flow
// functions. However, separating SQL from page flow control is a good practice. DAOs also share
// a common structure that allows entity-independent flow scenarios to be written.

dao.employee = function() {};

dao.employee.get = function(id) {
    return dao.dbi.first("select * from employee where id = ?", [id]);
};
    
dao.employee.getAll = function() {
    return dao.dbi.query("select * from employee order by name");
};

// Insert a new employee in the database
// This function (and other below) accepts a variable number of arguments, which should all be
// either JS objects of Maps. They are all combined to a single Map that defines the prepared
// statement parameters.
// The order of arguments is important as first parameters "hide" the properties held by those
// that follow.
dao.employee.insert = function() {
    var employee = JDBI.combine.apply(this, arguments);
    return dao.dbi.execute("insert into employee ( id,  department_id,  name,  description) " +
                                        " values (:id, :department_id, :name, :description)", employee);
};
    
dao.employee.update = function() {
    var employee = JDBI.combine.apply(this, arguments);
    return dao.dbi.execute("update employee set department_id = :department_id, name = :name, " +
        "description = :description where id = :id", employee);
};

// Deletion is named "remove" as "delete" is a JS keyword   
dao.employee.remove = function(id) {
    return dao.dbi.execute("delete from employee where id = ?", [id]);
};
    
// Now come the employee-related flow scenarios.

function do_list_employees() {
    var list = dao.employee.getAll();
    // Call the view, giving it the list and also the dao to get department names
    cocoon.sendPage("employee-list.html", { employees: list, dao: dao });
}

function do_edit_employee() {
    var id = cocoon.request.getParameter("id"); // Can be null for creation
    var form = new Form("forms/employee.xml");
    
    // Create a Map view of the form, that will be used with JDBI
    var formMap = form.asMap();
    
    if (id != null) {
        // Get the employee as a Map
        var employee = dao.employee.get(id);
        if (!employee) {
            // Should better display a dialog here
            throw "There is no customer with id " + id;
        }
        
        // Fill the form with the Map returned by JDBI.
        // No binding file, no Java object, no nothing! Cool, isn't it?
        //
        // This is possible because:
        // - both the JDBC ResultSet and the form widgets are represented as Maps and Lists
        // - form widgets and database columns have the same name.
        formMap.putAll(employee);
    }

    // Show the form
    form.showForm("viewform-employee-edit.html");

    if (form.isValid) {
        // User has not pressed "cancel": insert or update employee
        if (id == null) {
            id = dao.newId("employee");
            
            // And insert the employee.
            // Again, no binding file, no Java object, no nothing! Cool, isn't it?
            // All parameters will be combined to a single Map to feed the database.
            dao.employee.insert({id: id}, formMap);
        } else {
            // Use a shorter notation now
            dao.employee.update({id: id}, formMap);
        }
    }
    
    // Return to the list page
    cocoon.redirectTo("do_list_employees");
}

function do_delete_employee() {
    dao.employee.remove(cocoon.request.getParameter("id"));

    // Return to the list page
    cocoon.redirectTo("do_list_employees");
}

//-------------------------------------------------------------------------------------------------
dao.department = function() {};

dao.department.get = function(id) {
    return dao.dbi.first("select * from department where id = ?", [id]);
}

// Return a selection-list view of the departments. This is a list of (value, label) pairs
// that is later rendered to XML by a JXTemplate.
dao.department.selectionList = function() {
    return dao.dbi.query("select id as value, name as label from department order by name");
}
