/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.selection;


import org.apache.avalon.ConfigurationException;
import org.w3c.dom.traversal.NodeIterator;
import org.apache.cocoon.CodeFactory;


/**
 * This class generates source code which tests the enviromnent based on
 * embedded java code within the 'test' clause of the select.
 *
 * @author <a href="mailto:Marcus.Crafter@osa.de">Marcus Crafter</a>
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2001-02-14 11:39:50 $
 */
public class CodedSelectorFactory extends java.lang.Object
        implements CodeFactory {


    public String generateParameterSource( NodeIterator conf )
            throws ConfigurationException {
        return "org.apache.cocoon.selection.helpers.CodedSelectorHelper";
    }


    public String generateClassSource( String prefix, String test,
            NodeIterator conf ) throws ConfigurationException {
        StringBuffer sb = new StringBuffer();
        sb.append("static org.apache.cocoon.selection.helpers.CodedSelectorHelper " )
          .append( prefix )
          .append( "_expr = new org.apache.cocoon.selection.helpers.CodedSelectorHelper() {" )
          .append( "public boolean evaluate(Map objectModel) {" )
          .append( "initialize(objectModel);" )
          .append( "return (").append(test).append(");" )
          .append( "}" )
          .append( "};" );
        return sb.toString();
    }


    public String generateMethodSource( NodeIterator conf )
            throws ConfigurationException {
        StringBuffer sb = new StringBuffer();

        sb.append( "try {" )
          .append("return pattern.evaluate(objectModel);" )
          .append("} catch (Exception e) {" )
          .append("getLogger().error(\"CodedSelector Exception : \" + e.getMessage() + \", returning false\");" )
          .append( "return false;" )
          .append( "}" );

        return sb.toString();
    }
}
