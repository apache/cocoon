cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody.js");

function form2(form, documentURI, bindingURI) {
    var document = loadDocument(documentURI);
    var binding = loadBinding(bindingURI);
    var model = form.getModel();

    binding.loadFormFromModel(form.form, document);

    form.show("form2-display-pipeline", function(form) {
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
    });

    binding.saveFormToModel(form.form, document);
    saveDocument(document, makeTargetURI(documentURI));
    cocoon.sendPage("form2-success-pipeline");
    form.finish();
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
