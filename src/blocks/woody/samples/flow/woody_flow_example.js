cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody.js");

function form1(form) {
    var model = form.getModel();
    model.email = "bar@www.foo.com";
    model.somebool = true;
    model.account = 2;
    model.cowheight = 4;
    model.number1 = 1;
    model.number2 = 3;
    model.birthdate = new Date();
    
    model.contacts[0].firstname = "Jules";
    model.contacts[1].firstname =  "Lucien";
    model.contacts[2].firstname = "Chris";
    model.drinks = ["Jupiler", "Coca Cola"];

    form.show("form1-display-pipeline", function(form) {
        print("submitId="+form.getSubmitId());
        switch(form.getSubmitId()) {
        case "remove-selected-contacts":
            {
                for (var i = model.contacts.length-1; i >= 0; i--) {
                    if (model.contacts[i].select) {
                        model.contacts.remove(i);
                    }
                }
            }
            break;
        case "add-contact":
            {
                model.contacts.length++;
            }
            break;
        default:
            return true;
        }
        return false;
    });
    print("visa="+model.visa);
    cocoon.sendPage("form1-success-pipeline");
    form.finish();

}

