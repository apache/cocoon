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

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.util.jxpath.DOMFactory;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Node;

/**
 * Provides a base class for hooking up Binding implementations that use the 
 * Jakarta Commons <a href="http://jakarta.apache.org/commons/jxpath/index.html">
 * JXPath package</a>.
 */
public abstract class JXPathBindingBase implements Binding, LogEnabled {

    /** 
     * Avalon Logger to use in all subclasses.
     */
    private Logger logger;
    
    /**
     * Object holding the values of the common objects on all Bindings.
     */
    private final JXpathBindingBuilderBase.CommonAttributes commonAtts;
    

    private JXPathBindingBase() {
        this(JXpathBindingBuilderBase.CommonAttributes.DEFAULT);
    }
    
    protected JXPathBindingBase(JXpathBindingBuilderBase.CommonAttributes commonAtts) {
        this.commonAtts = commonAtts;
    }
    
    /**
     * Performs the actual load binding regardless of the flag {@see #loadEnabled}.
     * Abstract method that subclasses need to implement for specific activity.
     */
    public abstract void doLoad(Widget frmModel, JXPathContext jxpc);

    /**
     * Redefines the Binding action as working on a JXPathContext Type rather 
     * then on generic objects.
     * Executes the actual loading {@see #doLoad(Widget, JXPathContext)} 
     * depending on the value of {@see #loadEnabled}
     */
    public final void loadFormFromModel(Widget frmModel, JXPathContext jxpc) {
        applyLeniency(jxpc);
        if (this.commonAtts.loadEnabled) {
            doLoad(frmModel, jxpc);
        }    
    }

    /**
     * Hooks up with the more generic Binding of any objectModel by wrapping
     * it up in a JXPathContext object and then transfering control over to
     * the new overloaded version of this method.
     */
    public final void loadFormFromModel(Widget frmModel, Object objModel) {
        if (objModel == null) {
            throw new NullPointerException("null object passed to loadFormFromModel() method");
        }

        JXPathContext jxpc = makeJXPathContext(objModel);
        loadFormFromModel(frmModel, jxpc);
    }

    /**
     * Performs the actual save binding regardless of the flag {@see #saveEnabled}.
     * Abstract method that subclasses need to implement for specific activity.
     */
    public abstract void doSave(Widget frmModel, JXPathContext jxpc) throws BindingException;
    
    /**
     * Redefines the Binding action as working on a JXPathContext Type rather 
     * then on generic objects.
     * Executes the actual saving {@see #doSave(Widget, JXPathContext)} 
     * depending on the value of {@see #saveEnabled}
     */
    public final void saveFormToModel(Widget frmModel, JXPathContext jxpc) throws BindingException{
        applyLeniency(jxpc);
        if (this.commonAtts.saveEnabled) {
            doSave(frmModel, jxpc);
        }    
    }
    
    /**
     * Hooks up with the more generic Binding of any objectModel by wrapping
     * it up in a JXPathContext object and then transfering control over to
     * the new overloaded version of this method.
     */
    public void saveFormToModel(Widget frmModel, Object objModel) throws BindingException {
        if (objModel == null) {
            throw new NullPointerException("null object passed to saveFormToModel() method");
        }

        JXPathContext jxpc = makeJXPathContext(objModel);
        saveFormToModel(frmModel, jxpc);
    }

    private void applyLeniency(JXPathContext jxpc) {
        if (this.commonAtts.leniency != null) {
            jxpc.setLenient(this.commonAtts.leniency.booleanValue());
        }
    }
    
    private JXPathContext makeJXPathContext(Object objModel) {
        JXPathContext jxpc;
        if (!(objModel instanceof JXPathContext)) {
            jxpc = JXPathContext.newContext(objModel);
            jxpc.setLenient(true);
            if (objModel instanceof Node) {
                jxpc.setFactory(new DOMFactory());
            }
        } else {
            jxpc = (JXPathContext) objModel;
        }
        return jxpc;
    }

    /**
     * Receives the Avalon logger to use.
     * Subclasses should always start with <code>super.enableLogging(logger)
     * </code> in possible overriding versions.
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    protected Logger getLogger() {
        return logger;
    }
}
