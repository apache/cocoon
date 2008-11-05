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
dojo.provide("cocoon.forms.TextArea");

dojo.require("dijit.Tooltip");
dojo.require("dijit.form.TextBox");
dojo.require("cocoon.forms._FieldMixin");


/**
 * CForms TextArea Widget.
 * Supports hints and filtering
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.TextArea", [dijit.form.TextBox, cocoon.forms._FilterFieldMixin, cocoon.forms._TextAreaMixin, cocoon.forms._FieldHintMixin], {	

	baseClass: "dijitTextArea",

	attributeMap: dojo.mixin(dojo.clone(dijit.form.TextBox.prototype.attributeMap),
		{rows:"focusNode", cols: "focusNode"}),

	rows: "",
	cols: "",

	templateString:"<textarea class=\"dijit dijitReset dijitLeft\" dojoAttachPoint='textbox,focusNode,containerNode' name=\"${name}\"\n\tdojoAttachEvent='onmouseenter:_onMouse,onmouseleave:_onMouse,onfocus:_onMouse,onblur:_onMouse,onkeypress:_onKeyPress,onkeyup'>"
  
});
