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
 * @version CVS $Id: SessionPreTransformer.java,v 1.4 2004/03/19 14:16:54 cziegeler Exp $
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
                                                                     pars);
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
