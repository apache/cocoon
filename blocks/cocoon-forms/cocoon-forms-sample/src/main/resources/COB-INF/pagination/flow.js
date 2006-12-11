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
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

function do_paginated() {
    var form = new Form("paginated.def.xml");
    form.createBinding("paginated.bnd.xml");
    
    var countries = new Array();
    countries[0] = "Austria";
    countries[1] = "Italy";
    countries[2] = "Usa";
    countries[3] = "France";
    countries[4] = "Germany";
    countries[5] = "India";
    countries[6] = "UK";
    countries[7] = "Sweden";
    
    var team = new Packages.org.apache.cocoon.forms.samples.dreamteam.Team()
    var i;
    for (i = 1; i < 101 ; i++) {
      var player = new Packages.org.apache.cocoon.forms.samples.dreamteam.TeamMember();
      player.setMemberId(i);
      player.setName("Name "+i);
      player.setCountry(countries[i % countries.length]);
      player.setPosition("Position "+1);
      team.addMember(player);
    }
    form.setAttribute("counter",i-1);
    
    form.load(team);
    form.showForm("paginated-display-pipeline");
    form.save(team);
    
    cocoon.sendPage("paginated-result-pipeline",{"team":team});
}


function doMap() {
    var form = new Form("forms/map-definition.xml");
    form.showForm("map-display-pipeline.jx");
    var model = form.getModel();
    var mapValue1 = form.getWidget().lookupWidget("map1").getValue().toString();
    var mapValue2 = form.getWidget().lookupWidget("map2").getValue().toString();
    var mapValue3 = form.getWidget().lookupWidget("map3").getValue().toString();
    cocoon.sendPage("map-result-pipeline.jx",{"mapValue1":mapValue1,"mapValue2":mapValue2,"mapValue3":mapValue3});
}
