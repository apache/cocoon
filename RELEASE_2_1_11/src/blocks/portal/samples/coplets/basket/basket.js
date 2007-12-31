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
//
// @version $Id$
//
cocoon.load("resource://org/apache/cocoon/forms/flow/javascript/Form.js");

// This function is invoked by every coplet to check if the basket is already full
function add() {
    var copletId = cocoon.parameters["id"];
    var type = cocoon.parameters["type"];
    var storage = "basket";

    var pu = cocoon.createObject(Packages.org.apache.cocoon.components.flow.util.PipelineUtil);
    var dom = pu.processToDOM("fetch-quota", { "storage": storage, "type": type});

    var node = org.apache.excalibur.xml.xpath.XPathUtil.getFirstNodeFromPath(dom,
                        org.apache.excalibur.xml.xpath.XPathUtil.buildPathArray("result/attribute/item"), false);
    var itemCount = org.apache.cocoon.xml.dom.DOMUtil.getValueOfNode(node);
    node = org.apache.excalibur.xml.xpath.XPathUtil.getFirstNodeFromPath(dom,
                        org.apache.excalibur.xml.xpath.XPathUtil.buildPathArray("result/attribute/size"), false);
    var maxSize = org.apache.cocoon.xml.dom.DOMUtil.getValueOfNode(node);
    
    var manager = cocoon.getComponent(org.apache.cocoon.portal.coplets.basket.BasketManager.ROLE);
    var store;
    if ( storage.equals("basket" ) ) {
        store = manager.getBasket();
    } else {
        store = manager.getBriefcase();
    }
    var isFull = true;
    
    if ( store.size() < itemCount && store.contentSize() < maxSize ) {
        isFull = false;
    }
    cocoon.sendPage(cocoon.parameters["view"], {"bookmark" : type, "isBasketFull" : isFull});
}

// This function is invoked by every coplet to check if the basket is already full
function getQuota() {
    var type = cocoon.parameters["type"];
    var storage = "basket";

    var pu = cocoon.createObject(Packages.org.apache.cocoon.components.flow.util.PipelineUtil);
    var dom = pu.processToDOM("fetch-quota", { "storage": storage, "type": type});

    var node = org.apache.excalibur.xml.xpath.XPathUtil.getFirstNodeFromPath(dom,
                        org.apache.excalibur.xml.xpath.XPathUtil.buildPathArray("result/attribute/item"), false);
    var itemCount = org.apache.cocoon.xml.dom.DOMUtil.getValueOfNode(node);
    node = org.apache.excalibur.xml.xpath.XPathUtil.getFirstNodeFromPath(dom,
                        org.apache.excalibur.xml.xpath.XPathUtil.buildPathArray("result/attribute/size"), false);
    var maxSize = org.apache.cocoon.xml.dom.DOMUtil.getValueOfNode(node);
    if ( maxSize != null && maxSize.length() > 0 ) {
        var d = new java.lang.Double(maxSize).doubleValue();
        d = d / 10.24;
        d = Math.floor(d) / 100.0;
        if ( d < 0.1 && d != 0.0 ) { d = 0.1; }
        maxSize = java.lang.String.valueOf(d);
    }
    cocoon.sendPage(cocoon.parameters["view"], {"itemCount" : itemCount, "maxSize" : maxSize});
}

// This function is invoked by the coplet with input process
function eval() {
    var copletId = cocoon.parameters["id"];
    var type = cocoon.parameters["type"];
    var storage = "basket";

    var manager = cocoon.getComponent(org.apache.cocoon.portal.coplets.basket.BasketManager.ROLE);
    var store;
    if ( storage.equals("basket" ) ) {
        store = manager.getBasket();
    } else {
        store = manager.getBriefcase();
    }

    // get the portal service
    var service = cocoon.getComponent(org.apache.cocoon.portal.PortalService.ROLE);
    var linkService = service.getComponentManager().getLinkService();
    var profileManager = service.getComponentManager().getProfileManager();
    // update the attribute of the current coplet:
    var coplet = profileManager.getCopletInstanceData(copletId);
    coplet.setAttribute("value", cocoon.request["text"]);
   
    var url;
    
    // let's check the action
    if ( cocoon.request["content"] != null ) {
        // the content button has been pressed
        var target = profileManager.getCopletInstanceData("basket-sample-7");
        target.setAttribute("value", cocoon.request["text"]);
        var item = new Packages.org.apache.cocoon.portal.coplets.basket.ContentItem(target, true);
        var event = new Packages.org.apache.cocoon.portal.coplets.basket.events.AddItemEvent(store, item);
        url = linkService.getLinkURI(event);
        // now use a bookmark to switch the tab
        var pos = url.indexOf('?');
        url = "bookmark?baskettab=2&" + url.substring(pos+1);
    } else if ( cocoon.request["link"] != null ) {
        // the link button has been pressed
        var target = profileManager.getCopletInstanceData("basket-sample-7");
        target.setAttribute("value", cocoon.request["text"]);
        var item = new Packages.org.apache.cocoon.portal.coplets.basket.ContentItem(target, false);
        var event = new Packages.org.apache.cocoon.portal.coplets.basket.events.AddItemEvent(store, item);
        url = linkService.getLinkURI(event);
        // now use a bookmark to switch the tab
        var pos = url.indexOf('?');
        url = "bookmark?baskettab=2&" + url.substring(pos+1);
    } else {
        // we redirect to the main page
        url = "portal";
    }
    
    // we have to reset our state
    coplet.setTemporaryAttribute("doNotCache", "1");
    coplet.setTemporaryAttribute("application-uri", coplet.getCopletData().getAttribute("temporary:application-uri"));

    // and now do a redirect
    cocoon.redirectTo(url, true);
}

// This function changes the title
function changeTitle() {
    var copletId = cocoon.parameters["id"];

    // get the portal service
    var service = cocoon.getComponent(org.apache.cocoon.portal.PortalService.ROLE);
    var profileManager = service.getComponentManager().getProfileManager();
    var coplet = profileManager.getCopletInstanceData(copletId);

    // title is the first 25 characters
    var title = coplet.getAttribute("value");
    if ( title != null ) {
        var l = 25;
        if ( title.length() < 25 ) { l = title.length(); }
        title = title.substring(0, l);
    }
    coplet.setAttribute("title", title);
    cocoon.sendPage(cocoon.parameters["view"]);
}

// This function is invoked by the query sample
function query() {
    var copletId = cocoon.parameters["id"];

    // get the portal service
    var service = cocoon.getComponent(org.apache.cocoon.portal.PortalService.ROLE);
    var profileManager = service.getComponentManager().getProfileManager();
    var coplet = profileManager.getCopletInstanceData(copletId);

    var target = profileManager.getCopletInstanceData("basket-sample-2");
    var value = target.getAttribute("value");
    if ( value == null ) value = 0;
    var millis = Packages.java.lang.System.currentTimeMillis();
    millis = millis + ((new Packages.java.lang.Long(value).longValue()) * 60000);
    var date = new Packages.java.util.Date(millis);
    coplet.setAttribute("value", date);
    
    cocoon.sendPage(cocoon.parameters["view"], {"date" : date});
}

function showresult() {
    var copletId = cocoon.parameters["id"];

    // get the portal service
    var service = cocoon.getComponent(org.apache.cocoon.portal.PortalService.ROLE);
    var profileManager = service.getComponentManager().getProfileManager();
    var coplet = profileManager.getCopletInstanceData(copletId);

    cocoon.sendPage(cocoon.parameters["view"], {"date" : coplet.getAttribute("value")});
}

// This function is invoked to add the result of the query
function result() {
    var copletId = cocoon.parameters["id"];
    var storage = "basket";

    var manager = cocoon.getComponent(org.apache.cocoon.portal.coplets.basket.BasketManager.ROLE);
    var store;
    if ( storage.equals("basket" ) ) {
        store = manager.getBasket();
    } else {
        store = manager.getBriefcase();
    }

    // get the portal service
    var service = cocoon.getComponent(org.apache.cocoon.portal.PortalService.ROLE);
    var profileManager = service.getComponentManager().getProfileManager();
    var coplet = profileManager.getCopletInstanceData(copletId);

    var item = new Packages.org.apache.cocoon.portal.coplets.basket.ContentItem("cocoon://samples/blocks/portal/coplets/basket/copletwithappresult.showresult.flow", true);
    var event = new Packages.org.apache.cocoon.portal.coplets.basket.events.AddItemEvent(store, item);

    service.getComponentManager().getEventManager().getPublisher().publish(event);
    
    cocoon.sendPage(cocoon.parameters["view"]);
}

// this function is invoked to show the dialog after the query
function dialog() {
    var copletId = cocoon.parameters["id"];
    var storage = "basket";

    // get the portal service
    var service = cocoon.getComponent(org.apache.cocoon.portal.PortalService.ROLE);
    var profileManager = service.getComponentManager().getProfileManager();
    var coplet = profileManager.getCopletInstanceData(copletId);

    var manager = cocoon.getComponent(org.apache.cocoon.portal.coplets.basket.BasketManager.ROLE);
    var store;
    if ( storage.equals("basket" ) ) {
        store = manager.getBasket();
    } else {
        store = manager.getBriefcase();
    }

    var item = new Packages.org.apache.cocoon.portal.coplets.basket.ContentItem(coplet, false);
    var event = new Packages.org.apache.cocoon.portal.coplets.basket.events.AddItemEvent(store, item);

    service.getComponentManager().getEventManager().getPublisher().publish(event);

    var action;
    if ( storage.equals("basket" ) ) {
        action = manager.getBasketAction(cocoon.request["action"]);
    } else {
        action = manager.getBriefcaseAction(cocoon.request["action"]);
    }
    var freq = new Packages.java.lang.Integer(cocoon.request["frequency"]).intValue();
    item.setAttribute("action-replay", "true");
    item.setAttribute("action-name", cocoon.request["action"]);
    item.setAttribute("action-freq", new java.lang.Long(freq).toString());
   
    manager.addBatch(item, freq, action);

    cocoon.sendPage(cocoon.parameters["view"]);
}    

// This function is invoked by the coplet list sample
function list() {
    var copletId = cocoon.parameters["id"];
    var storage = "basket";

    var manager = cocoon.getComponent(org.apache.cocoon.portal.coplets.basket.BasketManager.ROLE);
    var store;
    if ( storage.equals("basket" ) ) {
        store = manager.getBasket();
    } else {
        store = manager.getBriefcase();
    }

    // get the portal service
    var service = cocoon.getComponent(org.apache.cocoon.portal.PortalService.ROLE);
    var profileManager = service.getComponentManager().getProfileManager();
    var coplet = profileManager.getCopletInstanceData(copletId);
    
    // now test check boxes
    if ( cocoon.request["sample1"] != null ) {
        addCoplet(profileManager, service, store, "basket-sample-1");
    }
    if ( cocoon.request["sample2"] != null ) {
        addCoplet(profileManager, service, store, "basket-sample-2");
    }
    if ( cocoon.request["sample3"] != null ) {
        addCoplet(profileManager, service, store, "basket-sample-3");
    }
    if ( cocoon.request["sample4"] != null ) {
        addCoplet(profileManager, service, store, "basket-sample-4");
    }
    if ( cocoon.request["sample5"] != null ) {
        addCoplet(profileManager, service, store, "basket-sample-5");
    }

    // we have to reset our state
    coplet.setTemporaryAttribute("application-uri", coplet.getCopletData().getAttribute("temporary:application-uri"));

    cocoon.redirectTo(coplet.getTemporaryAttribute("application-uri"), false);
}

// helper function
function addCoplet(profileManager, service, store, copletId) {
    var coplet = profileManager.getCopletInstanceData(copletId);
    var item = new Packages.org.apache.cocoon.portal.coplets.basket.ContentItem(coplet, true);
    var event = new Packages.org.apache.cocoon.portal.coplets.basket.events.AddItemEvent(store, item);

    service.getComponentManager().getEventManager().getPublisher().publish(event);
}

// process the form of the content portlet
function processBasket() {
    var manager = cocoon.getComponent(org.apache.cocoon.portal.coplets.basket.BasketManager.ROLE);
    var service = cocoon.getComponent(org.apache.cocoon.portal.PortalService.ROLE);
    if ( service.getPortalName() == null ) {
        service.setPortalName("portal");
    }
    
    // test parameters
    var names = cocoon.request.getParameterNames();
    while ( names.hasMoreElements()) {
        var name = names.nextElement();
        if ( name.startsWith("c" )) {
            var id = name.substring(1);
            var actionName = cocoon.request["a"+id];
            var storage = cocoon.request["s"+id]
            var store;
            if ( storage.equals("basket" ) ) {
                store = manager.getBasket();
            } else if ( storage.equals("folger") ) {
                store = manager.getFolder();
            } else {
                store = manager.getBriefcase();
            }
            var item = store.getItem(new Packages.java.lang.Long(id));
            
            if ( "delete".equals(actionName) ) {
                // delete
                var event = new Packages.org.apache.cocoon.portal.coplets.basket.events.RemoveItemEvent(store, item);
                service.getComponentManager().getEventManager().getPublisher().publish(event);
            } else if ( "briefcase".equals(actionName) ) {
                // move to briefcase
                var event = new Packages.org.apache.cocoon.portal.coplets.basket.events.MoveItemEvent(store, item, manager.getBriefcase());               
                service.getComponentManager().getEventManager().getPublisher().publish(event);
            } else if ( "basket".equals(actionName) ) {
                // move to basket
                var event = new Packages.org.apache.cocoon.portal.coplets.basket.events.MoveItemEvent(store, item, manager.getBasket());               
                service.getComponentManager().getEventManager().getPublisher().publish(event);
            } else {
                // this is a real action
			    var action;
			    if ( storage.equals("basket" ) ) {
			        action = manager.getBasketAction(actionName);
			    } else {
			        action = manager.getBriefcaseAction(actionName);
			    }
			    var freq = new Packages.java.lang.Integer(cocoon.request["f"+id]).intValue();
			    if ( cocoon.request["r"+id] != null ) {
  			        // store values
  			        item.setAttribute("action-replay", cocoon.request["r"+id]);
  			        item.setAttribute("action-name", actionName);
			        item.setAttribute("action-freq", new java.lang.Long(freq).toString());
  			    } else {
  			        item.removeAttribute("action-name");
  			        item.removeAttribute("action-freq");
  			        item.removeAttribute("action-replay");
  			        freq = -1;
  			    }
			    manager.update(store);
			    
			    // call batch
			    manager.addBatch(item, freq, action);
            }        
        }
    }
    cocoon.redirectTo("../../portal", true);
}