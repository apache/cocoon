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
package org.apache.cocoon.components.profiler;

import java.io.IOException;
import java.util.Iterator;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.pipeline.impl.NonCachingProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;

import org.xml.sax.SAXException;

/**
 * Special version of the NonCachingProcessingPipeline that supports capturing
 * the SAX-events that go through it and stores the result in the
 * ProfilerData.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:bruno@outerthought.org">Bruno Dumon</a>
 * @version CVS $Id: ProfilingNonCachingProcessingPipeline.java,v 1.2 2003/03/20 15:04:14 stephan Exp $
 */
public class ProfilingNonCachingProcessingPipeline
  extends NonCachingProcessingPipeline implements Disposable {

    private Profiler profiler;

    private ProfilerData data = null;

    private int index = 0;

    /**
     * Composable
     *
     * @param manager
     */
    public void compose(ComponentManager manager) throws ComponentException {

        super.compose(manager);
        this.profiler = (Profiler) manager.lookup(Profiler.ROLE);
    }

    /**
     * Disposable
     */
    public void dispose() {
        if (this.profiler!=null) {
            this.manager.release(this.profiler);
            this.profiler = null;
        }
    }

    /**
     * Recyclable
     */
    public void recycle() {
        this.data = null;
        this.index = 0;
        super.recycle();
    }

    /**
     * Set the generator that will be used as the initial step in the pipeline.
     * The generator role is given : the actual <code>Generator</code> is fetched
     * from the latest <code>ComponentManager</code> given by <code>compose()</code>
     * or <code>recompose()</code>.
     *
     * @param role the generator role in the component manager.
     * @param source the source where to produce XML from, or <code>null</code> if no
     *        source is given.
     * @param param the parameters for the generator.
     * @param hintParam
     * @throws ProcessingException if the generator couldn't be obtained.
     */
    public void setGenerator(String role, String source, Parameters param,
                             Parameters hintParam)
                               throws ProcessingException {

        super.setGenerator(role, source, param, hintParam);

        if (this.data==null) {
            this.data = new ProfilerData();
        }
        this.data.addComponent(super.generator, role, source);
    }

    /**
     * Add a transformer at the end of the pipeline.
     * The transformer role is given : the actual <code>Transformer</code> is fetched
     * from the latest <code>ComponentManager</code> given by <code>compose()</code>
     * or <code>recompose()</code>.
     *
     * @param role the transformer role in the component manager.
     * @param source the source used to setup the transformer (e.g. XSL file), or
     *        <code>null</code> if no source is given.
     * @param param the parameters for the transfomer.
     * @param hintParam
     * @throws ProcessingException if the generator couldn't be obtained.
     */
    public void addTransformer(String role, String source, Parameters param,
                               Parameters hintParam)
                                 throws ProcessingException {

        super.addTransformer(role, source, param, hintParam);

        if (this.data==null) {
            this.data = new ProfilerData();
        }
        this.data.addComponent(super.transformers.get(super.transformers.size()-
            1), role, source);
    }

    /**
     * Set the serializer for this pipeline
     *
     * @param role       
     * @param source     
     * @param param      
     * @param hintParam  
     * @param mimeType   
     */
    public void setSerializer(String role, String source, Parameters param,
                              Parameters hintParam,
                              String mimeType) throws ProcessingException {

        super.setSerializer(role, source, param, hintParam, mimeType);

        if (this.data==null) {
            this.data = new ProfilerData();
        }
        this.data.addComponent(super.serializer, role, source);
    }

    /**
     * Set the reader for this pipeline
     *
     * @param role       
     * @param source     
     * @param param      
     * @param mimeType   
     */
    public void setReader(String role, String source, Parameters param,
                          String mimeType) throws ProcessingException {

        super.setReader(role, source, param, mimeType);

        if (this.data==null) {
            this.data = new ProfilerData();
        }
        this.data.addComponent(super.reader, role, source);
    }

    /**
     * Setup pipeline components.
     *
     * @param environment
     */
    protected void setupPipeline(Environment environment)
      throws ProcessingException {
        try {

            // setup the generator
            long time = System.currentTimeMillis();

            this.generator.setup(environment, environment.getObjectModel(),
                                 generatorSource, generatorParam);
            this.data.setSetupTime(0, System.currentTimeMillis()-time);

            Iterator transformerItt = this.transformers.iterator();
            Iterator transformerSourceItt = this.transformerSources.iterator();
            Iterator transformerParamItt = this.transformerParams.iterator();

            int index = 1;

            while (transformerItt.hasNext()) {
                Transformer trans = (Transformer) transformerItt.next();

                time = System.currentTimeMillis();
                trans.setup(environment, environment.getObjectModel(),
                            (String) transformerSourceItt.next(),
                            (Parameters) transformerParamItt.next());
                this.data.setSetupTime(index++,
                                       System.currentTimeMillis()-time);
            }

            String mimeType = this.serializer.getMimeType();

            if (mimeType!=null) {
                // we have a mimeType from the component itself
                environment.setContentType(mimeType);
            } else if (serializerMimeType!=null) {
                // there was a mimeType specified in the sitemap pipeline
                environment.setContentType(serializerMimeType);
            } else if (this.sitemapSerializerMimeType!=null) {
                // use the mimeType specified in the sitemap component declaration
                environment.setContentType(this.sitemapSerializerMimeType);
            } else {
                // No mimeType available
                String message = "Unable to determine MIME type for "+
                                 environment.getURIPrefix()+"/"+
                                 environment.getURI();

                throw new ProcessingException(message);
            }
        } catch (SAXException e) {
            throw new ProcessingException("Could not setup pipeline.", e);
        } catch (IOException e) {
            throw new ProcessingException("Could not setup pipeline.", e);
        }
    }

    /**
     * Process the given <code>Environment</code>, producing the output.
     *
     * @param environment
     *
     * @return
     */
    public boolean process(Environment environment)
      throws ProcessingException {

        this.index = 0;
        if (this.data!=null) {
            // Capture environment info
            this.data.setEnvironmentInfo(new EnvironmentInfo(environment));

            // Execute pipeline
            long time = System.currentTimeMillis();
            boolean result = super.process(environment);

            this.data.setTotalTime(System.currentTimeMillis()-time);

            // Report
            profiler.addResult(environment.getURI(), this.data);
            return result;
        } else {
            getLogger().warn("Profiler Data havn't any components to measure");
            return super.process(environment);
        }
    }

    /**
     * Connect the next component
     *
     * @param environment
     * @param producer   
     * @param consumer   
     */
    protected void connect(Environment environment, XMLProducer producer,
                           XMLConsumer consumer) throws ProcessingException {
        ProfilingXMLPipe connector = new ProfilingXMLPipe();

        connector.setup(this.index, this.data);
        this.index++;
        super.connect(environment, producer, connector);
        super.connect(environment, connector, consumer);
    }

}
