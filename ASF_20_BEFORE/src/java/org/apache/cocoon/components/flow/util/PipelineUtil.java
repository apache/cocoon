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
package org.apache.cocoon.components.flow.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.avalon.excalibur.io.IOUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Utility class to process a pipeline to various destinations.
 * This class must be setup from the flowscript before being used. This means that instances must
 * be created with <code>cocoon.createObject(Packages.org.apache.cocoon.components.flow.util.PipelineUtil);
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: PipelineUtil.java,v 1.1 2003/12/09 21:21:54 sylvain Exp $
 */
public class PipelineUtil implements Contextualizable, Serviceable {
    
    private Context context;
    private ServiceManager manager;
    private SourceResolver resolver;

    public void contextualize(Context context) throws ContextException {
        this.context = context;
        
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
    }
    
    /**
     * Check that this object has been correctly set up.
     *
     * @throws IllegalStateException if not already set up.
     */
    private void checkSetup() {
        if (this.manager == null) {
            throw new IllegalStateException("Instances of " + this.getClass().getName() +
                " must be setup using either cocoon.createObject() or cocoon.setupObject().");
        }
    }
    
    /**
     * Process a pipeline to a stream.
     * 
     * @param uri the pipeline URI
     * @param viewData the view data object
     * @param output the stream where pipeline result is output. Note: this stream is not closed.
     * @throws IOException
     */
    public void processToStream(String uri, Object viewData, OutputStream output) throws IOException {
        checkSetup();
        
        Map objectModel = ContextHelper.getObjectModel(this.context);

        // Keep the previous view data, if any (is it really necessary?), and set the new one
        Object oldViewData = FlowHelper.getContextObject(objectModel);
        FlowHelper.setContextObject(objectModel, FlowHelper.unwrap(viewData));
        
        Source src = null;
        
        try {
            src = this.resolver.resolveURI("cocoon:/" + uri);
            InputStream input = src.getInputStream();
            
            IOUtil.copy(input, output);
        } finally {
            // Restore the previous view data
            FlowHelper.setContextObject(objectModel, oldViewData);

            if (src != null) {
                this.resolver.release(src);
            }
        }
    }
    
    /**
     * Process a pipeline to a SAX <code>ContentHandler</code>
     * 
     * @param uri the pipeline URI
     * @param viewData the view data object
     * @param handler where the pipeline should be streamed to.
     */
    public void processToSAX(String uri, Object viewData, ContentHandler handler) throws SAXException, IOException, ProcessingException {
        checkSetup();
        
        Map objectModel = ContextHelper.getObjectModel(this.context);
        Object oldViewData = FlowHelper.getContextObject(objectModel);
        FlowHelper.setContextObject(objectModel, FlowHelper.unwrap(viewData));
        
        Source src = null;
        
        try {
            src = this.resolver.resolveURI("cocoon:/" + uri);
            SourceUtil.toSAX(src, handler);
        } finally {
            FlowHelper.setContextObject(objectModel, oldViewData);
            if (src != null) {
                this.resolver.release(src);
            }
        }
    }
    
    /**
     * Process a pipeline and gets is result as a DOM <code>Document</code>
     * 
     * @param uri the pipeline URI
     * @param viewData the view data object
     * @return the document
     */
    public Document processToDOM(String uri, Object viewData) throws ProcessingException, SAXException, IOException  {
        checkSetup();
        
        Map objectModel = ContextHelper.getObjectModel(this.context);
        Object oldViewData = FlowHelper.getContextObject(objectModel);
        FlowHelper.setContextObject(objectModel, FlowHelper.unwrap(viewData));
        
        Source src = null;
        
        try {
            src = this.resolver.resolveURI("cocoon:/" + uri);
            return SourceUtil.toDOM(src);
        } finally {
            FlowHelper.setContextObject(objectModel, oldViewData);
            if (src != null) {
                this.resolver.release(src);
            }
        }
    }
}
