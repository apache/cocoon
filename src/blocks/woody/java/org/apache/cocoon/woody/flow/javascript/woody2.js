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

/**
 * New implementation of the Woody/FlowScript integration. Should replace
 * woody.js in the future.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: woody2.js,v 1.4 2003/12/23 11:06:45 mpo Exp $
 */

// Revisit this class, so it gives access to more than the value.
defineClass("org.apache.cocoon.woody.flow.javascript.ScriptableWidget");

/**
 * Create a Woody form, given the URI of its definition file
 */
function Form(uri) {
    var formMgr = null;
    var resolver = null;
    var src = null;
    try {
        formMgr = cocoon.getComponent(Packages.org.apache.cocoon.woody.FormManager.ROLE);
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        src = resolver.resolveURI(uri);
        this.form = formMgr.createForm(src);
        this.binding = null;
        this.validator = null;
        this.eventHandler = null;
        // TODO : do we keep this ?
        this.formWidget = new Widget(this.form);
        
    } finally {
        cocoon.releaseComponent(formMgr);
        if (src != null) resolver.release(src);
        cocoon.releaseComponent(resolver);
    }
}

Form.prototype.getModel = function() {
    return this.formWidget;
}

/**
 * Get a Widget (the java object) from the form.
 * If <code>name</code> is undefined, the form widget itself is returned.
 * Otherwise, the form's child widget of name <code>name</code> is returned.
 */
Form.prototype.getWidget = function(name) {
    if (name == undefined) {
        return this.form;
    } else {
        return this.form.getWidget(name);
    }
}

/**
 * Manages the display of a form and its validation.
 *
 * This uses some additionnal propertied on the form object :
 * - "locale" : the form locale (default locale is used if not set)
 * - "validator" : additional validation function. This function receives
 *   the form object as parameter and should return a boolean indicating
 *   if the form handling is finished (true) or if the form should be
 *   redisplayed again (false)
 *
 * On return, the calling code can check some properties to know the form result :
 * - "isValid" : true if the form was sucessfully validated
 * - "submitId" : the id of the widget that triggered the form submit (can be null)
 *
 * @parameter uri the page uri (like in cocoon.sendPageAndWait())
 * @parameter bizdata some business data for the view (like in cocoon.sendPageAndWait()).
 *            The "woody-form" and "locale" properties are added to this object.
 */
Form.prototype.showForm = function(uri, bizData) {

    if (bizData == undefined) bizData = new Object();
    bizData[Packages.org.apache.cocoon.woody.transformation.WoodyPipelineConfig.WOODY_FORM] = this.form;

    if (this.locale == null)
        this.locale = java.util.Locale.getDefault();
    bizData["locale"] = this.locale;
    
    // Keep the first continuation that will be created as the result of this function
    var result = null;

    var finished = false;
    this.isValid = false;

    do {
        var k = cocoon.sendPageAndWait(uri, bizData);
        if (result == null) result = k;
        
        var formContext = 
            Packages.org.apache.cocoon.woody.flow.javascript.WoodyFlowHelper.getFormContext(cocoon, this.locale);

		// Prematurely add the bizData as a request attribute so that event listeners can use it
		// (the same is done by cocoon.sendPage())
		cocoon.request.setAttribute(Packages.org.apache.cocoon.components.flow.FlowHelper.CONTEXT_OBJECT, bizData);

		finished = this.form.process(formContext);
        
        // Additional flow-level validation
        if (finished && this.form.isValid()) {
            if (this.validator == null) {
              this.isValid = true;
            } else {
              this.isValid = this.validator(this.form, bizData);
              finished = this.isValid;
            }
        }
        
        // FIXME: Theoretically, we should clone the form widget (this.form) to ensure it keeps its
        // value with the continuation. We don't do it since there should me not much pratical consequences
        // except a sudden change of repeaters whose size changed from a continuation to another.
        
    } while(!finished);

    var widget = this.form.getSubmitWidget();
    // Can be null on "normal" submit
    this.submitId = widget == null ? null : widget.getId(); 
    
    return result;
}

Form.prototype.createBinding = function(bindingURI) {
    var bindingManager = null;
    var source = null;
    var resolver = null;
    try {
        bindingManager = cocoon.getComponent(Packages.org.apache.cocoon.woody.binding.BindingManager.ROLE);
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        source = resolver.resolveURI(bindingURI);
        this.binding = bindingManager.createBinding(source);
    } finally {
        if (source != null)
            resolver.release(source);
        cocoon.releaseComponent(bindingManager);
        cocoon.releaseComponent(resolver);
    }
}

Form.prototype.load = function(object) {
    if (this.binding == null)
        throw new Error("Binding not configured for this form.");
    this.binding.loadFormFromModel(this.form, object);
}

Form.prototype.save = function(object) {
    if (this.binding == null)
        throw new Error("Binding not configured for this form.");
    this.binding.saveFormToModel(this.form, object);
}


function woody(form_function, form_definition) {
    var form = new Form(cocoon.parameters["form-definition"]);
    
    var args = [form];

    // set the binding on the form if there's any
    var bindingURI = cocoon.parameters["bindingURI"];
    if (bindingURI != null)
        form.createBinding(bindingURI);

    this[cocoon.parameters["function"]].apply(this, args);
}

