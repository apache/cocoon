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
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

function upload() {
    
    var form = new Form("forms/upload_model.xml");
    var k = form.showForm("upload-display-pipeline");

    k.invalidate();

    cocoon.sendPage("upload-success-pipeline.jx",
        {
            uploadContent: handleUpload(form), 
            username: form.getWidget("user").getValue(),
            filename: form.getWidget("upload").getValue().getHeaders().get("filename")
        }
    );
}

function handleUpload(form) {

  var buf = new java.lang.StringBuffer();
  
  var uploadWidget = form.getWidget("upload");
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
