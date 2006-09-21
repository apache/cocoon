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
package org.apache.cocoon.ajax;

import org.apache.cocoon.environment.Request;

/**
 * Helper class to handle Ajax requests
 * 
 * @version $Id$
 * @since 2.1.8
 */
public class AjaxHelper {
    
    private AjaxHelper() {
        // Forbid instanciation
    }

    /**
     * The request parameter that, if set, indicates an Ajax request.
     */
    public static final String AJAX_REQUEST_PARAMETER = "cocoon-ajax";
    
    /**
     * The namespace URI of the "browser update" xml dialect
     */
    public static final String BROWSER_UPDATE_URI = "http://apache.org/cocoon/browser-update/1.0";

    /**
     * Is the request an Ajax request?
     * 
     * @param req the request
     * @return true if this is an Ajax request
     */
    public static boolean isAjaxRequest(Request req) {
        return req.getParameter(AJAX_REQUEST_PARAMETER) != null;
    }

}
