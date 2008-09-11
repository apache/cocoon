/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
dojo.provide("cocoon.ajax.PartialLink");

dojo.require("dijit._Widget");
dojo.require("cocoon.ajax.common");

/**
 * Dojo widget for links that partially update the page.
 *
 * @version $Id$
 */

dojo.declare(
    "cocoon.ajax.PartialLink",
    dijit._Widget,
 {
    // Properties from HTML Attributes
    href: "",   // String, the url from which to load the page fragment
    target: "", // String, the ID of the element to be updated

    postCreate: function() {
        if (this.target.indexOf("#") < 0) {
            console.debug("PartialLink: wrong value for 'target' attribute: " + this.target);
            return;
        }
        dojo.connect(this.domNode, "onclick", this, "onClick");
    },

    onClick: function(event) {
        event.preventDefault();
        cocoon.ajax.update(this.href, this.target);
    }
});
