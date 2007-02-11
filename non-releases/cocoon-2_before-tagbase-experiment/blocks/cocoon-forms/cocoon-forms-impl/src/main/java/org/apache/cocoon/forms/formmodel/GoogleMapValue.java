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
