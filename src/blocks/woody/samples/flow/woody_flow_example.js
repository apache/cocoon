cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

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

    form.locale = locale;
    form.showForm("form1-display-pipeline");
    print("submitId = " + form.submitId);
    if (form.isValid) {
      print("visa=" + model.visa);  
    } else {
      print("Form is not valid");
    }
    // also store the form as a request attribute as the XSP isn't flow-aware
    cocoon.request.setAttribute("form1", form.getWidget());
    cocoon.sendPage("form1-success-pipeline.xsp");
}

function selectCar() {
    var form = new Form("forms/carselector_form.xml");
    form.getWidget("make").setValue(cocoon.parameters.defaultMake);
    form.showForm("carselector-display-pipeline");
    cocoon.request.setAttribute("carselectorform", form.getWidget());
    cocoon.sendPage("carselector-success-pipeline.xsp");
}

var states = [
    { key: "AL", value: "Alabama" },
    { key: "AK", value: "Alaska" },
    { key: "WY", value: "Wyoming" }
];

var countries = [
    { key: "ad", value: "Andorra, Principality of" },
    { key: "zw", value: "Zimbabwe" }
];

function selectCountry() {
    var form = new Form("forms/countryselector_form.xml");
    form.showForm("countryselector-display-pipeline");
    cocoon.request.setAttribute("countryselectorform", form.getWidget());
    cocoon.sendPage("countryselector-success-pipeline.xsp");
}

function determineLocale() {
    var localeParam = cocoon.request.get("locale");
    if (localeParam != null && localeParam.length > 0) {
        return Packages.org.apache.cocoon.i18n.I18nUtils.parseLocale(localeParam);
    }
    return null;
}

