/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
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
 * Apply a XSLT stylesheet to the contained layout. All following renderer aspects
 * are applied first before the XML is fed into the XSLT. All configuration and layout
 * parameters are made available to the stylesheet.
 *
 * <h2>Example XML:</h2>
 * <pre>
 *  &lt;-- result from output of following renderers transformed by stylesheet --&gt;
 * </pre>
 *
 * The parameter values may contain Strings and/or references to input modules which will be resolved each
 * time the aspect is rendered.
 * <h2>Applicable to:</h2>
 * {@link org.apache.cocoon.portal.om.Layout}
 *
 * <h2>Configuration</h2>
 * <h3>cocoon.xconf</h3>
 *
 * <pre>
 * &lt;aspect name="xslt" class="org.apache.cocoon.portal.layout.renderer.aspect.impl.XSLTAspect"&gt;
 *   &lt;parameters&gt;
 *     &lt;parameter name="<i>name1</i>" value="<i>parameter value</i>"/&gt;
 *     &lt;parameter name="<i>name2</i>" value="<i>parameter value</i>"/&gt;
 *   &lt;parameter&gt;
 * &lt;/aspect&gt;
 * </pre>
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 * <tr><th>style</th><td></td><td>req</td><td>String</td><td><code>null</code></td></tr>
 * <tr><th>xslt-processor-role</th><td></td><td>req</td><td>String</td><td><code>null</code></td></tr>
 * </tbody></table>  
 *
 * TODO - Remove all dependencies to Avalon.
 * @version $Id$
 */
public class XSLTAspect 
    extends AbstractAspect {

    protected List variables = new ArrayList();

    /** Additional parameters passed to the stylesheet. */
    protected Map parameters;

    /** Source resolver for resolving the stylesheets. */
    protected SourceResolver resolver;

    protected ServiceManager serviceManager;

    public void setServiceManager(ServiceManager sm) {
        this.serviceManager = sm;
    }

    public void setSourceResolver(SourceResolver resolver) {
        this.resolver = resolver;   
    }

    public void setXsltParameters(Map p) {
        this.parameters = p;
    }

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext rendererContext,
                      Layout layout,
                      ContentHandler handler)
    throws SAXException, LayoutException {
        PreparedConfiguration config = (PreparedConfiguration)rendererContext.getAspectConfiguration();

        XSLTProcessor processor = null;
        Source stylesheet = null;
        try {
            stylesheet = this.resolver.resolveURI(this.getStylesheetURI(config, layout));
            processor = (XSLTProcessor) this.serviceManager.lookup(config.xsltRole);
            TransformerHandler transformer = processor.getTransformerHandler(stylesheet);
            // Pass configured parameters to the stylesheet.
            if (config.parameters.size() > 0) {                
                Transformer theTransformer = transformer.getTransformer();
                Iterator iter = config.parameters.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String value = getParameterValue(entry);
                    theTransformer.setParameter((String) entry.getKey(), value);
                }
            }

            final Map parameter = layout.getParameters();
            if (parameter.size() > 0) {
                Transformer theTransformer = transformer.getTransformer();
                for (Iterator iter = parameter.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    theTransformer.setParameter((String)entry.getKey(), entry.getValue());
                }
            }
            SAXResult result = new SAXResult(new IncludeXMLConsumer((handler)));
            if (handler instanceof LexicalHandler) {
                result.setLexicalHandler((LexicalHandler) handler);
            }
            transformer.setResult(result);
            transformer.startDocument();
            rendererContext.invokeNext(layout, transformer);

            transformer.endDocument();
        } catch (XSLTProcessorException xpe) {
            throw new SAXException("XSLT Exception.", xpe);
        } catch (IOException io) {
            throw new SAXException("Error in resolving.", io);
        } catch (ServiceException ce) {
            throw new SAXException("Unable to lookup component.", ce);
        } finally {
            this.resolver.release(stylesheet);
            this.serviceManager.release(processor);
        }
	}

    protected String getStylesheetURI(PreparedConfiguration config, Layout layout) 
    throws SAXException {
        final Map objectModel = this.portalService.getProcessInfoProvider().getObjectModel();
        String stylesheet = layout.getParameter("stylesheet");
        if ( stylesheet != null ) {
            VariableResolver variableResolver = null;
            try {
                variableResolver = VariableResolverFactory.getResolver(stylesheet, this.serviceManager);
                stylesheet = variableResolver.resolve(objectModel);
            } catch (PatternException pe) {
                throw new SAXException("Unknown pattern for stylesheet " + stylesheet, pe);
            } finally {
                ContainerUtil.dispose(variableResolver);
            }            
        } else {
            try {
                stylesheet = config.stylesheet.resolve(objectModel);
            } catch (PatternException pe) {
                throw new SAXException("Pattern exception during variable resolving.", pe);            
            }
        }
        return stylesheet;
    }

    protected String getParameterValue(Map.Entry entry) throws SAXException {
        final Map objectModel = this.portalService.getProcessInfoProvider().getObjectModel();
        try {
            return ((VariableResolver)entry.getValue()).resolve(objectModel);
        } catch (PatternException pe) {
            throw new SAXException("Unable to get value for parameter " + entry.getKey(), pe);
        }
    }

    protected static class PreparedConfiguration {
        public VariableResolver stylesheet;
        public String xsltRole;
        public Map parameters = new HashMap();

        public void takeValues(PreparedConfiguration from) {
            this.stylesheet = from.stylesheet;
            this.xsltRole = from.xsltRole;
            this.parameters = from.parameters;
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractAspect#prepareConfiguration(java.util.Properties)
     */
    public Object prepareConfiguration(Properties configuration)
    throws PortalException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.xsltRole = configuration.getProperty("xslt-processor-role", XSLTProcessor.ROLE);
        String stylesheet = configuration.getProperty("style", null);
        try {
            pc.stylesheet = VariableResolverFactory.getResolver(stylesheet, this.serviceManager);
        } catch (PatternException pe) {
            throw new PortalException("Unknown pattern for stylesheet " + stylesheet, pe);
        }
        this.variables.add(pc.stylesheet);
        if (this.parameters != null) {
            final Iterator i = this.parameters.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                try {
                    VariableResolver variableResolver =
                        VariableResolverFactory.getResolver(current.getValue().toString(), this.serviceManager);
                    this.variables.add(variableResolver);
                    pc.parameters.put(current.getKey(), variableResolver);
                } catch (PatternException e) {
                    throw new PortalException("Invalid value for parameter " + current.getKey() + " : " + current.getValue(), e);
                }
            }
        }
        return pc;
    }

    /**
     * Destroy this component.
     */
    public void destroy() {
        if ( this.serviceManager != null ) {
            Iterator vars = this.variables.iterator();
            while ( vars.hasNext() ) {
                ContainerUtil.dispose(vars.next());
            }
            this.variables.clear();
        }
    }
}