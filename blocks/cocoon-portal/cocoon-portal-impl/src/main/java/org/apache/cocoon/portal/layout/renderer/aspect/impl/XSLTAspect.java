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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.services.VariableResolver;
import org.apache.cocoon.portal.util.IncludeXMLConsumer;
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

    /** Additional parameters passed to the stylesheet. */
    protected Map parameters;

    /** Source resolver for resolving the stylesheets. */
    protected SourceResolver resolver;

    protected XSLTProcessor xsltProcessor;

    public void setXsltProcessor(XSLTProcessor xsltProcessor) {
        this.xsltProcessor = xsltProcessor;
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

        Source stylesheet = null;
        try {
            stylesheet = this.resolver.resolveURI(this.getStylesheetURI(config, layout));
            TransformerHandler transformer = this.xsltProcessor.getTransformerHandler(stylesheet);
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
            handler = new IncludeXMLConsumer(handler);
            SAXResult result = new SAXResult(handler);
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
        } finally {
            this.resolver.release(stylesheet);
        }
	}

    protected String getStylesheetURI(PreparedConfiguration config, Layout layout)
    throws SAXException {
        String stylesheet = layout.getParameter("stylesheet");
        if ( stylesheet != null ) {
            stylesheet = this.portalService.getVariableResolver().resolve(stylesheet);
        } else {
            stylesheet = config.stylesheet.resolve();
        }
        return stylesheet;
    }

    protected String getParameterValue(Map.Entry entry) throws SAXException {
        return ((VariableResolver.CompiledExpression)entry.getValue()).resolve();
    }

    protected static class PreparedConfiguration {
        public VariableResolver.CompiledExpression stylesheet;
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
        pc.stylesheet = this.portalService.getVariableResolver().compile(stylesheet);
        if (this.parameters != null) {
            final Iterator i = this.parameters.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                VariableResolver.CompiledExpression compiledExpression =
                    this.portalService.getVariableResolver().compile(current.getValue().toString());
                pc.parameters.put(current.getKey(), compiledExpression);
            }
        }
        return pc;
    }
}