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

package org.apache.cocoon.components.modules.input;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.xmlform.Form;

/**
 * Accesses the form model of an
 * {@link org.apache.cocoon.components.xmlform.Form XMLForm Instance}.
 * The xmlform-id needs to be passed with the configuration. Additionally supports
 * all configuration options from {@link AbstractJXPathModule AbstractJXPathModule}.
 * This can be used for example to let the
 * <code>org.apache.cocoon.acting.modular.DatabaseAction</code> access
 * form data.
 *
 * <p>Configuration example:</p>
 * <table>
 * <tr><td><code>&lt;xmlform-id&gt;form-feedback&lt;/xmlform-id&gt;</td>
 * <td>XMLForm ID to use.</td>
 * </tr></table>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: XMLFormInput.java,v 1.3 2004/03/05 13:02:37 bdelacretaz Exp $
 */
public class XMLFormInput extends AbstractJXPathModule implements ThreadSafe {

    String formId = null;

    /**
     * Configure component. Preprocess list of packages and functions
     * to add to JXPath context later.
     *
     * @param config a <code>Configuration</code> value
     * @exception ConfigurationException if an error occurs
     */
    public void configure(Configuration config)
      throws ConfigurationException {
        this.formId = config.getChild("xmlform-id").getValue(null);
        super.configure(config);
    }

    /** 
     * Returns the object which should be used as JXPath context.
     * Descendants should override this method to return a specific object
     * that is requried by the implementing class.
     * Examples are: request, session and application context objects.
     */
    protected Object getContextObject(Configuration modeConf,
                                      Map objectModel)
                                        throws ConfigurationException {
        String id = this.formId;

        if (modeConf!=null) {
            id = modeConf.getChild("xmlform-id").getValue(this.formId);
        }
        Form form = Form.lookup(objectModel, id);
        Object tmp = null;

        if (form!=null) {
            tmp = form.getModel();
        }
        return tmp;
    }
}
