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
package org.apache.cocoon.template.environment;

import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.javascript.fom.FOM_JavaScriptFlowHelper;
import org.apache.cocoon.environment.TemplateObjectModelHelper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaPackage;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/**
 * Creation of an Expression context from the TemplateObjectModelHelper
 * 
 * @version SVN $Id$
 */
public class FlowObjectModelHelper {

    private static Scriptable rootScope;

    /** Avoid instantiation. */
    private FlowObjectModelHelper() {}

    public static Scriptable getScope() {
        Context ctx = Context.enter();
        try {
            // Create it if never used up to now
            if (rootScope == null) {
                rootScope = ctx.initStandardObjects(null);
            }
            try {
                Scriptable scope = ctx.newObject(rootScope);
                scope.setPrototype(rootScope);
                scope.setParentScope(null);
                return scope;
            } catch (Exception e) {
                throw new CascadingRuntimeException("Exception", e);
            }
        } finally {
            Context.exit();
        }
    }

    /**
     * Create an expression context that contains the object model
     */
    public static ExpressionContext getFOMExpressionContext(final Map objectModel, 
                                                            final Parameters parameters) {
        ExpressionContext context = new ExpressionContext();
        Map expressionContext = TemplateObjectModelHelper.getTemplateObjectModel(objectModel, parameters);
        FlowObjectModelHelper.addJavaPackages( expressionContext );
        context.setVars( expressionContext );
        context.setContextBean(FlowHelper.getContextObject(objectModel));

        return context;
    }

    /**
     * Add java packages to object model. Allows to construct java objects.
     * @param objectModel usually the result of invoking getTemplateObjectModel
     */
    public static void addJavaPackages( Map objectModel ) {
        Object javaPkg = FOM_JavaScriptFlowHelper.getJavaPackage(objectModel);
        Object pkgs = FOM_JavaScriptFlowHelper.getPackages(objectModel);
        
        // packages might have already been set up if flowscript is being used
        if ( javaPkg != null && pkgs != null ) {
            objectModel.put( "Packages", javaPkg );
            objectModel.put( "java", pkgs );
        } else { 
            Context.enter();
            try {
                final String JAVA_PACKAGE = "JavaPackage";
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                // FIXME - NativeJavaPackage is an internal class which we should not use
                Scriptable newPackages = new NativeJavaPackage( "", cl );
                newPackages.setParentScope( getScope() );
                newPackages.setPrototype( ScriptableObject.getClassPrototype(   getScope(),
                                                                                JAVA_PACKAGE ) );
                objectModel.put( "Packages", newPackages );
                objectModel.put( "java", ScriptableObject.getProperty( getScope(), "java" ) );
            } finally {
                Context.exit();
            }
        }
    }

}
