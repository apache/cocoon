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

function tools() {
	var toolManager = getPTM();
	if(toolManager.getToolsWithFunctions().size() > 0) {
		cocoon.sendPage("tools.jx", {"tools" : toolManager.getToolsWithFunctions()});
	} else {
		cocoon.sendPage("noTools.jx");
	}
	relPTM(toolManager);
}

function functions() {
	var toolManager = getPTM();
	var tool = toolManager.getTool(cocoon.parameters.name);
	cocoon.sendPage(tool.getId() + "/functions.jx", {"functions" : tool.getFunctions(), "tool" : tool});
	relPTM(toolManager);
}

function menu() {
	var toolManager = getPTM();
	cocoon.sendPage("menu.jx", {"tools" : toolManager.getToolsWithFunctions()});
	relPTM(toolManager);
}