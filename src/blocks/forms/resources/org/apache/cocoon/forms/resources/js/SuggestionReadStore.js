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
dojo.provide("cocoon.forms.SuggestionReadStore");
dojo.require("dojox.data.QueryReadStore");

/**
 * CForms SuggestionReadStore Widget.
 * Adapts Dojo's typical ReadStore queries to those expected by Cocoon
 *  -- except id queries are done with an id parameter, not the old phase=init means filter contains an id scheme 
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.SuggestionReadStore", [dojox.data.QueryReadStore], {	
   
  client: "", // id of my clent widget, to get name and continuation-id
  
  _fetchItems: function(request, fetchHandler, errorHandler){
    request.serverQuery = request.serverQuery || {};
    if (request.query) request.serverQuery.filter = request.query.label;
    request.serverQuery.locale = dojo.locale.replace("-","_");
    if (request.count < Infinity) { 
      request.serverQuery.start = request.start;
      request.serverQuery.count = request.count;
    }
    if (this.client) { // we are calling a widget's suggestion-list handler 
      var widget = dijit.byId(this.client);
      if (widget) {
        request.serverQuery.widget = widget.name;
        // NB. To use this in a cform, you need a properly populated 'continuation-id' field 
        request.serverQuery["continuation-id"] = widget.getForm()["continuation-id"].value;
      }
    }
    return this.inherited(arguments);
  }
   
   
});
