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
package org.apache.cocoon.woody.binding;

import java.util.HashMap;
import java.util.Map;

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
 *
 * @version CVS $Id: JXPathBindingBase.java,v 1.17 2004/03/09 13:54:05 reinhard Exp $
 */
public abstract class JXPathBindingBase implements Binding, LogEnabled {

    /**
     * Avalon Logger to use in all subclasses.
     */
    private Logger logger;

    /**
     * Object holding the values of the common objects on all Bindings.
     */
    private final JXPathBindingBuilderBase.CommonAttributes commonAtts;

    /**
     * Parent binding of this binding.
     */
    protected Binding parent;

    /**
     * Cache of class definitions
     */
    protected Map classes;

    private JXPathBindingBase() {
        this(JXPathBindingBuilderBase.CommonAttributes.DEFAULT);
    }

    protected JXPathBindingBase(
            JXPathBindingBuilderBase.CommonAttributes commonAtts) {
        this.commonAtts = commonAtts;
    }

    /**
     * Sets parent binding.
     */
    public void setParent(Binding binding) {
        this.parent = binding;
    }

    /**
     * Returns binding definition id.
     */
    public String getId() {
        return null;
    }

    public Binding getClass(String id) {
        Binding classBinding = null;
        if (classes != null) {
            // Query cache for class
            classBinding = (Binding)classes.get(id);
        }
        if (classBinding == null) {
            // Query parent for class
            if (parent != null) {
                classBinding = parent.getClass(id);
                // Cache result
                if (classes == null) {
                   classes = new HashMap();
                }
                classes.put(id, classBinding);
            } else {
                // TODO: Improve message to include source location.
                throw new RuntimeException("Class \"" + id + "\" not found.");
            }
        }
        return classBinding;
    }

    protected Widget getWidget(Widget widget, String id) {
        Widget childWidget = widget.getWidget(id);
        if (childWidget != null) {
            return childWidget;
        } else {
            throw new RuntimeException(getClass().getName() + ": Widget \"" +
                    id + "\" does not exist in container \"" +
                    widget.getFullyQualifiedId() + "\" (" +
                    widget.getLocation() + ").");
        }
    }

    /**
     * Performs the actual load binding regardless of the configured value of the "direction" attribute.
     * Abstract method that subclasses need to implement for specific activity.
     */
    public abstract void doLoad(Widget frmModel, JXPathContext jxpc)
        throws BindingException;

    /**
     * Redefines the Binding action as working on a JXPathContext Type rather
     * then on generic objects.
     * Executes the actual loading via {@link #doLoad(Widget, JXPathContext)}
     * depending on the configured value of the "direction" attribute.
     */
    public final void loadFormFromModel(Widget frmModel, JXPathContext jxpc)
            throws BindingException {
        boolean inheritedLeniency = jxpc.isLenient();
        applyLeniency(jxpc);
        if (this.commonAtts.loadEnabled) {
            doLoad(frmModel, jxpc);
        }
        jxpc.setLenient(inheritedLeniency);
    }

    /**
     * Hooks up with the more generic Binding of any objectModel by wrapping
     * it up in a JXPathContext object and then transfering control over to
     * the new overloaded version of this method.
     */
    public final void loadFormFromModel(Widget frmModel, Object objModel)
            throws BindingException {
        if (objModel != null) {
            JXPathContext jxpc = makeJXPathContext(objModel);
            loadFormFromModel(frmModel, jxpc);
        } else {
            throw new NullPointerException(
                    "null object passed to loadFormFromModel() method");
        }
    }

    /**
     * Performs the actual save binding regardless of the configured value of the "direction" attribute.
     * Abstract method that subclasses need to implement for specific activity.
     */
    public abstract void doSave(Widget frmModel, JXPathContext jxpc)
            throws BindingException;

    /**
     * Redefines the Binding action as working on a JXPathContext Type rather
     * then on generic objects.
     * Executes the actual saving via {@link #doSave(Widget, JXPathContext)}
     * depending on the configured value of the "direction" attribute.
     */
    public final void saveFormToModel(Widget frmModel, JXPathContext jxpc)
            throws BindingException{
        boolean inheritedLeniency = jxpc.isLenient();
        applyLeniency(jxpc);
        if (this.commonAtts.saveEnabled) {
            doSave(frmModel, jxpc);
        }
        jxpc.setLenient(inheritedLeniency);
    }

    /**
     * Hooks up with the more generic Binding of any objectModel by wrapping
     * it up in a JXPathContext object and then transfering control over to
     * the new overloaded version of this method.
     */
    public void saveFormToModel(Widget frmModel, Object objModel)
                throws BindingException {
        if (objModel != null) {
            JXPathContext jxpc = makeJXPathContext(objModel);
            saveFormToModel(frmModel, jxpc);    
        } else {
            throw new NullPointerException(
                    "null object passed to saveFormToModel() method");
        }
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
