/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-

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

*/
package org.apache.cocoon.components.flow.javascript;
import org.apache.cocoon.components.flow.Flow;
import org.mozilla.javascript.Scriptable;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import java.util.Map;

/**
 * Provides the interface between the JavaScript flow controller layer and the 
 * view layer. A view can obtain the JavaScript "live connect" objects (that
 * allow access to Java constructors) through this interface
 */

public class JavaScriptFlow extends Flow {

    public static final String COCOON_FLOW_JS_PACKAGES =
	"cocoon.flow.js.rhino.packages";
    public static final String COCOON_FLOW_JS_JAVA_PACKAGE =
	"cocoon.flow.js.rhino.packages.java";

    /** 
     * Return the JS "Packages" property (that gives access to Java
     * packages) for use by the view layer
     * @param objectModel The Cocoon Environment's object model
     * @return The Packages property
     */
    public static Scriptable getPackages(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Scriptable)request.getAttribute(COCOON_FLOW_JS_PACKAGES);
    }


    /** 
     * Return the JS "java" property (that gives access to the "java"
     * package) for use by the view layer
     * @param objectModel The Cocoon Environment's object model
     * @return The java package property
     */
    public static Scriptable getJavaPackage(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Scriptable)request.getAttribute(COCOON_FLOW_JS_JAVA_PACKAGE);
    }


    /** 
     * Set the JS "Packages" property in the current request
     * @param objectModel The Cocoon Environment's object model
     * @return The Packages property
     */
    public static void setPackages(Map objectModel, Scriptable pkgs) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(COCOON_FLOW_JS_PACKAGES, pkgs);
    }

    /** 
     * Set the JS "java" property in the current request
     * @param objectModel The Cocoon Environment's object model
     * @return The "java" property
     */
    public static void setJavaPackage(Map objectModel, Scriptable javaPkg) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(COCOON_FLOW_JS_JAVA_PACKAGE, javaPkg);
    }

}

