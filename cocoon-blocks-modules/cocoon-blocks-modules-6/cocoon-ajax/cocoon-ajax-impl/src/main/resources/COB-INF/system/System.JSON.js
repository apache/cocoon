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

importClass(Packages.java.util.Collection);
importClass(Packages.java.util.HashMap);
importClass(Packages.java.util.Map);
importClass(Packages.org.apache.cocoon.xml.ParamSaxBuffer);

/*

    Extensible System Library of static JSON Helper Functions

    @version $Id$

*/

// setup the namespace
var System = System || {};
System.JSON = System.JSON || {};


/* 
    Lookup an i18n key, return a String (there will be no tags)
    This function is intended to be used for embedding the result of i18n lookups in JSON responses
    
    @param key String the i18n key
    @param catalogue Bundle the i18n Catalogue Bundle to look up in
    @param params String[] optional parameters for the i18n message
    @return String the looked-up i18n key
    
*/
System.JSON.getI18nMessage = function(key, catalogue, params) {
    if (!params) params = [];
    var value = catalogue.getObject(key);
    if (!value) {
        print("System.JSON.getI18nMessage - Could not find i18n key: " + key);
    } else if (value instanceof ParamSaxBuffer) {
        var parammap = new HashMap(params.length);
        for (var i = 0; i < params.length; i++) parammap.put(""+i, new java.lang.String(params[i]));
        return value.toString(parammap);
    } else {
        return value.toString();
    }
};

/* 
    Serialize a Java Map to a JavaScript Object literal 
    This function is intended for sending a java.util.Map in a JSON Response
    
    @param map java.util.Map the Map to send
    @return String the JSON Object
*/
System.JSON.serializeMap = function(map) {
    var result = [], value;
    var keys = map.keySet().iterator();
    while (keys.hasNext()) {
        var key = keys.next();
        value = this._serializeValue(map.get(key));
        result.push('"' + key + '":' + value);
    }   
    return "{" + result.join(",") + "}";
};

/* 
    Serialize a Java Collection to a JavaScript Array literal 
    This function is intended for sending a java.util.Collection in a JSON Response

    @param col java.util.Collection the Map to send
    @return String the JSON Object
*/
System.JSON.serializeCollection = function(col) {
    var result = [], value;
    var values = col.iterator();
    while (values.hasNext()) {
        value = this._serializeValue(values.next());
        result.push(value);
    }   
    return "[" + result.join(",") + "]";
};

/* 
    Serialize a single value 
    This function is intended for sending a supported java.lang.Object in a JSON Response
    
    Currently supported Objects:
        java.lang.Boolean
        java.lang.Number
        java.lang.String
        java.util.Collection
        java.util.Map
    
    This is limited largely to Java Objects that have an equivalent JavaScript literal
    
    NB. this function is private, it should not be used as a starting point for making a JSON Response
    A JSON Response would typically be made from a Map or a Collection

    @param obj java.lang.Object the Object to send
    @return String the JSON Object
*/
System.JSON._serializeValue = function(obj) {
    if (obj instanceof Map) {
        return this.serializeMap(obj);
    } else if (obj instanceof Collection) {
        return this.serializeCollection(obj);
    } else if (obj instanceof Packages.java.lang.String) {
        return '"' + obj + '"';
    } else if (obj instanceof Packages.java.lang.Number || obj instanceof Packages.java.lang.Boolean) {
        return obj.toString();
    } else {
        return "undefined";
        print("System.JSON._serializeValue - could not serialize: " + obj);
    }
}
