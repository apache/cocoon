// XML Form Feedback Wizard Application

cocoon.load("resource://org/apache/cocoon/components/flow/javascript/xmlForm.js");

function feedbackWizard(xform) {
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

    xform.setModel(bean);

    xform.sendView("userIdentity", 
                   "flow/userIdentity.xml",
                   function(xform) {
        var bean = xform.getModel();
        print("I can also do validation in JavaScript");
        print("age = "+xform.getValue("number(/age)"));
        print("role = "+bean.role);
        if (bean.age > 40) {
            xform.addViolation("/age", "Hey, you're too old");
        }
    });
    print("handling user identity");

    xform.sendView("deployment", 
                   "flow/deployment.xml", 
                   function(xform) {
        var bean = xform.getModel();
        print("I can also do validation in JavaScript");
        if (bean.publish) {
            xform.addViolation("/publish", "Sorry, I won't let you publish");
        }
    });
    print("handling deployment");

    xform.sendView("system", "flow/system.xml");
    print("handling system");

    xform.sendView("confirm", "flow/confirm.xml");
    print("handling confirm");

    xform.finish("end", "flow/end.xml");
    print("done");
}
