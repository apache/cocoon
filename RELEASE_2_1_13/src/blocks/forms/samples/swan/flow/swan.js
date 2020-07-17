/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/*
* @version $Id$
*/

  importPackage(org.apache.cocoon.forms.formmodel);

cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

function binding_gui(form) {
    editor_gui(form, "binding");
}

function model_gui(form) {
    editor_gui(form, "model");
}

function sitemap_gui(form) {
    editor_gui(form, "sitemap");
}

function template_gui(form) {
    editor_gui(form, "template");
}

function xreport_gui(form) {
    editor_gui(form, "xreport");
}

function editor_gui(form, type) {

    var locale = determineLocale();
    var model = form.getModel();
    form.locale = locale;

    // get the documentURI parameter from the sitemap which
    // contains the location of the file to be edited
    var documentURI = cocoon.parameters["documentURI"];

    // Yes, this is a hack, but it closes
    // the read-any-file security hole so
    // that this can work out-of-the-box.
    var allowed_files = [
      "report1.xml", "sitemap.xmap",
      "form_model_gui_binding.xml", "form_model_gui_data.xml",
      "form_model_gui_template_data.xml",
      "sample_form_1.xml", "sample_form_1_template.xml",
      "sample_form_2.xml", "sample_form_2_template.xml"
    ];

    if (!present(documentURI, allowed_files)) {
      cocoon.sendPage(type + "-error-pipeline");
      return;
    }

    // prepend data directory
    documentURI = "data/" + documentURI;

    // parse the document to a DOM-tree
    var document = loadDocument(documentURI);

    // bind the document data to the form
    form.load(document);

    // show the form
    form.showForm(type + "-display-pipeline");
    print("submitId = " + form.submitId);
    if (form.isValid) {
      print("Form is valid");  
    } else {
      print("Form is not valid");
    }

    // Clear the old document
    clean_node(document);

    // bind the form's data back to the document
    form.save(document);

    // save the DOM-tree back to an XML file, the makeTargetURI
    // function makes a modified filename so that the
    // original document is not overwritten
    saveDocument(document, makeTargetURI(documentURI));

    // also store the form as a request attribute as the XSP isn't flow-aware
    cocoon.request.setAttribute("form_" + type + "_gui", form.getWidget());
    cocoon.sendPage(type + "-success-pipeline.jx");
}

function present(string, list) {
  for (var i = 0; i < list.length; i++)
    if (string == list[i]) return true;
  return false;
}

function clean_node(node) {
    var child = node.getFirstChild();
    while(child != null) {
      var type = child.getNodeType();
      var next = child.getNextSibling();
      //if(type == org.w3c.dom.Node.COMMENT_NODE) node.removeChild(child);
      if(type == org.w3c.dom.Node.TEXT_NODE) node.removeChild(child);
      if(type == org.w3c.dom.Node.ELEMENT_NODE) clean_node(child);
      if(type == org.w3c.dom.Node.DOCUMENT_NODE) clean_node(child);
      child = next;
    }
}

function determineLocale() {
    var localeParam = cocoon.request.get("locale");
    if (localeParam != null && localeParam.length > 0) {
        return Packages.org.apache.cocoon.i18n.I18nUtils.parseLocale(localeParam);
    }
    return null;
}

/**
 * Translate source path into target path so we keep a clean source XML.
 */
function makeTargetURI(path) {
    var sfx = ".xml";
    var newSfx = "-result.xml";
    var newPath = path;
    if (path.match(/^.*\.xml$/)) {
        newPath = path.substring(0, path.length - ".xml".length);
    }
    return newPath + newSfx;
}

function loadDocument(uri) {
    var parser = null;
    var source = null;
    var resolver = null;
    try {
        parser = cocoon.getComponent(Packages.org.apache.excalibur.xml.dom.DOMParser.ROLE);
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        source = resolver.resolveURI(uri);
        var is = new Packages.org.xml.sax.InputSource(source.getInputStream());
        is.setSystemId(source.getURI());
        return parser.parseDocument(is);
    } finally {
        if (source != null)
            resolver.release(source);
        cocoon.releaseComponent(parser);
        cocoon.releaseComponent(resolver);
    }
}

function saveDocument(document, uri) {
    var source = null;
    var resolver = null;
    var outputStream = null;
    try {
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        source = resolver.resolveURI(uri);

        var tf = Packages.javax.xml.transform.TransformerFactory.newInstance();

        if (source instanceof Packages.org.apache.excalibur.source.ModifiableSource
            && tf.getFeature(Packages.javax.xml.transform.sax.SAXTransformerFactory.FEATURE)) {

            outputStream = source.getOutputStream();
            var transformerHandler = tf.newTransformerHandler();
            var transformer = transformerHandler.getTransformer();
            transformer.setOutputProperty(Packages.javax.xml.transform.OutputKeys.INDENT, "true");
            transformer.setOutputProperty(Packages.javax.xml.transform.OutputKeys.METHOD, "xml");
            transformerHandler.setResult(new Packages.javax.xml.transform.stream.StreamResult(outputStream));

            var streamer = new Packages.org.apache.cocoon.xml.dom.DOMStreamer(transformerHandler);
            streamer.stream(document);
        } else {
            throw new Packages.org.apache.cocoon.ProcessingException("Cannot write to source " + uri);
        }
    } finally {
        if (source != null)
            resolver.release(source);
        cocoon.releaseComponent(resolver);
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (error) {
                cocoon.log.error("Could not flush/close outputstream: " + error);
            }
        }
    }
}
