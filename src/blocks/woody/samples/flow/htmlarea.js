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

function htmlarea() {
    var form = new Form("forms/htmlarea.xml");

    form.showForm("htmlarea-display-pipeline");

    var model = form.getModel();
    var htmldata = { "data" : model.data }
    cocoon.sendPage("htmlarea-success-pipeline", htmldata);
}
