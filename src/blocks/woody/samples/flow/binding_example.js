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
cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody.js");

// The purpose of the form2 example is to edit the contents of an XML file
// through a Woody form

// Note: this is a quick hack to get things working. More integration of the
// binding into the Woody - Javascript API is to be done.

// the form2 function is not directly called by the sitemap but by
// a generic "woody" function that instantiates the form based on
// parameters passed from the sitemap (see woody.js file)
function form2(form, documentURI, bindingURI) {
    // document contains the document to be edited as a DOM tree
    // (loadDocument is an utility function that looks up the
    //  Avalon DOMParser component to parse the file)
    var document = loadDocument(documentURI);
    // binding contains an object that can bind data between
    // a DOM-tree and Woody form (bidirectional). The binding
    // is described in an XML file and is based on JXPath
    // (loadBinding is an utility function that looks up an
    //  Avalon component which builds this binding)
    var binding = loadBinding(bindingURI);

    // we start by binding the document data to the form
    // the 'form' variable is a javascript wrapper around the
    // actual form, and 'form.form' is the actual Java form object
    binding.loadFormFromModel(form.form, document);

    // shows the form to the user until is validated successfully
    form.show("form2-display-pipeline", formHandler);

    // use the binding to update the DOM-tree with the
    // data from the form
    binding.saveFormToModel(form.form, document);

    // save the DOM-tree back to an XML file, the makeTargetURI
    // function makes a modified filename so that the
    // original document is not overwritten
    saveDocument(document, makeTargetURI(documentURI));

    cocoon.sendPage("form2-success-pipeline");
    form.finish();
}

function formHandler(form) {
    print("submitId="+form.getSubmitId());
    var model = form.getModel();
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

function loadBinding(uri) {
    var bindingManager = null;
    var source = null;
    var resolver = null;
    try {
        bindingManager = cocoon.getComponent(Packages.org.apache.cocoon.woody.binding.BindingManager.ROLE);
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        source = resolver.resolveURI(uri);
        return bindingManager.createBinding(source);
    } finally {
        if (source != null)
            resolver.release(source);
        cocoon.releaseComponent(bindingManager);
        cocoon.releaseComponent(resolver);
    }
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
        return parser.parseDocument(new Packages.org.xml.sax.InputSource(source.getURI()));
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
            throw new Error("Cannot write to source " + uri);
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
