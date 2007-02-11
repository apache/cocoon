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
 * @version $Id: HandleFormSubmitAction.java,v 1.13 2004/02/11 09:53:45 antonio Exp $
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
