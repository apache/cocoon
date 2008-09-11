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
dojo.provide("cocoon.forms.ValidatingTextArea");

dojo.require("dijit.form.ValidationTextBox");
dojo.require("cocoon.forms._FieldMixin");


/**
 * CForms ValidatingTextArea Widget.
 * Supports validation, content filtering and status display
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.ValidatingTextArea", [dijit.form.ValidationTextBox, cocoon.forms._ErrorAwareFieldMixin, cocoon.forms._TextAreaMixin], {	

  attributeMap: dojo.mixin(dojo.clone(dijit.form.ValidationTextBox.prototype.attributeMap),
		{rows:"focusNode", cols: "focusNode"}),

	rows: "",
	cols: "",

  // adding a place for the status marker to dijit.form.ValidationTextBox's template
	templateString:"<div style=\"width:auto;\" class=\"dijit dijitReset dijitInlineTable dijitLeft\"\n\tid=\"widget_${id}\"\n\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\" waiRole=\"presentation\"\n\t><div style=\"overflow:hidden;\"\n\t\t><div class=\"dijitReset dijitValidationIcon\"><br></div\n\t\t><div class=\"dijitReset dijitValidationIconText\">${_cforms_statusMarker}</div\n\t\t><div class=\"dijitReset dijitInputField\"\n\t\t\t><textarea style=\"background: transparent\" class=\"dijitReset\" dojoAttachPoint='textbox,focusNode' dojoAttachEvent='onfocus:_update,onkeyup:_onkeyup,onblur:_onMouse,onkeypress:_onKeyPress' autocomplete=\"off\"\n\t\t\ttype='${type}' name='${name}'\n\t\t/></div\n\t></div\n></div>\n",
  
  validator: function(value, constraints){
    return (new RegExp("^(" + this.regExpGen(constraints) + ")"+(this.required?"":"?")+"$", "m")).test(value) &&
      (!this.required || !this._isEmpty(value)) &&
      (this._isEmpty(value) || this.parse(value, constraints) !== undefined); // Boolean
  },
  
});
