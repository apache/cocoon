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
dojo.provide("cocoon.forms.FileField");

dojo.require("dijit.form.TextBox");
dojo.require("cocoon.forms._FieldMixin");

/**
 * CForms FileField Widget.
 * Intended to be a file input that looks the same across browsers
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
 
 /*
    TODO: Implement this, probably based on code ripped from dojox.widget.FileInput
    
    Will exist in 3 primary states :
    
      Unchosen, Chosen, Uploaded
    
    Unchosen:
      [ Browse... ] [*] [no file selected         ] [?]
      
    Chosen:
      [   Clear   ] [*] [selected file name       ] [?]
      
    Uploaded:
      [   Delete  ] [*] [selected file name, bytes] [?]
      
            ^        ^               ^               ^
            |        |               |               |
         Button   status     <input> lookalike   help button
 
    NB. "<input> lookalike" sized by @size
        Try to get the Buttons all the same width (how with i18n?)
 
 */
 
dojo.declare("cocoon.forms.FileField", [dijit.form.TextBox, cocoon.forms._ErrorAwareFieldMixin, cocoon.forms._SizedFieldMixin, cocoon.forms._FieldHintMixin], {	

    // add _ErrorAwareFieldMixin ?
});
