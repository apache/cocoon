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
dojo.registerModulePath("cocoon.ajax", "../ajax/js");
dojo.require("cocoon.ajax.BUHandler");

cocoon.portal = {};

cocoon.portal.process = function(uri) {
  if ( uri.indexOf("?") == -1 ) {
    uri =  uri + "?cocoon-ajax=true";
  } else {
    uri = uri + "&cocoon-ajax=true";
  }
  dojo.io.bind({
    url : uri,
    load : function(type, data, evt) {
      cocoon.portal.handleBrowserUpdate(data);
    },
    method: "POST",
    mimetype: "text/xml"
  });
}

cocoon.portal.handleBrowserUpdate = function(doc) {
  var updater = new cocoon.ajax.BUHandler();
  updater.handlers['coplet'] = function(element) {
    var content = dojo.dom.getFirstChildElement(element);
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
      var newElement = cocoon.ajax.insertionHelper.importNode(content, document).element;
      // Warn: it's replace(new, old)!!
      oldElement.parentNode.replaceChild(newElement, oldElement);
      // Ensure the new node has the correct id
      newElement.setAttribute("id", id);
    }
  };
  updater.processResponse(doc);
}
