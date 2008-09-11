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
dojo.provide("cocoon.forms.NavGroup");

dojo.require("dijit.layout.StackContainer");
dojo.require("cocoon.forms._GroupMixin");

/**
 * CForms NavGroup Widget.
 * A set of panes that are switched between using a custom controller        
 *
 * eg: <fi:group id"someNavGroup">
 *         <fi:styling type="nav" . . . />
 *
 * NB. The id attribute is required
 * 
 * Use this when you want to make your own custom controller, which may be anywhere in the template
 * For an example see: src/blocks/forms/samples/forms/group_styles_template.xml
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.NavGroup", [dijit.layout.StackContainer, cocoon.forms._GroupMixin], {	

  // the controller to use
	_controllerWidget: "dijit.layout.NavController",
  
  // use my buttons for the statusWidget
  getStatusWidget: function() {
    return this.controlButton ? this.controlButton : this;
  }
});

/**
 * CForms NavController Widget.
 * A container of buttons, one for each page
 * NB. Must be placed (and can be customised) by the User, in their own template
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms.NavController", [dijit.layout.StackController], {
  
  //	The name of the tab widget to create to correspond to each page
	buttonWidget: "cocoon.forms._NavButton" 	

});

/**
 * CForms _NavButton Widget.
 * A dijit.layout._StackButton that shows a status mark
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms._NavButton", [dijit.layout._StackButton], {

  // copy of the dijit.form.Button template, adding statusMarker
	templateString:"<div class=\"dijit dijitReset dijitLeft dijitInline\"\n\tdojoAttachEvent=\"onclick:_onButtonClick,onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\"\n\twaiRole=\"presentation\"\n\t><button class=\"dijitReset dijitStretch dijitButtonNode dijitButtonContents\" dojoAttachPoint=\"focusNode,titleNode,statusNode\"\n\t\ttype=\"${type}\" waiRole=\"button\" waiState=\"labelledby-${id}_label\"\n\t\t><span class=\"dijitReset dijitValidationIconText\">${_cforms_statusMarker}</span><span class=\"dijitReset dijitInline ${iconClass}\" dojoAttachPoint=\"iconNode\" \n \t\t\t><span class=\"dijitReset dijitToggleButtonIconChar\">&#10003;</span \n\t\t></span\n\t\t><div class=\"dijitReset dijitInline\"><center class=\"dijitReset dijitButtonText\" id=\"${id}_label\" dojoAttachPoint=\"containerNode\">${label}</center></div\n\t></button\n></div>\n",

  // get my status marker
  postMixInProperties: function() {
    this._cforms_statusMarker = cocoon.forms.defaults.statusMark;
    this.inherited(arguments); 
  },

});
