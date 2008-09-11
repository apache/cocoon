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
dojo.provide("cocoon.forms.FoldingGroup");

dojo.require("dijit.layout.AccordionContainer");
dojo.require("cocoon.forms._GroupMixin");

/* Folding Group
  eg: <fi:group>
          <fi:styling type="folding" . . . />
          
  A set of panes that can be switched between by clicking on their titles

  We extend dijit.layout.AccordionContainer with cocoon.forms._GroupMixin providing behaviour required for CForms
          
*/

/* 

  TODO: pane sizing at start is broken, if there is not absolute height set in CSS 
  The approaches below are not working
  try extending AccordionPane ??????? (and change in the xslt !!)

*/
/* TODO: adding status mark to labels is broken */

dojo.declare("cocoon.forms.FoldingGroup", [dijit.layout.AccordionContainer, cocoon.forms._GroupMixin], {	

  // the controller to use
	//_controllerWidget: "dijit.layout.StackController" 
  xxlayout: function(){ 
    if (this.selectedChildWidget && this.selectedChildWidget.containerNode.clientHeight == 0) {
      var high = 0, border = /* eek! */3;
      
      high = this.selectedChildWidget.containerNode.scrollHeight;

      if (high > 0) this.selectedChildWidget.containerNode.style.height = high + "px";
    }
    this.inherited(arguments);
  },
  
  postCreate: function() {
    this.inherited(arguments);
    
    var totalCollapsedHeight = 0;
    dojo.forEach(this.getChildren(), function(child){
      totalCollapsedHeight += child.getTitleHeight();
    });
    console.debug("AccordionGroup.postCreate: " + totalCollapsedHeight);
    
    //this.domNode.style.height = "200px";
  },
  
  startup: function() {
    this.inherited(arguments);
    console.debug("AccordionGroup.startup: " + this.selectedChildWidget.containerNode.style.height);
  },
  layout: function() {
    //console.debug("AccordionGroup.layout");
    var totalCollapsedHeight = 0;
    dojo.forEach(this.getChildren(), function(child){
      totalCollapsedHeight += child.getTitleHeight();
    });
    console.debug("AccordionGroup.layout: " + (totalCollapsedHeight));
    this.inherited(arguments);
  },
  _setupChild: function(page) {
    console.debug("AccordionGroup._setupChild");
    this.inherited(arguments);
  }
  
});

