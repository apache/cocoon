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
package org.apache.cocoon.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

/**
 * Sets the character encoding of parameters.
 * Components use this encoding as default after the action.
 * <p>
 * <b>Configuration parameters:</b>
 * <dl>
 * <dt><i>form-encoding</i> (optional)
 * <dd>The supposed encoding of the request parameter.
 * </dl>
 * These configuration options supported in both declaration and use time.
 * <p>If no encoding specified, the action does nothing.
 *
 * @author <a href="mailto:miyabe@jzf.co.jp">MIYABE Tatsuhiko</a>
 * @version CVS $Id: SetCharacterEncodingAction.java,v 1.6 2004/03/08 13:57:35 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type="Action"
 * @x-avalon.lifestyle type="singleton"
 * @x-avalon.info name="char-encoding"
 * 
 */
public class SetCharacterEncodingAction extends ServiceableAction implements Parameterizable {
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
