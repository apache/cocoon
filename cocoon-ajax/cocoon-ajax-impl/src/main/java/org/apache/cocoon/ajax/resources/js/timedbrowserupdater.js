/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

/* @version $Id$ */

/* TO BE REWRITTEN WITH DOJO
   -------------------------

Cocoon.Ajax.TimedBrowserUpdater = Class.create();
Cocoon.Ajax.TimedBrowserUpdater.prototype = Object.extend(new Ajax.Base(), {
  initialize: function(url, options) {
    this.setOptions(options);
    this.onComplete = this.options.onComplete;
		this.autostart = (this.options.autostart || true); // default autostart: true
    this.frequency = (this.options.frequency || 30);   // default frequency: 5 minutes
    this.decay = 1;																		 // default decay: none
		this.populated = false;
    this.updater = {};
    this.widgets = {};
    this.url = url;
		this.timerIsRunning = false;
    this.start();
  },

  start: function() {
    this.options.onComplete = this.updateComplete.bind(this);
    this.onTimerEvent();
    this.timerIsRunning = true;
    self.status = "Timer Starting";
  },

  stop: function() {
    this.updater.onComplete = undefined;
    clearTimeout(this.timer);
    this.timerIsRunning = false;
    self.status = "Timer Stoping";
  },

  updateComplete: function(request) {
    if (this.options.decay) {
      this.decay = (request.responseText == this.lastText ? 
        this.decay * this.options.decay : 1);

      this.lastText = request.responseText;
    }
    this.updateWidgets(request.responseXML);
    this.timer = setTimeout(this.onTimerEvent.bind(this), this.decay * this.frequency * 1000);
  },

  onTimerEvent: function() {
    if (this.populated) {
    	this.options.postBody = this.buildQueryString();
    	this.updater = new Cocoon.Ajax.BrowserUpdater(this.url, this.options);
    	    self.status = "Timer fired event";
    } else {
    	this.timer = setTimeout(this.onTimerEvent.bind(this), this.decay * this.frequency * 1000);
    }
  },
  
  updateWidgets: function(doc) {
		var nodes = doc.documentElement.childNodes;
		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			if (node.nodeType == DOMUtils.ELEMENT_NODE) {
				this.widgets[node.getAttribute("id")] = node.getAttribute("state");
			}
		}
	},

	registerWidget: function(id, state) {
		this.widgets[id] = state;
		this.populated = true;
	},
	
	buildQueryString: function () {
		var result = "cocoon-ajax=true";
		for (var key in this.widgets) {
			result += "&" + encodeURIComponent(key) + "=" + encodeURIComponent(this.widgets[key]);
		}
		// see if there is a form with a continuation parameter
		for (var form in document.forms) {
			if (form != null && form.elements != null && form.elements["continuation-id"] != null) {
				result += "&continuation-id=" + form.elements["continuation-id"].value;
				break;
			}
		}
		self.status = result;
		return result;
	}
	
});

Cocoon.Ajax.TimedBrowserUpdater.Console = Class.create();
Cocoon.Ajax.TimedBrowserUpdater.Console.prototype = Object.extend(new Ajax.Base(), {
	initialize: function(client, options) {
		this.expires = 604800000; // cookie expiry, 1 week
		this.client = client;
		this.setOptions(options);
		this.console = this.options.console;
		this.message("Initialise");
    this.isRunningTitle = ( this.options.isRunningTitle || "ON" );
		this.notRunningTitle = ( this.options.notRunningTitle || "OFF" );
		this.isRunningHint = ( this.options.isRunningHint || "Click to Stop" );
		this.notRunningHint = ( this.options.notRunningHint || "Click to Start" );
		this.frequencyControl = this.options.frequencyControl;
		this.toggleControl = this.options.toggleControl;
		
		var autorun = this.getPreference( "TimedBrowserUpdater.autorun" );
		if ( autorun == undefined ) {
			this.message("Autorun undefined");
			this.startTimer(  );
		} else if ( autorun == "true" ) {
			this.message("Autorun true");
			this.startTimer(  );
		} else {
			this.message("Autorun false");
			this.stopTimer(  );
		}
		
		var frequency =  this.getPreference( "TimedBrowserUpdater.frequency" );
		if ( frequency == undefined ) frequency = 300;
		this.setFrequency(frequency);
	},
	
	message: function(message) {
		if (this.console != undefined) {
			this.console.value = this.console.value + "\n" + message;
		} else {
			self.status = message;
		}
	},
	
	toggleTimer: function() {
		if (this.client.timerIsRunning) {
			this.stopTimer();
		} else {
			this.startTimer();
		}
	},
	
	startTimer: function() {
			if (!this.client.timerIsRunning) {
				this.client.start(  );
				this.message("Client Started");
			}
			this.setPreference( "TimedBrowserUpdater.autorun", "true" );
			this.toggleControl.value = this.isRunningTitle;
			this.toggleControl.title =  this.isRunningHint;
	},

	stopTimer: function() {
			if (this.client.timerIsRunning) {
				this.client.stop(  );
				this.message("Client Stopped");
			}
			this.setPreference( "TimedBrowserUpdater.autorun", "false" );
			this.toggleControl.value = this.notRunningTitle;
			this.toggleControl.title =  this.notRunningHint;
	},	
	
	setFrequency: function() {
		var frequency = this.frequencyControl.value;
		this.client.frequency = frequency;
		this.setPreference( "TimedBrowserUpdater.frequency", frequency );
		for ( var i=0;i < this.frequencyControl.options.length;i++ ) {
			if ( frequency == this.frequencyControl.options[i].value ) {
				this.frequencyControl.selectedIndex = i;
				break;
			}
		}
	},
	
	setPreference: function(name, value) {
		var expiredate = new Date();
		expiredate.setTime(expiredate.getTime() + this.expires);
		document.cookie = name + "=" + value + "; expires=" + expiredate.toGMTString() + "; path=/";
		this.message("Set Preference: " + name + ": " + value);
	},

	getPreference: function(name){
		var neq = name + "=";
		var k = document.cookie.split(';');
		for (var i=0; i < k.length; i++) {
			var c = k[i];
			while (c.charAt(0)==' ') c = c.substring(1,c.length);
			if (c.indexOf(neq) == 0) {
				var result = c.substring(neq.length,c.length);
				this.message( "Read Preference: " + name + ": " + result);
				return result;
			}
		}
		this.message( "Read Preference: " + name + ": undefined");
		return undefined;
	}



});

TimedBrowserUpdaterInstance = new Cocoon.Ajax.TimedBrowserUpdater(
	document.location, 
	{
		method: 'post', 
		onFailure: Cocoon.Ajax.BrowserUpdater.handleError,
		insertion: Cocoon.Ajax.Insertion.Replace
	}
);
*/
