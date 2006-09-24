package org.apache.cocoon.forms.formmodel;

import org.apache.excalibur.xml.sax.XMLizable;

public class GoogleMapMarker {
    
    private float lng;
    private float lat;
    private XMLizable text;
    
    
    
    
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
    public XMLizable getText() {
        return text;
    }
    public void setText(XMLizable text) {
        this.text = text;
    }
    
    public String toString() {
        return "[lat=" + getLat() + ", lng=" + getLng() + "]";
    }
    
    
}
