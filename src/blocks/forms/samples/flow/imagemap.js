/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

var form = new Form("forms/imagemap.xml");

function imagemap() {
    
    form.showForm("imagemap-display-pipeline");

    var model = form.getModel();
    var bizdata = { "x" : model.x, "y" : model.y }
    cocoon.sendPage("imagemap-success-pipeline.jx", bizdata);
   
}

function onClickMap (event) {
    var x= event.getX();
    var y= event.getY();
    
    form.getWidget().lookupWidget("x").setValue(x.toString());
    form.getWidget().lookupWidget("y").setValue(y.toString());

}
