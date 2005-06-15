/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

function employee_edit() {

	var employeeId = cocoon.parameters["employeeId"];
	var nextPage = cocoon.parameters["nextPage"];

    var pu = null;

	try {
        pu = cocoon.createObject(Packages.org.apache.cocoon.components.flow.util.PipelineUtil);

        // load document for form data, via a pipeline to use the SQLTransformer
        var document = pu.processToDOM("employees/" + employeeId + ".xml", null);

        // create form, bind to document and load document into it
        var form = new Form("form-definition.xml");
        form.createBinding("form-binding.xml");
        form.load(document);

        // display and edit form
        form.showForm("edit-employee-form");

        // save modified data to document object
        // TODO if would be good to do this only if the data has changed
        form.save(document);

        // run pipeline to let SQLTransformer update data based on document object
        var unusedOutput = new Packages.java.io.ByteArrayOutputStream();
        cocoon.processPipelineTo("save-employee",{"document": document},unusedOutput);

    } finally {
        if(pu!=null) cocoon.disposeObject(pu);
    }

	cocoon.redirectTo(nextPage,true);
}
