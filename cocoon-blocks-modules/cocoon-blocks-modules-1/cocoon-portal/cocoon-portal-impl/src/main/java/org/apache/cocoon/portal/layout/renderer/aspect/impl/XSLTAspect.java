/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
import java.util.Map;
import java.util.HashMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
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
 * {@link org.apache.cocoon.portal.layout.Layout}
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
 * @version $Id$
 */
public class XSLTAspect 
    extends AbstractAspect
    implements Configurable {

    protected List variables = new ArrayList();

    /** Additional parameters passed to the stylesheet. */
    protected Parameters parameters;

    /** Source resolver for resolving the stylesheets. */
    protected SourceResolver resolver;

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        Configuration parameterItems = config.getChild("parameters", false);
        if (parameterItems != null) {
            this.parameters = Parameters.fromConfiguration(parameterItems);
        }
    }

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext rendererContext,
                      Layout layout,
                      PortalService service,
                      ContentHandler handler)
    throws SAXException {
        PreparedConfiguration config = (PreparedConfiguration)rendererContext.getAspectConfiguration();

        XSLTProcessor processor = null;
        Source stylesheet = null;
        try {
            stylesheet = this.resolver.resolveURI(this.getStylesheetURI(config, layout));
            processor = (XSLTProcessor) this.manager.lookup(config.xsltRole);
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
            rendererContext.invokeNext(layout, service, transformer);

            transformer.endDocument();
        } catch (XSLTProcessorException xpe) {
            throw new SAXException("XSLT Exception.", xpe);
        } catch (IOException io) {
            throw new SAXException("Error in resolving.", io);
        } catch (ServiceException ce) {
            throw new SAXException("Unable to lookup component.", ce);
        } finally {
            this.resolver.release(stylesheet);
            this.manager.release(processor);
        }
	}

    protected String getStylesheetURI(PreparedConfiguration config, Layout layout) 
    throws SAXException {
        String stylesheet = layout.getParameter("stylesheet");
        if ( stylesheet != null ) {
            VariableResolver variableResolver = null;
            try {
                variableResolver = VariableResolverFactory.getResolver(stylesheet, this.manager);
                stylesheet = variableResolver.resolve(ContextHelper.getObjectModel(this.context));
            } catch (PatternException pe) {
                throw new SAXException("Unknown pattern for stylesheet " + stylesheet, pe);
            } finally {
                ContainerUtil.dispose(variableResolver);
            }            
        } else {
            try {
                stylesheet = config.stylesheet.resolve(ContextHelper.getObjectModel(this.context));
            } catch (PatternException pe) {
                throw new SAXException("Pattern exception during variable resolving.", pe);            
            }
        }
        return stylesheet;
    }

    protected String getParameterValue(Map.Entry entry) throws SAXException {
        try {
            return ((VariableResolver)entry.getValue()).resolve(ContextHelper.getObjectModel(this.context));
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
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.xsltRole = configuration.getParameter("xslt-processor-role", XSLTProcessor.ROLE);
        String stylesheet = configuration.getParameter("style");
        try {
            pc.stylesheet = VariableResolverFactory.getResolver(stylesheet, this.manager);
        } catch (PatternException pe) {
            throw new ParameterException("Unknown pattern for stylesheet " + stylesheet, pe);
        }
        this.variables.add(pc.stylesheet);
        if (this.parameters != null) {
            String[] name = this.parameters.getNames();
            for (int i=0; i < name.length; ++i) {
                try {
                    VariableResolver variableResolver =
                        VariableResolverFactory.getResolver(this.parameters.getParameter(name[i]), this.manager);
                    this.variables.add(variableResolver);
                    pc.parameters.put(name[i], variableResolver);
                } catch (PatternException e) {
                    throw new ParameterException("Invalid value for parameter " + name[i], e);
                }
            }
        }
        return pc;
    }

    /**
     * @see org.apache.cocoon.portal.impl.AbstractComponent#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        super.service(aManager);
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            Iterator vars = this.variables.iterator();
            while ( vars.hasNext() ) {
                ContainerUtil.dispose(vars.next());
            }
            this.variables.clear();
            this.manager.release(this.resolver);
            this.resolver = null;
        }
        super.dispose();
    }
}