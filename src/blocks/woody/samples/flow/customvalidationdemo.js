/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

function customValidationDemo() {
    var form = new Form("forms/customvalidationdemo_form.xml");
    
    // the line below is the crucial piece for this demo: assigning
    // a function to do extra validation
    form.validator = myValidator;

    form.showForm("customvalidationdemo-display-pipeline");
}

function myValidator(form) {
    // Add an error message to specific widgets as follows:
    var validationError = new Packages.org.apache.cocoon.woody.datatype.ValidationError("This is so wrong.");
    form.getWidget("number1").setValidationError(validationError);
    form.getWidget("number2").setValidationError(validationError);

    // Add some other messages to the 'messages' widget
    form.getWidget("messages").addMessage("You'll never be able to enter valid data!");
    form.getWidget("messages").addMessage("Ha ha ha!");

    // to add i18n messages, do something like:
    // var i18nMessage = new Packages.org.apache.cocoon.woody.util.I18nMessage("key");
    // form.getWidget("messages").addMessage(i18nMessage);
    // The I18nMessage class supports a variety of other constructors to do more
    // powerful things, be sure to check them out.

    // Note that instead of doing all the above here in Javascript,
    // you can also delegate this work to a Java class (or Avalon component).

    // always return false, so that the form will never be valid
    return false;
}
