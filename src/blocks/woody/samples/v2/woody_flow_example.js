/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
        var row = wid.contacts.addRow();
        row.select.onChange = function(oldValue, newValue) {
            if (newValue) {
                print("you selected: " + row.firstname.value);
            } else {
                print("you deselected: " + row.firstname.value);
            }
        }
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
    // You can set additional properties on any widget that will
    // be accessible in the pipeline (e.g. with JXTemplateGenerator)
    //
    wid.buttonName = "Commit";

    //
    // showForm() repeatedly sends the form to the browser and doesn't return
    // until validation is complete.
    //
    form.showForm("form1-display-pipeline");
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
              wid.message.value = "So you prefer a " +  " ?";
            }
        }
    }
    form.showForm("carselector-display-pipeline");
    cocoon.request.setAttribute("carselectorform", form.getWidget().unwrap());
    cocoon.sendPage("carselector-success-pipeline.xsp");
}

function determineLocale() {
    var localeParam = cocoon.request.get("locale");
    if (localeParam != null && localeParam.length > 0) {
        return Packages.org.apache.cocoon.i18n.I18nUtils.parseLocale(localeParam);
    }
    return null;
}

