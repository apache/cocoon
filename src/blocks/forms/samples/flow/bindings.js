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
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

/** 
 * Disclaimer: 
 *   To make this flowscript quite generic we've applied some typical javascript 
 *   hacks here and there. Don't let them overwhelm you, the purpose of this tutorial
 *   is _not_ to get into the depths of Cocoon Forms and flowscript. Instead you should 
 *   focus on the effects of the applied definitions in the binding files:
 *   see ../forms/bindings/*-bind.xml.  And how to shape your backend-models:
 *    - for JS see here: createJSBeanfor*() functions
 *    - for Java see the org.apache.cocoon.forms.samples.bindings.* classes
 *    - for XML see ../forms/bindings/*-data.xml.
 *   
 * In the same area of genericity we are not using the FormsTemplateTransformer 
 *   since that would require to provide an extra template file to every sample.
 *
 * To add more binding-tutorial-samples follow these steps:
 *  - decide on a sample-code (e.g. '01value')
 *  - provide a Cocoon Forms formDefinition ../forms/bindings/{sampleCode}-def.xml
 *  - provide a Cocoon Forms formBinding    ../forms/bindings/{sampleCode}-bind.xml
 *  - provide the 3 types of backend-models:
 *    - for JS (javascript) you add here a function createJSBeanFor{sampleCode}()
 *      and let it return the js object to bind to
 *    - for Java you create an actual Java class that gets instantiated and returned
 *      by a function createJavaBeanFor{sampleCode}() over here
 *    - for XML you create a simple XML file over at
 *      ../forms/bindings/{sampleCode}-data.xml
 *  - finally you add a link into the ../welcome.xml page to link to your new sample      
 */




/**
 *  Generic entry-function for all binding samples.  This uses the 
 *   'sample-code' and the 'backend-type' to effectively select the form 
 *   and specific backend-model to use.
 */
function bindingSample(sampleCode, backendType) {
    // sample-code holds the number-and-name of this binding sample in the tutorial
    if (cocoon.parameters["sample-code"] != undefined) {
        sampleCode = cocoon.parameters["sample-code"];
    }
	
    // backend-type holds one of 'JS', 'Java' or 'XML' to indicate 
    // the type of backend to use.
    if (cocoon.parameters["backend-type"] != undefined) {
        backendType = cocoon.parameters["backend-type"];
    }

    // all back-end models are bound to essentially the same form, using the same binding!
    var form = createFormForSample(sampleCode);

    // the beack-end model itself however depends on sample and type. 
    var bean = createBeanForSample(backendType, sampleCode);
	
    // loads the backend-bean into the form
    form.load(bean);

    // wait for user to submit the form correctly	
    form.showForm("binding.form." + sampleCode);
	
    // saves the form into the backend-bean
    form.save(bean);
    var bizData = new Object();
    //hack the XML content to a string for display
    if (backendType.equals("XML")) {
    	bean = {xml: serializeNode(bean)};
    }
    bizData["bean"] = bean;
    bizData["backendType"] = backendType;
    bizData["sampleCode"] = sampleCode;

    cocoon.sendPage("binding.done", bizData);
}

/** 
 * Uses internal cocoon utility class to convert the DOM Node to it's 
 * String representation .
 */
function serializeNode(node) {
    return Packages.org.apache.cocoon.xml.XMLUtils.serializeNode(node);
}

/** 
 * Creates the form for this sample. And automatically creates the accompanied 
 * binding.
 */
function createFormForSample(sampleCode) {
    var form = new Form("forms/binding/" + sampleCode + "-def.xml");
    form.createBinding("forms/binding/" + sampleCode +"-bind.xml");
    return form;
}

/** 
 * Creates the Bean of the desired type for this sample.
 */
function createBeanForSample(backendType, sampleCode) {
    if (backendType.equals("XML")) {
        return createXMLBean(sampleCode);
    } else {
        var factoryFunction = "create" + backendType + "BeanFor" + sampleCode;
        print("Using the bean returned by function " + factoryFunction + "()");
        return this[factoryFunction].apply();
    }
}

/** 
 * Finds the sample specific XML file to bind to and parses it into a DOM Document.
 */
function createXMLBean(sampleCode) {
    // note if you want to toss around with the XML model (e.g. sample 02lenient)
    // then you should do that by editing the files: ../forms/binding/*-data.xml

    var uri = "forms/binding/" + sampleCode +"-data.xml";
    print("Using the XML data file at " + uri);

    var parser = null;
    var source = null;
    var resolver = null;
    try {
        parser = cocoon.getComponent(Packages.org.apache.excalibur.xml.dom.DOMParser.ROLE);
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        source = resolver.resolveURI(uri);
        var is = new Packages.org.xml.sax.InputSource(source.getInputStream());
        is.setSystemId(source.getURI());
        //Note: we immediately narrow down to the root-element here to avoid
        // needing to wrap js and Java beans in a silly 'root'
        return parser.parseDocument(is).getDocumentElement();
    } finally {
        if (source != null)
            resolver.release(source);
        cocoon.releaseComponent(parser);
        cocoon.releaseComponent(resolver);
    }
}	

/**
 * Creates the JS Bean for sample '01value'
 */ 
function createJSBeanFor01value() {
    var bean;
    bean = new Object();
    bean.simple = "Simple";
    bean.readOnly = "Read-Only";
    bean.writeOnly = "Write-Only";
    bean.diffIn = "Diff-in/out";
    // diffOut doesn't need to exist, binding will create it.
    bean.onUpdate = "On Update";
    bean.updateCount = 0;
    bean.bool = true;
    bean.date = "19700605";
    bean.other = "This field is not involved in the form.";
    return bean;
}

/**
 * Creates the Java Bean for sample '01value'
 */ 
function createJavaBeanFor01value() {
    return new Packages.org.apache.cocoon.forms.samples.bindings.ValuesBean();
}

/**
 * Creates the JS Bean for sample '02lenient'
 */ 
function createJSBeanFor02lenient() {
    var bean = new Object();
    var contexts = ["one","two","three"];	  
    for(var i=0; i<contexts.length; i++) {
        bean[contexts[i]] = new Object();
        // using javascript beans seem to survive even non lenient binding
        // so you can do here what you want, the bean itself seems to be leninet?
        //bean[contexts[i]]["breakingField"] = "present";
    }
    return bean;
}

/**
 * Creates the Java Bean for sample '02lenient'
 */ 
function createJavaBeanFor02lenient() {
    var bean = new Packages.java.util.HashMap();
    // to see the runtime effect of non-lenient binding
    // remove/replace the 'one' in the following list:
    var contexts = ["one","two","three"];	  // only the 'one' context is required by non-lenient binding
        for(var i=0; i<contexts.length; i++) {
        // to see the runtime effect of non-lenient binding
        // swap the following 2 lines from comment to code 
            var subBean = new Packages.org.apache.cocoon.forms.samples.bindings.LenientOKBean("init");
//            var subBean = new Packages.org.apache.cocoon.forms.samples.bindings.LenientNotOKBean("init");
	    
            // the NotOkBean does not have a getBreakingField() required by the non-lenient binding

            bean.put(contexts[i], subBean);
    }
    return bean;
}


/**
 * Creates the JS Bean for sample '03aggregate'
 */ 
function createJSBeanFor03aggregate() {
    var bean = new Object();
    bean["match-combined"] = new Object();
    bean["match-combined"].split = new Object();
    bean["match-combined"].split.day = "7";
    bean["match-combined"].split.month = "4";
    bean["match-combined"].split.year = "2004";

    bean["match-split"] = new Object();    
    bean["match-split"].combined = "07/04/2004";
    bean["match-split"].split = new Object();
    return bean;
}

/**
 * Creates the Java Bean for sample '03aggregate'
 */ 
function createJavaBeanFor03aggregate() {
    var bean = new Packages.java.util.HashMap();
    bean.put("match-combined", new Packages.org.apache.cocoon.forms.samples.bindings.DateWrapper("07","04","2004"));
    bean.put("match-split", new Packages.org.apache.cocoon.forms.samples.bindings.DateWrapper("07","04","2004"));
    return bean;
}

/**
 * sample conversion methods used by the javascript binding in '05custom'
 */

function doLoadConversion(backendValue, delimiter){
  var presuffix = "" + delimiter + delimiter;
  var result = "";
  if (backendValue.startsWith(presuffix) 
      && backendValue.endsWith(presuffix)
      && backendValue.length() >= 4) {
    result = backendValue.substring(2,backendValue.length() - 2);
  }
  return result;
}

function doSaveConversion(formValue, delimiter){
  var result = "" + delimiter + delimiter + formValue + delimiter + delimiter;
  return result;
}

/**
 * Creates the JS Bean for sample '05custom'
 */ 
function createJSBeanFor05custom() {
    var bean = {"jswrap-value": "--wrapped value--",
                "custom-value": "**custom value**",
                "config-value": "[[config value]]"};
    return bean;
}

/**
 * Creates the Java Bean for sample '05custom'
 */ 
function createJavaBeanFor05custom() {
    var bean = new Packages.java.util.HashMap();
    bean.put("jswrap-value", "--wrapped value--");
    bean.put("custom-value", "**custom value**");
    bean.put("config-value", "[[config value]]");
    return bean;
}
