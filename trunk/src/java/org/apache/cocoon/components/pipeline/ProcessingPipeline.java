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
 * <code>ComponentManager</code> used to get the generators, transformers etc.
 * depends on the pipeline assembly engine where they are defined (i.e. a given
 * sitemap file).
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Id: ProcessingPipeline.java,v 1.7 2004/01/27 13:27:47 unico Exp $
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
