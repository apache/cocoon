/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.variables.VariableResolver;
import org.apache.cocoon.components.variables.VariableResolverFactory;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.apache.excalibur.xml.xslt.XSLTProcessorException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: XSLTAspect.java,v 1.9 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public class XSLTAspect 
    extends AbstractAspect
    implements Disposable {

    protected List variables = new ArrayList();
    
    protected VariableResolverFactory variableFactory;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.variableFactory = (VariableResolverFactory) this.manager.lookup(VariableResolverFactory.ROLE);
    }

    /* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
                        Layout layout,
                        PortalService service,
                        ContentHandler handler)
    throws SAXException {
        PreparedConfiguration config = (PreparedConfiguration)context.getAspectConfiguration();

        XSLTProcessor processor = null;
        Source stylesheet = null;
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            stylesheet = resolver.resolveURI(this.getStylesheetURI(config, layout));
            processor = (XSLTProcessor) this.manager.lookup(config.xsltRole);
            TransformerHandler transformer = processor.getTransformerHandler(stylesheet);
            SAXResult result = new SAXResult(new IncludeXMLConsumer((handler)));
            if (handler instanceof LexicalHandler) {
                result.setLexicalHandler((LexicalHandler) handler);
            }
            transformer.setResult(result);
            transformer.startDocument();
            context.invokeNext(layout, service, transformer);

            transformer.endDocument();
        } catch (XSLTProcessorException xpe) {
            throw new SAXException("XSLT Exception.", xpe);
        } catch (IOException io) {
            throw new SAXException("Error in resolving.", io);
        } catch (ServiceException ce) {
            throw new SAXException("Unable to lookup component.", ce);
        } finally {
            if (null != resolver) {
                resolver.release(stylesheet);
                this.manager.release(resolver);
            }
            this.manager.release(processor);
        }
	}

    protected String getStylesheetURI(PreparedConfiguration config, Layout layout) 
    throws SAXException {
        // FIXME Get the stylesheet either from a layout attribute or another aspect
        try {
            String stylesheet = config.stylesheet.resolve();
            return stylesheet;
        } catch (PatternException pe) {
            throw new SAXException("Pattern exception during variable resolving.", pe);            
        }
    }

    protected class PreparedConfiguration {
        public VariableResolver stylesheet;
        public String xsltRole; 

        public void takeValues(PreparedConfiguration from) {
            this.stylesheet = from.stylesheet;
            this.xsltRole = from.xsltRole;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.xsltRole = configuration.getParameter("xslt-processor-role", XSLTProcessor.ROLE);
        String stylesheet = configuration.getParameter("style");
        try {
            pc.stylesheet = this.variableFactory.lookup( stylesheet );
        } catch (PatternException pe) {
            throw new ParameterException("Unknown pattern for stylesheet " + stylesheet, pe);
        }
        this.variables.add(pc.stylesheet);
        return pc;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            Iterator vars = this.variables.iterator();
            while ( vars.hasNext() ) {
                this.variableFactory.release( (VariableResolver) vars.next() );
            }
            this.variables.clear();
            this.manager.release( this.variableFactory);
            this.manager = null;
            this.variableFactory = null;
        }
    }

}

