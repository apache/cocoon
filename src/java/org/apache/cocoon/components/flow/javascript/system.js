// system.js
//
// JavaScript definitions
//
// Author: Ovidiu Predescu <ovidiu@apache.org>
// Date: March 19, 2002
//

var suicide;

var lastContinuation = null;

function callFunction(func, args)
{
  suicide = new Continuation();
  return func.apply(this, args);
}

function sendPageAndWait(uri, bizData, timeToLive)
{
  var kont = _sendPageAndWait(uri, bizData, timeToLive);
  lastContinuation = kont;
  return kont;
}

function _sendPageAndWait(uri, bizData, timeToLive)
{
  var k = new Continuation();
  var kont = new WebContinuation(cocoon, k, lastContinuation, timeToLive);
  cocoon.forwardTo("cocoon://" + cocoon.environment.getURIPrefix() + uri,
                   bizData, kont);
  suicide();
}

function sendPageAndContinue(uri, bizData)
{
    log.error("Deprecated: Please use sendPage instead");
}

function sendPage(uri, bizData)
{
  cocoon.forwardTo("cocoon://" + cocoon.environment.getURIPrefix() + uri,
                   bizData, null);
}

function process(uri, bizData, output)
{
  cocoon.process(uri, bizData, output);
}

// This function is called to restart a previously saved continuation
// passed as argument.
function handleContinuation(kont)
{
  kont.continuation(kont);
}

// Function called to handle an expired or invalid continuation
// identified by 'id'
function handleInvalidContinuation(id)
{
  sendPage("invalidContinuation.xml", {"ident" : id});
}

// Action Support
//
// call an action from JS
function act(type, src, param)
{
  if (type == undefined || src == undefined || param == undefined) {
    log.error("Signature does not match act(type,src,param)");
    return undefined;
  }
  return  cocoon.callAction(type,src,param);
}

// InputModule Support
//
// obtain value form InputModule
function inputValue(type, name)
{
  if (type == undefined || name == undefined) {
    log.error("Signature does not match inputValue(type,name)");
    return undefined;
  }
  return cocoon.inputModuleGetAttribute(type, name);
}

// OutputModule Support
// 
// set an attribute (starts transaction, commit or rollback required!)
function outputSet(type, name, value)
{
  if (type == undefined || name == undefined || value == undefined) {
    log.error("Signature does not match outputSet(type,name,value)");
  } else {
    cocoon.outputModuleSetAttribute(type, name, value);
  }
}

// makes attributes permanent (ends transaction)
function outputCommit(type)
{
  if (type == undefined) {
    log.error("Signature does not match outputCommit(type)");
  } else {
    cocoon.outputModuleCommit(type);
  }
}

// deletes attributes (ends transaction)
function outputRollback(type)
{
  if (type == undefined) {
    log.error("Signature does not match outputCommit(type)");
  } else {
    cocoon.outputModuleRollback(type);
  }
}

//
// XMLForm Support
//

/**
 * Creates a new JavaScript wrapper of a Form object
 * see org.apache.cocoon.components.xmlform.Form
 * @param id form id
 * @param validatorNS Namespace of validator
 * @param validatorDoc Validator document
 */

function XForm(id, validatorNS, validatorDoc) {
    cocoon.createSession();
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
 * @param model Any Java bean, JavaScript, DOM, or JDOM object 
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
 * @param lastCont previous web continuation
 * @param timeToLive expiration time for this continuation
 * @return a new WebContinuation instance
 */
XForm.prototype.start = function(lastCont, timeToLive) {
    var k = new Continuation();
    var kont = new WebContinuation(cocoon, k, 
                                   lastCont, timeToLive);
    return kont;
} 

/**
 * Adds a violation to this form
 * @param xpath xpath expression of field that contains invalid data
 * @param message error message
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
 * Computes the value of an xpath expression against the model of this form
 * @param expr xpath expression
 * @return result of computing <code>expr</code>
 */
XForm.prototype.getValue = function(expr) {
    return this.context.getValue(expr);
}

/**
 * Returns an iterator over a nodeset value of an xpath expression evaluated 
 * against the model of this form
 * @param expr xpath expression
 * @return java.util.Iterator representing a nodeset 
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
 * @param phase view to send (and phase to validate)
 * @param uri presentation pipeline resource identifier
 * @param validator optional function invoked to perform validation
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
        try {
            this.form.save(cocoon.environment.objectModel, "request");
        } catch (e if (e instanceof java.lang.IllegalStateException)) {
            if (cocoon.session.getAttribute(this.id) != null) {
                // someone else has taken my session
                this.dead = true;
                cocoon.removeSession();
                handleInvalidContinuation();
                suicide();
            }
            throw e;
        }
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
        if (this.form.violationsAsSortedSet == null ||
            this.form.violationsAsSortedSet.size() == 0) {
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
 * @param view view to send
 * @param uri presentation pipeline uri
 */

XForm.prototype.finish = function(view, uri) {
    try {
        this.form.save(cocoon.environment.objectModel, "request");
    } catch (e if (e instanceof java.lang.IllegalStateException)) {
        if (cocoon.session.getAttribute(this.id) != null) {
            // someone else has taken my session
            this.dead = true;
            cocoon.removeSession();
            handleInvalidContinuation();
            suicide();
        }
        throw e;
    }
    cocoon.forwardTo("cocoon://" + cocoon.environment.getURIPrefix() + uri,
                     this.form.getModel(), null);
    delete XForm.forms[this.id]; // delete myself
    this.dead = true;
    cocoon.removeSession();
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
 */

function xmlForm(application, id, validator_ns, validator_doc) {
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
        var xform = XForm.forms[id];
        if (xform != undefined) {
            // invoke a continuation 
            var continuationsMgr =
                cocoon.componentManager.lookup(Packages.org.apache.cocoon.components.flow.ContinuationsManager.ROLE);
            var wk = continuationsMgr.lookupWebContinuation(command);
            cocoon.componentManager.release(continuationsMgr);
            if (wk != null) {
                var jswk = wk.userObject;
                xform.form.clearViolations();
                jswk.continuation(jswk);
            }
        }
        handleInvalidContinuation(command);
        return;
    } 
    // Just start a new instance of the application
    cocoon.session.removeAttribute(id);
    this[application](new XForm(id, validator_ns, validator_doc));
}

//
// Prototype Database API
//
// TBD: Move this Database stuff to its own library outside of flow
//

defineClass("org.apache.cocoon.components.flow.javascript.ScriptableConnection");
defineClass("org.apache.cocoon.components.flow.javascript.ScriptableResult");

Database.getConnection = function(selectorValue) {
    var selector = cocoon.componentManager.lookup(Packages.org.apache.avalon.excalibur.datasource.DataSourceComponent.ROLE + "Selector");
    try {
	var ds = selector.select(selectorValue);
	return new Database(ds.getConnection());
    } finally {
	cocoon.componentManager.release(selector);
    }
}


