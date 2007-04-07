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

function upload() {
    
    var form = new Form("forms/upload_model.xml");
    var k = form.showForm("upload-display-pipeline.jx");

    k.invalidate();

    var widget = form.lookupWidget("upload");
    cocoon.sendPage("upload-success-pipeline.jx",
        {
            uploadContent: handleUpload(form), 
            username: form.lookupWidget("user").getValue(),
            files: [
                {
                    filename: widget.getValue().getUploadName(),
                    bytes: widget.getValue().getSize()
                }
            ]
        }
    );
}

function handleUpload(form) {

  var buf = new java.lang.StringBuffer();
  
  var uploadWidget = form.lookupWidget("upload");
  if (uploadWidget.getValue() != null) {
    var stream = uploadWidget.getValue().getInputStream();
    var reader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));

    var line;
    while ((line=reader.readLine())!=null)
      buf.append(line).append("\n");

    reader.close();
  }
  
  return buf.toString();
}

function uploadprogress() {
    
    var form = new Form("forms/uploadprogress_model.xml");
    form.setAttribute("counter", new java.lang.Integer(0));
    var k = form.showForm("uploadprogress-display-pipeline.jx");

    k.invalidate(); // do not allow return to the form

        var files = [];
        var repeater = form.getChild("uploads");
        for (var i = 0; i < repeater.getSize(); i++) {
            var upload = repeater.getRow(i).getChild("upload");
            files[i] = {
                filename: upload.value.fileName,
                bytes: upload.value.size
            };
        }
    cocoon.sendPage("upload-success-pipeline.jx",
        {
            username: form.lookupWidget("user").getValue(),
            files: files
        }
    );
}
