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

cocoon.load("servlet:forms:/resource/internal/flow/javascript/Form.js");

function show_form1(form) {
    process_form(form, "form1", "forms/form1_data.xml");
}

function show_form2(form) {
    process_form(form, "form2", "forms/form2_data.xml");
}

function show_hotel(form) {
    process_form(form, "hotel", "forms/hotel_data.xml");
}

function process_form(form, type, input) {

    var locale = determineLocale();
    var model = form.getModel();
    form.locale = locale;

    
    // parse the document to a DOM-tree
    var document = loadDocument(input);

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


    // bind the form's data back to the document
    form.save(document);

    // save the DOM-tree back to an XML file, the makeTargetURI
    // function makes a modified filename so that the
    // original document is not overwritten
    //saveDocument(document, makeTargetURI(documentURI));

    // show the xml generated from the form
    var bizdata = { "document" : document };
    cocoon.sendPage(type + "-success-pipeline.jx", bizdata);
}

function determineLocale() {
    var localeParam = cocoon.request.get("locale");
    if (localeParam != null && localeParam.length > 0) {
        return Packages.org.apache.cocoon.i18n.I18nUtils.parseLocale(localeParam);
    }
    return null;
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
