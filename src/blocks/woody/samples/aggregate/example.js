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

cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/woody2.js");

Bean.prototype.date;
Bean.prototype.phone;
function Bean () {
};

function example() {
    var bean = new Bean();
    var form = new Form("form.xml");
    var template = "combine";

    while (true) {
        form.createBinding("binding-" + template + ".xml");

        // java.lang.System.out.println("Loading. Date: " + bean.date + ", Day: " + bean.day + ", Month: " + bean.month + ", Year: " + bean.year);
	form.load(bean);
        form.showForm(template + "-display-pipeline", bean);
        form.save(bean);
        // java.lang.System.out.println("Saved. Date: " + bean.date + ", Day: " + bean.day + ", Month: " + bean.month + ", Year: " + bean.year);

        if (form.submitId == "switch") {
            if (template == "combine") {
                template = "split";
            } else {
                template = "combine";
            }

            // Preserve values after switch
            form.createBinding("binding-" + template + ".xml");
            form.save(bean);
        }
    }
}
