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
package org.apache.cocoon.forms.binding;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.util.jxpath.DOMFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPropertyPointer;
import org.apache.commons.jxpath.util.TypeUtils;

/**
 * Provides a base class for hooking up Binding implementations that use the
 * Jakarta Commons <a href="http://jakarta.apache.org/commons/jxpath/index.html">
 * JXPath package</a>.
 *
 * @version $Id$
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
     * Gets source location of this binding.
     */
    public String getLocation() {
        return this.commonAtts.location;
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
                throw new RuntimeException("Class \"" + id + "\" not found (" + getLocation() + ")");
            }
        }
        return classBinding;
    }

    /**
     * Helper method that selects a child-widget with a given id from a parent.
     *  
     * @param parent containing the child-widget to return. 
     * @param id of the childWidget to find, if this is <code>null</code> then the parent is returned.
     * @return the selected widget
     * 
     * @throws RuntimeException  if the id is not null and points to a 
     *   child-widget that cannot be found. 
     */
    protected Widget selectWidget(Widget parent, String id) {
        if (id == null) return parent;
        
        Widget childWidget = null;
        
        childWidget = parent.lookupWidget(id);
            
        if (childWidget == null) {
            String containerId = parent.getRequestParameterName();
            if(containerId == null || "".equals(containerId)) {
                containerId = "top-level form-widget";
            } else {
                containerId = "container \"" + containerId + "\"";
            }
            throw new RuntimeException(getClass().getName() + " (" + getLocation() + "): Widget \"" +
                    id + "\" does not exist in the " + containerId +
                    " (" + parent.getLocation() + ").");
        }
        
        return childWidget;
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
        applyNSDeclarations(jxpc);
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
        applyNSDeclarations(jxpc);
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

    private void applyNSDeclarations(JXPathContext jxpc)
    {
        if (this.commonAtts.nsDeclarations != null)
        {
            Iterator keysIter = this.commonAtts.nsDeclarations.keySet().iterator();
            while (keysIter.hasNext())
            {
                String nsuri = (String) keysIter.next();
                String pfx = (String) this.commonAtts.nsDeclarations.get(nsuri);
                jxpc.registerNamespace(pfx, nsuri);
            }
        }
    }

    private JXPathContext makeJXPathContext(Object objModel) {
        JXPathContext jxpc;
        if (!(objModel instanceof JXPathContext)) {
            jxpc = JXPathContext.newContext(objModel);
            jxpc.setLenient(true);
            jxpc.setFactory(new BindingJXPathFactory());
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
    
    /**
     * JXPath factory that combines the DOMFactory and support for collections.
     */
    private static class BindingJXPathFactory extends DOMFactory {
        
        public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
            if (createCollectionItem(context, pointer, parent, name, index)) {
                return true;
            // AG: If this is a bean, then the object is supposed to exists.  
            } else if (pointer instanceof BeanPropertyPointer) {
                return createBeanField(context, pointer, parent, name, index);
            } else {
                return super.createObject(context, pointer, parent, name, index);
            }
        }
        
        private boolean createCollectionItem(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
            // FIXME: don't clearly understand how this works.
            // see http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=111148567029114&w=2
            final Object o = context.getValue(name);
            if (o == null) {
                return false;
            } 
            if (o instanceof Collection) {
                ((Collection)o).add(null);
            } else if(o.getClass().isArray()) {
                // not yet supported
                return false;
            } else {
                return false;
            }
            return true;
        }

        // AG: Create the Object for the field as defined in the Bean.
        // The value we will set here is not important. JXPath knows that it is UNITIALIZED.
        // if we set it Pointer Value to null then the code will throw an exception.
        //
        // In short, there is no harm. The value will never show up.
        // TODO: Manage other forms' types as Date, Bean and others not covered by this method.
        private boolean createBeanField(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
            try {
                Class clazz = parent.getClass().getDeclaredField(name).getType();
                Object o = context.getValue(name);
                if (o == null) {
                    final Class[] parametersTypes = {String.class};
                    final Object[] initArgs = {"0"};
                    try {
                        // AG: Here we service Booleans, Strings and Number() + his Direct know subclasses:
                        // (BigDecimal, BigInteger, Byte, Double, Float, Integer, Long, Short)
                        // as well as other classes that use an String as Constructor parameter.
                        o = clazz.getConstructor(parametersTypes).newInstance(initArgs);
                    } catch (Exception e) {
                        // AG: The class has not a constructor using a String as a parameter.
                        // ie: Boolean(String), Integer(String), etc.
                        // Lets try with a constructor with no parameters. ie: Number().
                        o = clazz.newInstance();
                    }
                } else if (TypeUtils.canConvert(o, clazz)) {
                    o = TypeUtils.convert(o, clazz);
                }
                if (o != null) {
                    pointer.setValue(o);
                    return true;  // OK. We have an initial Object of the right Class initialized.
                }
            } catch (Exception e) {
                // TODO: Output info in logs.
            }
            return false;
        }
    }
}
