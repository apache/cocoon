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

package org.apache.cocoon.forms.flow.java;

import java.util.Locale;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.java.AbstractContinuable;
import org.apache.cocoon.components.flow.java.VarMap;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.FormManager;
import org.apache.cocoon.forms.binding.Binding;
import org.apache.cocoon.forms.binding.BindingManager;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.transformation.FormsPipelineConfig;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Element;

/**
 * Implementation of the Cocoon Forms/Java Flow integration.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: FormInstance.java,v 1.6 2004/04/16 09:39:57 stephan Exp $
 */
public class FormInstance extends AbstractContinuable {

    private Form form;
    private Binding binding;
    private Widget formWidget;
    private Locale locale;
    private boolean isValid;
    private Object validator; // Used?
  
    /**
     * Create a form, given the URI of its definition file
     */
    public FormInstance(String uri) {
        FormManager formMgr = null;
        SourceResolver resolver = null;
        Source src = null;
        try {
            formMgr = (FormManager)getComponent(FormManager.ROLE);
            resolver = (SourceResolver)getComponent(SourceResolver.ROLE);
            src = resolver.resolveURI(uri);
            this.form = formMgr.createForm(src);
            this.binding = null;
            // this.validator = null;
            // TODO : do we keep this ?
            // this.formWidget = new Widget(this.form); could not create instance
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not create form instance", e);
        } finally {
            releaseComponent(formMgr);
            if (src != null) resolver.release(src);
            releaseComponent(resolver);
        }
    }

    /**
     * Create a form, given the URI of its definition file, the
     * binding file.
     */
    public FormInstance(String definitionFile, String bindingFile) {
        this(definitionFile);
        createBinding(bindingFile);
    }

    /**
     * Create a form of an fd:form element in the form of a org.w3c.dom.Element
     */
    public FormInstance(Element formDefinition) {
        FormManager formMgr = null;
        SourceResolver resolver = null;
        Source src = null;
        try {
            formMgr = (FormManager)getComponent(FormManager.ROLE);
            resolver = (SourceResolver)getComponent(SourceResolver.ROLE);
            Form form = formMgr.createForm(formDefinition);
            this.binding = null;
            // this.formWidget = new Widget(form);  could not create instance
            // this.local = cocoon.createPageLocal(); PageLocal ?
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not create form instance", e);
        } finally {
            releaseComponent(formMgr);
            if (src != null) resolver.release(src);
            releaseComponent(resolver);
        }
    }

    public Widget getModel() {
        return this.formWidget;
    }

    /**
     * Get a Widget (the java object) from the form.
     * If <code>name</code> is undefined, the form widget itself is returned.
     * Otherwise, the form's child widget of name <code>name</code> is returned.
     */
    public Widget getWidget(String name) {
        if (name == null) {
            return this.form/*Widget*/;
        } else {
            return this.form/*Widget*/.getWidget(name);
        }
    }

		public String getSubmitId() {
			
		    Widget widget = this.form.getSubmitWidget();
  	    // Can be null on "normal" submit
				return  widget == null ? null : widget.getId();
		}

    /**
     * Sets the point in your script that will be returned to when the form is
     * redisplayed. If setBookmark() is not called, this is implicitly set to
     * the beginning of showForm().
     */
/*    public WebContinuation setBookmark() {
          return (this.local_.webContinuation = cocoon.createWebContinuation());
    }*/

    /**
     * Returns the bookmark continuation associated with this form, or undefined
     * if setBookmark() has not been called.
     *
     */
/*    public WebContinuation getBookmark() {
        return this.local_.webContinuation;
    }  */

    public void show(String uri) {
        show(uri, new VarMap());
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
     *            The "{FormsPipelineConfig.CFORMSKEY}" and "locale" properties are added to this object.
     */
    public void show(String uri, Object bizData) {

        if (bizData==null) bizData = new VarMap();
        ((VarMap)bizData).add(FormsPipelineConfig.CFORMSKEY, this.form);

        if (this.locale == null)
            this.locale = java.util.Locale.getDefault();
        ((VarMap)bizData).add("locale", this.locale);
    
        // Keep the first continuation that will be created as the result of this function
        //var result = null;

        boolean finished = false;
        this.isValid = false;

        do {
            sendPageAndWait(uri, bizData);
        
            FormContext formContext = new FormContext(getRequest(), locale);

            // Prematurely add the bizData as a request attribute so that event listeners can use it
            // (the same is done by cocoon.sendPage())
            getRequest().setAttribute(FlowHelper.CONTEXT_OBJECT, bizData);

            finished = this.form.process(formContext);
          
            // Additional flow-level validation
            if (finished) {
                if (this.validator == null) {
                    this.isValid = this.form.isValid();
                } else {
                    this.isValid = this.form.isValid() /*& this.validator(this.form, bizData)*/;
                }
                finished = this.isValid;
            }
        
            // FIXME: Theoretically, we should clone the form widget (this.form) to ensure it keeps its
            // value with the continuation. We don't do it since there should me not much pratical consequences
            // except a sudden change of repeaters whose size changed from a continuation to another.
        
        } while(!finished);
    }

    /**
     * Manages the display of a form and its validation.
     * @parameter uri the page uri (like in cocoon.sendPageAndWait())
     * @parameter fun optional function which will be executed after pipeline
     *  processing. Useful for releasing resources needed during pipeline
     *  processing but which should not become part of the continuation
     * @parameter ttl Time to live (in milliseconds) for the continuation
     *  created
     * @returns The web continuation associated with submitting this form
     */
/*    public showForm(String uri, Object fun, ttl) {
        if (!this.getBookmark()) {
            this.setBookmark();
        }
        FormContext formContext = FormsFlowHelper.getFormContext(cocoon, this.locale);
        // this is needed by the FormTemplateTransformer:
        //var javaWidget = this.formWidget_.unwrap();;
        //this.formWidget_["CocoonFormsInstance"] = javaWidget;
        getRequest().setAttribute(Packages.org.apache.cocoon.forms.transformation.CFORMSKEY, this.formWidget);
        WebContinuation wk = sendPageAndWait(uri, this.formWidget, fun, ttl);
        var formContext = new FormContext(cocoon.request, javaWidget.getLocale());
        var userErrors = 0;
        this.formWidget_.validationErrorListener = function(widget, error) {
            if (error != null) {
                userErrors++;
            }
        }
        var finished = javaWidget.process(formContext);
        if (this.onValidate) {
            this.onValidate(this);
        }
        if (!finished || userErrors > 0) {
            cocoon.continuation = this.local_.webContinuation;
            this.local_.webContinuation.continuation(this.local_.webContinuation);
        }
        return wk;
    }*/

    public void createBinding(String bindingURI) {
        BindingManager bindingManager = null;
        Source source = null;
        SourceResolver resolver = null;
        try {
            bindingManager = (BindingManager)getComponent(BindingManager.ROLE);
            resolver = (SourceResolver)getComponent(SourceResolver.ROLE);
            source = resolver.resolveURI(bindingURI);
            this.binding = bindingManager.createBinding(source);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not create bindinh", e);
        } finally {
            if (source != null)
                resolver.release(source);
            releaseComponent(bindingManager);
            releaseComponent(resolver);
        }
    }

    public void load(Object object) {
        if (this.binding == null)
            throw new Error("Binding not configured for this form.");

        try {
            this.binding.loadFormFromModel(this.form, object);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not load form from model", e);
        }
    }

    public void save(Object object) {
        if (this.binding == null)
            throw new Error("Binding not configured for this form.");

        try {
            this.binding.saveFormToModel(this.form, object);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not save form into model", e);
        }
    }

    public void setAttribute(String name, Object value) {
        this.form.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        return this.form.getAttribute(name);
    }

    public void removeAttribute(String name) {
        this.form.removeAttribute(name);
    }
}
