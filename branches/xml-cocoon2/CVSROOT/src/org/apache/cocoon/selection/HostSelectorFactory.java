/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.selection;

import org.apache.avalon.configuration.ConfigurationException;

import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.TreeWalker;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import org.apache.xerces.dom.TreeWalkerImpl;

import org.apache.cocoon.CodeFactory;

/**
 * This class generates source code to implement a selector that 
 * matches a string from within the host parameter of the HTTP request
 *
 *  <map:selector name="host" factory="org.apache.cocoon.selection.HostSelectorFactory">
 *       <host name="uk-site" value="www.foo.co.uk"/>
 *  </map:selector>
 *
 *
 *   <map:select type="host">
 *      <map:when test="uk-site">
 *    	<map:transform src="stylesheets/page/uk.xsl"/>
 *      </map:when>
 *      <map:otherwise>
 *     <map:transform src="stylesheets/page/us.xsl"/>
 *       </map:otherwise>
 *   </map:select>
 *
 * @author 
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-03-23 09:05:48 $
*/


public class HostSelectorFactory implements CodeFactory {

    public String generateParameterSource (NodeList conf)
    throws ConfigurationException {
        return "String []";
    }

    public String generateClassSource (String prefix, String test, NodeList conf)
    throws ConfigurationException {
        Node node = null;
        Node nodeattrname  = null;
        Node nodeattrhost = null;
        NamedNodeMap nm = null;
        int cnt = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("static String [] ")
          .append(prefix)
          .append("_expr = {");
        int count = conf.getLength();
        for(int k = 0; k < count;k++) {
            node = conf.item(k);
            if (node.getNodeName().equals("host") &&
                node.getNodeType() == Node.ELEMENT_NODE) {
                nm = node.getAttributes();
                if (nm != null) {
                    nodeattrname = nm.getNamedItem("name");
                    nodeattrhost = nm.getNamedItem("value");
                    if (nodeattrname != null && nodeattrhost != null
                            && nodeattrname.getNodeValue().equals(test)) {
                        sb.append(cnt++==0 ? "\"" : ",\"")
                          .append(nodeattrhost.getNodeValue())
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
          .append("javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest) objectModel.get(Constants.REQUEST_OBJECT);")
          .append("String hostServer = request.getHeader(\"host\");")
          .append("javax.servlet.http.HttpServletResponse response = (javax.servlet.http.HttpServletResponse) objectModel.get(Constants.RESPONSE_OBJECT);")
          .append("response.addHeader(\"Vary\", \"host\");")
          .append("for (int i = 0; i < pattern.length; i++) {")
          .append("if (hostServer.indexOf(pattern[i]) != -1) return true;}");
        return sb.append("} return false;").toString();
    }
}
