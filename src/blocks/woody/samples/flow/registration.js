cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

function registration() {
    var form = new Form("forms/registration.xml");

    // The showForm function will keep redisplaying the form until
    // everything is valid
    form.showForm("registration-display-pipeline");

    var model = form.getModel();
    var bizdata = { "username" : model.name }
    cocoon.sendPage("registration-success-pipeline.jx", bizdata);
}
