//
// CVS $Id: xmlForm.js,v 1.6 2003/03/28 22:56:18 coliver Exp $
//
// XMLForm Support
//


/**
 * Creates a new JavaScript wrapper of a Form object
 * see org.apache.cocoon.components.xmlform.Form
 * @param id [String] unique form id
 * @param validatorNS [String] Namespace of validator
 * @param validatorDoc [String] Validator document
 * @param scope [String] either "request" or "session"
 */

function XForm(id, validatorNS, validatorDoc, scope) {
    if (scope != "request") {
        cocoon.createSession();
    }
    this.id = id;
    this.lastContinuation = null;
    XForm.forms[id] = this;
    this.validatorNS = validatorNS;
    this.validatorDoc = validatorDoc;
    this.dead = false;
}

/**
 * Global variable that stores XForm instances by id
 */
XForm.forms = {};

/**
 * Return the model object of this form
 * @return a Java bean, JavaScript, DOM, or JDOM object 
 */
XForm.prototype.getModel = function() {
    return this.form.getModel();
}

/**
 * Set the model object of this form
 * @param model [Object] Any Java bean, JavaScript, DOM, or JDOM object 
 */
XForm.prototype.setModel = function(model) {
    this.form = 
       new Packages.org.apache.cocoon.components.xmlform.Form(this.id, 
                                                               model);
    this.context = 
       Packages.org.apache.commons.jxpath.JXPathContext.newContext(model);
    this.form.setAutoValidate(false);
    if (this.validatorNS != undefined && this.validatorDoc != undefined) {
        this._setValidator(this.validatorNS, this.validatorDoc);
    }
}

/**
 * Creates a new web continuation
 * @param lastCont [WebContinuation] previous web continuation
 * @param timeToLive [Number] expiration time for this continuation in milliseconds
 * @return [WebContinuation] a new WebContinuation instance
 */
XForm.prototype.start = function(lastCont, timeToLive) {
    var k = new Continuation();
    var kont = new WebContinuation(cocoon, k, 
                                   lastCont, timeToLive);
    return kont;
} 

/**
 * Adds a violation to this form
 * @param xpath [String] xpath location of field that contains invalid data
 * @param message [String] error message
 */
XForm.prototype.addViolation = function(xpath, message) {
    var violation = 
       new Packages.org.apache.cocoon.components.validation.Violation();
    violation.path = xpath;
    violation.message = message;
    var list = new java.util.LinkedList();
    list.add(violation);
    try {
        this.form.addViolations(list);
    } catch (e) {
        print(e);
        if (e instanceof java.lang.Throwable) {
            e.printStackTrace();
        }
    }
}

/**
 * Does this form have violations?
 * @return [Boolean] true if violations have been added to this form
 */
XForm.prototype.hasViolations = function() {
    var set = this.form.violationsAsSortedSet;
    return set != null && set.size() > 0;
}

/**
 * Computes the value of an xpath expression against the model of this form
 * @param expr [String] xpath expression
 * @return [Object] result of computing <code>expr</code>
 */
XForm.prototype.getValue = function(expr) {
    return this.context.getValue(expr);
}

/**
 * Returns an iterator over a nodeset value of an xpath expression evaluated 
 * against the model of this form
 * @param expr [String] xpath expression
 * @return [java.util.Iterator] representing a nodeset 
 */
XForm.prototype.iterate = function(expr) {
    return this.context.iterate(expr);
}

XForm.prototype._sendView = function(uri, lastCont, timeToLive) {
  var k = new Continuation();
  var kont = new WebContinuation(cocoon, k, lastCont, timeToLive);
  var bizData = this.form.getModel();
  if (bizData == undefined) {
      bizData = null;
  }

  cocoon.forwardTo("cocoon://" + cocoon.environment.getURIPrefix() + uri,
                   bizData, kont);
  this.lastContinuation = kont;
  suicide();
}

/**
 * Sends view to presentation pipeline and waits for subsequent submission.
 * Automatically resends view if validation fails.
 * Creates two continuations: one immediately before the page is sent 
 * and one immediately after. These are used to implement automated support
 * for back/forward navigation in the form. When you move forward in the
 * form the second continuation is invoked. When you move back from the
 * following page the first continuation is invoked.
 * @param phase [String] phase to validate
 * @param uri [String] presentation pipeline resource identifier of view
 * @param validator [Function] optional function invoked to perform validation
 */
XForm.prototype.sendView = function(phase, uri, validator) {
    var lastCont = this.lastContinuation;
    this.form.clearViolations();
    var view = this.form.getFormView(cocoon.environment.objectModel);
    while (true) {
        // create a continuation, the invocation of which will resend
        // the page: this is used to implement <xf:submit continuation="back">
        var k = this.start(lastCont);
        if (cocoon.request == null) {
            // this continuation has been invalidated
            this.dead = true;
            handleInvalidContinuation();
            suicide();
        }
        // reset the view in case this is a re-invocation of a continuation
        cocoon.request.setAttribute("view", view);
	this.form.remove(this.id);
	this.form.save(cocoon.environment.objectModel, "request");
        this._sendView(uri, k);
        // _sendView creates a continuation, the invocation of which
        // will return right here: it is used to implement 
        // <xf:submit continuation="forward">
        if (this.dead || cocoon.request == null) {
            // this continuation has been invalidated
            handleInvalidContinuation();
            suicide();
        }
        this.form.populate(cocoon.environment.objectModel);
        if (validator != undefined) {
            validator(this);
        }
        this.form.validate(phase);
        if (!this.hasViolations()) {
            break;
        }
    }
}

XForm.prototype._setValidator = function(schNS, schDoc) {
    // if validator params are not specified, then
    // there is no validation by default
    if (schNS == null || schDoc == null ) return null;
    var resolver = cocoon.environment;
    var schemaSrc = resolver.resolveURI( schDoc );
    try {
        var is = Packages.org.apache.cocoon.components.source.SourceUtil.getInputSource(schemaSrc);
        var schf = Packages.org.apache.cocoon.components.validation.SchemaFactory.lookup ( schNS );
        var sch = schf.compileSchema ( is );
        this.form.setValidator(sch.newValidator());
    } finally {
        resolver.release(schemaSrc);
    }
}

/**
 * Sends view to presentation pipeline but doesn't wait for submission
 * @param uri [String] presentation pipeline uri
 */

XForm.prototype.finish = function(uri) {
    try {
        this.form.save(cocoon.environment.objectModel, "request");
    } catch (e if (e instanceof java.lang.IllegalStateException)) {
        if (cocoon.session.getAttribute(this.id) != null) {
            // someone else has taken my session
            this.dead = true;
            handleInvalidContinuation();
            suicide();
        }
        throw e;
    }
    cocoon.forwardTo("cocoon://" + cocoon.environment.getURIPrefix() + uri,
                     this.form.getModel(), null);
    delete XForm.forms[this.id]; // delete myself
    this.dead = true;
    if (this.lastContinuation != null) {
        this.lastContinuation.invalidate();
        this.lastContinuation = null;
    }
    
}

/**
 * Entry point to a flow-based XMLForm application. Replaces the functionality
 * of XMLForm actions.
 * @param application Name of a JavaScript function that represents the page flow for a form
 * @param id form id
 * @param validator_ns XML namespace of validator
 * @param validator_doc validator document
 * @param scope 
 */

function xmlForm(application, id, validator_ns, validator_doc, scope) {
    if (cocoon.request == null) {
        handleInvalidContinuation("");
        return;
    }
    function getCommand() {
        var enum_ = cocoon.request.parameterNames;
        var command = undefined;
        while (enum_.hasMoreElements()) {
            var paramName = enum_.nextElement();
            // search for the command
            if (paramName.startsWith(Packages.org.apache.cocoon.Constants.ACTION_PARAM_PREFIX)) {
                command =
                    paramName.substring(Packages.org.apache.cocoon.Constants.ACTION_PARAM_PREFIX.length(), paramName.length());
                break;
            }
        }
        // command encodes the continuation id for "back" or "next" actions
        return command;
    }
    var command = getCommand();
    if (command != undefined) {
        // invoke a continuation 
        var continuationsMgr =
            cocoon.componentManager.lookup(Packages.org.apache.cocoon.components.flow.ContinuationsManager.ROLE);
        var wk = continuationsMgr.lookupWebContinuation(command);
        cocoon.componentManager.release(continuationsMgr);
        if (wk != null) {
            var jswk = wk.userObject;
            jswk.continuation(jswk);
            // note: not reached
        }
        handleInvalidContinuation(command);
        return;
    } 
    if (id != null) {
        // Just start a new instance of the application
        var args = new Array(arguments.length - 5 + 1);
        args[0] = new XForm(id, validator_ns, validator_doc, scope);
        for (var i = 5; i < arguments.length; i++) {
            args[i-4] = arguments[i];
        }
        this[application].apply(this, args);
    } else {
        handleInvalidContinuation(command);
    }
}

