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
package org.apache.cocoon.forms.util;

import java.util.Map;

import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon;
import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon.FOM_Context;
import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon.FOM_Request;
import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon.FOM_Session;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * A simplified javascript cocoon object to use when using javaflow. In that case
 * the cocoon object would not be accessible from definition javascript snippets.
 */
public class FOM_SimpleCocoon extends ScriptableObject {

    private Map objectModel;

    public String getClassName() {
        return "FOM_SimpleCocoon";
    }

    private FOM_Cocoon.FOM_Request request;
    private FOM_Cocoon.FOM_Context context;
    private FOM_Cocoon.FOM_Session session;
    
    private Scriptable response;
    //private Scriptable parameters;
    
    
    public Scriptable jsGet_request() {
        return getRequest();
    }

    public Scriptable jsGet_response() {
        return getResponse();
    }

    public Scriptable jsGet_context() {
        return getContext();
    }

    public Scriptable jsGet_session() {
        return getSession();
    }

    /**
     * Get Sitemap parameters
     *
     * @return a <code>Scriptable</code> value whose properties represent
     * the Sitemap parameters from <map:call>
     */
    public Scriptable jsGet_parameters() {
        //return getParameters();
        // TODO parameters are accessible someway?
        return null;
    }

    public Scriptable getSession() {
        if (session != null) {
            return session;
        }
        session = new FOM_Session(
                getParentScope(),
                ObjectModelHelper.getRequest(objectModel).getSession(true));
        return session;
    }

    public Scriptable getRequest() {
        if (request != null) {
            return request;
        }
        request = new FOM_Request(
                getParentScope(),
                ObjectModelHelper.getRequest(objectModel));
        return request;
    }

    public Scriptable getContext() {
        if (context != null) {
            return context;
        }
        context = new FOM_Context(
                getParentScope(),
                ObjectModelHelper.getContext(objectModel));
        return context;
    }

    public Scriptable getResponse() {
        if (response != null) {
            return response;
        }
        response = org.mozilla.javascript.Context.toObject(
                ObjectModelHelper.getResponse(objectModel),
                getParentScope());
        return response;
    }
    
    public void setObjectModel(Map objectModel) {
        this.objectModel = objectModel;
    }
}
