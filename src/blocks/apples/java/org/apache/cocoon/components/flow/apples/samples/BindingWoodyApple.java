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

*/
package org.apache.cocoon.components.flow.apples.samples;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.apples.AppleController;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.FormManager;
import org.apache.cocoon.woody.binding.Binding;
import org.apache.cocoon.woody.binding.BindingManager;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.dom.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * BindingWoodyApple
 */
public class BindingWoodyApple extends AbstractLogEnabled implements AppleController, Serviceable {

    private static final boolean CONTINUE = false;
    private static final boolean FINISHED = true;

    private Form form;
    private Binding binding;
    private Document document;
    private ServiceManager serviceManager;
    private String formPipeURI;
    private String validPipeURI;
    private String backendURI;
    private Map wrapperContextMap;
    private State state;

    private interface State {
        public void processRequest(AppleRequest req, AppleResponse res) throws ProcessingException;
    }

    private final State initializationDelegate = new State() {
        public void processRequest(AppleRequest req, AppleResponse res) throws ProcessingException {
            BindingWoodyApple.this.processInitialization(req, res);
        }
    };

    private final State validationDelegate = new State() {
        public void processRequest(AppleRequest req, AppleResponse res) throws ProcessingException {
            BindingWoodyApple.this.processValidation(req, res);
        }
    };

    {
        state = initializationDelegate;
    }


    public void process(AppleRequest req, AppleResponse res) throws ProcessingException {
        this.state.processRequest(req, res);
    }

    protected void processInitialization(AppleRequest req, AppleResponse res) throws ProcessingException {

        String formURI = req.getSitemapParameter("form-src");
        String bindURI = req.getSitemapParameter("binding-src");
        this.backendURI = req.getSitemapParameter("documentURI");
        this.formPipeURI = req.getSitemapParameter("form-pipe");
        this.validPipeURI = req.getSitemapParameter("valid-pipe");

        FormManager formManager = null;
        BindingManager binderManager = null;
        SourceResolver resolver = null;
        Source formSource = null;
        Source bindSource = null;
        Source documentSource = null;

        try {
            formManager = (FormManager) this.serviceManager.lookup(FormManager.ROLE);
            binderManager = (BindingManager) this.serviceManager.lookup(BindingManager.ROLE);
            resolver = (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);

            formSource = resolver.resolveURI(formURI);
            this.form = formManager.createForm(formSource);

            bindSource = resolver.resolveURI(bindURI);
            this.binding = binderManager.createBinding(bindSource);

            documentSource = resolver.resolveURI(this.backendURI);
            this.document = loadDocumentFromSource(documentSource); 
            this.binding.loadFormFromModel(this.form, this.document);

            this.getLogger().debug("apple initialisation finished .. ");
            this.state = validationDelegate;

            completeResult(res, this.formPipeURI, CONTINUE);
        } catch (Exception e) {
            throw new ProcessingException("Failed to initialize BindingWoodyApple. ", e);
        } finally {
            if (formManager != null) {
                this.serviceManager.release(formManager);
            }
            if (binderManager != null) {
                this.serviceManager.release(binderManager);
            }
            if (resolver != null) {
                if (formSource != null) {
                    resolver.release(formSource);
                }
                if (bindSource != null) {
                    resolver.release(bindSource);
                }
                if (documentSource != null) {
                    resolver.release(documentSource);
                }
                this.serviceManager.release(resolver);
            }
        }
    }

    protected void processValidation(AppleRequest req, AppleResponse res) throws ProcessingException {

        Source documentTarget = null;
        SourceResolver resolver = null;

        try {
            FormContext formContext = new FormContext(req.getCocoonRequest(), Locale.US);

            if (!this.form.process(formContext)) {
                // form is not valid or there was just an event handled
                completeResult(res, this.formPipeURI, CONTINUE);
            } else {

                resolver = (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
                documentTarget = resolver.resolveURI(makeTargetURI(this.backendURI));

                this.binding.saveFormToModel(this.form, this.document);                
                saveDocumentToSource(documentTarget, this.document);

                completeResult(res, this.validPipeURI, FINISHED);
            }

            getLogger().debug("apple processing done .. ");
        } catch (Exception e) {
            throw new ProcessingException("Error processing BindingWoodyApple", e);
        } finally {
            if (resolver != null) {
                if (documentTarget != null) {
                    resolver.release(documentTarget);
                }
                this.serviceManager.release(resolver);
            }
        }
    }

    private Object makeInstance(String fqcn)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        Class clazz = Class.forName(fqcn);
        return clazz.newInstance();
    }

    private void completeResult(AppleResponse res, String uri, boolean finished) {
        res.sendPage(uri, getContextMap());
        // TODO think about transferring the fact that the use case has ended.
    }

    private Map getContextMap() {
        if (this.wrapperContextMap == null) {
            if (this.form != null) {
                this.wrapperContextMap = new HashMap();
                this.wrapperContextMap.put("woody-form", this.form);
            }
        }
        return this.wrapperContextMap;
    }


    /**
     * Translate source path into target path so we keep a clean source XML
     * 
     * @param path
     * @return
     */
    private String makeTargetURI(String path) {
        final String sfx = ".xml";
        final String newSfx = "-result.xml";
        if (path.endsWith(sfx)) {
            path = path.substring(0, path.length() - sfx.length());
        }
        return path + newSfx;
    }


    /**
     * Saves (and serializes) the given Document to the path indicated by the 
     * specified Source.
     * 
     * @param docTarget must be the ModifieableSource where the doc will be
     *   serialized to.
     * @param doc org.w3c.dom.Document to save
     * @throws ProcessingException
     */
    private void saveDocumentToSource(Source docTarget, Document doc)
        throws ProcessingException {
        DOMParser parser = null;
        OutputStream os = null;
        String uri = docTarget.getURI();

        try {
            parser =
                (DOMParser) this.serviceManager.lookup(DOMParser.ROLE);
            getLogger().debug("request to save file " + uri);
            TransformerFactory tf = TransformerFactory.newInstance();

            if (docTarget instanceof ModifiableSource
                && tf.getFeature(SAXTransformerFactory.FEATURE)) {

                ModifiableSource ws = (ModifiableSource) docTarget;
                os = ws.getOutputStream();
                SAXTransformerFactory stf = (SAXTransformerFactory) tf;
                TransformerHandler th = stf.newTransformerHandler();
                Transformer t = th.getTransformer();
                t.setOutputProperty(OutputKeys.INDENT, "true");
                t.setOutputProperty(OutputKeys.METHOD, "xml");
                th.setResult(new StreamResult(os));

                DOMStreamer streamer = new DOMStreamer(th);
                streamer.stream(doc);
            } else {
                getLogger().error("Cannot not write to source " + uri);
            }
        } catch (Exception e) {
            getLogger().error("Error parsing mock file " + uri, e);
            throw new ProcessingException(
                "Error parsing mock file " + uri,
                e);
        } finally {
            if (parser != null) {
                this.serviceManager.release(parser);
            }
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e1) {
                    getLogger().warn(
                        "Failed to flush/close the outputstream. ",
                        e1);
                }
            }
        }
    }


    /**
     * Loads (and parses) the Document from the specified Source
     * 
     * @param documentSrc
     * @return
     * @throws ProcessingException
     */
    private Document loadDocumentFromSource(Source documentSrc)
        throws ProcessingException {
        DOMParser parser = null;
        try {
            parser =
                (DOMParser) this.serviceManager.lookup(DOMParser.ROLE);
            getLogger().debug(
                "request to load file " + documentSrc.getURI());
            InputSource input = new InputSource(documentSrc.getURI());
            return parser.parseDocument(input);
        } catch (Exception e) {
            throw new ProcessingException(
                "failed to load file to bind to: ",
                e);
        } finally {
            if (parser != null) {
                this.serviceManager.release(parser);
            }
        }
    }


    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
    }
}
