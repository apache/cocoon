cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

function employeeform_jdo(form) {
    // Create a empty Bean

   	var factory = cocoon.getComponent(Packages.org.apache.cocoon.ojb.jdo.components.JdoPMF.ROLE);
    var bean = new Packages.org.apache.cocoon.ojb.samples.Employee();
    var ojbEmployee = Packages.org.apache.cocoon.ojb.samples.EmployeeImpl();
	// Fill some initial data to the bean
    // Load bean based on the given PrimaryKey
    bean = ojbEmployee.load(1, factory);

    // Load the Bean to the form
    form.load(bean);
    // Let woody handle the form
    form.showForm("jdo/woody/employee-form-display");
    // Update the Bean based on user input
	form.save(bean);

    // Save Bean in Database
	ojbEmployee.save(bean, factory);
	cocoon.releaseComponent(factory);
    cocoon.request.setAttribute("employeeform", form.getWidget());
    cocoon.sendPage("jdo/woody/employee-form-success");
}
