cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

function htmlarea() {
    var form = new Form("forms/htmlarea.xml");

    form.showForm("htmlarea-display-pipeline");

    var model = form.getModel();
    var htmldata = { "data" : model.data }
    cocoon.sendPage("htmlarea-success-pipeline", htmldata);
}
