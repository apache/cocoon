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
import java.util.List;
import java.util.Locale;

import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.xml.AttributesImpl;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 */
public class GoogleMap extends AbstractWidget   {

    private static final String GOOGLEMAP_FIELD_EL = "googlemap";
    private static final String VALUE_EL = "value";
    private static final String MARKERS_EL = "markers";
    private static final String MARKER_EL = "marker";
    private static final String USERMARKER_EL = "usermarker";
    private static final String MARKER_TEXT_EL = "text";

    private GoogleMapValue value;
    private final GoogleMapDefinition definition;


    public GoogleMap(GoogleMapDefinition definition) {
        super(definition);
        this.definition = definition;
    }

    public WidgetDefinition getDefinition() {
        return this.definition;
    }

    public void initialize() {
        GoogleMapValue value = this.definition.getInitialValue();
        if (value != null) {
            setValue(value);
        }
        super.initialize();
    }

    public void readFromRequest(FormContext formContext) {
        if (!getCombinedState().isAcceptingInputs()) {
            return;
        }

        final String prefix = getRequestParameterName();
        String paramLng = formContext.getRequest().getParameter(prefix + "_lng");
        String paramLat = formContext.getRequest().getParameter(prefix + "_lat");
        String paramZoom = formContext.getRequest().getParameter(prefix + "_zoom");
        String currentMarker = formContext.getRequest().getParameter(prefix + "_current");
        String userMarkerLng = formContext.getRequest().getParameter(prefix + "_usermarker-lng");
        String userMarkerLat = formContext.getRequest().getParameter(prefix + "_usermarker-lat");

        GoogleMapValue newValue = new GoogleMapValue();
        newValue.setLng(Float.valueOf(paramLng).floatValue());
        newValue.setLat(Float.valueOf(paramLat).floatValue());
        newValue.setZoom(Integer.valueOf(paramZoom).intValue());
        newValue.setCurrentMarker(Integer.valueOf(currentMarker).intValue());
        newValue.setMarkers(this.value.getMarkers());

        if (userMarkerLng != null && userMarkerLat != null) {
            try {
                GoogleMapMarker marker = new GoogleMapMarker();
                marker.setLng(Float.valueOf(userMarkerLng).floatValue());
                marker.setLat(Float.valueOf(userMarkerLat).floatValue());
                newValue.setUsermarker(marker);
            } catch (NumberFormatException e) { /* ignored */ }
        }

        GoogleMapValue oldValue = this.value;
        value = newValue;

        if (!value.equals(oldValue)) {
            getForm().addWidgetEvent(new ValueChangedEvent(this, oldValue, value));
        }
    }

    /**
     * @return "googlemap"
     */
    public String getXMLElementName() {
        return GOOGLEMAP_FIELD_EL;
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // value element
        AttributesImpl attributesImpl = new AttributesImpl();
        attributesImpl.addAttribute("","lng","lng","CDATA",this.value.getLng()+"");
        attributesImpl.addAttribute("","lat","lat","CDATA",this.value.getLat()+"");
        attributesImpl.addAttribute("","zoom","zoom","CDATA",this.value.getZoom()+"");
        attributesImpl.addAttribute("","current","current","CDATA",this.value.getCurrentMarker()+"");

        contentHandler.startElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL, attributesImpl);

        // usermarker
        if (this.value.getUsermarker() != null) {
            attributesImpl = new AttributesImpl();
            attributesImpl.addAttribute("","lng","lng","CDATA",this.value.getUsermarker().getLng()+"");
            attributesImpl.addAttribute("","lat","lat","CDATA",this.value.getUsermarker().getLat()+"");
            contentHandler.startElement(FormsConstants.INSTANCE_NS, USERMARKER_EL, FormsConstants.INSTANCE_PREFIX_COLON + USERMARKER_EL, attributesImpl);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, USERMARKER_EL, FormsConstants.INSTANCE_PREFIX_COLON + USERMARKER_EL);
        }

        // markers
        List markers = this.value.getMarkers();
        if (markers != null) {
            contentHandler.startElement(FormsConstants.INSTANCE_NS, MARKERS_EL, FormsConstants.INSTANCE_PREFIX_COLON + MARKERS_EL, new AttributesImpl());
            for (Iterator iter = markers.iterator(); iter.hasNext();) {
                GoogleMapMarker marker = (GoogleMapMarker) iter.next();
                attributesImpl = new AttributesImpl();
                attributesImpl.addAttribute("","lng","lng","CDATA",marker.getLng()+"");
                attributesImpl.addAttribute("","lat","lat","CDATA",marker.getLat()+"");
                contentHandler.startElement(FormsConstants.INSTANCE_NS, MARKER_EL, FormsConstants.INSTANCE_PREFIX_COLON + MARKER_EL, attributesImpl);
                contentHandler.startElement(FormsConstants.INSTANCE_NS, MARKER_TEXT_EL, FormsConstants.INSTANCE_PREFIX_COLON + MARKER_TEXT_EL, new AttributesImpl());
                marker.getText().toSAX(contentHandler);
                contentHandler.endElement(FormsConstants.INSTANCE_NS, MARKER_TEXT_EL, FormsConstants.INSTANCE_PREFIX_COLON + MARKER_TEXT_EL);
                contentHandler.endElement(FormsConstants.INSTANCE_NS, MARKER_EL, FormsConstants.INSTANCE_PREFIX_COLON + MARKER_EL);
            }
            contentHandler.endElement(FormsConstants.INSTANCE_NS, MARKERS_EL, FormsConstants.INSTANCE_PREFIX_COLON + MARKERS_EL);
        }
        contentHandler.endElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL);
    }

    public Object getValue() {
        return value;
    }

    /**
     * Sets value of the field. If value is null, it is considered to be false
     * (see class comment).
     */
    public void setValue(Object object) {
        if (!(object instanceof GoogleMapValue)) {
            throw new RuntimeException("Cannot set value of googlemap \"" + getRequestParameterName() + "\" to a non-GoogleMapValue.");
        }

        Object oldValue = value;
        value = (GoogleMapValue)object;
        if (!value.equals(oldValue)) {
            Form form = getForm();
            form.addWidgetUpdate(this);
        }
    }

    public void broadcastEvent(WidgetEvent event) {
        if (event instanceof ValueChangedEvent) {
            //
        } else {
            // Other kinds of events
            super.broadcastEvent(event);
        }
    }

}
