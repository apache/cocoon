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

/**
 * Simple flow method to send email according to passed in flow parameters.
 */
function mail() {

    var mms = null;
    try {
        mms = cocoon.getComponent(Packages.org.apache.cocoon.mail.MailSender.ROLE);

        if (cocoon.parameters.host != "" || cocoon.parameters.user != "") {
            mms.setSmtpHost(cocoon.parameters.host,
                            cocoon.parameters.user,
                            cocoon.parameters.password);
        }

        mms.setFrom(cocoon.parameters.from);
        mms.setTo(cocoon.parameters.to);
        mms.setSubject(cocoon.parameters.subject);
        mms.setCc(cocoon.parameters.cc);
        mms.setBcc(cocoon.parameters.bcc);
        mms.setBody(cocoon.parameters.body, "text/plain; charset=utf-8");

        mms.addAttachment(cocoon.request.get("attachment"));
        mms.addAttachmentURL("cocoon:///");
        mms.addAttachmentURL("context://welcome.xml");

        mms.send();
    } finally {
        cocoon.releaseComponent(mms);
    }

    cocoon.sendPage("view/page");
}
