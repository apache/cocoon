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

import org.apache.cocoon.components.flow.java.*;
import org.apache.cocoon.forms.*;
import org.apache.cocoon.forms.binding.*;
import org.apache.cocoon.forms.formmodel.*;
import org.apache.cocoon.forms.transformation.FormsPipelineConfig;
import org.apache.excalibur.source.*;

/**
 *
 */
public abstract class AbstractFormFlow extends AbstractCocoonFlow {

    protected Form loadForm(String formSource) {

        Locale locale = Locale.getDefault();
        //if (localeStr != null)
        //    locale = I18nUtils.parseLocale(localeStr, locale);

				SourceResolver resolver = null;
				FormManager formManager = null;
        Source source = null;
        Form form = null;
        try {
            resolver = (SourceResolver) getComponent(SourceResolver.ROLE);
					
            source = resolver.resolveURI(formSource);

						formManager = (FormManager) getComponent(FormManager.ROLE);
            form = formManager.createForm(source);

            /*if (formHandler != null) {
                LifecycleHelper.setupComponent(formHandler, null, null, manager, null, null);
                form.setFormHandler(formHandler);
            }

            FormContext formContext = new FormContext(getRequest(), locale);

            boolean finished = form.process(formContext);
            getRequest().setAttribute(formAttribute, form);

            if (finished)
                return Collections.EMPTY_MAP;
            else
                return null;*/
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        } finally {
            if (source != null)
                resolver.release(source);
						releaseComponent(resolver);
						releaseComponent(formManager);
        }
        return form;
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
     *            The "form-form" and "locale" properties are added to this object.
     */
    protected String showForm(Form form, /*Validator validator,*/ String uri, VarMap bizdata) {

        Locale locale = java.util.Locale.getDefault();

        boolean finished = false;
        boolean isValid = false;

        do {
            sendPageAndWait(uri, bizdata.add(FormsPipelineConfig.CFORMSKEY, form).add("locale", locale));

            FormContext formContext = new FormContext(getRequest(), locale);

            // Prematurely add the bizData as a request attribute so that event listeners can use it
            // (the same is done by cocoon.sendPage())
            //getRequest.setAttribute(Packages.org.apache.cocoon.components.flow.FlowHelper.CONTEXT_OBJECT, bizData);

            finished = form.process(formContext);

            // Additional flow-level validation
            if (finished && form.isValid()) {
                //if (validator == null) {
                isValid = true;
                /*} else {
                  this.isValid = this.validator(this.form, bizData);
                  finished = this.isValid;
                }*/
            }

            // FIXME: Theoretically, we should clone the form widget (this.form) to ensure it keeps its
            // value with the continuation. We don't do it since there should me not much pratical consequences
            // except a sudden change of repeaters whose size changed from a continuation to another.

        }
        while (!finished);

        Widget widget = form.getSubmitWidget();
        // Can be null on "normal" submit
        return widget == null ? null : widget.getId();
    }

		protected String showForm(Form form, String uri) {
			  return showForm(form, uri, new VarMap());
		}

    protected Binding loadBinding(String bindingURI) {
			  SourceResolver resolver = null;
			  BindingManager bindingManager = null;
        Source source = null;
        Binding binding = null;
        try {
					  resolver = (SourceResolver) getComponent(SourceResolver.ROLE);
            source = resolver.resolveURI(bindingURI);
						bindingManager = (BindingManager) getComponent(BindingManager.ROLE);
            binding = bindingManager.createBinding(source);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load form binding from '"+bindingURI+"'", e);
        } finally {
            if (source != null)
                resolver.release(source);
						releaseComponent(resolver);
            releaseComponent(bindingManager);
        }
        return binding;
    }
}
