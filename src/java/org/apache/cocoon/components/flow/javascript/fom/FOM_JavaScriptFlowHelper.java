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
package org.apache.cocoon.components.flow.javascript.fom;

import org.apache.cocoon.components.flow.FlowHelper;

import org.mozilla.javascript.Scriptable;

import java.util.Map;

/**
 * Provides the interface between the JavaScript flow controller layer and the
 * view layer. A view can obtain the JavaScript "live connect" objects (that
 * allow access to Java constructors) through this interface, as well as
 * the FOM objects.
 *
 * @version CVS $Id: FOM_JavaScriptFlowHelper.java,v 1.5 2004/04/25 12:12:08 sylvain Exp $
 */
public class FOM_JavaScriptFlowHelper extends FlowHelper {

    // Constants defining keys in the object model used to store the various objects.
    // These constants are private so that access to these objects only go through the
    // accessors provided below.
    //
    // These objects are stored in the object model rather than as request attributes,
    // as object model is cloned for subrequests (see EnvironmentWrapper), whereas
    // request attributes are shared between the "real" request and all of its
    // child requests.
    private static final String PACKAGES_OBJECT =
        "cocoon.flow.js.packages";
    private static final String JAVA_PACKAGE_OBJECT =
        "cocoon.flow.js.packages.java";
    private static final String FOM_REQUEST =
        "cocoon.flow.js.fom.FOM_Request";
    private static final String FOM_RESPONSE =
        "cocoon.flow.js.fom.FOM_Response";
    private static final String FOM_SESSION =
        "cocoon.flow.js.fom.FOM_Session";
    private static final String FOM_CONTEXT =
        "cocoon.flow.js.fom.FOM_Context";
    private static final String FOM_WEB_CONTINUATION =
        "cocoon.flow.js.fom.FOM_WebContinuation";
    /**
     * The parent scope to be used by nested scripts (e.g. Woody event handlers)
     */
    private static final String FOM_SCOPE =
        "cocoon.flow.js.fom.FOM_Scope";

    /**
     * Return the JS "Packages" property (that gives access to Java
     * packages) for use by the view layer
     * @param objectModel The Cocoon Environment's object model
     * @return The Packages property
     */
    public static Scriptable getPackages(Map objectModel) {
        return (Scriptable)objectModel.get(PACKAGES_OBJECT);
    }

    /**
     * Set the JS "Packages" property in the current request
     * @param objectModel The Cocoon Environment's object model
     */
    public static void setPackages(Map objectModel, Scriptable pkgs) {
        objectModel.put(PACKAGES_OBJECT, pkgs);
    }

    /**
     * Return the JS "java" property (that gives access to the "java"
     * package) for use by the view layer
     * @param objectModel The Cocoon Environment's object model
     * @return The java package property
     */
    public static Scriptable getJavaPackage(Map objectModel) {
        return (Scriptable)objectModel.get(JAVA_PACKAGE_OBJECT);
    }

    /**
     * Set the JS "java" property in the current request
     * @param objectModel The Cocoon Environment's object model
     */
    public static void setJavaPackage(Map objectModel, Scriptable javaPkg) {
        objectModel.put(JAVA_PACKAGE_OBJECT, javaPkg);
    }

    public static Scriptable getFOM_Request(Map objectModel) {
        return (Scriptable)objectModel.get(FOM_REQUEST);
    }

    public static void setFOM_Request(Map objectModel, Scriptable fom_request) {
        objectModel.put(FOM_REQUEST, fom_request);
    }

    public static Scriptable getFOM_Response(Map objectModel) {
        return (Scriptable)objectModel.get(FOM_RESPONSE);
    }

    public static void setFOM_Response(Map objectModel, Scriptable fom_response) {
        objectModel.put(FOM_RESPONSE, fom_response);
    }

    public static Scriptable getFOM_Session(Map objectModel) {
        return (Scriptable)objectModel.get(FOM_SESSION);
    }

    public static void setFOM_Session(Map objectModel, Scriptable fom_session) {
        objectModel.put(FOM_SESSION, fom_session);
    }

    public static Scriptable getFOM_Context(Map objectModel) {
        return (Scriptable)objectModel.get(FOM_CONTEXT);
    }

    public static void setFOM_Context(Map objectModel, Scriptable fom_context) {
        objectModel.put(FOM_CONTEXT, fom_context);
    }

    public static Scriptable getFOM_WebContinuation(Map objectModel) {
        return (Scriptable)objectModel.get(FOM_WEB_CONTINUATION);
    }

    public static void setFOM_WebContinuation(Map objectModel,
                                              Scriptable fom_webContinuation) {
        objectModel.put(FOM_WEB_CONTINUATION, fom_webContinuation);
    }
    
    /**
     * Get the flowscript scope, usable by JS snippets part of the control layer, such
     * as forms event listeners.
     * 
     * @param objectModel the object model where the scope is stored
     * @return the flowscript scope
     */
    public static Scriptable getFOM_FlowScope(Map objectModel) {
        return (Scriptable)objectModel.get(FOM_SCOPE);
    }
    
    /**
     * Set the flowscript scope usable by JS snippets.
     * 
     * @see #getFOM_FlowScope(Map)
     * @param objectModel
     * @param fom_scope
     */
    public static void setFOM_FlowScope(Map objectModel, Scriptable fom_scope) {
        objectModel.put(FOM_SCOPE, fom_scope);
    }
}
