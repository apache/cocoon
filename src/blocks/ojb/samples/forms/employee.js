/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js")

function employeeform_jdo(form) {
    // Get OJB factory
    var factory = cocoon.getComponent(Packages.org.apache.cocoon.ojb.jdo.components.JdoPMF.ROLE);

    // Create a empty Bean
    var bean = new Packages.org.apache.cocoon.ojb.samples.bean.Employee();
    var dao = new Packages.org.apache.cocoon.ojb.samples.EmployeeDAO();

    // Fill some initial data to the bean
    bean.setId(1);
    // Load bean based on the given PrimaryKey
    dao.retrieve(bean, factory);

    // Load the Bean to the form
    form.load(bean);
    // Let Cocoon Forms handle the form
    form.showForm("jdo/forms/employee-form-display");
    // Update the Bean based on user input
    form.save(bean);

    // Update Bean in Database
    dao.update(bean, factory);
    // Release the factory
    cocoon.releaseComponent(factory);

    // Send response to the user
    cocoon.request.setAttribute("employeeform", form.getWidget());
    cocoon.sendPage("jdo/forms/employee-form-success");
}
