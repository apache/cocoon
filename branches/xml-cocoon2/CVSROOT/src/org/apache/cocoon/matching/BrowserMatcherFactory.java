/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.matching;

import java.util.Stack;

import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import org.apache.avalon.ConfigurationException;

import org.apache.xerces.dom.TreeWalkerImpl;

/**
 * This class generates source code which matches a specific browser pattern
 * for request URIs
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2001-01-31 15:48:37 $
 */

public class BrowserMatcherFactory implements org.apache.cocoon.CodeFactory {

    public String generateMethodSource (NodeIterator conf)
    throws ConfigurationException {
        StringBuffer sb = new StringBuffer();
        Node node = null;
        Node nodea = null;
        NamedNodeMap nm = null;

        sb.append ("/*\n");
        while ((node = conf.nextNode()) != null) {
            sb.append("name=")
              .append(node.getNodeName())
              .append(" type=")
              .append(node.getNodeType())
              .append(" value=").append(node.getNodeValue()).append("\n");
            nm = node.getAttributes();
            if (nm != null) {
                int i = nm.getLength();
                for (int j = 0; j < i; j++) {
                    nodea = nm.item(j);
                    sb.append("name=").append(nodea.getNodeName())
                      .append(" type=").append(nodea.getNodeType())
                      .append(" value=").append(nodea.getNodeValue()).append("\n");
                }
            }
        }
        return sb.append("*/\nreturn null;").toString();
    }

    public String generateClassSource (String prefix, String pattern,
                                       NodeIterator conf)
    throws ConfigurationException {
        return "\n// Dummy values\nstatic String " + prefix + "_expr = \"" + pattern + "\";\n";
    }

    public String generateParameterSource (NodeIterator conf)
    throws ConfigurationException {
        return "String";
    }
}
