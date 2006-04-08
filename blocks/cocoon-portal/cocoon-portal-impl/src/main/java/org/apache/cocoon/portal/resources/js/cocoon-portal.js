/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
cocoon.portal = {};

cocoon.portal.process = function(uri) {
  var req = cocoon.ajax.newXMLHttpRequest();
  if ( uri.indexOf("?") == -1 ) {
    req.open("GET", uri + "?cocoon-ajax=true");
  } else {
    req.open("GET", uri + "&cocoon-ajax=true");
  }
  req.onreadystatechange = function() {
    if (req.readyState == 4) {
      cocoon.portal.handleBrowserUpdate(req);
    }
  }
  req.send(null);
}

cocoon.portal.handleBrowserUpdate = function(req) {
  if (req.status == 200) {
     // Handle browser update directives
     var doc = req.responseXML;
     if (!doc) {
       cocoon.ajax.BrowserUpdater.handleError("No xml answer", req);
       return;
     }
     var updater = new cocoon.ajax.BrowserUpdater();
     updater.handlers['coplet'] = function(element) {
       var content = cocoon.ajax.DOMUtils.firstChildElement(element);
       var id = content.getAttribute("id");
       if (!id) {
         alert("no id found on update element");
         return;
       }    
       var oldElement = document.getElementById(id);
       // in some cases, the server sends a coplet that
       // is currently not displayed. So it's not an
       // error if the element can't be found.
       if (oldElement) {
         var newElement = cocoon.ajax.DOMUtils.importNode(content, document);
        
         // Warn: it's replace(new, old)!!
         oldElement.parentNode.replaceChild(newElement, oldElement);
         // Ensure the new node has the correct id
         newElement.setAttribute("id", id);
       }    
     }
     updater.processResponse(doc, req);
  } else {
     cocoon.ajax.BrowserUpdater.handleError("Request failed - status=" + req.status, req);
  }
}
