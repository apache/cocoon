/*-- $Id: SQLQueryCreator.java,v 1.3 1999-11-09 02:30:48 dirkx Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.cocoon.processor.sql;

import java.sql.*;
import java.text.*;
import java.util.*;
import javax.servlet.http.*;
import org.w3c.dom.*;

/**
 * A class that can create a SQL query. It's given a query to start with,
 * plus a query_props table that contains parameters from the XML file, and the
 * parameters table from cocoon that notably may contain a HttpServletRequest
 * object keyed from "request".
 *
 * @author <a href="mailto:balld@webslingerZ.com">Donald Ball</a>
 * @version $Revision: 1.3 $ $Date: 1999-11-09 02:30:48 $
 */

public class SQLQueryCreator {

    public String getQuery(Connection conn, String query, Element query_element, Properties query_props, Dictionary parameters) throws Exception {
        HttpServletRequest req = (HttpServletRequest)parameters.get("request");
        String ldelim = query_props.getProperty("variable-left-delimiter");
        int llength = ldelim.length();
        String rdelim = query_props.getProperty("variable-right-delimiter");
        int rlength = rdelim.length();
        int offset = 0;
        while (true) {
            int lindex = query.indexOf(ldelim,offset);
            if (lindex < 0) break;
            int rindex = query.indexOf(rdelim,offset+llength);
            if (rindex < 0 || rindex < lindex) break;
            String name = query.substring(lindex+llength,rindex);
            String value = req.getParameter(name);
            if (value == null) break;
            query = query.substring(0,lindex)+value+query.substring(rindex+rlength);
            offset = lindex+value.length();
        }
        return query;
    }

}
