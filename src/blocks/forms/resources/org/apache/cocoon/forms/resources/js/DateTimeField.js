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
dojo.provide("cocoon.forms.DateTimeField");

//dojo.require("dijit.form.DateTextBox");
//dojo.require("dijit.form.TimeTextBox");
dojo.require("dijit.form._DateTimeTextBox");
dojo.require("cocoon.forms._FieldMixin");

/**
 * CForms DateTimeField Widget.
 * A field that validate combined date/times in different locales
 *
 * eg: <fd:field id="myDate">
 *       <fd:datatype base=" . . . " . . . />
 *         <fd:convertor type=" . . . " variant=" . . . " . . . />
 *
 * This behaves a bit differently to the equivalent in dojo @see: cocoon.forms.NumberField for a description
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
 
 /*
 
  TODO: This needs Dojo Editors attached to it :
  
    dijit._Calendar - for the date portion
    dijit._TimePicker - for the time portion
 
  TODO: get validation working
  
  TODO: Display:
    show date + time in what looks like a single field (but is effectively two onClick regions)
    when you click on the date or time portion :
      switch view to an input containing the whole string 
      bring up the appropriate editor for date or time
 
    copy date/time splitting code etc. from cocoon.forms.DropdownDateTimePicker
 
 */
 
 
 
dojo.declare("cocoon.forms.DateTimeField", [dijit.form._DateTimeTextBox, cocoon.forms._FormattingFieldMixin, cocoon.forms._SizedFieldMixin], {	

  // adding a place for the status marker to dijit.form.ValidationTextBox's template
  templateString:"<div class=\"dijit dijitReset dijitInlineTable dijitLeft\"\n\tid=\"widget_${id}\"\n\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\" waiRole=\"presentation\"\n\t><div style=\"overflow:hidden;\"\n\t\t><div class=\"dijitReset dijitValidationIcon\"><br></div\n\t\t><div class=\"dijitReset dijitValidationIconText\">${_cforms_statusMarker}</div\n\t\t><div class=\"dijitReset dijitInputField\"\n\t\t\t><input class=\"dijitReset\" dojoAttachPoint='textbox,focusNode' dojoAttachEvent='onfocus:_update,onkeyup:_onkeyup,onblur:_onMouse,onkeypress:_onKeyPress' autocomplete=\"off\"\n\t\t\ttype='${type}' name='${name}'\n\t\t/></div\n\t></div\n></div>\n",
    
  // Widget interface
  postMixInProperties: function() {
    this.constraints.datePattern = this.constraints.pattern;
    this.constraints.TimePattern = this.constraints.pattern;
    this.constraints.pattern = null;
    this._selector = "datetime";
    this.inherited(arguments);
  }

});

