/*-- $Id: ProducerFromFile.java,v 1.2 1999-12-16 11:45:07 stefano Exp $ -- 

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
package org.apache.cocoon.producer;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.cocoon.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements the producer interface in order to produce a document
 * based on its tranlated path. This should work on most of the servlet engine 
 * available, even if we should use getResource().
 * 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.2 $ $Date: 1999-12-16 11:45:07 $
 */

public class ProducerFromFile extends AbstractProducer implements Status {
    
    private Monitor monitor = new Monitor(10);
    
    public Reader getStream(HttpServletRequest request) throws IOException {
        File file = new File(this.getBasename(request));
        this.monitor.watch(Utils.encode(request), file);
        return new InputStreamReader(new FileInputStream(file));
    }

    public String getPath(HttpServletRequest request) {
        String basename = this.getBasename(request);
        return basename.substring(0, basename.lastIndexOf('/') + 1);
    }
    
    public boolean hasChanged(Object context) {
        return this.monitor.hasChanged(Utils.encode((HttpServletRequest) context));
    }

    /**
     * XXX: This is a dirty hack. The worst piece of code I ever wrote
     * and it clearly shows how Cocoon must change to support the Servlet API
     * 2.2 which has _much_ better mapping support thru the use of "getResource()"
     * but then, all the file system abstraction should be URL based.
     *
     * So, for now, leave the dirty code even if totally deprecated and work
     * out a better solution in the future.
     */
    protected String getBasename(HttpServletRequest request) {
        try {
            // detect if the engine supports at least Servlet API 2.2
            request.getContextPath();
            URL resource = ((ServletContext) context).getResource(request.getServletPath());
            if (resource.getProtocol().equals("file")) {
                return resource.getFile();
            } else {
                throw new RuntimeException("Cannot handle remote resources.");
            }
        } catch (NoSuchMethodError e) {
            // if there is no such method we must be in Servlet API 2.1
            if (request.getPathInfo() != null) {
                // this must be Apache JServ
                return request.getPathTranslated().replace('\\','/');
            } else {
                // otherwise use the deprecated method on all other servlet engines.
                return request.getRealPath(request.getRequestURI());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed request URL.");
        } catch (NullPointerException e) {
            throw new RuntimeException("Context cannot be null.");
        }
    }
    
    public String getStatus() {
        return "Producer from local file";
    }
}