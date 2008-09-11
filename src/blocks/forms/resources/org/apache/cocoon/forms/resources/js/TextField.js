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
dojo.provide("cocoon.forms.TextField");

dojo.require("dijit.form.TextBox");
dojo.require("cocoon.forms._FieldMixin");

/**
 * CForms TextField Widget.
 * A non client-side validating, non-required text field
 * Forms the basis of all of the other CForms TextFields
 * Extends dijit.form.TextBox with cocoon.forms._FilterFieldMixin
 * to add ancestor status reporting behaviour and content filtering.
 * Extends cocoon.forms._SizedFieldMixin to support @size.
 * Extends cocoon.forms._FieldHintMixin to support @promptMessage (<fd:hint>).
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.TextField", [dijit.form.TextBox, cocoon.forms._FilterFieldMixin, cocoon.forms._SizedFieldMixin, cocoon.forms._FieldHintMixin], {	

    
});
