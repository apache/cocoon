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
 */package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.*;
import org.apache.cocoon.xml.AttributesImpl;

/**
 * A server-side map widget. An ImageMap widget can cause a {@link ImageMapEvent} to be triggered
 * on the server side, which will be handled by either the event handlers defined in the
 * form definition, and/or by the {@link org.apache.cocoon.forms.event.FormHandler FormHandler}
 * registered with the form, if any. An ImageMap widget is basically an Action widget
 * displayed as an image and with mouse coordinates stored upon clicking.
 * The image's URI can be set or get, or bind via the binding framework, mouse coordinates
 * can be either retrieved from the ImageMapEvent triggered or from the widget itself.
 *
 * @version $Id$
 * @since 2.1.8
 */
public class ImageMap extends AbstractWidget implements ActionListenerEnabled {

    // XML element and attributes
    public static final String COMMAND_AT = "command";
    public static final String VALUE_EL = "imageuri";
    public static final String ONACTION_EL = "on-action";
    public static final String IMAGEMAP_EL = "imagemap";

    private final ImageMapDefinition definition;
    private ActionListener listener;
    private String imgURI; // URI of widget's image
    private int x; // Mouse x coordinate
    private int y; // Mouse y coordinate

    
    public ImageMap(ImageMapDefinition definition) {
        super(definition);
        this.definition = definition;
        this.imgURI = definition.getImageURI();
        this.x = 0;
        this.y = 0;
    }

    public WidgetDefinition getDefinition() {
        return this.definition;
    }

    // Retrieves mouse coordinates
    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    // Get/set image URI
    public String getImageURI() {
        if (this.imgURI != null) {
            return this.imgURI;
        } else {
            return "";
        }
    }

    public void setImageURI(String newImgURI) {
        this.imgURI= newImgURI;
    }

    // The set/getValue methods are used to set the widget's image URI during binding
    public void setValue(Object newImgURI) {
        setImageURI(newImgURI.toString());
    }

    public Object getValue() {
        return getImageURI();
    }

    public void readFromRequest(final FormContext formContext) {
        if (!getCombinedState().isAcceptingInputs()) {
            return;
        }

        Form form = getForm();

        // Set the submit widget if we can determine it from the request
        String fullId = getRequestParameterName();
        Request request = formContext.getRequest();

        // Extracts mouse coordinates from request (ignores malformed numbers)
        try {
            this.x = (new Integer(formContext.getRequest().getParameter(fullId + ".x"))).intValue();
            this.y = (new Integer(formContext.getRequest().getParameter(fullId + ".y"))).intValue();
        } catch (java.lang.NumberFormatException e) {
            this.x = 0;
            this.y = 0;
        }

        String value = request.getParameter(fullId);
        if (value != null && value.length() > 0) {
            form.setSubmitWidget(this);

        } else {
            // Special workaround an IE bug for <input type="image" name="foo"> :
            // in that case, IE only sends "foo.x" and "foo.y" and not "foo" whereas
            // standards-compliant browsers such as Mozilla do send the "foo" parameter.
            //
            // Note that since actions are terminal widgets, there's no chance of conflict
            // with a child "x" or "y" widget.
            value = request.getParameter(fullId + ".x");
            if ((value != null) && value.length() > 0) {
                form.setSubmitWidget(this);
            }
        }

        if (form.getSubmitWidget() == this) {
            form.addWidgetEvent(new ImageMapEvent(this, definition.getActionCommand()));

            handleActivate();
        }
    }

    /**
     * Adds the @imageuri attribute to the XML element
     */
    public AttributesImpl getXMLElementAttributes() {
        AttributesImpl attrs = super.getXMLElementAttributes();
        attrs.addCDATAAttribute("imageuri", this.imgURI);
        return attrs;
    }

    /**
     * Handle the fact that this action was activated. The default here is to end the
     * current form processing and redisplay the form, which means that actual behaviour
     * should be implemented in event listeners.
     */
    protected void handleActivate() {
        getForm().endProcessing(true);
    }

    /**
     * Always return <code>true</code> (an action has no validation)
     */
    public boolean validate() {
        return true;
    }

    public String getXMLElementName() {
        return IMAGEMAP_EL;
    }

    /**
     * Adds an ActionListener to this widget instance. Listeners defined
     * on the widget instance will be executed in addtion to any listeners
     * that might have been defined in the widget definition.
     */
    public void addActionListener(ActionListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    public void removeActionListener(ActionListener listener) {
        this.listener = WidgetEventMulticaster.remove(this.listener, listener);
    }

    private void fireActionEvent(ActionEvent event) {
        if (this.listener != null) {
            this.listener.actionPerformed(event);
        }
    }

    public void broadcastEvent(WidgetEvent event) {
        if (event instanceof ActionEvent) {
            this.definition.fireActionEvent((ActionEvent)event);
            fireActionEvent((ActionEvent)event);
        } else {
            // Other kinds of events
            super.broadcastEvent(event);
        }
    }

}
