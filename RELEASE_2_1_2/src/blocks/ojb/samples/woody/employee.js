cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

function employeeform_jdo(form) {
    var factory = cocoon.getComponent(Packages.org.apache.cocoon.ojb.jdo.components.JdoPMF.ROLE);
    var pm = factory.getPersistenceManager();

    if (pm == null)
        print("Error: Cannot get Persistent Manager\n");
    // Create a empty Bean
    var bean = new Packages.org.apache.cocoon.ojb.samples.Employee();
	// Fill some initial data to the bean
    //bean.setId("2");
    //bean.setDepartmentId("2");
    //bean.setName("Carlos Chávez");
   
    form.load(bean);
    form.showForm("jdo/woody/employee-form-display");
	form.save(bean);
	// Save the bean using JDO
	var tx = pm.currentTransaction();
	tx.begin();
	pm.makePersistence(bean);
	tx.commit();
	cocoon.releaseComponent(factory);
    // cocoon.request.setAttribute("employeeform", form.getWidget());
    cocoon.sendPage("jdo/woody/employee-form-success", {"employeeform": bean});
}
