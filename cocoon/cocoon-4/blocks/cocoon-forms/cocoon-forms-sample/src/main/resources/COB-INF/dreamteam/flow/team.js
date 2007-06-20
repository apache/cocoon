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

cocoon.load("servlet:forms:/resource/internal/flow/javascript/Form.js");
importClass (Packages.org.apache.cocoon.forms.util.I18nMessage);

var manager = Packages.org.apache.cocoon.forms.samples.dreamteam.Manager.getManager();

function initDreamTeamChooser() {
  var documentURI = cocoon.parameters["documentURI"];
  // parse the document to a DOM-tree
  var document = loadDocument(documentURI);
  // initialise the Manager
  manager.readPlayers(document);
  cocoon.sendPage("home.form");
}

function prot_showTeam() {
  var persons = manager.getDreamTeam();
  if(persons == null || persons == undefined) {
     var size = 0;
     persons = new Packages.java.util.ArrayList(0);
  }
  else {
    var size = persons.getTeam().size();
  }
  var viewData = {
     list : persons,
     teamsize : size
  };
  cocoon.sendPage("showTeam.form", viewData);
} // showTeam

function prot_buildTeam() {
  var formDisplay = "team.form";
  var formDef = "content/teamDef.xml";
  var formBind = "content/teamBind.xml"

  var allPersons = manager.getAllPersonsByPosition();
  var list = new Packages.org.apache.cocoon.forms.samples.dreamteam.Team();

  var keys = allPersons.keySet().toArray();
  var key;
  var positionList = new Array();
  positionList[0] = {value:"", label: " "};
  for (var i = 0; i < keys.length; i++) {
    key = keys[i];
    // set the label to the localized message using i18n
    positionList[i + 1] = {value: key, label: new I18nMessage(key)};
  }
  /* 
  // add the first player to the team
  var firstPlayer = allPersons.get(keys[0]).getTeam().get(0);
  var member = new Packages.org.apache.cocoon.samples.dreamteam.TeamMember();
  member.position = firstPlayer.position;
  member.memberId = firstPlayer.memberId;
  member.name = firstPlayer.name;
  member.country = firstPlayer.country;
  list.addMember(member);	
  */  

  var form = new Form(formDef);
  form.setAttribute("counter", new java.lang.Long(0));
  form.setAttribute("everyone", allPersons);

  form.createBinding(formBind);
  form.load(list);

  var viewData = {
         list : list,
         positionList: positionList
   };
  form.showForm(formDisplay, viewData);
  form.save(list);
  manager.buildDreamTeam(list);
  // next page presenting the team
  cocoon.sendPage("showteam.html");
}


// function used in the 'position' widget to change the selection-list of the 'memberId' widget
// that displays the names

function updateNameWidget(event) {
  var value = event.source.value;
  var memberIdwidget = event.source.lookupWidget("../memberId");
  if (value != null) {
      // Get the corresponding names list
      var form = event.source.form;
      var everyone = form.getAttribute("everyone");
      if (everyone != null) {
         var nameList = everyone.get(value).team;
         memberIdwidget.setSelectionList(nameList, "memberId", "name");
         // Always set the name value to the first in the list.
         // Note that it will also fire an event on the
         // "name" widget if it already had a value.
         memberIdwidget.setValue(nameList.get(0).memberId);
      }
  } else {
    // Set an empty selection list
    memberIdwidget.setSelectionList(new Packages.org.apache.cocoon.forms.datatype.EmptySelectionList("Choose position first"));
    // Always set the name value to null.
    // Note that it will also fire an event on the
    // "name" widget if it already had a value.
    memberIdwidget.setValue(null);
  }
}

function updateCountryWidget(event) {
  print("memberId changed from " + event.oldValue + " to " + event.newValue);
  var value = event.source.value;
  var form = event.source.form;
  if (form != null){
    var everyone = form.getAttribute("everyone");
    if (everyone != null){
      var position = event.source.lookupWidget("../position").value;
      if (position != null){
        var team = everyone.get(position);
        var member = team.getMember(value);
        if (member != null) {
          var country = member.getCountry();
          var countrywidget = event.source.lookupWidget("../country");
          countrywidget.setValue(country);
        }
      }
    }
  }
}
function updateRowIDcounter(event) {
  // Increment the row creation ID counter
  // (it has been initialized in the flowscript when the
  // form was created).
  // This shows how attributes can be used as a communication
  // means between application logic and widget event handlers.

  var form = event.source.form;
  var count = new java.lang.Long(form.getAttribute("counter").longValue() + 1);
  form.setAttribute("counter", count);
  var repeater = form.getChild("teammembers");
  repeater.getRow(repeater.getSize() - 1).getChild("ID").setValue(count);
}

function validateRepeater(widget) {
  // This demonstrates validating a repeater: we check here if all
  // teammembers are distinct.
  // A repeater cannot itself display a validation error, and therefore
  // sets a validation error on a field in the offending row.
  var list = new java.util.ArrayList();
  var success = true;
  // Iterate on all rows

  for (var i = 0; i < widget.size; i++) {
      // Get the row
      var row = widget.getRow(i);
      // Compute a key combining the first and last name
      var key = row.lookupWidget("memberId").value;
      if (list.contains(key)) {
         // already in the list
         row.lookupWidget("memberId").setValidationError(
           new Packages.org.apache.cocoon.forms.
             validation.ValidationError("Duplicate player", false));
          success = false;
          break; // no need to continue
      }
      // Add the current row's key to the list
      list.add(key);
  }

  // Must return true/false
  return success;
}


/*
  This function is only used to read in an XML file and pass it on as DOM Document for further processing.
*/
function loadDocument(uri) {
  var parser = null;
  var source = null;
  var resolver = null;
  try {
      parser = cocoon.getComponent(Packages.org.apache.excalibur.xml.dom.DOMParser.ROLE);
      resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
      source = resolver.resolveURI(uri);
      var is = new Packages.org.xml.sax.InputSource(source.getInputStream());
      is.setSystemId(source.getURI());
      return parser.parseDocument(is);
  } finally {
      if (source != null)
          resolver.release(source);
      cocoon.releaseComponent(parser);
      cocoon.releaseComponent(resolver);
  }
}
