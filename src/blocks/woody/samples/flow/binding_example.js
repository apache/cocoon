/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

// The purpose of the form2 example is to edit the contents of an XML file
// or a Java bean through a Woody form

// the form2 function is not directly called by the sitemap but by
// a generic "woody" function that instantiates the form based on
// parameters passed from the sitemap (see above loaded woody2.js file)
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

// bean variant of the binding sample
function form2bean(form) {
    var bean = new Packages.org.apache.cocoon.woody.samples.Form2Bean();

    // fill bean with some data to avoid users having to type to much
    bean.setEmail("yourname@yourdomain.com");
    bean.setIpAddress("10.0.0.1");
    bean.setPhoneCountry("32");
    bean.setPhoneZone("2");
    bean.setPhoneNumber("123456");
    bean.setBirthday(new java.util.Date());
    bean.setSex(Packages.org.apache.cocoon.woody.samples.Sex.FEMALE);
    var contact = new Packages.org.apache.cocoon.woody.samples.Contact();
    contact.setId("1");
    contact.setFirstName("Herman");
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
