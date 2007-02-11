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
package org.apache.cocoon.forms.formmodel;

import java.util.Iterator;
import java.util.Locale;

import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.validation.ValidationError;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A general-purpose abstract Widget which can hold zero or more widgets.
 *
 * @version $Id$
 */
public abstract class AbstractContainerWidget extends AbstractWidget
                                              implements ContainerWidget {

    /**
     * List of contained widgets.
     */
    protected WidgetList widgets;

    /**
     * validation errors on container widgets
     */
    protected ValidationError validationError;

    /**
     * Constructs AbstractContainerWidget
     */
    public AbstractContainerWidget(AbstractContainerDefinition definition) {
        super(definition);
        widgets = new WidgetList();
    }

    /**
     * Called after widget's environment has been setup,
     * to allow for any contextual initalization such as
     * looking up case widgets for union widgets.
     */
    public void initialize() {
        Iterator it = this.getChildren();
        while(it.hasNext()) {
          ((Widget)it.next()).initialize();
        }

        super.initialize();
    }

    public void addChild(Widget widget) {
        // order is important
        widgets.addWidget(widget);
        widget.setParent(this);
    }

    public boolean hasChild(String id) {
        return widgets.hasWidget(id);
    }

    public Widget getChild(String id) {
        return widgets.getWidget(id);
    }

    public Iterator getChildren() {
        return widgets.iterator();
    }

    public int getSize() {
        return widgets.getWidgetList().size();
    }

    /**
     * Delegates the readFromRequest() down to the contained child-widgets.
     *
     * When overriding one should call <code>super.readFromRequest()</code>
     * to allow child-widgets to process the request.
     *
     * Overide only to add possible request-reading statements on the containment level.
     *
     * @param formContext to be passed to the {@link Widget#readFromRequest(FormContext)}
     *                    of the contained widgets.
     */
    public void readFromRequest(FormContext formContext) {
        if (getCombinedState().isAcceptingInputs()) {
            widgets.readFromRequest(formContext);
        }
    }

    /**
     * Delegates the <code>validate()</code> down to the contained child-widgets,
     * and validates the extra rules on this containment level regardless of
     * children widget's validities.
     *
     * <p>When overriding one should call <code>super.validate()</code> as the first
     * statement to keep in sync with this behaviour.</p>
     *
     * @return <code>true</code> only if all contained widgets are valid and the
     *         extra validation rules on this containment level are ok.
     */
    public boolean validate() {
        if (!getCombinedState().isValidatingValues()) {
            this.wasValid = true;
            return true;
        }
        // Validate children first, then always validate self. Return combined result.
        final boolean valid = widgets.validate();
        this.wasValid = super.validate() && valid;
        return this.wasValid;
    }

    /**
     * Subclass container widgets can call this super.generateItemSaxFragment(..)
     * to just insert the child-widget content wrapped in a @lt;fi:widgets@gt;
     *
     * @param contentHandler where the SAX is sent to via {@link Widget#generateSaxFragment(ContentHandler, Locale)}
     * @param locale
     * @throws SAXException
     */
    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        if (getCombinedState().isDisplayingValues()) {
            widgets.generateSaxFragment(contentHandler, locale);
        }
    }
}
