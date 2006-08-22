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
package org.apache.cocoon.components.profiler;

import java.io.IOException;
import java.util.Iterator;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.pipeline.impl.CachingProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:bruno@outerthought.org">Bruno Dumon</a>
 * @version $Id$
 */
public class ProfilingCachingProcessingPipeline extends CachingProcessingPipeline {

    private Profiler profiler;

    private ProfilerData data;

    private int index;

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
        super.dispose();
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
                              String mimeType)
    throws ProcessingException {

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
                          String mimeType)
    throws ProcessingException {

        super.setReader(role, source, param, mimeType);

        if (this.data==null) {
            this.data = new ProfilerData();
        }
        this.data.addComponent(super.reader, role, source);
    }

    /**
     * Timed version of {@link org.apache.cocoon.components.pipeline.AbstractProcessingPipeline#setupPipeline}
     * and {@link org.apache.cocoon.components.pipeline.impl.AbstractCachingProcessingPipeline#setupPipeline}.
     */
    protected void setupPipeline(Environment environment)
    throws ProcessingException {
        try {
            // Setup the generator
            long time = System.currentTimeMillis();
            this.generator.setup(environment, environment.getObjectModel(),
                                 generatorSource, generatorParam);
            this.data.setSetupTime(0, System.currentTimeMillis() - time);

            Iterator transformerItt = this.transformers.iterator();
            Iterator transformerSourceItt = this.transformerSources.iterator();
            Iterator transformerParamItt = this.transformerParams.iterator();

            // Setup transformers
            int index = 1;
            while (transformerItt.hasNext()) {
                Transformer trans = (Transformer) transformerItt.next();

                time = System.currentTimeMillis();
                trans.setup(environment, environment.getObjectModel(),
                            (String) transformerSourceItt.next(),
                            (Parameters) transformerParamItt.next());
                this.data.setSetupTime(index++, System.currentTimeMillis() - time);
            }

            // Setup serializer
            time = System.currentTimeMillis();
            if (this.serializer instanceof SitemapModelComponent) {
                ((SitemapModelComponent)this.serializer).setup(
                    environment,
                    environment.getObjectModel(),
                    serializerSource,
                    serializerParam
                );
            }
            this.data.setSetupTime(index++, System.currentTimeMillis() - time);

            setMimeTypeForSerializer(environment);
        } catch (SAXException e) {
            throw new ProcessingException("Could not setup pipeline.", e);
        } catch (IOException e) {
            throw new ProcessingException("Could not setup pipeline.", e);
        }

        // Generate the key to fill the cache
        generateCachingKey(environment);

        // Test the cache for a valid response
        if (this.toCacheKey!=null) {
            validatePipeline(environment);
        }

        setupValidities();
    }

    /**
     * Process the given <code>Environment</code>, producing the output.
     *
     * @param environment
     *
     * @return true on success
     */
    public boolean process(Environment environment)
    throws ProcessingException {
        this.index = 0;
        if (this.data != null) {
            // Capture environment info
            this.data.setEnvironmentInfo(new EnvironmentInfo(environment));

            // Execute pipeline
            long time = System.currentTimeMillis();
            boolean result = super.process(environment);
            this.data.setTotalTime(System.currentTimeMillis() - time);

            // Report
            profiler.addResult(environment.getURI(), this.data);
            return result;
        } else {
            getLogger().warn("Profiler data has no components to measure");
            return super.process(environment);
        }
    }

    /**
     * Process the SAX event pipeline
     * FIXME: VG: Why override processXMLPipeline, not process(env, consumer)?
     */
    protected boolean processXMLPipeline(Environment environment)
    throws ProcessingException {
        this.index = 0;
        if (this.data != null) {
            // Capture environment info
            this.data.setEnvironmentInfo(new EnvironmentInfo(environment));

            // Execute pipeline
            long time = System.currentTimeMillis();
            boolean result = super.processXMLPipeline(environment);
            this.data.setTotalTime(System.currentTimeMillis() - time);

            // Report
            profiler.addResult(environment.getURI(), this.data);
            return result;
        } else {
            getLogger().warn("Profiler data has no components to measure");
            return super.processXMLPipeline(environment);
        }
    }

    /**
     * Process the pipeline using a reader.
     */
    protected boolean processReader(Environment environment)
    throws ProcessingException {
        this.index = 0;
        if (this.data != null) {
            // Capture environment info
            this.data.setEnvironmentInfo(new EnvironmentInfo(environment));

            // Execute pipeline
            long time = System.currentTimeMillis();
            boolean result = super.processReader(environment);
            this.data.setTotalTime(System.currentTimeMillis() - time);

            // Report
            profiler.addResult(environment.getURI(), this.data);
            return result;
        } else {
            getLogger().warn("Profiler data has no components to measure");
            return super.processReader(environment);
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
                           XMLConsumer consumer)
    throws ProcessingException {
        ProfilingXMLPipe connector = new ProfilingXMLPipe();
        connector.setup(this.index, this.data);
        this.index++;
        super.connect(environment, producer, connector);
        super.connect(environment, connector, consumer);
    }
}
