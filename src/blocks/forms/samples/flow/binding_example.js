/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

// The purpose of the form2 example is to edit the contents of an XML file
// or a Java bean through a Cocoon form

// the form2 function is not directly called by the sitemap but by
// a generic "forms" function that instantiates the form based on
// parameters passed from the sitemap (see above loaded forms.js file)
function form2xml(form) {
    // get the documentURI parameter from the sitemap which contains the
    // location of the file to be edited
    var documentURI = cocoon.parameters["documentURI"];

    // parse the document to a DOM-tree
    var document = loadDocument(documentURI);

    // bind the document data to the form
    form.load(document);

    // show the form to the user until it is validated successfully
    form.showForm("form2-display-pipeline");

    // bind the form's data back to the document
    form.save(document);

    // save the DOM-tree back to an XML file, the makeTargetURI
    // function makes a modified filename so that the
    // original document is not overwritten
    saveDocument(document, makeTargetURI(documentURI));

    cocoon.sendPage("form2-success-pipeline");
}

function form2simpleXML(form) {
    // get the documentURI parameter from the sitemap which contains the
    // location of the file to be edited
    var documentURI = cocoon.parameters["documentURI"];

    // get the XML adapter
    var xmlAdapter = form.getXML();

    // parse the document to a widget tree
    var pipeUtil = 
        cocoon.createObject(Packages.org.apache.cocoon.components.flow.util.PipelineUtil);
    pipeUtil.processToSAX(documentURI, null, xmlAdapter);

    // show the form to the user until it is validated successfully
    form.showForm("form2-display-pipeline");

    // show the xml generated from the form
    cocoon.sendPage("form2simpleXML-success-pipeline", xmlAdapter);
}

// bean variant of the binding sample
function form2bean(form) {
    var bean = new Packages.org.apache.cocoon.forms.samples.Form2Bean();

    // fill bean with some data to avoid users having to type to much
    bean.setEmail("yourname@yourdomain.com");
    bean.setIpAddress("10.0.0.1");
    bean.setPhoneCountry("32");
    bean.setPhoneZone("2");
    bean.setPhoneNumber("123456");
    bean.setBirthday(new java.util.Date());
    bean.setSex(Packages.org.apache.cocoon.forms.samples.Sex.FEMALE);
    var contact = new Packages.org.apache.cocoon.forms.samples.Contact();
    contact.setId("1");
    contact.setFirstName("Hermann");
    bean.addContact(contact);
    
    form.load(bean);
    form.showForm("form2-display-pipeline");
    form.save(bean);

    cocoon.sendPage("form2bean-success-pipeline", { "form2bean": bean });
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
