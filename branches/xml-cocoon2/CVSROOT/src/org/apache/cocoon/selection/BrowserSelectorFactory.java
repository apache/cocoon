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

/**
 * This class generates source code which tests a specific browser pattern
 * agains the requesting user-agent
 *
 * @author <a href="mailto:cziegeler@sundn.de">Carsten Ziegeler</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2000-09-07 21:38:37 $
*/


public class BrowserSelectorFactory implements SelectorFactory {

    public String generateClassSource (String test, String prefix, DocumentFragment conf)
    throws ConfigurationException {
        return "";
    }

    public String generateMethodSource (String test, String prefix, DocumentFragment conf)
    throws ConfigurationException {
        TreeWalker tw = new TreeWalkerImpl (conf, NodeFilter.SHOW_ALL, null, false);
        Node node = null;
        Node nodeattrname  = null;
        Node nodeattruseragent = null;
        NamedNodeMap nm = null;

        StringBuffer sb = new StringBuffer();
         sb.append("if (pattern != null && objectModel.get(\"request\") != null) {")
          .append("javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest) objectModel.get(\"request\");")
          .append("String userAgent = request.getHeader(\"User-Agent\");");
        while ((node = tw.nextNode()) != null) {
            if (node.getNodeName().equals("browser") &&
                node.getNodeType() == Node.ELEMENT_NODE) {
                nm = node.getAttributes();
                if (nm != null) {
                    nodeattrname = nm.getNamedItem("name");
                    nodeattruseragent = nm.getNamedItem("useragent");
                    if (nodeattrname != null && nodeattruseragent != null
                            && nodeattrname.getNodeValue().equals(test)) {
                        sb.append("if (userAgent.indexOf(\"")
                          .append(nodeattruseragent.getNodeValue())
                          .append("\") != -1) return true;");
                    }
                }
            }
        }
        return sb.append("} return false;").toString();
    }
}
