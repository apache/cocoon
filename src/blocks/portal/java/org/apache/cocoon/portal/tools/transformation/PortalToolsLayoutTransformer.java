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
package org.apache.cocoon.portal.tools.transformation;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.tools.PortalTool;
import org.apache.cocoon.portal.tools.PortalToolFunction;
import org.apache.cocoon.portal.tools.PortalToolManager;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.SAXException;

/**
 * Adds the navigation etc. to the document
 *
 * @version $Id$
 */
public class PortalToolsLayoutTransformer extends AbstractSAXTransformer
                                          implements Disposable /*, Parameterizable */ {

	public static final String ROLE = PortalToolsLayoutTransformer.class.getName();

	private PortalToolManager pm;
	private String selected;

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
	 */
	public void service(ServiceManager manager) throws ServiceException {
		super.service(manager);
		pm =  (PortalToolManager) manager.lookup(PortalToolManager.ROLE);
	}

	/* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
            Parameters par) throws ProcessingException, SAXException,
            IOException {
        super.setup(resolver, objectModel, src, par);
        try {
            selected = par.getParameter("selected");
        } catch (ParameterException e) {
            // does not matter, default handling
        }
    }

	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose() {
        if (this.manager != null) {
            this.manager.release(pm);
            this.pm = null;
        }
        super.dispose();
	}


	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		super.startDocument();
		AttributesImpl a = new AttributesImpl();

		// took the div-tag as root, because it does not matter in the output, if it passes the xsl transformation
		super.startPrefixMapping("i18n", "http://apache.org/cocoon/i18n/2.1");
		super.startElement("", "div","div", a);
		super.startElement("", "tab-layout", "tab-layout", a);
			Collection tools = pm.getToolsWithFunctions();
			for(Iterator it = tools.iterator(); it.hasNext();) {
				AttributesImpl attr = new AttributesImpl();
				PortalTool pt = (PortalTool) it.next();
				attr.addCDATAAttribute("parameter", "tools/functions/" + pt.getId());
				attr.addCDATAAttribute("name", pt.getName());
				attr.addCDATAAttribute("http://apache.org/cocoon/i18n/2.1", "attr", "i18n:attr", "name");
				if(selected != null) {
				    if(selected.equals(pt.getId())) {
				        attr.addCDATAAttribute("selected", "true");
				    }
				}
				super.startElement("","named-item", "named-item", attr);
				super.endElement("", "named-item", "named-item");
			}
		super.endElement("", "tab-layout", "tab-layout");
		if(selected != null) {
			PortalTool ct = pm.getTool(selected);
			if(ct != null) {
				super.startElement("", "tool-functions", "tool-functions", a);
				Collection funs = ct.getFunctions();
				for(Iterator it = funs.iterator();it.hasNext();) {
						PortalToolFunction ptf = (PortalToolFunction) it.next();
						AttributesImpl attr = new AttributesImpl();
						attr.addCDATAAttribute("parameter", "tools/plugins/" + ct.getId() + "/" + ptf.getFunction());
						attr.addCDATAAttribute("name", ptf.getName());
						attr.addCDATAAttribute("http://apache.org/cocoon/i18n/2.1", "attr", "i18n:attr", "name");						super.startElement("", "function", "function", attr);
						super.endElement("", "function", "function");
				}
				super.endElement("", "tool-functions", "tool-functions");
			}
	    }
	}


	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		super.endElement("", "div", "div");
		super.endPrefixMapping("i18n");
		selected= null;
		super.endDocument();
	}

}
