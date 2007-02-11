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

// flowscript for supersonic tour example app

// Load the javascript Cocoon Forms library
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

// Access java "database" facade object
var db = Packages.org.apache.cocoon.samples.tour.beans.DatabaseFacade.getInstance();

// Query all TaskBean objects and display them
function query_allTasks() {
    list = db.getTasks();

    cocoon.sendPage("internal/generate-view/taskList", {
        title : "List of tasks",
        task : list,
        db : db
    });
}

// Query a single TaskBean object and display it
function query_singleTask() {
    id = cocoon.request.getParameter("taskId");
    bean = db.getTaskBeanById(id);
    displayTaskBean(id,bean);
}

// Edit a single TaskBean object using Cocoon Forms
function singleTaskEditor(form) {
    id = cocoon.request.getParameter("taskId");
    bean = db.getTaskBeanById(id);

    form.load(bean);
    form.showForm("internal/show-form/singleTask");
    form.save(bean);
    displayTaskBean(id,bean);
}

// Display a single TaskBean
function displayTaskBean(id,bean) {
    cocoon.sendPage("internal/generate-view/singleTask", {
        title : "Task #" + id,
        task : bean,
        selectedTaskId : id
    });
}
