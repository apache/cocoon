cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

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
