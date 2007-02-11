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
package org.apache.cocoon.webapps.session.transformation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.webapps.session.SessionConstants;
import org.apache.excalibur.source.SourceParameters;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This is the session pre transformer. It does all the getting
 * and creation commands. This transformer should be the first in the
 * pipeline.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionPreTransformer.java,v 1.2 2003/05/04 20:19:39 cziegeler Exp $
*/
public class SessionPreTransformer
extends AbstractSessionTransformer {

    /*
     * The XML commands
     */
    public static final String CREATECONTEXT_ELEMENT = "createcontext";
    public static final String CREATECONTEXT_NAME_ATTRIBUTE = "name";
    public static final String CREATECONTEXT_SAVE_ATTRIBUTE = "save"; // optional
    public static final String CREATECONTEXT_LOAD_ATTRIBUTE = "load"; // optional

    public static final String GETXML_ELEMENT = "getxml";
    public static final String GETXML_CONTEXT_ATTRIBUTE = "context";
    public static final String GETXML_PATH_ATTRIBUTE = "path";

    public static final String LOADCONTEXT_ELEMENT = "loadxml";
    public static final String LOADCONTEXT_CONTEXT_ATTRIBUTE = "context";
    public static final String LOADCONTEXT_PATH_ATTRIBUTE = "path"; // optional

    /** The contexturl element.
     */
    public static final String CONTEXT_URL_ELEMENT = "contexturl";

    /** Are we inside a getxml? */
    protected int processingGetXML;

    public SessionPreTransformer() {
        this.namespaceURI = SessionConstants.SESSION_NAMESPACE_URI;
    }

    /**
     * Setup the next round.
     * The instance variables are initialised.
     * @param resolver The current SourceResolver
     * @param objectModel The objectModel of the environment.
     * @param src The value of the src attribute in the sitemap.
     * @param par The parameters from the sitemap.
     */
    public void setup(SourceResolver resolver,
                      Map            objectModel,
                      String         src,
                      Parameters     par)
    throws ProcessingException,
           SAXException,
           IOException {
        super.setup(resolver, objectModel, src, par);
        this.processingGetXML = 0;
    }

    /**
     * Process the SAX event.
     * The namespace of the event is checked. If it is the defined namespace
     * for this transformer the endTransformingElement() hook is called.
     */
    public void endElement(String uri, String name, String raw) throws SAXException {
        super.endElement(uri, name, raw);
        if (uri != null
            && namespaceURI != null
            && uri.equals(namespaceURI) == true
            && this.processingGetXML > 0
            && name.equals(GETXML_ELEMENT) == true) {
            this.processingGetXML--;
            this.ignoreEventsCount--;
            this.ignoreHooksCount--;
        }
    }

    /**
     * Process the SAX event.
     * The namespace of the event is checked. If it is the defined namespace
     * for this transformer the endTransformingElement() hook is called.
     */
    public void startElement(String uri,
                             String name,
                             String raw,
                             Attributes attr)
    throws SAXException {
        if (uri != null
            && namespaceURI != null
            && uri.equals(namespaceURI) == true
            && this.processingGetXML > 0
            && name.equals(GETXML_ELEMENT) == true) {
            this.processingGetXML++;
            this.ignoreEventsCount++;
            this.ignoreHooksCount++;
        }
        super.startElement(uri, name, raw, attr);
    }

    /**
     * This is the real implementation of the startElement event for the transformer
     * The event is checked for a valid element and the corresponding command
     * is executed.
     */
    public void startTransformingElement(String uri,
                                       String name,
                                       String raw,
                                       Attributes attr)
    throws ProcessingException, IOException, SAXException {

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN startTransformingElement uri=" + uri +
                              ", name=" + name +
                              ", raw=" + raw +
                              ", attr=" + attr);
        }
        if (name.equals(CREATECONTEXT_ELEMENT) == true) {
            this.getContextManager().createContext(attr.getValue(CREATECONTEXT_NAME_ATTRIBUTE),
                                                      attr.getValue(CREATECONTEXT_LOAD_ATTRIBUTE),
                                                      attr.getValue(CREATECONTEXT_SAVE_ATTRIBUTE));

        } else if (name.equals(GETXML_ELEMENT) == true) {
            final String path        = attr.getValue(GETXML_PATH_ATTRIBUTE);
            final String contextName = attr.getValue(GETXML_CONTEXT_ATTRIBUTE);

            if (this.getSessionManager().streamContextFragment(contextName,
                                                     path,
                                                     this) == true) {
                this.ignoreEventsCount++;
                this.ignoreHooksCount++;
                this.processingGetXML++;
            }

        } else if (name.equals(LOADCONTEXT_ELEMENT) == true) {
            this.startParametersRecording();
            stack.push(attr.getValue(LOADCONTEXT_CONTEXT_ATTRIBUTE));
            if (attr.getValue(LOADCONTEXT_PATH_ATTRIBUTE) != null) {
                stack.push(attr.getValue(LOADCONTEXT_PATH_ATTRIBUTE));
            } else {
                stack.push("/");
            }

        // Element context url
        } else if (name.equals(SessionPreTransformer.CONTEXT_URL_ELEMENT) == true) {
            this.ignoreEventsCount++;

        // DEFAULT
        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }

        if (this.getLogger().isDebugEnabled() == true) {
           this.getLogger().debug("END startTransformingElement");
        }
    }

    public void endTransformingElement(String uri,
                                       String name,
                                       String raw)
    throws ProcessingException ,IOException, SAXException {

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN endTransformingElement uri=" + uri + ", name=" + name + ", raw=" + raw);
        }
        if (name.equals(CREATECONTEXT_ELEMENT) == true) {
            // do nothing, the context was created on the startElement event

        } else if (name.equals(GETXML_ELEMENT) == true) {
            // do nothing

        // Element: loadxml
        } else if (name.equals(LOADCONTEXT_ELEMENT) == true) {
            String path       = (String)stack.pop();
            String contextName = (String)stack.pop();
            SourceParameters pars = this.endParametersRecording((SourceParameters)null);
            pars.setSingleParameterValue("contextname", contextName);
            pars.setSingleParameterValue("path", path);

            this.getContextManager().getContext(contextName).loadXML(path,
                                                                        pars,
                                                                        this.objectModel,
                                                                        this.resolver,
                                                                        this.manager);
        // Element context url
        } else if (name.equals(SessionPreTransformer.CONTEXT_URL_ELEMENT) == true) {
            this.ignoreEventsCount--;
            String contextUrl = this.request.getScheme() + "://" +
                                this.request.getServerName() + ":" +
                                this.request.getServerPort() +
                                this.request.getContextPath();
            this.sendTextEvent(contextUrl);


        // DEFAULT
        } else {
            super.endTransformingElement(uri, name, raw);
        }
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END endTransformingElement");
        }
    }
}
