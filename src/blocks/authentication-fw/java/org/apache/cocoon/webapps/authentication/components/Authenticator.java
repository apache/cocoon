/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.webapps.authentication.components;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Verify if the a user could be authenticated
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: Authenticator.java,v 1.1 2003/04/27 09:42:42 cziegeler Exp $
*/
public class Authenticator 
    extends AbstractLogEnabled {
        
    /**
     * Check the fragment if it is valid
     */
    private boolean isValidAuthenticationFragment(Document authenticationFragment) 
    throws ProcessingException {
        // calling method is synced
        if (this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("BEGIN isValidAuthenticationFragment fragment=" + XMLUtils.serializeNodeToXML(authenticationFragment));
        }
        boolean isValid = false;

        // authenticationFragment must only have exactly one child with
        // the name authentication
        if (authenticationFragment.hasChildNodes() == true
            && authenticationFragment.getChildNodes().getLength() == 1) {
            Node child = authenticationFragment.getFirstChild();

            if (child.getNodeType() == Node.ELEMENT_NODE
                && child.getNodeName().equals("authentication") == true) {

                // now authentication must have one child ID
                if (child.hasChildNodes() == true) {
                    NodeList children = child.getChildNodes();
                    boolean  found = false;
                    int      i = 0;
                    int      l = children.getLength();

                    while (found == false && i < l) {
                        child = children.item(i);
                        if (child.getNodeType() == Node.ELEMENT_NODE
                            && child.getNodeName().equals("ID") == true) {
                            found = true;
                        } else {
                            i++;
                        }
                    }

                    // now the last check: ID must have a TEXT child
                    if (found == true) {
                        child.normalize(); // join text nodes
                        if (child.hasChildNodes() == true &&
                            child.getChildNodes().getLength() == 1 &&
                            child.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                            String value = child.getChildNodes().item(0).getNodeValue().trim();
                            if (value.length() > 0) isValid = true;
                        }
                    }
                }

            }
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END isValidAuthenticationFragment valid="+isValid);
        }
        return isValid;
    }

}
