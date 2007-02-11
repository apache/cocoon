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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the action used to validate Request parameters.
 * The parameters are described via the external xml
 * file (its format is defined in AbstractValidatorAction).
 * @see org.apache.cocoon.acting.AbstractValidatorAction
 *
 * @author <a href="mailto:Martin.Man@seznam.cz">Martin Man</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: FormValidatorAction.java,v 1.5 2004/03/05 13:02:43 bdelacretaz Exp $
 */
public class FormValidatorAction extends AbstractValidatorAction implements ThreadSafe {

    /**
     * Reads parameter values from request parameters for all parameters
     * that are contained in the active constraint list. If a parameter
     * has multiple values, all are stored in the resulting map.
     * 
     * @param objectModel the object model
     * @param set a collection of parameter names
     * @return HashMap of required parameters 
     */
    protected HashMap createMapOfParameters(Map objectModel, Collection set) {
        String name;
        HashMap params = new HashMap(set.size());
        // put required params into hash
        Request request = ObjectModelHelper.getRequest(objectModel);
        for (Iterator i = set.iterator(); i.hasNext();) {
            name = ((Configuration) i.next()).getAttribute("name", "").trim();
            Object[] values = request.getParameterValues(name);
            if (values != null) {
                switch (values.length) {
                    case 0 :
                        params.put(name, null);
                        break;
                    case 1 :
                        params.put(name, values[0]);
                        break;
                    default :
                        params.put(name, values);
                }
            } else {
                params.put(name, values);
            }
        }
        return params;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.acting.AbstractValidatorAction#isStringEncoded()
     */
    boolean isStringEncoded() {
        return true;
    }


}
