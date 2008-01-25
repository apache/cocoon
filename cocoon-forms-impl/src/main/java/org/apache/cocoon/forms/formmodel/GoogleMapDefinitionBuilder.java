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

import java.util.ArrayList;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Element;

/**
 * Builds {@link GoogleMapDefinition}s.
 */
public final class GoogleMapDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement, WidgetDefinitionBuilderContext context)
    throws Exception {
        GoogleMapDefinition definition = new GoogleMapDefinition();
        setupDefinition(widgetElement, definition, context);
        setDisplayData(widgetElement, definition);

        // Initial value
        Element initialValueElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "initial-value", false);
        if (initialValueElement != null) {
            GoogleMapValue googleMapValue = new GoogleMapValue();
            googleMapValue.setLng(Float.valueOf(initialValueElement.getAttribute("lng")).floatValue());
            googleMapValue.setLat(Float.valueOf(initialValueElement.getAttribute("lat")).floatValue());
            googleMapValue.setZoom(Integer.valueOf(initialValueElement.getAttribute("zoom")).intValue());
            
            // usermarker
            Element userMarkerElement = DomHelper.getChildElement(initialValueElement, FormsConstants.DEFINITION_NS, "usermarker", false);
            if (userMarkerElement != null) {
                GoogleMapMarker usermarker = new GoogleMapMarker();
                usermarker.setLng(Float.valueOf(userMarkerElement.getAttribute("lng")).floatValue());
                usermarker.setLat(Float.valueOf(userMarkerElement.getAttribute("lat")).floatValue());
                googleMapValue.setUsermarker(usermarker);
            }
            
            
            Element markersElement = DomHelper.getChildElement(initialValueElement, FormsConstants.DEFINITION_NS, "markers", false);
            if (markersElement != null) {
                ArrayList markers = new ArrayList();
                Element[] markerElements = DomHelper.getChildElements(markersElement,FormsConstants.DEFINITION_NS,"marker");
                for (int i = 0; i < markerElements.length; i++) {
                    Element markerElement = markerElements[i];
                    GoogleMapMarker googleMapMarker = new GoogleMapMarker();
                    googleMapMarker.setLng(Float.valueOf(markerElement.getAttribute("lng")).floatValue());
                    googleMapMarker.setLat(Float.valueOf(markerElement.getAttribute("lat")).floatValue());
                    XMLizable data = null;
                    Element dataElement = DomHelper.getChildElement(markerElement, FormsConstants.DEFINITION_NS, "text");
                    if (dataElement != null) {
                        data = DomHelper.compileElementContent(dataElement);
                    }
                    googleMapMarker.setText(data);
                    markers.add(googleMapMarker);
                }
                googleMapValue.setMarkers(markers);
            }
            googleMapValue.setCurrentMarker(-1);
            
            definition.setInitialValue(googleMapValue);
        }
        
        definition.makeImmutable();
        return definition;
    }
}
