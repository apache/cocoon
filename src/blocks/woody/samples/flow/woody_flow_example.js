cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody.js");

function form1(form) {
    var locale = determineLocale();
    var model = form.getModel();
    model.email = "bar@www.foo.com";
    model.somebool = true;
    model.account = 2;
    model.cowheight = 4;
    model.number1 = 1;
    model.number2 = 3;
    model.birthdate = new java.util.Date();
    
    model.contacts[0].firstname = "Jules";
    model.contacts[1].firstname =  "Lucien";
    model.contacts[2].firstname = "Chris";
    model.drinks = ["Jupiler", "Coca Cola"];

    form.show("form1-display-pipeline", formHandler, locale);
    print("visa="+model.visa);
    cocoon.sendPage("form1-success-pipeline");
    form.finish();

}

function formHandler(form) {
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
}

function determineLocale() {
    var localeParam = cocoon.request.get("locale");
    if (localeParam != null && localeParam.length > 0) {
        return Packages.org.apache.cocoon.i18n.I18nUtils.parseLocale(localeParam);
    }
    return null;
}

