/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.reading;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.jsp.JSPEngine;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.http.HttpEnvironment;

/**
 * The <code>JSPReader</code> component is used to serve Servlet and JSP page 
 * output data in a sitemap pipeline.
 *
 * @author <a href="mailto:kpiroumian@flagship.ru">Konstantin Piroumian</a>
 * @version CVS $Id: JSPReader.java,v 1.10 2004/03/05 13:01:57 bdelacretaz Exp $
 */
public class JSPReader extends ServiceableReader implements Configurable {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    // buffer size for IO
    private int bufferSize;

    // output encoding
    private String outputEncoding;

    public void configure(Configuration conf) throws ConfigurationException {
        bufferSize = conf.getChild("buffer-size").getValueAsInteger(DEFAULT_BUFFER_SIZE);
        outputEncoding = conf.getChild("output-encoding").getValue(null);
    }

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
            String url = this.source;

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

            if (this.outputEncoding != null) {
                recodeResult (bytes, this.outputEncoding);
            } else {
                out.write(bytes);
                out.flush();
            }

            bytes = null;
        } catch (ServletException e) {
            throw new ProcessingException("ServletException while executing JSPEngine", e);
        } catch (IOException e) {
            throw new ProcessingException("IOException JSPReader.generate()", e);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException("Exception JSPReader.generate()", e);
        } finally {
            if (engine != null) {
                super.manager.release(engine);
            }
        }
    }

    private void recodeResult(byte[] bytes, String encoding) throws IOException {
        char[] buffer = new char[this.bufferSize];

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        // UTF-8 is the default encoding/contract of the JSPEngine
        Reader reader = new InputStreamReader(bais, "UTF-8");
        Writer writer = new OutputStreamWriter(out, encoding);

        int length = -1;
        while ((length = reader.read(buffer)) > -1) {
            writer.write(buffer, 0, length);
        }
        writer.flush();
    }
}
