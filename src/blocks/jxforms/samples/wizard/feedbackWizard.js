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
// Feedback Wizard Sample

cocoon.load("resource://org/apache/cocoon/components/jxforms/flow/javascript/JXForm.js");

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

    form.sendView("wizard/userIdentity.xml",
                  function(form) {
        var bean = form.getModel();
        cocoon.log.info("I can also do validation in JavaScript");
        cocoon.log.info("age = "+form.getValue("number(/age)"));
        cocoon.log.info("role = "+bean.role);
        if (bean.age > 40) {
            form.addViolation("/age", "Hey, you're too old");
        }
    });
    cocoon.log.info("handling user identity");

    form.sendView("wizard/deployment.xml", 
                  function(form) {
        var bean = form.getModel();
        cocoon.log.info("I can also do validation in JavaScript");
        if (bean.publish) {
            form.addViolation("/publish", "Sorry, I won't let you publish");
        }
    });
    cocoon.log.info("handling deployment");

    form.sendView("wizard/system.xml");
    cocoon.log.info("handling system");

    form.sendView("wizard/confirm.xml");
    cocoon.log.info("handling confirm");

    form.finish("wizard/end.xml");
    cocoon.log.info("done");
}
