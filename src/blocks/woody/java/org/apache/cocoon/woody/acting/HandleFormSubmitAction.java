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
package org.apache.cocoon.woody.acting;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.event.FormHandler;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.excalibur.source.Source;

import java.util.Map;
import java.util.Collections;
import java.util.Locale;

/**
 * An action that will create a form instance, let it handle the current request (and
 * do validation), and will return not-null if validation was successfully or null when
 * validation failed. In both cases, the created form instance is stored in a request attribute,
 * so that it can be picked up later on by other components.
 *
 * <p>Required parameters:
 * <ul>
 *  <li><strong>form-definition</strong>: filename (URL) pointing to the form definition file
 *  <li><strong>attribute-name</strong>: name of the request attribute in which the form instance should be stored
 * </ul>
 * 
 * @version $Id: HandleFormSubmitAction.java,v 1.14 2004/03/05 13:02:26 bdelacretaz Exp $
 */
public class HandleFormSubmitAction extends AbstractWoodyAction implements Action, ThreadSafe {

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters)
            throws Exception {
        String formSource = parameters.getParameter("form-definition");
        String formAttribute = parameters.getParameter("attribute-name");
        String formHandlerClassName = parameters.getParameter("formhandler", null);

        Locale locale = Locale.getDefault();
        String localeStr = parameters.getParameter("locale", null);
        if (localeStr != null)
            locale = I18nUtils.parseLocale(localeStr, locale);

        Source source = resolver.resolveURI(formSource);
        try {
            Form form = formManager.createForm(source);

            Request request = ObjectModelHelper.getRequest(objectModel);
            FormHandler formHandler = null;

            if (formHandlerClassName != null) {
                // TODO cache these classes
                Class clazz = Class.forName(formHandlerClassName);
                formHandler = (FormHandler)clazz.newInstance();
                LifecycleHelper.setupComponent(formHandler, null, null, manager, null, null);
                form.setFormHandler(formHandler);
            }

            FormContext formContext = new FormContext(request, locale);

            boolean finished = form.process(formContext);
            request.setAttribute(formAttribute, form);

            if (finished)
                return Collections.EMPTY_MAP;
            else
                return null;
        } finally {
            resolver.release(source);
        }
    }
}
