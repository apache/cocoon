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

importClass(Packages.java.util.HashMap);
importClass(Packages.java.util.Map);
importClass(Packages.org.apache.cocoon.i18n.BundleFactory);
importClass(Packages.org.apache.cocoon.servlet.multipart.MultipartParser);

/*

    A Cocoon System Library to provide upload progress bar support

    @version $Id$

*/


// load the JSON Library
cocoon.load("System.JSON.js");

// setup the extensible System.Upload namespace
var System = System || {};
System.Upload = System.Upload || {};


/*
    Retrieve a JSON Object containg data about the current file-upload
    
    This flowscript is called by the FormsUploadProgress Dojo Widget
    
    Requires the following sitemap parameters :
    
        catalogue : the name of the i18n Catalogue to use for lookups
        location  : the path to the Catalogue's folder
    
*/
System.Upload.status = function(){
    var json = "{}";
    var i18n = null;
    var status = cocoon.session.getAttribute(MultipartParser.UPLOAD_STATUS_SESSION_ATTR);
    if (status instanceof Map && status.size() > 0) {
        var tempMap = new HashMap();
        tempMap.putAll(status);
        var sent = Math.ceil(status.get("sent").intValue()/1024);
        var total = Math.ceil(status.get("total").intValue()/1024);
        var catalogue;
        try {
            i18n = cocoon.getComponent(BundleFactory.ROLE);
            catalogue = i18n.select(cocoon.parameters["location"], cocoon.parameters["catalogue"], cocoon.request.getLocale());
        } finally {
            if (i18n != null) cocoon.releaseComponent(i18n);
        }       
        if (status.get("finished").booleanValue()) {
            tempMap.put("message", System.JSON.getI18nMessage("progress.finished", catalogue, [status.get("uploadsdone"), sent]));
            status.clear();
        } else if (sent) {
            var percent = Math.floor((sent/total)*100) + "";
            if (percent == "NaN") percent = "0";
            tempMap.put("message", System.JSON.getI18nMessage("progress.sent", catalogue, [percent, sent, total, status.get("filename")]));
            tempMap.put("percent", percent);
        }       
        json = System.JSON.serializeMap(tempMap);
    }
    cocoon.sendPage("send-json", {json: json});
};

/*
    A utility function to clear the current Upload Status
    Used for debugging purposes only
    
*/
System.Upload.clear = function(){
    var success = '{"cleared":true}';
    var json = '{"cleared":false}';
    var map = cocoon.session.getAttribute(MultipartParser.UPLOAD_STATUS_SESSION_ATTR);
    if (map instanceof Map) {
        var started = map.get("started") ? map.get("started").booleanValue() : null;
        var finished = map.get("finished") ? map.get("finished").booleanValue() : null;
        if (started && finished) {
            map.clear();
            json = success;
        }
    }
    cocoon.sendPage("send-json", {json: json});
};
