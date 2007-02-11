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
package org.apache.cocoon.portal.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.xml.sax.SAXException;

/**
 *
 * @version $Id$
 */
public class PortalToolBuilder {

    public PortalTool buildTool(File confFile, String rootDir, String pluginDir, String i18nDir) {
        PortalTool pTool = null;
        try {
	        DefaultConfigurationBuilder dcb = new DefaultConfigurationBuilder();
	        Configuration conf = dcb.buildFromFile(confFile);
	        String toolName = conf.getAttribute("name");
	        String toolId = conf.getAttribute("id");
	        HashMap functions = new HashMap();
			ArrayList i18n = new ArrayList();

			Configuration[] funcs = conf.getChild("functions").getChildren();
			for(int i = 0; i < funcs.length; i++) {
			    PortalToolFunction ptf = new PortalToolFunction();
			    ptf.setName(funcs[i].getAttribute("name"));
			    ptf.setFunction(funcs[i].getAttribute("pipeline"));
			    ptf.setId(funcs[i].getAttribute("id"));
			    ptf.setInternal(funcs[i].getAttributeAsBoolean("internal", false));
			    functions.put(ptf.getName(), ptf);
			}
			Configuration[] i18ns = conf.getChild("i18n").getChildren();
			for(int i = 0; i < i18ns.length; i++) {
			    PortalToolCatalogue ptc = new PortalToolCatalogue();
			    ptc.setId(i18ns[i].getAttribute("id"));
			    ptc.setLocation(rootDir + pluginDir + toolId + "/" + i18nDir);
			    ptc.setName(i18ns[i].getAttribute("name"));
			    i18n.add(ptc);
			}
			pTool = new PortalTool(toolName, toolId, functions, i18n);
        } catch (ConfigurationException ece) {
            // TODO
            ece.printStackTrace();
        } catch (SAXException esax) {
            // TODO
            esax.printStackTrace();
		} catch (IOException eio) {
            // TODO
            eio.printStackTrace();
        }
		return pTool;
    }
}
