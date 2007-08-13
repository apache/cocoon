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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  This is the action used to validate Cookie parameters (values). The
 *  parameters are described via the external xml file.
 *  @see org.apache.cocoon.acting.AbstractValidatorAction 
 *
 * @version $Id$
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
            Cookie cookie = getCookie(objectModel, name);
            if (cookie != null) {
                params.put(name, cookie.getValue());
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
    public static Cookie getCookie(Map objectModel, String cookieName) {
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        Cookie[] cookies = request.getCocoonCookies();
        if (cookies != null) {
            for(int count = 0; count < cookies.length; count++) {
                Cookie currentCookie = cookies[count];
                if (currentCookie.getName().equals(cookieName)) {
                    return currentCookie;
                }
            }
        }
        
        return null;
    }    

}

