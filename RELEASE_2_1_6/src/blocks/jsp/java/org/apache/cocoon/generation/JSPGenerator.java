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
package org.apache.cocoon.generation;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.jsp.JSPEngine;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Allows Servlets and JSPs to be used as a generator.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: JSPGenerator.java,v 1.6 2004/05/05 21:39:28 joerg Exp $
 */
public class JSPGenerator extends ServiceableGenerator {

    /**
     * Generate XML data from JSPEngine output.
     */
    public void generate() throws ProcessingException {

        final HttpServletResponse servletResponse =
            (HttpServletResponse) this.objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        final HttpServletRequest servletRequest =
            (HttpServletRequest) this.objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        final ServletContext servletContext =
            (ServletContext) this.objectModel.get(HttpEnvironment.HTTP_SERVLET_CONTEXT);

        // ensure that we are running in a servlet environment
        if (servletResponse == null || servletRequest == null || servletContext == null) {
            throw new ProcessingException("JSPGenerator can only be used from within a Servlet environment.");
        }

        JSPEngine engine = null;
        SAXParser parser = null;
        Source inputSource = null;
        Source contextSource = null;
        try {
            inputSource = this.resolver.resolveURI(this.source);
            contextSource = this.resolver.resolveURI("context:/");

            String inputSourceURI = inputSource.getURI();
            String contextSourceURI = contextSource.getURI();

            if (!inputSourceURI.startsWith(contextSourceURI)) {
                throw new ProcessingException("You must not reference a file "
                        + "outside of the servlet context at " + contextSourceURI + ".");
            }

            String url = inputSourceURI.substring(contextSourceURI.length());
            if (url.charAt(0) != '/') {
                url = "/" + url;
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("JSPGenerator executing:" + url);
            }

            engine = (JSPEngine) super.manager.lookup(JSPEngine.ROLE);
            byte[] bytes = engine.executeJSP(url, servletRequest, servletResponse, servletContext);

            InputSource input = new InputSource(new ByteArrayInputStream(bytes));
            // utf-8 is default encoding; specified explicitely here as a reminder.
            input.setEncoding("utf-8");

            // pipe the results into the parser
            parser = (SAXParser) super.manager.lookup(SAXParser.ROLE);
            parser.parse(input, super.xmlConsumer);
        } catch (ServletException e) {
            throw new ProcessingException("ServletException while executing JSPEngine", e);
        } catch (SAXException e) {
            throw new ProcessingException("SAXException while parsing JSPEngine output", e);
        } catch (IOException e) {
            throw new ProcessingException("IOException JSPGenerator.generate()", e);
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException("Exception JSPGenerator.generate()", e);
        } finally {
            super.manager.release(parser);
            super.manager.release(engine);
            this.resolver.release(inputSource);
            this.resolver.release(contextSource);
        }
    }
}
