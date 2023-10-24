/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.xslt;

import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.XMLFilter;

/**
 * This is the interface of the XSLT processor.
 *
 * @version $Id$
 */
public interface XSLTProcessor {

    public static class TransformerHandlerAndValidity {

        private final TransformerHandler transformerHandler;
        private final SourceValidity transformerValidity;

        protected TransformerHandlerAndValidity( final TransformerHandler transformerHandler,
                                                 final SourceValidity transformerValidity ) {
            this.transformerHandler = transformerHandler;
            this.transformerValidity = transformerValidity;
        }

        public TransformerHandler getTransfomerHandler() {
            return transformerHandler;
        }

        public SourceValidity getTransfomerValidity() {
            return transformerValidity;
        }
    }

    /**
     * <p>Return a <code>TransformerHandler</code> for a given
     * stylesheet {@link Source}. This can be used in a pipeline to
     * handle the transformation of a stream of SAX events. See {@link
     * org.apache.cocoon.transformation.TraxTransformer#setConsumer} for
     * an example of how to use this method.
     *
     * <p>The additional <code>filter</code> argument, if it's not
     * <code>null</code>, is inserted in the chain SAX events as an XML
     * filter during the parsing or the source document.
     *
     * <p>This method caches the Templates object with meta information
     * (modification time and list of included stylesheets) and performs
     * a reparsing only if this changes.
     *
     * @param stylesheet a {@link Source} value
     * @param filter a {@link XMLFilter} value
     * @return a {@link TransformerHandler} value
     * @exception XSLTProcessorException if an error occurs
     */
    TransformerHandler getTransformerHandler( Source stylesheet, XMLFilter filter )
    throws XSLTProcessorException;

    /**
     * <p>Return a {@link TransformerHandler} and
     * <code>SourceValidity</code> for a given stylesheet
     * {@link Source}. This can be used in a pipeline to
     * handle the transformation of a stream of SAX events. See {@link
     * org.apache.cocoon.transformation.TraxTransformer#setConsumer} for
     * an example of how to use this method.
     *
     * <p>The additional <code>filter</code> argument, if it's not
     * <code>null</code>, is inserted in the chain SAX events as an XML
     * filter during the parsing or the source document.
     *
     * <p>This method caches the Templates object with meta information
     * (modification time and list of included stylesheets) and performs
     * a reparsing only if this changes.
     *
     * @param stylesheet a {@link Source} value
     * @param filter a {@link XMLFilter} value
     * @return a <code>TransformerHandlerAndValidity</code> value
     * @exception XSLTProcessorException if an error occurs
     */
    TransformerHandlerAndValidity getTransformerHandlerAndValidity( Source stylesheet, XMLFilter filter )
    throws XSLTProcessorException;

    /**
     * Same as {@link #getTransformerHandler(Source,XMLFilter)}, with
     * <code>filter</code> set to <code>null</code>.
     *
     * @param stylesheet a {@link Source} value
     * @return a {@link TransformerHandler} value
     * @exception XSLTProcessorException if an error occurs
     */
    TransformerHandler getTransformerHandler( Source stylesheet )
    throws XSLTProcessorException;

    /**
     * Same as {@link #getTransformerHandlerAndValidity(Source,XMLFilter)}, with
     * <code>filter</code> set to <code>null</code>.
     *
     * @param stylesheet a {@link Source} value
     * @return a {@link TransformerHandlerAndValidity} value
     * @exception XSLTProcessorException if an error occurs
     */
    TransformerHandlerAndValidity getTransformerHandlerAndValidity( Source stylesheet )
    throws XSLTProcessorException;

    /**
     * Applies an XSLT stylesheet to an XML document. The source and
     * stylesheet documents are specified as {@link Source}
     * objects. The result of the transformation is placed in
     * {@link Result}, which should be properly initialized before
     * invoking this method. Any additional parameters passed in
     * {@link Map params} will become arguments to the stylesheet.
     *
     * @param source a {@link Source} value
     * @param stylesheet a {@link Source} value
     * @param params a <code>Map</code>
     * @param result a <code>Result</code> value
     * @exception XSLTProcessorException if an error occurs
     */
    void transform( Source source, Source stylesheet, Map params, Result result )
    throws XSLTProcessorException;
}
