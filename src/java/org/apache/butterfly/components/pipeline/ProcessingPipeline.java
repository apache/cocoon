/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.butterfly.components.pipeline;

import org.apache.butterfly.environment.Environment;
import org.apache.butterfly.generation.Generator;
import org.apache.butterfly.reading.Reader;
import org.apache.butterfly.serialization.Serializer;
import org.apache.butterfly.source.SourceValidity;
import org.apache.butterfly.transformation.Transformer;
import org.apache.butterfly.xml.XMLConsumer;


/**
 * A <code>ProcessingPipeline<code> produces the response for a given request.
 * It is assembled according to the commands in the sitemap and can either
 * <ul>
 *  <li>collect a <code>Reader</code> and let it produce a character stream</li>
 *  <li>or connect a <code>Generator</code> with zero or more
 *      <code>Transformer</code>s and a <code>Serializer</code> and let them
 *      produce the byte stream. This pipeline uses SAX events for
 *      communication.
 *  </li>
 * </ul>
 *
 * @version CVS $Id: ProcessingPipeline.java,v 1.2 2004/07/24 20:21:33 ugo Exp $
 */
public interface ProcessingPipeline {

    /**
     * Setup this component
     */
    // void setup(Parameters params);

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
     * @throws ProcessingException if the generator couldn't be obtained.
     */
    void setGenerator(Generator generator);

    /**
     * Get the generator - used for content aggregation
     */
    Generator getGenerator();

    /**
     * Informs pipeline we have come across a branch point
     */
    void informBranchPoint();

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
     * @throws ProcessingException if the generator couldn't be obtained.
     */
    void addTransformer(Transformer transformer);

    /**
     * Set the serializer for this pipeline
     * @param mimeType Can be null
     */
    void setSerializer(Serializer serializer);

    /**
     * Set the reader for this pipeline
     * @param mimeType Can be null
     */
    void setReader(Reader reader);

    /**
     * Process the given <code>Environment</code>, producing the output.
     */
    boolean process(Environment environment);

    /**
     * Prepare an internal processing
     * @param environment          The current environment.
     * @throws ProcessingException
     */
    void prepareInternal(Environment environment);

    /**
     * Process the given <code>Environment</code>, but do not use the
     * serializer. Instead the sax events are streamed to the XMLConsumer.
     * Make sure to call {@link #prepareInternal(Environment)} beforehand.
     */
    boolean process(Environment environment, XMLConsumer consumer);

    /**
     * Return valid validity objects for the event pipeline
     * If the "event pipeline" (= the complete pipeline without the
     * serializer) is cacheable and valid, return all validity objects.
     * Otherwise return <code>null</code>
     */
    SourceValidity getValidityForEventPipeline();
    
    /**
     * Return the key for the event pipeline
     * If the "event pipeline" (= the complete pipeline without the
     * serializer) is cacheable and valid, return a key.
     * Otherwise return <code>null</code>
     */
    String getKeyForEventPipeline();
}
