/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.layout.renderer.impl;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: AbstractRenderer.java,v 1.5 2003/09/24 21:22:33 cziegeler Exp $
 */
public abstract class AbstractRenderer
    extends AbstractLogEnabled
    implements Renderer, Composable, Disposable, ThreadSafe {

    protected ComponentSelector rendererSelector;
    protected ComponentManager componentManager;

    public String getStylesheetURI(Layout layout) {
        return null;
    }

    public boolean useStylesheet() {
        return false;
    }

    /**
     * @see org.apache.avalon.framework.component.Composable#compose(ComponentManager)
     */
    public void compose(ComponentManager componentManager) throws ComponentException {
        this.componentManager = componentManager;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (null != this.componentManager) {
            this.componentManager.release(this.rendererSelector);
            this.componentManager = null;
            this.rendererSelector = null;
        }
    }

    /**
     * Stream out raw layout 
     */
    public void toSAX(Layout layout, PortalService service, ContentHandler handler) throws SAXException {
        if (this.useStylesheet()) {
            XSLTProcessor processor = null;
            Source stylesheet = null;
            SourceResolver resolver = null;
            try {
                resolver = (SourceResolver) this.componentManager.lookup(SourceResolver.ROLE);
                stylesheet = resolver.resolveURI(this.getStylesheetURI(layout));
                processor = (XSLTProcessor) this.componentManager.lookup(XSLTProcessor.ROLE);
                TransformerHandler transformer = processor.getTransformerHandler(stylesheet);
                SAXResult result = new SAXResult(new IncludeXMLConsumer((handler)));
                if (handler instanceof LexicalHandler) {
                    result.setLexicalHandler((LexicalHandler) handler);
                }
                transformer.setResult(result);
                transformer.startDocument();
                this.process(layout, service, transformer);
                transformer.endDocument();
            } catch (Exception ce) {
                throw new SAXException("Unable to lookup component.", ce);
            } finally {
                if (null != resolver) {
                    resolver.release(stylesheet);
                    this.componentManager.release(resolver);
                }
                this.componentManager.release(processor);
            }
        } else {
            this.process(layout, service, handler);
        }
    }

    /**
     * Process a Layout
     */
    protected void processLayout(Layout layout, PortalService service, ContentHandler handler) throws SAXException {
        final String rendererName = layout.getRendererName();
        Renderer renderer = null;
        renderer = service.getComponentManager().getRenderer(rendererName);
        renderer.toSAX(layout, service, handler);
    }

    protected abstract void process(Layout layout, PortalService service, ContentHandler handler) throws SAXException;

    /**
     * Return the aspects required for this renderer
     */
    public Iterator getAspectDescriptions() {
        return Collections.EMPTY_LIST.iterator();
    }
    
}
