/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
package org.apache.cocoon.matching; 

import java.util.Stack;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.traversal.TreeWalker;
import org.w3c.dom.traversal.NodeFilter;

import org.apache.avalon.ConfigurationException;

import org.apache.xerces.dom.TreeWalkerImpl;
 
/** 
 * This class generates source code which matches a specific browser pattern
 * for request URIs
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2000-07-27 21:49:02 $ 
 */ 

public class BrowserMatcherFactory implements MatcherFactory {
    public String generateMethodSource (String prefix, String test_expression, 
                                        DocumentFragment conf)
    throws ConfigurationException {
        StringBuffer sb = new StringBuffer();
        TreeWalker tw = new TreeWalkerImpl (conf, NodeFilter.SHOW_ALL, null, false);
        Node node = null;
        Node nodea = null;
        NamedNodeMap nm = null;

        sb.append ("/*\n");
        while ((node = tw.nextNode()) != null) {
            sb.append("name=")
              .append(node.getNodeName())
              .append(" type=")
              .append(node.getNodeType())
              .append(" value="+node.getNodeValue()+"\n");
            nm = node.getAttributes();
            if (nm != null) {
                int i = nm.getLength();
                for (int j = 0; j < i; j++) {
                    nodea = nm.item(j);
                    sb.append("name="+nodea.getNodeName())
                      .append(" type="+nodea.getNodeType())
                      .append(" value="+nodea.getNodeValue()+"\n");
                }
            }
        }
        return sb.append("*/\nreturn null;").toString();
    }

    public String generateClassSource (String prefix, String pattern, 
                                       DocumentFragment conf)
    throws ConfigurationException {
        return "";
    }
}
