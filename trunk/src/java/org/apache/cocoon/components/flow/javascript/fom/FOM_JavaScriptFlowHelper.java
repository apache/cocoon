/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.flow.javascript.fom;

import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

import org.mozilla.javascript.Scriptable;

import java.util.Map;

/**
 * Provides the interface between the JavaScript flow controller layer and the
 * view layer. A view can obtain the JavaScript "live connect" objects (that
 * allow access to Java constructors) through this interface, as well as
 * the FOM objects.
 *
 * @version CVS $Id: FOM_JavaScriptFlowHelper.java,v 1.3 2004/02/20 18:53:46 sylvain Exp $
 */
public class FOM_JavaScriptFlowHelper extends FlowHelper {

    public static final String PACKAGES_OBJECT =
        "cocoon.flow.js.packages";
    public static final String JAVA_PACKAGE_OBJECT =
        "cocoon.flow.js.packages.java";
    public static final String FOM_REQUEST =
        "cocoon.flow.js.fom.FOM_Request";
    public static final String FOM_RESPONSE =
        "cocoon.flow.js.fom.FOM_Response";
    public static final String FOM_SESSION =
        "cocoon.flow.js.fom.FOM_Session";
    public static final String FOM_CONTEXT =
        "cocoon.flow.js.fom.FOM_Context";
    public static final String FOM_WEB_CONTINUATION =
        "cocoon.flow.js.fom.FOM_WebContinuation";
    /**
     * The parent scope to be used by nested scripts (e.g. Woody event handlers)
     */
    public static final String FOM_SCOPE =
        "cocoon.flow.js.fom.FOM_Scope";

    /**
     * Return the JS "Packages" property (that gives access to Java
     * packages) for use by the view layer
     * @param objectModel The Cocoon Environment's object model
     * @return The Packages property
     */
    public static Scriptable getPackages(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Scriptable)request.getAttribute(PACKAGES_OBJECT);
    }

    /**
     * Set the JS "Packages" property in the current request
     * @param objectModel The Cocoon Environment's object model
     */
    public static void setPackages(Map objectModel, Scriptable pkgs) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(PACKAGES_OBJECT, pkgs);
    }

    /**
     * Return the JS "java" property (that gives access to the "java"
     * package) for use by the view layer
     * @param objectModel The Cocoon Environment's object model
     * @return The java package property
     */
    public static Scriptable getJavaPackage(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Scriptable)request.getAttribute(JAVA_PACKAGE_OBJECT);
    }

    /**
     * Set the JS "java" property in the current request
     * @param objectModel The Cocoon Environment's object model
     */
    public static void setJavaPackage(Map objectModel, Scriptable javaPkg) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JAVA_PACKAGE_OBJECT, javaPkg);
    }

    public static Scriptable getFOM_Request(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Scriptable)request.getAttribute(FOM_REQUEST);
    }

    public static void setFOM_Request(Map objectModel, Scriptable fom_request) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(FOM_REQUEST, fom_request);
    }

    public static Scriptable getFOM_Response(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Scriptable)request.getAttribute(FOM_RESPONSE);
    }

    public static void setFOM_Response(Map objectModel, Scriptable fom_response) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(FOM_RESPONSE, fom_response);
    }

    public static Scriptable getFOM_Session(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Scriptable)request.getAttribute(FOM_SESSION);
    }

    public static void setFOM_Session(Map objectModel, Scriptable fom_session) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(FOM_SESSION, fom_session);
    }

    public static Scriptable getFOM_Context(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Scriptable)request.getAttribute(FOM_CONTEXT);
    }

    public static void setFOM_Context(Map objectModel, Scriptable fom_context) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(FOM_CONTEXT, fom_context);
    }

    public static Scriptable getFOM_WebContinuation(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Scriptable)request.getAttribute(FOM_WEB_CONTINUATION);
    }

    public static void setFOM_WebContinuation(Map objectModel,
                                              Scriptable fom_webContinuation) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(FOM_WEB_CONTINUATION, fom_webContinuation);
    }
}
