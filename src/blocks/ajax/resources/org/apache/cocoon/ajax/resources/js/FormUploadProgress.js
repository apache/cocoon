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

dojo.provide("cocoon.ajax.FormUploadProgress");
dojo.require("dijit.ProgressBar");

/**
 * Ajax Upload Progress Bar Widget.
 * A wrapper for dijit.ProgressBar that polls the server during uploads
 * and displays the status in a ProgressBar
 *
 * NOTE: introduced in 2.1.?
 *
 * @version $Id$
 */
dojo.declare("cocoon.ajax.FormUploadProgress", [dijit.ProgressBar], {	

  statusUri: dojo.moduleUrl("cocoon.ajax", "../../../system/ajax/upload/status").toString(), // this is not great

  progress: "0%", // prime the progress bar to accept percentages
  templateString: "<div class=\"FormUpload\"><div class=\"FormUploadUser\" dojoAttachPoint=\"containerNode\"></div><div class=\"dijitProgressBar dijitProgressBarEmpty\"\r\t><div waiRole=\"progressbar\" tabindex=\"0\" dojoAttachPoint=\"internalProgress\" class=\"dijitProgressBarFull\"\r\t\t><div class=\"dijitProgressBarTile\"></div\r\t\t><span style=\"visibility:hidden\">&nbsp;</span\r\t></div\r\t><div dojoAttachPoint=\"label\" class=\"dijitProgressBarLabel\" id=\"${id}_label\">&#160;</div\r\t><img dojoAttachPoint=\"inteterminateHighContrastImage\" class=\"dijitProgressBarIndeterminateHighContrastImage\"\r\t></img\r></div></div>",
  
  constructor: function() {
    this.form = null;   // the form I am in
    this.target = null; // the form widget
    this.timer = null;  
    this.busy = false;
    this.delay = 2000;
  }, 

  // Widget interface
  postCreate: function() {
    this.inherited(arguments);
    this.startedHidden = this.domNode.style.visibility !== "visible";
    var el = this.domNode.parentNode
    while (el && el.tagName && el.tagName.toLowerCase() !== "form") el = el.parentNode; // find my form
    this.form = el && el.tagName && el.tagName.toLowerCase() === "form" ? el : null;
    this.target = dijit.byNode(this.form);
    if (this.target) {
      this.afterSubmitSubscription = dojo.subscribe(this.target.getAfterSubmitTopic(), this, this.startUpload);
    } else {
      throw new Error("FormUploadProgress Error: ", "This may only be used inside a form widget.");
    }    
  },
  
  // Widget interface
  destroy: function() {
    this.inherited(arguments);
    dojo.unsubscribe(this.afterSubmitSubscription);
  },
    
  // event handler, should fire after the forms starts submitting
  startUpload: function() {
    if (this.checkForActiveFile(this.form)) {
      if (this.startedHidden) {
        this.domNode.style.visibility = "visible"; 
        dojo.fadeIn({ duration: 500, node: this.domNode }).play();
      }
      this.indeterminate = true;
      this.update({progress:"0%"});
      this.timer = setTimeout(dojo.hitch(this, this.getStatus), this.delay);
    }
  },

  // get the status of the file upload from the server
  getStatus: function() {
    if (this.busy) return; // only deal with one request at a time
    dojo.xhrGet({
      url: this.statusUri,
      preventCache: true,
      handleAs: "json-comment-filtered",
      load: dojo.hitch(this, function(response) {
        this.status = response;
        this.busy = false;
        this._update();
      }),
      error: dojo.hitch(this, function(){
        console.error("FormUploadProgress Error: ", "Status request failed");
      })
    });
    this.busy = true;
  },
  
  // update the widget with the new data
  _update: function() {
    if (this.status) {
      this.indeterminate = false;
      if (this.status.finished) {
        this.update({progress:"100%"});
        if (this.startedHidden) 
          dojo.fadeOut({ duration: 500, node: this.domNode }).play(5000); // fade after 5 secs
        return;
      } else if (this.status.percent) {
        this.update({progress:this.status.percent + "%"});
      }
    }
    this.timer = setTimeout(dojo.hitch(this, this.getStatus), this.delay);
  },
  
  // ProgressBar interface - provide a status message
  report: function() {
    return this.status && this.status.message ? this.status.message : "";
  },
  
  // look to see if there are active (file has been chosen) file-upload fields in the form
  checkForActiveFile: function(node) {
    var hasFile = false;
    var inputs = node.getElementsByTagName("input");
    dojo.forEach(inputs, function(input){
      if (hasFile){ return; }
      if (input.getAttribute("type") === "file" && input.value){
        hasFile = true;
      }
    });
    return hasFile;
  }
});
