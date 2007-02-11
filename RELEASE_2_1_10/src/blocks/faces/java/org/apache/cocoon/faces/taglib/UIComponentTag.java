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
package org.apache.cocoon.faces.taglib;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.taglib.Tag;
import org.apache.cocoon.taglib.XMLProducerTagSupport;

import org.apache.cocoon.faces.FacesUtils;
import org.apache.cocoon.faces.renderkit.XMLResponseWriter;
import org.apache.commons.lang.BooleanUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public abstract class UIComponentTag extends XMLProducerTagSupport {

    private static final String CREATED_COMPONENTS = "javax.faces.webapp.COMPONENT_IDS";
    private static final String CREATED_FACETS = "javax.faces.webapp.FACET_NAMES";

    //
    // Attributes
    //

    private String id;
    private String binding;
    private String rendered;

    //
    // Internal state
    //

    private FacesContext context;
    private UIComponent component;

    private boolean created;
    private List createdComponents;
    private List createdFacets;

    //
    // Services for subclasses
    //

    protected final FacesContext getFacesContext() {
        return this.context;
    }

    protected final Application getApplication() {
        return getFacesContext().getApplication();
    }

    protected final UIComponent getComponentInstance() {
        return this.component;
    }

    protected final boolean getCreated() {
        return this.created;
    }

    protected final ValueBinding createValueBinding(String valueRef) {
        return getApplication().createValueBinding(valueRef);
    }

    protected final Object evaluate(String value) {
        if (FacesUtils.isExpression(value)) {
            return createValueBinding(value).getValue(getFacesContext());
        }

        return value;
    }

    protected final boolean evaluateBoolean(String value) {
        if (FacesUtils.isExpression(value)) {
            Boolean obj = (Boolean) createValueBinding(value).getValue(getFacesContext());
            return obj.booleanValue();
        }

        return BooleanUtils.toBoolean(value);
    }

    protected final int evaluateInteger(String value) {
        if (FacesUtils.isExpression(value)) {
            Number obj = (Number) createValueBinding(value).getValue(getFacesContext());
            return obj.intValue();
        }

        return Integer.parseInt(value);
    }

    protected final long evaluateLong(String value) {
        if (FacesUtils.isExpression(value)) {
            Number obj = (Number) createValueBinding(value).getValue(getFacesContext());
            return obj.longValue();
        }

        return Long.parseLong(value);
    }

    protected final double evaluateDouble(String value) {
        if (FacesUtils.isExpression(value)) {
            Number obj = (Number) createValueBinding(value).getValue(getFacesContext());
            return obj.doubleValue();
        }

        return Double.parseDouble(value);
    }

    //
    // Tag Interface
    //

    public void setup(SourceResolver resolver, Map objectModel, Parameters parameters)
    throws SAXException, IOException {
        super.setup(resolver, objectModel, parameters);

        // Obtain Faces context
        this.context = FacesUtils.getFacesContext(this, objectModel);

        // Set up response writer
        ResponseWriter writer = this.context.getResponseWriter();
        if (writer == null) {
            // Not calling RenderKit here.
            Request request = ObjectModelHelper.getRequest(objectModel);
            writer = new XMLResponseWriter(this.xmlConsumer, null, request.getCharacterEncoding());
            this.context.setResponseWriter(writer);
        }
    }

    public int doStartTag(String namespaceURI, String localName, String qName, Attributes attrs)
    throws SAXException {
        final UIComponentTag parentTag = findParent();

        this.component = findComponent(parentTag);

        try {
            if (!isSuppressed()) {
                if (!this.component.getRendersChildren()) {
                    encodeBegin();
                    getFacesContext().getResponseWriter().flush();
                }
            }
        } catch (IOException e) {
            throw new SAXException("Exception in doStartTag", e);
        }

        return getDoStartValue();
    }

    public int doEndTag(String namespaceURI, String localName, String qName)
    throws SAXException {
        removeOldChildren();
        removeOldFacets();
        try {
            if (!isSuppressed()) {
                if (this.component.getRendersChildren()) {
                    encodeBegin();
                    encodeChildren();
                }
                encodeEnd();
                getFacesContext().getResponseWriter().flush();
            }
        } catch (IOException e) {
            throw new SAXException("Exception in doEndTag", e);
        }

        return getDoEndValue();
    }


    //
    // Lifecycle
    //

    public void recycle() {
        super.recycle();

        this.component = null;
        this.context = null;

        this.id = null;
        this.binding = null;
        this.created = false;
        this.rendered = null;

        this.createdComponents = null;
        this.createdFacets = null;
    }


    //
    // Methods to be implemented or overridden in subclasses
    //

    protected abstract String getComponentType();

    protected abstract String getRendererType();

    protected int getDoEndValue() {
        return Tag.EVAL_PAGE;
    }

    protected int getDoStartValue() {
        return Tag.EVAL_BODY;
    }

    protected void encodeBegin() throws IOException {
        this.component.encodeBegin(this.context);
    }

    protected void encodeChildren() throws IOException {
        this.component.encodeChildren(this.context);
    }

    protected void encodeEnd() throws IOException {
        this.component.encodeEnd(this.context);
    }

    protected void setProperties(UIComponent component) {
        if (this.rendered != null) {
            if (FacesUtils.isExpression(this.rendered)) {
                ValueBinding vb = createValueBinding(this.rendered);
                component.setValueBinding("rendered", vb);
            } else {
                component.setRendered(BooleanUtils.toBoolean(this.rendered));
            }
        }

        if (getRendererType() != null) {
            component.setRendererType(getRendererType());
        }
    }

    protected final void setProperty(UIComponent component, String name, String value) {
        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                ValueBinding vb = createValueBinding(value);
                component.setValueBinding(name, vb);
            } else {
                component.getAttributes().put(name, value);
            }
        }
    }

    protected final void setBooleanProperty(UIComponent component, String name, String value) {
        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                ValueBinding vb = createValueBinding(value);
                component.setValueBinding(name, vb);
            } else {
                component.getAttributes().put(name, BooleanUtils.toBooleanObject(value));
            }
        }
    }

    protected final void setIntegerProperty(UIComponent component, String name, String value) {
        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                ValueBinding vb = createValueBinding(value);
                component.setValueBinding(name, vb);
            } else {
                component.getAttributes().put(name, new Integer(value));
            }
        }
    }


    //
    // Implementation methods
    //

    private UIComponentTag findParent() {
        Tag parent = this;
        do {
            parent = parent.getParent();
        } while (parent != null && !(parent instanceof UIComponentTag));

        return (UIComponentTag) parent;
    }

    /**
     * Creates or finds (if pre-created) UIComponent for this tag instance
     */
    private UIComponent findComponent(UIComponentTag parentTag) {
        // Check if this is root
        if (parentTag == null) {
            UIComponent root = getFacesContext().getViewRoot();
            setProperties(root);
            if (this.id != null) {
                root.setId(this.id);
            }
            return root;
        }

        String id = createId();

        // Create  facet
        String facet = getFacetName();
        if (facet != null) {
            return createFacet(parentTag, facet, id);
        }

        // Create child
        return createChild(parentTag, id);
    }

    /**
     * Get name of the facet or null
     */
    private String getFacetName() {
        final Tag parentTag = getParent();
        if (parentTag instanceof FacetTag) {
            return ((FacetTag) parentTag).getName();
        }

        return null;
    }

    private boolean isSuppressed() {
        if (getFacetName() != null) {
            return true;
        }

        if (!this.component.isRendered()) {
            return true;
        }

        for (UIComponent component = this.component.getParent(); component != null; component = component.getParent()) {
            if (!component.isRendered()) {
                return true;
            }

            if (component.getRendersChildren()) {
                return true;
            }
        }

        return false;
    }

    private void addChild(UIComponent child) {
        if (createdComponents == null) {
            createdComponents = new ArrayList();
        }
        createdComponents.add(child.getId());
    }

    private void addFacet(String name) {
        if (createdFacets == null) {
            createdFacets = new ArrayList();
        }
        createdFacets.add(name);
    }

    private UIComponent createComponent(String id) {
        UIComponent component;
        Application application = context.getApplication();
        if (this.binding != null) {
            ValueBinding vb = application.createValueBinding(this.binding);
            component = application.createComponent(vb, context, getComponentType());
            component.setValueBinding("binding", vb);
        } else {
            component = application.createComponent(getComponentType());
        }
        component.setId(id);
        this.created = true;

        setProperties(component);
        return component;
    }

    private UIComponent createChild(UIComponentTag parentTag, String id) {
        final UIComponent parent = parentTag.getComponentInstance();
        UIComponent component = FacesUtils.getChild(parent, id);
        if (component == null) {
            component = createComponent(id);
            parent.getChildren().add(component);
        }

        parentTag.addChild(component);
        return component;
    }

    private UIComponent createFacet(UIComponentTag parentTag, String name, String id) {
        final UIComponent parent = parentTag.getComponentInstance();
        UIComponent component = (UIComponent) parent.getFacets().get(name);
        if (component == null) {
            component = createComponent(id);
            parent.getFacets().put(name, component);
        }

        parentTag.addFacet(name);
        return component;
    }

    private String createId() {
        if (this.id == null) {
            return getFacesContext().getViewRoot().createUniqueId();
        }

        return this.id;
    }

    private void removeOldChildren() {
        List oldList = (List) component.getAttributes().remove(CREATED_COMPONENTS);
        if (oldList != null) {
            if (createdComponents != null) {
                for (Iterator olds = oldList.iterator(); olds.hasNext();) {
                    String old = (String) olds.next();
                    if (!createdComponents.contains(old)) {
                        FacesUtils.removeChild(component, old);
                    }
                }
            } else {
                for (Iterator i = oldList.iterator(); i.hasNext();) {
                    FacesUtils.removeChild(component, (String) i.next());
                }
            }
        }

        if (createdComponents != null) {
            component.getAttributes().put(CREATED_COMPONENTS, createdComponents);
            createdComponents = null;
        }
    }

    private void removeOldFacets() {
        List oldList = (List) component.getAttributes().remove(CREATED_FACETS);
        if (oldList != null) {
            if (createdFacets != null) {
                for (Iterator olds = oldList.iterator(); olds.hasNext();) {
                    String old = (String) olds.next();
                    if (!createdFacets.contains(old)) {
                        component.getFacets().remove(old);
                    }
                }
            } else {
                for (Iterator olds = oldList.iterator(); olds.hasNext(); ) {
                    String old = (String) olds.next();
                    component.getFacets().remove(old);
                }
            }
        }

        if (createdFacets != null) {
            component.getAttributes().put(CREATED_FACETS, createdFacets);
            createdFacets = null;
        }
    }


    //
    // Setters / Getters
    //

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBinding() {
        return this.binding;
    }

    public void setBinding(String binding) {
        if (!FacesUtils.isExpression(binding)) {
            throw new IllegalArgumentException("Binding value must be an expression");
        }

        this.binding = binding;
    }

    public String getRendered() {
        return this.rendered;
    }

    public void setRendered(String rendered) {
        this.rendered = rendered;
    }
}
