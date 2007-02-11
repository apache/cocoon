/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.forms.formmodel;

import java.util.List;

public class GoogleMapValue {
    
    private float lng;
    private float lat;
    private int zoom;
    private List markers;
    private int currentMarker;
    private GoogleMapMarker usermarker;
    
    
    public float getLat() {
        return lat;
    }
    public void setLat(float lat) {
        this.lat = lat;
    }
    public float getLng() {
        return lng;
    }
    public void setLng(float lng) {
        this.lng = lng;
    }
    public int getZoom() {
        return zoom;
    }
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
    public int getCurrentMarker() {
        return currentMarker;
    }
    public void setCurrentMarker(int currentMarker) {
        this.currentMarker = currentMarker;
    }
    public List getMarkers() {
        return markers;
    }
    public void setMarkers(List markers) {
        this.markers = markers;
    }
    public String toString() {
        return "[lat=" + getLat() + ", lng=" + getLng() + " ,zoom=" +getZoom() + ", currentMarker="+getCurrentMarker()+",usermarker="+getUsermarker()+"]";
    }
    public GoogleMapMarker getUsermarker() {
        return usermarker;
    }
    public void setUsermarker(GoogleMapMarker usermarker) {
        this.usermarker = usermarker;
    }
    
    
    
}
