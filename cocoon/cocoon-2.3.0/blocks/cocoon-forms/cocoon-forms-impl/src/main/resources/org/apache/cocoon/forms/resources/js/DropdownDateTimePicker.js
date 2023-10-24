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
dojo.provide("cocoon.forms.DropdownDateTimePicker");

dojo.require("dojo.date.format");
dojo.require("dojo.string.*");

/*
 * A date, time, datetime picker widget for CForms.
 * Uses the date and time pickers from dojo.
 * Compared to the dojo dropdown date and time pickers, this one supports also combined datetime input
 * in one field. It also makes use of the correct forms locale.
 */
dojo.widget.defineWidget("cocoon.forms.DropdownDateTimePicker", dojo.widget.HtmlWidget,
    function() {

    },

    {
        variant: "date", /* date, time or datetime */

        pattern: "yyyy-MM-dd",

        showInputSample: "false", /* should a sample date entry be shown next to the input field? */

        locale: "en", /* normally taken from the form */

        isContainer: true,

        dateIconURL: cocoon.formsResourcesUri + "/js/templates/images/dateIcon.gif",

        timeIconURL: cocoon.formsResourcesUri + "/js/templates/images/timeIcon.gif",

        containerToggle: "plain", /* plain, explode, wipe, fade */

        containerToggleDuration: 150,

        postMixInProperties: function(args, frag, parent) {
            cocoon.forms.DropdownDateTimePicker.superclass.postMixInProperties(this, args, frag, parent);

            // search form locale
            var fragNode = this.getFragNodeRef(frag);
            if (fragNode.form) {
                var formLocale = fragNode.form.getAttribute("locale");
                if (formLocale != null)
                    this.locale = formLocale;
            }

            this.locale = dojo.locale;
            if (this.locale == null)
                this.locale = "en";
            if (this.locale != formLocale) {
                dojo.debug("DropdownDateTimePicker: form locale (" + formLocale + ") is different from dojo locale (" + dojo.locale + ")")
            }

            this._splitPattern();
            this._initDateFormatOptions();

            this.templateString = "<span style='white-space: nowrap'><input dojoAttachPoint='inputNode' autocomplete='off' style='vertical-align: middle'/>";

            if (this._datePickerNeeded()) {
                this.templateString += "<img src='${this.dateIconURL}' dojoAttachEvent='onclick:_onDateIconClick' dojoAttachPoint='dateButtonNode' style='vertical-align: middle; cursor: pointer; cursor: hand'/>";
            }

            if (this._timePickerNeeded()) {
                this.templateString += "<img src='${this.timeIconURL}' dojoAttachEvent='onclick:_onTimeIconClick' dojoAttachPoint='timeButtonNode' style='vertical-align: middle; cursor: pointer; cursor: hand'/>";
            }

            if (this.showInputSample == "true") {
                var inputSample = dojo.date.format(new Date(), this.dateFormatOptions);
                this.templateString += "&#160;&#160;[&#160;" + inputSample + "&#160;]";
            }

            this.templateString += "</span>";
        },

        attachTemplateNodes: function(){
            // summary: use attachTemplateNodes to specify containerNode, as fillInTemplate is too late for this
            cocoon.forms.DropdownDateTimePicker.superclass.attachTemplateNodes.apply(this, arguments);

            if (this._datePickerNeeded()) {
                this.datePopup = dojo.widget.createWidget("PopupContainer", {toggle: this.containerToggle, toggleDuration: this.containerToggleDuration});
                this.datePopupContainerNode = this.datePopup.domNode;
            }

            if (this._timePickerNeeded()) {
                this.timePopup = dojo.widget.createWidget("PopupContainer", {toggle: this.containerToggle, toggleDuration: this.containerToggleDuration});
                this.timePopupContainerNode = this.timePopup.domNode;
            }
        },

        fillInTemplate: function(args, frag) {
            cocoon.forms.DropdownDateTimePicker.superclass.fillInTemplate(this, args, frag);

            if (this._datePickerNeeded())
                this.domNode.appendChild(this.datePopup.domNode);
            if (this._timePickerNeeded())
                this.domNode.appendChild(this.timePopup.domNode);

            // Copy some stuff from the original input field
            var fragNode = this.getFragNodeRef(frag);
            this.inputNode.id = fragNode.id;
            this.inputNode.name = fragNode.name;
            this.inputNode.disabled = fragNode.disabled;
            this.inputNode.className = fragNode.className;
            this.inputNode.style.width = fragNode.style.width;
            this.inputNode.value = fragNode.value;

            // Construct date picker
            if (this._datePickerNeeded()) {
                var dpArgs = {widgetContainerId: this.widgetId, lang: this.locale, value: this.value};
                this.datePicker = dojo.widget.createWidget("DatePicker", dpArgs, this.datePopupContainerNode, "child");
                dojo.event.connect(this.datePicker, "onValueChanged", this, "_updateDate");
            }

            // Construct time picker
            if (this._timePickerNeeded()) {
                var tpArgs = { widgetContainerId: this.widgetId, lang: this.locale, value: this.value };
                this.timePicker = dojo.widget.createWidget("TimePicker", tpArgs, this.timePopupContainerNode, "child");
                dojo.event.connect(this.timePicker, "onValueChanged", this, "_updateTime");
            }

        },

        _datePickerNeeded: function() {
            return this.variant == "date" || this.variant == "datetime";
        },

        _timePickerNeeded: function() {
            return this.variant == "time" || this.variant == "datetime";
        },

        _onDateIconClick: function() {
            if (this.inputNode.disabled)
                return;

            if(!this.datePopup.isShowingNow) {
                var currentValue = this._parseCurrentInput();
                if (currentValue != null)
                    this.datePicker.setDate(currentValue);
                this.datePopup.open(this.inputNode, this, this.dateButtonNode);
            } else {
                this.datePopup.close();
            }
        },

        _onTimeIconClick: function() {
            if (this.inputNode.disabled)
                return;

            if(!this.timePopup.isShowingNow) {
                var currentValue = this._parseCurrentInput();
                if (currentValue != null)
                    this.timePicker.setTime(currentValue);
                this.timePopup.open(this.inputNode, this, this.timeButtonNode);
            } else {
                this.timePopup.close();
            }
        },

        _updateDate: function(value) {
            // in case there's a time component, preserve it if current input is parseable
            if (this.variant == "datetime") {
                var currentValue = this._parseCurrentInput();
                if (currentValue != null) {
                    value.setHours(currentValue.getHours());
                    value.setMinutes(currentValue.getMinutes());
                    value.setSeconds(currentValue.getSeconds());
                }
            }
            this.inputNode.value = dojo.date.format(value, this.dateFormatOptions);

            if(this.datePopup.isShowingNow) {
                this.datePopup.close();
            }
        },

        _updateTime: function(value) {
            // in case there's a date component, preserve it if current input is parseable
            if (this.variant == "datetime") {
                var currentValue = this._parseCurrentInput();
                if (currentValue != null) {
                    value.setFullYear(currentValue.getFullYear(), currentValue.getMonth(), currentValue.getDate());
                }
            }
            this.inputNode.value = dojo.date.format(value, this.dateFormatOptions);
        },

        _parseCurrentInput: function() {
            var currentInput = this.inputNode.value;
            if (currentInput != "") {
                return dojo.date.parse(currentInput, this.dateFormatOptions);
            }
            return null;
        },

        _splitPattern: function() {
            // Dojo wants date and time patterns separately, however in CForms these form one string
            // Therefore, when variant is dateTime we try to split them. This is not perfect of course,
            // it assume date and time information is grouped and time information is after date information
            // (which is however very common).

            if (this.variant == "date") {
                // pure date mode, assume pattern is only for dates
                this.datePattern = this.pattern;
                return;
            } else if (this.variant == "time") {
                // pure time mode, assume patter is only for times
                this.timePattern = this.pattern;
                return;
            }

            // pattern characters for times
            // http://www.unicode.org/reports/tr35/tr35-4.html#Date_Format_Patterns
            var timeFormattingChars = ["a", "h", "H", "K", "k", "m", "s", "S", "A", "z", "Z"];

            var pattern = this.pattern;
            if (pattern == null || pattern == "")
                return;

            // search position of first time pattern character
            var beginTimePattern = -1;
            for (var i = 0; i < pattern.length; i++) {
                var c = pattern.charAt(i);
                if (dojo.lang.inArray(timeFormattingChars, c)) {
                    beginTimePattern = i;
                    break;
                }
            }

            // split pattern in date and time component
            if (beginTimePattern == -1) {
                // pure date pattern
                this.datePattern = pattern;
            } else {
                this.datePattern = dojo.string.trimEnd(pattern.substr(0, beginTimePattern));
                this.timePattern = pattern.substr(beginTimePattern, pattern.length);
            }
        },

        _initDateFormatOptions: function() {
            // These are the options to be passed to dojo's date format/parse functions
            var options = {};

            switch (this.variant) {
                case "date":
                    options.selector = "dateOnly";
                    break;
                case "time":
                    options.selector = "timeOnly";
                    break;
                case "datetime":
                    options.selector = "dateTime";
                    break;
            }

            options.datePattern = this.datePattern;
            options.timePattern = this.timePattern;
            options.locale = this.locale;

            this.dateFormatOptions = options;
        },

        destroy: function(/*Boolean*/finalize) {
            if (this._datePickerNeeded()) {
                if (this.datePicker != null)
                    this.datePicker.destroy(finalize);
                else
                    dojo.debug("DropdownDateTimePicker: no datePicker to destroy?");
            }

            if (this._timePickerNeeded()) {
                if (this.timePicker != null)
                    this.timePicker.destory(finalize);
                else
                    dojo.debug("DropdownDateTimePicker: no timePicker to destroy?");
            }

            cocoon.forms.DropdownDateTimePicker.superclass.destroy.apply(this, arguments);
        }
    }
);