cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

function employeeform_jdo(form) {

	// Get OJB factory
   	var factory = cocoon.getComponent(Packages.org.apache.cocoon.ojb.jdo.components.JdoPMF.ROLE);

    // Create a empty Bean
    var bean = new Packages.org.apache.cocoon.ojb.samples.Employee();
    var ojbEmployee = Packages.org.apache.cocoon.ojb.samples.EmployeeImpl();

	// Fill some initial data to the bean
	bean.setId(1);
    // Load bean based on the given PrimaryKey
    ojbEmployee.retrieve(bean, factory);

    // Load the Bean to the form
    form.load(bean);
    // Let woody handle the form
    form.showForm("jdo/woody/employee-form-display");
    // Update the Bean based on user input
	form.save(bean);

    // Insert Bean in Database
	ojbEmployee.insert(bean, factory);
	// Clean up the operation
	cocoon.releaseComponent(factory);

    // Send response to the user
    cocoon.request.setAttribute("employeeform", form.getWidget());
    cocoon.sendPage("jdo/woody/employee-form-success");
}
