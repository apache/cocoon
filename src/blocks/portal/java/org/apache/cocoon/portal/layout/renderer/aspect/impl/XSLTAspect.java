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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.apache.excalibur.xml.xslt.XSLTProcessorImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: XSLTAspect.java,v 1.1 2003/05/07 06:22:22 cziegeler Exp $
 */
public class XSLTAspect 
    extends AbstractLogEnabled 
    implements RendererAspect, ThreadSafe, Composable {

    protected ComponentManager manager;
    
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
                        Layout layout,
                        PortalService service,
                        ContentHandler handler)
    throws SAXException {
        XSLTProcessor processor = null;
        Source stylesheet = null;
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            stylesheet = resolver.resolveURI(this.getStylesheetURI(context, layout));
            processor = (XSLTProcessor) this.manager.lookup(XSLTProcessorImpl.ROLE);
            TransformerHandler transformer = processor.getTransformerHandler(stylesheet);
            SAXResult result = new SAXResult(new IncludeXMLConsumer((handler)));
            if (handler instanceof LexicalHandler) {
                result.setLexicalHandler((LexicalHandler) handler);
            }
            transformer.setResult(result);
            transformer.startDocument();
            context.invokeNext(layout, service, transformer);

            transformer.endDocument();
        } catch (Exception ce) {
            throw new SAXException("Unable to lookup component.", ce);
        } finally {
            if (null != resolver) {
                resolver.release(stylesheet);
                this.manager.release(resolver);
            }
            this.manager.release(processor);
        }
	}

    protected String getStylesheetURI(RendererAspectContext context, Layout layout) {
        // FIXME Get the stylesheet either from a layout attribute or another aspect
        return context.getAspectParameters().getParameter("style", "NOTFOUND");
    }
	/* (non-Javadoc)
	 * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
	 */
	public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
	}

}
