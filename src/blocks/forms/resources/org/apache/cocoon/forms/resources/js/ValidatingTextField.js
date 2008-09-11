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
dojo.provide("cocoon.forms.ValidatingTextField");

dojo.require("dijit.form.ValidationTextBox");
dojo.require("cocoon.forms._FieldMixin");

/**
 * CForms ValidatingTextField Widget.
 * A single line field to edit text and perform client-side validation, 
 * that is either required, or has user-supplied constraints and filters.
 * Also forms the basis of all of the CForms NumberFields
 * Extends dijit.form.ValidationTextBox with cocoon.forms._ErrorAwareFieldMixin
 * to add ancestor status reporting behaviour and the ability to display errors on load.
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.ValidatingTextField", [dijit.form.ValidationTextBox, cocoon.forms._ErrorAwareFieldMixin, cocoon.forms._SizedFieldMixin], {	

  /* TODO: more fieldTypes: email, creditcard, uri etc. etc. */

  // adding a place for the status marker to dijit.form.ValidationTextBox's template
  templateString:"<div class=\"dijit dijitReset dijitInlineTable dijitLeft\"\n\tid=\"widget_${id}\"\n\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\" waiRole=\"presentation\"\n\t><div style=\"overflow:hidden;\"\n\t\t><div class=\"dijitReset dijitValidationIcon\"><br></div\n\t\t><div class=\"dijitReset dijitValidationIconText\">${_cforms_statusMarker}</div\n\t\t><div class=\"dijitReset dijitInputField\"\n\t\t\t><input class=\"dijitReset\" dojoAttachPoint='textbox,focusNode' dojoAttachEvent='onfocus:_update,onkeyup:_onkeyup,onblur:_onMouse,onkeypress:_onKeyPress' autocomplete=\"off\"\n\t\t\ttype='${type}' name='${name}'\n\t\t/></div\n\t></div\n></div>\n"
  
});
