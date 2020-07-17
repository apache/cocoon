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
package org.apache.cocoon.woody.binding;

import java.lang.reflect.Method;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;

/**
 * InsertBeanJXPathBinding provides an implementation of a {@link Binding}
 * that inserts a new instance of the specified bean (classname) into the target
 * back-end model upon save.
 * <p>
 * NOTES: <ol>
 * <li>This Binding does not perform any actions when loading.</li>
 * </ol>
 *
 * @version CVS $Id$
 */
public class InsertBeanJXPathBinding extends JXPathBindingBase {

    private final String className;
    private final String addMethodName;

    /**
     * Constructs InsertBeanJXPathBinding
     */
    public InsertBeanJXPathBinding(JXPathBindingBuilderBase.CommonAttributes commonAtts, String className, String addMethod) {
        super(commonAtts);
        this.className = className;
        this.addMethodName = addMethod;
    }

    /**
     * Do-nothing implementation of the interface.
     */
    public void doLoad(Widget frmModel, JXPathContext jxpc) {
        // doesn't do a thing when loading.
    }

    /**
     * Registers a JXPath Factory on the JXPath Context.
     * <p>
     * The factory will insert a new instance of the specified bean (classname)
     * inside this object into the target objectmodel.
     */
    public void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException {
        try {
            Object parent = jxpc.getContextBean();
            Object[] args = new Object[1];
            Class[] argTypes = new Class[1];

            // instantiate the new object
            argTypes[0] = Class.forName(this.className);
            args[0] = argTypes[0].newInstance();

            // lookup the named method on the parent
            Method addMethod =
                parent.getClass().getMethod(this.addMethodName, argTypes);

            // invoke this method with this new beast.
            addMethod.invoke(parent, args);

            if (getLogger().isDebugEnabled())
                getLogger().debug("InsertBean performed.");
        } catch (Exception e) {
            throw new CascadingRuntimeException("InsertBean failed.", e);
        }

        // jxpc.setFactory(new AbstractFactory() {
        //     public boolean createObject(JXPathContext context, Pointer pointer,
        //                                 Object parent, String name, int index) {
        //         try {
        //             Object[] args = new Object[1];
        //             Class[] argTypes = new Class[1];
        //
        //             // instantiate the new object
        //             argTypes[0] = Class.forName(InsertBeanJXPathBinding.this.className);
        //             args[0] = argTypes[0].newInstance();
        //             // lookup the named method on the parent
        //
        //             Method addMethod =
        //                 parent.getClass().getMethod(InsertBeanJXPathBinding.this.addMethodName, argTypes);
        //             // invoke this method with this new beast.
        //
        //             addMethod.invoke(parent, args);
        //
        //             if (getLogger().isDebugEnabled())
        //                 getLogger().debug("InsertBean jxpath factory executed for index " + index);
        //             return true;
        //         } catch (Exception e) {
        //             throw new CascadingRuntimeException("InsertBean jxpath factory failed.", e);
        //         }
        //     }
        // });
        //
        // if (getLogger().isDebugEnabled())
        //     getLogger().debug("done registered factory for inserting node -- " + toString());
    }

    public String toString() {
        return "InsertBeanJXPathBinding [for class " + this.className + " to addMethod " + this.addMethodName + "]";
    }

}
