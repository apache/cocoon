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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.jsp.JSPEngine;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Allows JSP to be used as a generator.  Builds upon the JSP servlet
 * functionality - overrides the output method in order to pipe the
 * results into SAX events.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: JspGenerator.java,v 1.11 2003/12/18 21:09:47 vgritsenko Exp $
 */
public class JspGenerator extends ServletGenerator {

    /**
     * Generate XML data from JSP.
     */
    public void generate() throws ProcessingException {

        // ensure that we are running in a servlet environment
        HttpServletResponse httpResponse =
            (HttpServletResponse)this.objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        HttpServletRequest httpRequest =
            (HttpServletRequest)this.objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        ServletContext httpContext =
            (ServletContext)this.objectModel.get(HttpEnvironment.HTTP_SERVLET_CONTEXT);

        if (httpResponse == null || httpRequest == null || httpContext == null) {
            throw new ProcessingException("HttpServletRequest or HttpServletResponse or ServletContext object not available");
        }

        JSPEngine engine = null;
        SAXParser parser = null;
        try {
            // TODO (KP): Should we exclude not supported protocols, say 'context'?
            String url = this.source;
            // absolute path is processed as is
            if (!url.startsWith("/")) {
                // get current request path
                String servletPath = httpRequest.getServletPath();
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

            engine = (JSPEngine)this.manager.lookup(JSPEngine.ROLE);

            getLogger().debug("JspGenerator executing JSP:" + url);
            byte[] bytes = engine.executeJSP(url, httpRequest, httpResponse, httpContext);

            InputSource input = new InputSource(new ByteArrayInputStream(bytes));
            // utf-8 is default encoding; specified explicitely here as a reminder.
            input.setEncoding("utf-8");

            // pipe the results into the parser
            parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
            parser.parse(input, this.xmlConsumer);
        } catch (ServletException e) {
            throw new ProcessingException("ServletException in JspGenerator.generate()", e.getRootCause());
        } catch (SAXException e) {
            // FIXME: e.getException can be null
            throw new ProcessingException("SAXException JspGenerator.generate()", e.getException());
        } catch (IOException e) {
            throw new ProcessingException("IOException JspGenerator.generate()", e);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException("Exception JspGenerator.generate()", e);
        } finally {
            this.manager.release((Component)parser);
            this.manager.release(engine);
        }
    }
}
