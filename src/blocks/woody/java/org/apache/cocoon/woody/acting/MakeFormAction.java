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

import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.woody.FormManager;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.excalibur.source.Source;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;

import java.util.Map;

/**
 * An action that creates a new form instance and stores it in a request attribute.
 *
 * <p>Required parameters:
 * <ul>
 *  <li><strong>form-definition</strong>: filename (URL) of the form definition file
 *  <li><strong>attribute-name</strong>: name of the request attribute in which to store the form instance
 * </ul>
 * 
 * @version $Id: MakeFormAction.java,v 1.9 2004/03/09 13:54:22 reinhard Exp $
 */
public class MakeFormAction implements Action, ThreadSafe, Serviceable {

    FormManager formManager;

    public void service(ServiceManager serviceManager) throws ServiceException {
        formManager = (FormManager)serviceManager.lookup(FormManager.ROLE);
    }

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters)
            throws Exception {
        String formSource = parameters.getParameter("form-definition");
        String formAttribute = parameters.getParameter("attribute-name");

        Source source = null;
        try {
            source = resolver.resolveURI(formSource);
            Form form = formManager.createForm(source);

            Request request = ObjectModelHelper.getRequest(objectModel);
            request.setAttribute(formAttribute, form);
        } finally {
            resolver.release(source);
        }

        return null;
    }

}
