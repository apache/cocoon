// Feedback Wizard Sample

cocoon.load("resource://org/apache/cocoon/components/jxforms/flow/jxForm.js");

function feedbackWizard(form) {
    var bean = {
        firstName: "Donald",
        lastName: "Duck",
        email: "donald_duck@disneyland.com",
        age: 5,
        number: 1,
        liveUrl: "http://",
        publish: true,
        hidden: true,
        count: 1,
        notes: "<your notes here>",
        favorite: ["http://xml.apache/org/cocoon", 
                   "http://jakarta.apache.org",
                   "http://www.google.com",
                   "http://www.slashdot.com",
                   "http://www.yahoo.com"],
        hobby: ["swim", "movies", "ski", "gym", "soccer"],
        allHobbies: [
            {
                key: "swim",
                value: "Swimming"
            },
            {
                key: "gym", 
                value: "Body Building"
            },
            {
                key: "ski", 
                value: "Skiing"
            },
            {
                key: "run", 
                value: "Running"
            },
            {  
                key: "football", 
                value: "Football"
            },
            {
                key: "read",
                value: "Reading" 
            },
            {
                key: "write",
                value: "Writing"
            },
            {
                key: "soccer:",
                value: "Soccer" 
            },
            {
                key: "blog",
                value: "Blogging" 
            }],
        role: ["Hacker", "Executive"],
        system: {
            os: "Unix",
            processor: "p4",
            ram: 512,
            servletEngine: "Tomcat",
            javaVersion: "1.3",
        }
    }

    form.setModel(bean);

    form.sendView("view/userIdentity.xml",
		  function(form) {
	var bean = form.getModel();
	print("I can also do validation in JavaScript");
	print("age = "+form.getValue("number(/age)"));
	print("role = "+bean.role);
	if (bean.age > 40) {
	    form.addViolation("/age", "Hey, you're too old");
	}
    });
    print("handling user identity");

    form.sendView("view/deployment.xml", 
		  function(form) {
        var bean = form.getModel();
        print("I can also do validation in JavaScript");
        if (bean.publish) {
            form.addViolation("/publish", "Sorry, I won't let you publish");
        }
    });
    print("handling deployment");

    form.sendView("view/system.xml");
    print("handling system");

    form.sendView("view/confirm.xml");
    print("handling confirm");

    form.finish("view/end.xml");
    print("done");
}
