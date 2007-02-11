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

import org.apache.cocoon.components.language.markup.xsp.XSPCookieHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  This is the action used to validate Cookie parameters (values). The
 *  parameters are described via the external xml file.
 *  @see org.apache.cocoon.acting.AbstractValidatorAction 
 *
 * @author <a href="mailto:paolo@arsenio.net">Paolo Scaffardi</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: CookieValidatorAction.java,v 1.2 2004/03/05 10:07:25 bdelacretaz Exp $
 */

public class CookieValidatorAction extends AbstractValidatorAction {

    /* (non-Javadoc)
     * @see org.apache.cocoon.acting.AbstractValidatorAction#createMapOfParameters(java.util.Map, java.util.Collection)
     */
    protected HashMap createMapOfParameters(Map objectModel, Collection set) {
        String name;
        HashMap params = new HashMap(set.size());
        // put required params into hash
        for (Iterator i = set.iterator(); i.hasNext();) {
            name = ((Configuration) i.next()).getAttribute("name", "").trim();
            Object value = XSPCookieHelper.getCookie(objectModel, name, -1).getValue();
            params.put(name, value);
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

