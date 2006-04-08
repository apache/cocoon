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
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

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

    var renderMode = cocoon.parameters["renderMode"];

    form.locale = locale;
    form.showForm("form1-display-pipeline." + renderMode);

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
    form.lookupWidget("make").setValue(cocoon.parameters.defaultMake);
    form.showForm("carselector-display-pipeline.jx");
    cocoon.request.setAttribute("carselectorform", form.getWidget());
    cocoon.sendPage("carselector-success-pipeline.xsp", {
        make: form.lookupWidget("make").value,
	type: form.lookupWidget("type").value,
	model: form.lookupWidget("model").value}
    );
}

function xhrSelectCar() {
    var form = new Form("forms/xhr_carselector_form.xml");
    // form.lookupWidget("make").setValue(cocoon.parameters.defaultMake);
    form.showForm("xhr_carselector-display-pipeline");
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
    if (localeParam != null && localeParam.length() > 0) {
        return Packages.org.apache.cocoon.i18n.I18nUtils.parseLocale(localeParam);
    }
    return null;
}

function do_dynaRepeater() {
    var form = new Form("forms/dynamicrepeater.xml");
    form.setAttribute("counter", new java.lang.Integer(0));
    form.showForm("dynamicrepeater-display-pipeline.jx");
    
    var doc = Packages.javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    doc.appendChild(doc.createElement("contacts"));
    form.createBinding("forms/dynamicrepeater_binding.xml");
    
    form.save(doc);
    cocoon.sendPage("xmlresult-display-pipeline.jx",
        {title: "Contact list", document: doc}
    );
}

function do_dojoRepeater() {
    var form = new Form("forms/dynamicrepeater.xml");
    form.setAttribute("counter", new java.lang.Integer(0));
    form.getChild("addcontact").performAction(); // to increment the counter
    form.showForm("dynamicrepeater_dojo-display-pipeline.jx");
    
    var doc = Packages.javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    doc.appendChild(doc.createElement("contacts"));
    form.createBinding("forms/dynamicrepeater_binding.xml");
    
    form.save(doc);
    cocoon.sendPage("xmlresult-display-pipeline.jx",
        {title: "Contact list", document: doc}
    );
}

function do_datasourceChooser() {
    var form = new Form("forms/datasource_chooser.xml");
    form.showForm("datasource_chooser-display-pipeline.jx");
    
    var doc = Packages.javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    doc.appendChild(doc.createElement("config"));
    form.createBinding("forms/datasource_chooser_binding.xml");
    form.save(doc);
    
    cocoon.sendPage("xmlresult-display-pipeline.jx",
        {title: "Here's the datasource definition", document: doc}
    );
}

function do_taskTree() {
    var form = new Form("forms/tasktree.xml");
    form.showForm("tasktree-display-pipeline.jx");
    
    var doc = Packages.javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    doc.appendChild(doc.createElement("project"));
    form.createBinding("forms/tasktree_binding.xml");
    form.save(doc);
    
    cocoon.sendPage("xmlresult-display-pipeline.jx",
        {title: "Here's the resulting project definition", document: doc}
    );
}

// import WidgetState for direct access in event handlers.
// FIXME(SW) would be better to import it implicitely within Forms.js
importClass(org.apache.cocoon.forms.formmodel.WidgetState);

function do_multipage() {
    var form = new Form("forms/multipage_model.xml");
    form.showForm("multipage-display-pipeline.jx");
    
    var doc = Packages.javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    doc.appendChild(doc.createElement("result"));
    form.createBinding("forms/multipage_binding.xml");
    form.save(doc);
    
    cocoon.sendPage("xmlresult-display-pipeline.jx",
        {title: "Here's the resulting document", document: doc}
    );
}

function do_fileExplorer() {
    var form = new Form("forms/file_explorer_model.xml");
    form.showForm("file_explorer-display-pipeline.jx");
    cocoon.sendPage("xmlresult-display-pipeline.jx",
        {title: "Tree binding is not yet done", document: null}
    );
}
function do_sampleTree() {
    var form = new Form("forms/sampletree_model.xml");
    var resolver = cocoon.getComponent(org.apache.excalibur.source.SourceResolver.ROLE);
    var model = new org.apache.cocoon.forms.formmodel.tree.SourceTreeModel(resolver, "context://samples/blocks/forms");
    form.getChild("files").setModel(model);
    form.showForm("sampletree-display-pipeline.jx");
    cocoon.sendPage("xmlresult-display-pipeline.jx",
        {title: "Tree binding is not yet done", document: null}
    );
}

function do_suggest() {
    var form = new Form("forms/ajax_suggest_form.xml");
    var path = form.getChild("path");

    form.showForm("ajax_suggest-display-pipeline.jx");
    
    cocoon.sendPage("textresult-display-pipeline.jx",
        {title: "Suggest results", text: "path value = " + path.value +
              "\npath suggested label = " +  
              (path.suggested ? path.suggestionLabel : "(none)") });
}

function do_inplace() {
    var form = new Form("forms/inplace_edit_form.xml");
    var path = form.getChild("path");

    form.showForm("inplace_edit-display-pipeline.jx");
    
    cocoon.sendPage("textresult-display-pipeline.jx",
        {title: "Inplace results", text: "name: " + form.getChild("name").value +
            "\noccupation: " + form.getChild("occupation").value +
            "\ncomments: " + form.getChild("comments") });
}
