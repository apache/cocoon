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
package org.apache.cocoon.portlet.multipart;

import org.apache.cocoon.servlet.multipart.MultipartException;
import org.apache.cocoon.servlet.multipart.MultipartParser;

import javax.portlet.ActionRequest;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * This is the interface of Request Wrapper in Cocoon for JSR-168 Portlet environment.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: RequestFactory.java,v 1.2 2003/12/03 13:20:30 vgritsenko Exp $
 */
public class RequestFactory {

    private boolean saveUploadedFilesToDisk;
    private File uploadDirectory;
    private boolean allowOverwrite;
    private boolean silentlyRename;
    private String defaultCharEncoding;
    private int maxUploadSize;

    public RequestFactory(boolean saveUploadedFilesToDisk,
                          File uploadDirectory,
                          boolean allowOverwrite,
                          boolean silentlyRename,
                          int maxUploadSize,
                          String defaultCharEncoding) {
        this.saveUploadedFilesToDisk = saveUploadedFilesToDisk;
        this.uploadDirectory = uploadDirectory;
        this.allowOverwrite = allowOverwrite;
        this.silentlyRename = silentlyRename;
        this.maxUploadSize = maxUploadSize;
        this.defaultCharEncoding = defaultCharEncoding;
    }

    /**
     * If the request includes a "multipart/form-data", then wrap it with
     * methods that allow easier connection to those objects since the servlet
     * API doesn't provide those methods directly.
     */
    public ActionRequest getServletRequest(ActionRequest request)
    throws IOException, MultipartException {
        ActionRequest req = request;
        String contentType = request.getContentType();

        if ((contentType != null) && (contentType.toLowerCase().indexOf("multipart/form-data") > -1)) {
            if (request.getContentLength() > maxUploadSize) {
                throw new IOException("Content length exceeds maximum upload size.");
            }

            String charEncoding = request.getCharacterEncoding();
            if (charEncoding == null || charEncoding.equals("")) {
                charEncoding = this.defaultCharEncoding;
            }

            MultipartParser parser =
                    new MultipartParser(
                            this.saveUploadedFilesToDisk,
                            this.uploadDirectory,
                            this.allowOverwrite,
                            this.silentlyRename,
                            this.maxUploadSize,
                            charEncoding);

            Hashtable parts = parser.getParts(request.getContentLength(),
                                              request.getContentType(),
                                              request.getPortletInputStream());

            req = new MultipartActionRequest(request, parts);
        }

        return req;
    }
}
