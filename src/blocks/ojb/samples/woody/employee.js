cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

function employeeform_jdo(form) {
    // Create a empty Bean
    var bean = new Packages.org.apache.cocoon.ojb.samples.Employee();
	// Fill some initial data to the bean
    bean.setId("35");
    bean.setDepartmentId("2");
    bean.setName("Bernardo Robelo");
    // Load the Bean to the form
    form.load(bean);
    // Let woody handle the form
    form.showForm("jdo/woody/employee-form-display");
    // Update the Bean based on user input
	form.save(bean);
	var factory = cocoon.getComponent(Packages.org.apache.cocoon.ojb.jdo.components.JdoPMF.ROLE);
    // Get persistent Manager
	var dbManage = Packages.org.apache.cocoon.ojb.samples.EmployeeImpl();
	dbManage.saveEmployee(bean, factory);
	cocoon.releaseComponent(factory);
    cocoon.request.setAttribute("employeeform", form.getWidget());
    cocoon.sendPage("jdo/woody/employee-form-success");
}
