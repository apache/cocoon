defineClass("org.apache.cocoon.woody.flow.javascript.ScriptableWidget");

function Form(formDef, attrName) {
  var formMgr = cocoon.componentManager.lookup(Packages.org.apache.cocoon.woody.FormManager.ROLE);
  try {
    var resolver = cocoon.environment;
    var src = resolver.resolveURI(formDef);
    this.form = formMgr.createForm(src);
    this.formWidget = new Widget(this.form);
    this.attrName = attrName;
    this.lastWebContinuation = null;
    this.rootContinuation = null;
  } finally {
    cocoon.componentManager.release(formMgr);
  }
}

Form.prototype.start = function(lastWebCont, timeToLive) {
    var result = this._start(lastWebCont, timeToLive);
    // 
    // _start() will return an Object when it's called
    // the first time. However, when its Continuation is invoked it
    // will return a WebContinuation instead. In the latter case
    // we're going back to the previous page: so 
    // clear the current page's violations before showing the previous page.
    // Without this, violations from the current page will
    // incorrectly be displayed on the previous page.
    if (result instanceof WebContinuation) {
        return result;
    }
    return result.kont;
} 

Form.prototype._start = function(lastWebCont, timeToLive) {
    var k = new Continuation();
    var kont = new WebContinuation(cocoon, k, 
                                   lastWebCont, timeToLive);
    if (this.rootContinuation == null) {
        this.rootContinuation = kont;
    }
    return {kont: kont};
} 

Form.prototype.getModel = function() {
  return this.formWidget;
}

Form.prototype.show = function(uri, validator) {
    var lastWebCont = this.lastWebContinuation;
    // create a continuation, the invocation of which will resend
    // the page: this is used to implement <xf:submit continuation="back">
    var wk = this.start(lastWebCont);
    while (true) {
        if (cocoon.request == null) {
            // this continuation has been invalidated
            this.dead = true;
            handleInvalidContinuation();
            suicide();
        }
        var thisWebCont = this._show(uri, wk);
        // _sendView creates a continuation, the invocation of which
        // will return right here: it is used to implement 
        // <xf:submit continuation="forward">
        if (this.dead ||  cocoon.request == null) {
            // this continuation has been invalidated
            handleInvalidContinuation();
            suicide();
        }
	var formContext = 
	  new Packages.org.apache.cocoon.woody.FormContext(cocoon.request, 
							   java.util.Locale.US);
	var e = cocoon.request.getParameterNames();
	while (e.hasMoreElements()) {
	  var paramName = e.nextElement();
	  print(paramName + "="+cocoon.request.get(paramName));
	}
	cocoon.request.setAttribute(this.attrName, this.form);
        var finished = this.form.process(formContext);
	print("finished="+finished);
	var evt = formContext.getActionEvent();
	if (evt != null) {
	  this.submitId = String(evt.getActionCommand());
	} else {
	  this.submitId = undefined;
	}
        if (validator != undefined) {
	  finished = validator(this) && finished;
        }
	if (finished) {
            break;
        }
    }
}

Form.prototype._show = function(uri, lastWebCont, timeToLive) {
    var k = new Continuation();
    var wk = new WebContinuation(cocoon, k, lastWebCont, timeToLive);
    var bizData = this.form;
    if (bizData == undefined) {
        bizData = null;
    }
    this.lastWebContinuation = wk;
    cocoon.request.setAttribute(this.attrName, this.form);
    cocoon.forwardTo("cocoon://" + 
                          cocoon.environment.getURIPrefix() + uri,
                          bizData, wk);
    suicide();
}

Form.prototype.finish = function() {
  this.rootContinuation.invalidate();
  this.rootContinuation = null;
  this.lastWebContinuation = null;
  this.form = null;
  this.formWidget = null;
}

Form.prototype.getSubmitId = function() {
  return this.submitId;
}

function woody(application, form_definition, attribute_name) {
    var args = new Array(arguments.length - 2 + 1);
    args[0] = new Form(form_definition, attribute_name);
    for (var i = 2; i < arguments.length; i++) {
      args[i-1] = arguments[i];
    }
    this[application].apply(this, args);
}
