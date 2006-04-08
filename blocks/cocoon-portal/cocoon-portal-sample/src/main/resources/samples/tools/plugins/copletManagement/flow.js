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
cocoon.load("resource://org/apache/cocoon/portal/tools/ptm.js");

// Functions to modify the PortalLayout
// var layoutActions = new Packages.org.apache.cocoon.portal.tools.copletManagement.LayoutActions(portalLayout, componentManager.getLayoutFactory(), componentManager.getCopletFactory(), profileManager);

function showTab(showId) {
	var toolManager = getPTM();
	var portalObjects = toolManager.getPortalObjects();
	var portalLayout = portalObjects.getPortalLayout();
	var portalService = portalObjects.getPortalService();
	var componentManager = portalObjects.getComponentManager();
	var profileManager = portalObjects.getProfileManager();
	var safeLayout = portalLayout;

	/* Use a copy of the Layout in the Tools - DOES NOT WORK ATM
	var safeLayout = cocoon.session.getAttribute("safeLayout");	
	if(safeLayout == null) {
		safeLayout = portalLayout.copy();
		cocoon.session.setAttribute("safeLayout", safeLayout);
	}
	*/
var layoutActions = new Packages.org.apache.cocoon.portal.tools.copletManagement.LayoutActions(safeLayout, componentManager.getLayoutFactory(), componentManager.getCopletFactory(), profileManager);
	var id = cocoon.request.id;
	if(showId != null) {
		id = showId;
	}
	if (id == null) id = "";
	
	var action = cocoon.request.action;
	var actionitem = cocoon.request.actionitem;
	
	if(action == "del") {
		layoutActions.del(actionitem);
	}
	if(action == "up") {
		layoutActions.move(actionitem, true);
	}
	if(action == "down") {
		layoutActions.move(actionitem, false);
	}
	if(action == "addCol") {
		layoutActions.add(actionitem, "column");
	}
	if(action == "addRow") {
		layoutActions.add(actionitem, "row");
	}
	if(action == "addTab") {
		var form = new Form("cocoon:/model/addTab");
		form.showForm("form/addTab/template");
		var name = form.getChild("name").getValue();
		
		if (actionitem == "maintab") 
			actionitem = "1";
		
		layoutActions.addTab(actionitem, name);
	}

	if(action =="addCoplet") {
		handleAddCoplets(actionitem, layoutActions);
	}	
	
	if(action == "save") {
		profileManager.saveUserProfiles(null);
	}
	
	/* save and restore functions if we work with a copy of the layout later.
	if(action == "save") {
		print("safe layout");
		profileManager.storeProfile(safeLayout, null);
		cocoon.session.setAttribute("safeLayout", null);
	}
	
	if(action == "restore") {
		print("restore layout");
		safeLayout = portalLayout.copy();
		cocoon.session.setAttribute("safeLayout", safeLayout);
	}
	*/ 
	
	cocoon.sendPage("page/showTab/" + id, {"layout" : safeLayout });
	toolManager.releasePortalObjects(portalObjects);
	relPTM(toolManager);
}

function showXml() {
	var toolManager = getPTM();
	var portalObjects = toolManager.getPortalObjects();
	var portalLayout = portalObjects.getPortalLayout();
	var bla = cocoon.request.foo;
	cocoon.sendPage("layoutProfile/" + bla, {"layout" : portalLayout });
	toolManager.releasePortalObjects(portalObjects);
	relPTM(toolManager);
}

function showCopletList(current, item) {
	var toolManager = getPTM();
	var portalObjects = toolManager.getPortalObjects();
	var profileManager = portalObjects.getProfileManager();
	var coplets = profileManager.getCopletInstanceDatas();
	
	cocoon.sendPage("jx/copletList.jx", {"coplets": coplets, "item" : item, "current" : current});
	toolManager.releasePortalObjects(portalObjects);
	relPTM(toolManager);
}

function handleAddCoplets(parent, layoutActions) {
	var toolManager = getPTM();
	var portalObjects = toolManager.getPortalObjects();
	var portalService = portalObjects.getPortalService();
	var profileManager = portalObjects.getProfileManager();
	
	var form = new Form("cocoon:/model/addCoplet");
	form.createBinding("form/addCoplet/binding.xml");
	var coplets = profileManager.getCopletDatas();
	form.load(profileManager);
	form.showForm("form/addCoplet/template");
	layoutActions.getSelectedCoplets(form.getChild("coplets"), coplets, parent);

	toolManager.releasePortalObjects(portalObjects);
	relPTM(toolManager);
}

function selectSkin() {
	var toolManager = getPTM();
	var portalObjects = toolManager.getPortalObjects();
	var portalService = portalObjects.getPortalService();
	var portalLayout = portalObjects.getPortalLayout();

	var id = cocoon.request.id;
	var skins = portalService.getSkinDescriptions();
	if(id != null) {
		for(var it = skins.iterator(); it.hasNext();) {
			var skinName = it.next().getName();
			if(skinName.equals(id))
				portalLayout.getParameters().put("skin", skinName);
		}
		id = null;
	}
	cocoon.sendPageAndWait("jx/selectSkin.jx", {"skins" : skins});

	toolManager.releasePortalObjects(portalObjects);
	relPTM(toolManager);
}

function editCoplet() {
	var toolManager = getPTM();
	var portalObjects = toolManager.getPortalObjects();
	var portalService = portalObjects.getPortalService();
	var portalLayout = portalObjects.getPortalLayout();
	var componentManager = portalObjects.getComponentManager();
	var profileManager = portalObjects.getProfileManager();
	var layoutActions = new Packages.org.apache.cocoon.portal.tools.copletManagement.LayoutActions(portalLayout, componentManager.getLayoutFactory(), componentManager.getCopletFactory(), profileManager);

	
	var copletId = cocoon.request.actionitem;
	var backId = cocoon.request.id;
	var instanceData = layoutActions.getCopletInstanceData(copletId);
	var copletData = instanceData.getCopletData();
	var copletType = instanceData.getCopletData().getCopletBaseData().getCopletAdapterName();
	var visible = instanceData.getAttributes().get("visible");
	if(visible == null) {
		instanceData.getAttributes().put("visible", new Packages.java.lang.Boolean(true));
	}
	var which = "all";
	if(copletType.equals("uri"))
		which = "uriCoplet";
	var form = new Form("cocoon:/model/editCoplet/" + which);
	form.createBinding("form/editCoplet/" + which + "/binding.xml");
	while(1) {
		form.load(instanceData);
		form.showForm("form/editCoplet/" + which +"/template", {"backId" : backId});
		form.save(instanceData);
	}

	toolManager.releasePortalObjects(portalObjects);
	relPTM(toolManager);
}
