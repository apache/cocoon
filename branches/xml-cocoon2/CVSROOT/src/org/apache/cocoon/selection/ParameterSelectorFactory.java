/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.selection;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.CodeFactory;
import org.apache.xerces.dom.TreeWalkerImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

/**
 * This class generates source code to implement a selector that
 * matches a string in the parameters object passed to it.
 *
 *  <map:selector name="parameter" factory="org.apache.cocoon.selection.ParameterSelectorFactory"/>
 *
 *   <map:select type="parameter">
 *      <parameter name="parameter-selector-test" value="{$mySitemapParameter}"/>
 *      <map:when test="myParameterValue">
 *         <!-- executes iff {$mySitemapParameter} == "myParameterValue" -->
 *         <map:transform src="stylesheets/page/uk.xsl"/>
 *      </map:when>
 *      <map:otherwise>
 *         <map:transform src="stylesheets/page/us.xsl"/>
 *      </map:otherwise>
 *   </map:select>
 *
 * The purpose of this selector is to allow an action to set parameters
 * and to be able to select between different pipeline configurations
 * depending on those parameters.
 *
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-05-09 16:42:22 $
 */
public class ParameterSelectorFactory implements CodeFactory {

    public String generateParameterSource (NodeList conf)
    throws ConfigurationException {
        return "String ";
    }

    public String generateClassSource (String prefix, String test, NodeList conf)
    throws ConfigurationException {
		StringBuffer sb = new StringBuffer ();
        sb.append("static String ")
            .append(prefix)
            .append("_expr = \"")
			.append (test)
			.append ("\";");
        return sb.toString();
    }

    public String generateMethodSource (NodeList conf)
    throws ConfigurationException {
        StringBuffer sb = new StringBuffer();
         sb.append("if (param != null) {")
          .append("String compareToString = param.getParameter (\"parameter-selector-test\", null);")
          .append("return compareToString != null && compareToString.equals (pattern);")
          .append("} return false;");
        return sb.toString();
    }
}
