/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.selection;

import org.apache.avalon.ConfigurationException;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.traversal.TreeWalker;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import org.apache.xerces.dom.TreeWalkerImpl;

import org.apache.cocoon.CodeFactory;

/**
 * This class generates source code which tests a specific browser pattern
 * agains the requesting user-agent
 *
 * @author <a href="mailto:cziegeler@sundn.de">Carsten Ziegeler</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.10 $ $Date: 2000-10-30 20:21:44 $
*/


public class BrowserSelectorFactory implements CodeFactory {

    public String generateParameterSource (DocumentFragment conf)
    throws ConfigurationException {
        return "String []";
    }

    public String generateClassSource (String test, String prefix, DocumentFragment conf)
    throws ConfigurationException {
        TreeWalker tw = new TreeWalkerImpl (conf, NodeFilter.SHOW_ALL, null, false);
        Node node = null;
        Node nodeattrname  = null;
        Node nodeattruseragent = null;
        NamedNodeMap nm = null;
        int cnt = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("static String [] ")
          .append(prefix)
          .append("_expr = {");
        while ((node = tw.nextNode()) != null) {
            if (node.getNodeName().equals("browser") &&
                node.getNodeType() == Node.ELEMENT_NODE) {
                nm = node.getAttributes();
                if (nm != null) {
                    nodeattrname = nm.getNamedItem("name");
                    nodeattruseragent = nm.getNamedItem("useragent");
                    if (nodeattrname != null && nodeattruseragent != null
                            && nodeattrname.getNodeValue().equals(test)) {
                        sb.append(cnt==0 ? "\"" : ",\"")
                          .append(nodeattruseragent.getNodeValue())
                          .append("\"");
                    }
                }
            }
        }
        return sb.append("};").toString();
    }

    public String generateMethodSource (DocumentFragment conf)
    throws ConfigurationException {
        TreeWalker tw = new TreeWalkerImpl (conf, NodeFilter.SHOW_ALL, null, false);
        Node node = null;
        Node nodeattrname  = null;
        Node nodeattruseragent = null;
        NamedNodeMap nm = null;

        StringBuffer sb = new StringBuffer();
         sb.append("if (pattern != null && objectModel.get(Cocoon.REQUEST_OBJECT) != null) {")
          .append("javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest) objectModel.get(Cocoon.REQUEST_OBJECT);")
          .append("String userAgent = request.getHeader(\"User-Agent\");")
	  .append("for (int i = 0; i < pattern.length; i++) {")
          .append("if (userAgent.indexOf(pattern[i]) != -1) return true;}");
        return sb.append("} return false;").toString();
    }
}
