/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.selection;

import org.apache.avalon.configuration.ConfigurationException;
import org.apache.cocoon.CodeFactory;
import org.apache.xerces.dom.TreeWalkerImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

/**
 * This class generates source code which tests a specific browser pattern
 * agains the requesting user-agent
 *
 * @author <a href="mailto:cziegeler@sundn.de">Carsten Ziegeler</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.18 $ $Date: 2001-04-25 17:08:18 $
*/


public class BrowserSelectorFactory implements CodeFactory {

    public String generateParameterSource (NodeList conf)
    throws ConfigurationException {
        return "String []";
    }

    public String generateClassSource (String prefix, String test, NodeList conf)
    throws ConfigurationException {
        Node node = null;
        Node nodeattrname  = null;
        Node nodeattruseragent = null;
        NamedNodeMap nm = null;
        int cnt = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("static String [] ")
          .append(prefix)
          .append("_expr = {");
        int count = conf.getLength();
        for(int k = 0; k < count;k++) {
            node = conf.item(k);
            if (node.getNodeName().equals("browser") &&
                node.getNodeType() == Node.ELEMENT_NODE) {
                nm = node.getAttributes();
                if (nm != null) {
                    nodeattrname = nm.getNamedItem("name");
                    nodeattruseragent = nm.getNamedItem("useragent");
                    if (nodeattrname != null && nodeattruseragent != null
                            && nodeattrname.getNodeValue().equals(test)) {
                        sb.append(cnt++==0 ? "\"" : ",\"")
                          .append(nodeattruseragent.getNodeValue())
                          .append("\"");
                    }
                }
            }
        }
        return sb.append("};").toString();
    }

    public String generateMethodSource (NodeList conf)
    throws ConfigurationException {
        StringBuffer sb = new StringBuffer();
         sb.append("if (pattern != null && objectModel.get(Constants.REQUEST_OBJECT) != null) {")
          .append("String userAgent = XSPRequestHelper.getHeader(objectModel,\"User-Agent\");")
          .append("XSPResponseHelper.addHeader(objectModel, \"Vary\", \"User-Agent\");")
          .append("for (int i = 0; i < pattern.length; i++) {")
          .append("if (userAgent.indexOf(pattern[i]) != -1) return true;}");
        return sb.append("} return false;").toString();
    }
}
