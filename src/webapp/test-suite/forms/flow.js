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

/*
 * @version $Id: form_model_gui.js 279803 2005-09-09 15:50:52Z hepabolu $
 */

cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

function do_a() {
    var data = {}
    data.value="shared value"
    var form = new Form("form-a.xml");
    while (true) {
        form.showForm("form-a", data)
        do_b(data)
    }
}

function do_b(data) {
    var form = new Form("form-b.xml");
    form.showForm("form-b", data)
}
