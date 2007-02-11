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
package org.apache.cocoon.environment.portlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implements the {@link org.apache.cocoon.environment.Request} interface for the
 * JSR-168 (Portlet) environment.
 *
 * @version CVS $Id: ActionRequest.java,v 1.2 2003/12/03 13:20:29 vgritsenko Exp $
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vadim.gritsenko@dc.gov">Vadim Gritsenko</a>
 */
public final class ActionRequest extends PortletRequest {

    /**
     * Creates a ActionRequest based on a real ActionRequest object
     */
    protected ActionRequest(String servletPath,
                            String pathInfo,
                            javax.portlet.ActionRequest request,
                            PortletEnvironment environment) {
        super(servletPath, pathInfo, request, environment);
    }

    // Request API methods

    public String getCharacterEncoding() {
        if (super.getCharacterEncoding() == null) {
            return getActionRequest().getCharacterEncoding();
        } else {
            return super.getCharacterEncoding();
        }
    }

    /**
     * Action request can be always recognized by POST method
     */
    public String getMethod() {
        return "POST";
    }


    // ActionRequest API methods

    /**
     * Type cast portletRequest to ActionRequest
     *
     * @return type casted portletRequest
     */
    public javax.portlet.ActionRequest getActionRequest() {
        return (javax.portlet.ActionRequest) getPortletRequest();
    }

    public InputStream getPortletInputStream() throws IOException {
        return getActionRequest().getPortletInputStream();
    }

    public BufferedReader getReader() throws IOException {
        return getActionRequest().getReader();
    }
}
