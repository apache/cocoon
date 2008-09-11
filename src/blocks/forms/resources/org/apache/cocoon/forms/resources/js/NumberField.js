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
dojo.provide("cocoon.forms.NumberField");

dojo.require("dijit.form.NumberTextBox");
dojo.require("cocoon.forms.common");
dojo.require("cocoon.forms._FieldMixin");

/**
 * CForms NumberField Widget.
 * A field that validate numbers in different locales        
 *
 * eg: <fd:field id="myNumber">
 *       <fd:datatype base=" . . . " . . . />
 *         <fd:convertor type=" . . . " variant=" . . . " . . . />
 *
 * This behaves a bit differently to dojo's version :
 *
 *   dojo:
 *     receive the string primitive of the number 
 *     display it using the specified constraints, or locale
 *     validate it using specified constraints
 *     send back the primitive string version
 *     
 *   cforms:
 *     change: receive the preformatted string
 *     add:    set the preformatted value on the display field
 *     same:   display it using the specified constraints and locale
 *     same:   validate it using the specified constraints and locale
 *     change: send back the formatted verion
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.NumberField", [dijit.form.NumberTextBox, cocoon.forms._FormattingFieldMixin, cocoon.forms._SizedFieldMixin], {	

  value: "", // override dojo's behaviour of not showing invalid numbers at start
  valueType: "number", // integer|long|float|double|number|percent 

  // adding a place for the status marker to dijit.form.ValidationTextBox's template
  templateString:"<div class=\"dijit dijitReset dijitInlineTable dijitLeft\"\n\tid=\"widget_${id}\"\n\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\" waiRole=\"presentation\"\n\t><div style=\"overflow:hidden;\"\n\t\t><div class=\"dijitReset dijitValidationIcon\"><br></div\n\t\t><div class=\"dijitReset dijitValidationIconText\">${_cforms_statusMarker}</div\n\t\t><div class=\"dijitReset dijitInputField\"\n\t\t\t><input class=\"dijitReset\" dojoAttachPoint='textbox,focusNode' dojoAttachEvent='onfocus:_update,onkeyup:_onkeyup,onblur:_onMouse,onkeypress:_onKeyPress' autocomplete=\"off\"\n\t\t\ttype='${type}' name='${name}'\n\t\t/></div\n\t></div\n></div>\n",
  
  // TODO: don't clear invalid entries ???
  
  // Widget interface
  postMixInProperties: function() {
    // get the default validation rules for the valueType
    var defaults = cocoon.forms.defaults.constraints[this.valueType] || { pattern: "#.######", type: "number" };
    // mixin the validation rules from cforms
    this.constraints = dojo.mixin(dojo.mixin({}, defaults), this.constraints);
    if (this.constraints.locale) this.lang = this.constraints.locale;
    this.inherited(arguments);
  }

});

