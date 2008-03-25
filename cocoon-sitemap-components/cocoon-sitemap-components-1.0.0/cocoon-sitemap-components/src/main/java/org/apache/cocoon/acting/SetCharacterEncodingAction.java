/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.acting;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import java.util.Map;

/**
 * Sets the character encoding of parameters.
 * 
 * <p>Components use this encoding as default after the action.
 *
 * <p><b>Configuration parameters:</b>
 *
 * <dl>
 * <dt><i>form-encoding</i> (optional)
 * <dd>The supposed encoding of the request parameter.
 * </dl>
 * These configuration options supported in both declaration and use time.
 *
 * <p>If no encoding specified, the action does nothing.
 *
 * @cocoon.sitemap.component.documentation
 * Sets the character encoding of parameters.
 *
 * @version $Id$
 */
public class SetCharacterEncodingAction extends ServiceableAction implements ThreadSafe, Parameterizable {
    private String global_form_encoding = null;

    public void parameterize(Parameters parameters)
    throws ParameterException {
        // super.parameterize(parameters);

        global_form_encoding = parameters.getParameter("form-encoding", null);
    }

    /**
     * Set character encoding of current request.
     */
    public Map act (Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        Request request = ObjectModelHelper.getRequest(objectModel);
        if (request != null) {
            String form_encoding = par.getParameter("form-encoding", global_form_encoding);
            if (form_encoding != null) {
                request.setCharacterEncoding(form_encoding);
            }
        }

        return null;
    }
}
