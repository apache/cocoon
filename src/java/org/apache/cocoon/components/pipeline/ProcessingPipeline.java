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
package org.apache.cocoon.components.pipeline;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.SourceValidity;

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
 * <p>
 * A <code>ProcessingPipeline</code> is <code>Recomposable</code> since the
 * <code>ServiceManager</code> used to get the generators, transformers etc.
 * depends on the pipeline assembly engine where they are defined (i.e. a given
 * sitemap file).
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Id: ProcessingPipeline.java,v 1.10 2004/03/08 14:01:57 cziegeler Exp $
 */
public interface ProcessingPipeline {

    String ROLE = ProcessingPipeline.class.getName();

    /**
     * Set the correct manager for the sitemap this pipeline
     * is used in
     */
    void reservice(ServiceManager manager)
    throws ServiceException;
    
    /**
     * Setup this component
     */
    void setup(Parameters params);

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
    void setGenerator (String role, String source, Parameters param, Parameters hintParam)
    throws ProcessingException;

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
    void addTransformer (String role, String source, Parameters param, Parameters hintParam)
    throws ProcessingException;

    /**
     * Set the serializer for this pipeline
     * @param mimeType Can be null
     */
    void setSerializer (String role, String source, Parameters param, Parameters hintParam, String mimeType)
    throws ProcessingException;

    /**
     * Set the reader for this pipeline
     * @param mimeType Can be null
     */
    void setReader (String role, String source, Parameters param, String mimeType)
    throws ProcessingException;

    /**
     * Process the given <code>Environment</code>, producing the output.
     */
    boolean process(Environment environment)
    throws ProcessingException;

    /**
     * Prepare an internal processing
     * @param environment          The current environment.
     * @throws ProcessingException
     */
    void prepareInternal(Environment environment)
    throws ProcessingException;

    /**
     * Process the given <code>Environment</code>, but do not use the
     * serializer. Instead the sax events are streamed to the XMLConsumer.
     * Make sure to call {@link #prepareInternal(Environment)} beforehand.
     */
    boolean process(Environment environment, XMLConsumer consumer)
    throws ProcessingException;

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
