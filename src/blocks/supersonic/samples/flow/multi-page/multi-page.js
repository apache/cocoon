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

// Multi-page Flow example
// Simple multi-page form, without using Cocoon Forms

var date = new Packages.java.util.Date();

// simulated email message data
function MessageData() {
    this.sender = "you@somewhere.com";
    this.subject = "Type the subject here";
    this.text = "Type the text of your message here";
}

// page flow
function public_startMultiPage() {
    var message = new MessageData();

    while(true) {

        // decide which page to show based on request parameters
        page = "page1";
        if(cocoon.request.getParameter("action_send") != null) {
            break;
        } else if(cocoon.request.getParameter("action_page2") != null) {
            page = "page2";
        }

        // show form and wait for results
        cocoon.sendPageAndWait("multi-page/views/" + page, { "message" : message, "date" : date });

        // now for the boring part: copy form data into message
        // that's where Forms bindings would help
        tmp = cocoon.request.getParameter("sender");
        if(tmp != null) message.sender = tmp;

        tmp = cocoon.request.getParameter("subject");
        if(tmp != null) message.subject = tmp;

        tmp = cocoon.request.getParameter("text");
        if(tmp != null) message.text = tmp;
    }

    // user selected "send", show message contents
    cocoon.sendPage("multi-page/views/result", { "message" : message });
}