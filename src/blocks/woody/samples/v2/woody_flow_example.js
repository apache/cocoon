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

cocoon.load("resource://org/apache/cocoon/woody/flow/javascript/v2/Form.js");

// Multipage Form example

function example() {
    showForm1();
    selectCar();
}

function showForm1() {
    var form = new Form("form1.xml");
    var locale = determineLocale();
    var wid = form.getWidget();
    //
    // 'wid' is a javascript wrapper of the Woody Form widget.
    //
    // Its subwidgets can be accessed by id.
    //
    // Each widget has a 'value' property to access or modify its value.
    //
    wid.email.value = "bar@www.foo.com";
    wid.somebool.value = true;
    wid.fourchars.value = "aaaa";
    wid.account.value = 2;
    wid.cowheight.value = 2;
    //
    // A Complex widget allows initialization of its subwidgets by
    // assigning an object containing properties corresponding to its
    // subwidgets and their properties:
    //
    wid.visa.value = {part1: {value: "4111"},
                      part2: {value: "1111"},
                      part3: {value: "1111"},
                      part4: {value: "1111"}};
    wid.number1.value = 1;
    wid.number2.value = 3;
    //
    // A Field Widget can receive ValueChange events through its 'onChange' 
    // property:
    //
    wid.number1.onChange = function(oldValue, newValue) {
        print("number1 changing from " + oldValue + " to " + newValue);
        print("wid="+wid);
        print("wid.number2="+wid.number2);
        wid.number2.value = newValue + 1;
    }
    wid.ipaddress.value = "127.0.0.1";
    wid.ipaddress.onChange = function(oldValue, newValue) {
        print("ipaddress changed from " + oldValue + " to " + newValue);
    }
    wid.dieselprice.value = 2.00;
    wid.birthdate.value = new java.util.Date();
    //
    // You can perform actions when a new row is added to a repeater (like
    // setting the row's 'onChange' properties or initializing its values)
    // by assigning a function to the repeater's 'onAddRow' property:
    //
    wid.contacts.onAddRow = function(row) {
        row.firstname.value = "<first name>";
        row.lastname.value = "<last name>";
        row.select.onChange = function(oldValue, newValue) {
            if (newValue) {
                print("you selected: " + row.firstname.value);
            } else {
                print("you deselected: " + row.firstname.value);
            }
        }
    }
    //
    // You can perform actions when a row is removed from a repeater
    // by assigning a function to the repeater's 'onRemoveRow' property:
    //
    wid.contacts.onRemoveRow = function(row) {
      print("you're about to remove: " + row.firstname.value);
    }
    //
    // The rows of a Repeater widget can be accessed using array syntax:
    //
    wid.contacts[0].firstname.value = "Jules";
    wid.contacts[1].firstname.value =  "Lucien";
    // This also works:
    wid.contacts[2].value = {firstname: {value : "Chris"}};
    //
    // A Repeater also has a 'length' property like an array
    //
    print("contacts.length = " + wid.contacts.length);
    //
    // An Action widget can handle ActionEvents through its 'onClick' property
    //
    wid.addcontact.onClick = function() {
        //
        // You can add a row using the addRow() function of Repeater
        //
        wid.contacts.addRow();
    }
    wid.removecontacts.onClick = function() {
        //
        // You can remove rows using the removeRow() function: the argument
        // may be an integer index, or, as in this example, a function acting
        // as a predicate. Each row that satisfies the predicate will
        // be removed.
        //
        wid.contacts.removeRow(function(row) {return row.select.value});
    }
    //
    // A MultiValue widget can be initialized using an array:
    //
    wid.drinks.value = ["Jupiler", "Coca Cola"];

    //
    // You can do additional validation of a form in your flowscript by
    // assigning a function to the form's 'onValidate' property:
    //
    form.onValidate = function() {
        if (wid.cowheight.value == 3) {
            wid.cowheight.setValidationError("cowheight cannot be 3");
        }
    }

    //
    // By calling the Form's setBookmark() function you can set the
    // point in your script to return to when the form is redisplayed. This
    // is useful if you need to acquire resources to process the 
    // pipeline used by the form but also have them be released
    // before the script is suspended e.g.:
    //
    //   form.setBookmark();
    //   var resource = getResource();
    //   form.showForm(uri, function() {
    //      releaseResource(resource);
    //      resource = null;
    //   });
    //
    // Each time the form is redisplayed (e.g. during validation) your
    // script will resume right after the call to setBookmark() and
    // the resource will be reacquired.
    //
    form.setBookmark();

    print("hit bookmark");
    //
    // You can set additional properties on any widget that will
    // be accessible in the pipeline (e.g. with JXTemplateGenerator)
    //
    wid.buttonName = "Commit";

    //
    // showForm() repeatedly sends the form to the browser and doesn't return
    // until validation is complete.
    //
    form.showForm("form1-display-pipeline", function() {
                    // if you supply a function as the second argument
                    // to showForm() it will be called after pipeline
                    // processing, but before the script is suspended.
                    // This is where you would release resources you
                    // don't want to become part of the continuation.
                    delete wid.buttonName;
                  });
    print("cowheight = "+wid.cowheight.value);
}

function selectCar() {
    var form = new Form("carselector_form.xml");
    var wid = form.getWidget();
    wid.make.value = cocoon.parameters.defaultMake;
    wid.make.onChange = function(oldValue, newValue) {
        print("onChange: oldValue: " + oldValue);
        print("onChange: newValue: " + newValue);
        if (newValue != null) {
            wid.type.setSelectionList("cocoon:/cars/"+newValue);
        } else {
            wid.type.setSelectionList(form.createEmptySelectionList("Select a maker first"));
        }
        print("onChange: " + wid.make.value);
        if (wid.make.value == null) {
            wid.message.value = "Yep. Choosing a maker is not that easy...";
        } else {
            if (oldValue == null) {
                wid.message.value = "Good. " + wid.make.value + " makes good cars!";
            } else {
                wid.message.value = "Why not? " + wid.make.value + " also makes good cars!";
            }
        }
    }
    wid.type.onChange = function(oldValue, newValue) {
        if (newValue != null) {
            wid.model.setSelectionList("cocoon:/cars/"+ wid.make.value + "/" + newValue);
        } else {
            wid.model.setSelectionList(form.createEmptySelectionList("Select a type first"));
        }
        if (newValue != null) {
            if (oldValue == null) {
              wid.message.value = "A " + wid.make.value + " " + newValue + " is a very good choice.";
            } else {
              wid.message.value = "So you prefer a " + wid.make.value + " " + newValue + " ?";
            }
        }
        wid.model.value = null;
    }
    form.showForm("carselector-display-pipeline");
    cocoon.sendPage("carselector-success-pipeline", wid);
}

function determineLocale() {
    var localeParam = cocoon.request.get("locale");
    if (localeParam != null && localeParam.length > 0) {
        return Packages.org.apache.cocoon.i18n.I18nUtils.parseLocale(localeParam);
    }
    return null;
}

