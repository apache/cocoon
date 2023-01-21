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

package org.apache.cocoon.transformation;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.cocoon.xml.XMLConsumer;

/**
 * <p>An error aware TRAX-based transformer.</p>
 * 
 * <p>This might be a very stupid extension to the {@link TraxTransformer}, but in
 * some very specific cases (for example when using Apache Xalan-J 2), the message
 * output of the stylesheets, and error messages, can be greatly improved by just
 * using this class instead of the default one.</p>
 * 
 * <p>Using this transformer with Apache Xalan-J 2, for example, will allow Cocoon
 * to capture the output of <code>&lt;xsl:message&#nbsp;terminate="yes"/&gt;</code>
 * and to serve it up using the standard error handling pipelines.</p>
 *
 * @version $Id$
 */
public class ErrorAwareTraxTransformer extends TraxTransformer
implements ErrorListener {

    /**
     * <p>Handle an error notification from the original TRAX transformer.</p>
     * 
     * <p>This method simply throws the same exception passed in as a parameter.</p>
     *
     * @see ErrorListener#error(TransformerException)
     */
    public void error(TransformerException exception)
    throws TransformerException {
      if (exception != null) throw(exception);
    }

    /**
     * <p>Handle an error notification from the original TRAX transformer.</p>
     * 
     * <p>This method simply throws the same exception passed in as a parameter.</p>
     *
     * @see ErrorListener#fatalError(TransformerException)
     */
    public void fatalError(TransformerException exception)
    throws TransformerException {
        if (exception != null) throw(exception);
    }

    /**
     * <p>Handle an error notification from the original TRAX transformer.</p>
     * 
     * <p>This method simply throws the same exception passed in as a parameter.</p>
     *
     * @see ErrorListener#warning(TransformerException)
     */
    public void warning(TransformerException exception)
    throws TransformerException {
        if (exception != null) throw(exception);
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     */
    public void setConsumer(XMLConsumer consumer) {
        super.setConsumer(consumer);
        super.transformerHandler.getTransformer().setErrorListener(this);
    }
}
