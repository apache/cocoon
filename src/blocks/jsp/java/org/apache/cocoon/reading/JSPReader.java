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
package org.apache.cocoon.reading;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.jsp.JSPEngine;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.http.HttpEnvironment;

/**
 * The <code>JSPReader</code> component is used to serve Servlet and JSP page 
 * output data in a sitemap pipeline.
 *
 * @author <a href="mailto:kpiroumian@flagship.ru">Konstantin Piroumian</a>
 * @version CVS $Id: JSPReader.java,v 1.7 2004/01/16 13:53:13 unico Exp $
 */
public class JSPReader extends ServiceableReader {

    /**
     * Generates the output from JSPEngine.
     */
    public void generate() throws IOException, ProcessingException {
        
        if (this.source == null) {
            throw new ProcessingException("JSPReader: source JSP is not specified");
        }

        HttpServletResponse servletResponse =
            (HttpServletResponse) super.objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        HttpServletRequest servletRequest =
            (HttpServletRequest) super.objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        ServletContext servletContext =
            (ServletContext) super.objectModel.get(HttpEnvironment.HTTP_SERVLET_CONTEXT);
        
        // ensure that we are running in a servlet environment
        if (servletResponse == null || servletRequest == null || servletContext == null) {
            throw new ProcessingException("JSPReader can only be used from within a Servlet environment.");
        }

        JSPEngine engine = null;
        try {
            // TODO (KP): Should we exclude not supported protocols, say 'context'?
            String url = super.source;

            // absolute path is processed as is
            if (!url.startsWith("/")) {
                // get current request path
                String servletPath = servletRequest.getServletPath();
                // remove sitemap URI part
                String sitemapURI = ObjectModelHelper.getRequest(objectModel).getSitemapURI();
                if (sitemapURI != null) {
                    servletPath = servletPath.substring(0, servletPath.indexOf(sitemapURI));
                } else {
                    // for example when using cocoon:/ pseudo protocol
                    servletPath = servletPath.substring(0, servletPath.lastIndexOf("/") + 1);
                }
                url = servletPath + url;
            }

            engine = (JSPEngine) super.manager.lookup(JSPEngine.ROLE);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("JSPReader executing:" + url);
            }

            byte[] bytes = engine.executeJSP(url, servletRequest, servletResponse, servletContext);
            
            // TODO (KP): Make buffer size configurable
            byte[] buffer = new byte[8192];
            int length = -1;

            // TODO: Recode from UTF8 to desired output encoding.
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            while ((length = bais.read(buffer)) > -1) {
                out.write(buffer, 0, length);
                System.out.write(buffer,0,length);
            }
            bais.close();
            bais = null;
            out.flush();
        } catch (ServletException e) {
            throw new ProcessingException("ServletException while executing JSPEngine", e.getRootCause());
        } catch (IOException e) {
            throw new ProcessingException("IOException JSPReader.generate()", e);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException("Exception JSPReader.generate()", e);
        } finally {
            if (engine != null) super.manager.release(engine);
        }
    }
}
