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
 * @version CVS $Id: InsertBeanJXPathBinding.java,v 1.7 2004/01/27 11:43:29 joerg Exp $
 */
public class InsertBeanJXPathBinding extends JXPathBindingBase {

    private final String className;
    private final String addMethodName;

    /**
     * Constructs InsertBeanJXPathBinding
     */
    public InsertBeanJXPathBinding(JXpathBindingBuilderBase.CommonAttributes commonAtts, String className, String addMethod) {
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
