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

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.i18n.I18nUtils;

import org.apache.cocoon.faces.FacesUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import java.io.IOException;
import java.util.Locale;

/**
 * @version CVS $Id$
 */
public class ViewTag extends UIComponentTag {

    protected String locale;

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {
        int rc = super.doStartTag(namespaceURI, localName, qName, atts);

        Response response = ObjectModelHelper.getResponse(objectModel);
        response.setLocale(getFacesContext().getViewRoot().getLocale());

        ResponseWriter writer = getFacesContext().getResponseWriter();
        try {
            writer.startDocument();
        } catch (IOException e) {
            throw new SAXException(e);
        }

        return rc;
    }

    public int doEndTag(String namespaceURI, String localName, String qName)
    throws SAXException {
        int rc = super.doEndTag(namespaceURI, localName, qName);

        StateManager stateManager = getApplication().getStateManager();
        StateManager.SerializedView view;
        try {
            view = stateManager.saveSerializedView(getFacesContext());
        } catch (IllegalStateException e) {
            throw new SAXException(e);
        } catch (Exception e) {
            throw new SAXException("Could not save faces view", e);
        }

        try {
            // TODO: Saving state on the client not supported
            if (view != null) {
                stateManager.writeState(getFacesContext(), view);
            }
        } catch (IOException e) {
            throw new SAXException("Could not save faces view", e);
        }

        ResponseWriter writer = getFacesContext().getResponseWriter();
        try {
            writer.endDocument();
        } catch (IOException e) {
            throw new SAXException("Exception in endDocument", e);
        }

        return rc;
    }

    public String getComponentType() {
        throw new IllegalStateException();
    }

    public String getRendererType() {
        return null;
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        if (this.locale != null) {
            Object localeVal = this.locale;
            if (FacesUtils.isExpression(this.locale)) {
                ValueBinding vb = createValueBinding(this.locale);
                component.setValueBinding("locale", vb);
                localeVal = vb.getValue(getFacesContext());
            }

            Locale value = null;
            if (localeVal instanceof Locale) {
                value = (Locale) localeVal;
            } else if (localeVal instanceof String) {
                value = I18nUtils.parseLocale((String) localeVal);
            }
            ((UIViewRoot) component).setLocale(value);
        }
    }

    public void recycle() {
        super.recycle();
        this.locale = null;
    }
}
