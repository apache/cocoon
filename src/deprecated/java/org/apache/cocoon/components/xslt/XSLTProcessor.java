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
package org.apache.cocoon.components.xslt;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Source;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.XMLFilter;

import javax.xml.transform.Result;
import javax.xml.transform.sax.TransformerHandler;

/**
 * This is the interface of the XSLT processor in Cocoon.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: XSLTProcessor.java,v 1.3 2004/03/05 13:02:41 bdelacretaz Exp $
 * @version 1.0
 * @since   July 11, 2001
 */
public interface XSLTProcessor extends Component
{
  /**
   * The role implemented by an <code>XSLTProcessor</code>.
   */
  String ROLE = XSLTProcessor.class.getName();

  /**
   * The default factory identifier. (simply used as a pointer, the actual
   * content is not meaningful)
   */
  String DEFAULT_FACTORY = "default";

  /**
   * Set the {@link org.apache.cocoon.environment.SourceResolver} for
   * this instance. The <code>resolver</code> is invoked to return a
   * <code>Source</code> object, given an HREF.
   *
   * @deprecated The processor can now simply lookup the source resolver.
   * @param resolver a <code>SourceResolver</code> value
   */
  void setSourceResolver(SourceResolver resolver);

  /**
   * Set the TransformerFactory for this instance.
   * The <code>factory</code> is invoked to return a
   * <code>TransformerHandler</code> to perform the transformation.
   *
   * @param classname the name of the class implementing
   * <code>TransformerFactory</code> value. If an error is found
   * or the indicated class doesn't implement the required interface
   * the original factory of the component is maintained.
   */
  void setTransformerFactory(String classname);

  /**
   * <p>Return a <code>TransformerHandler</code> for a given
   * stylesheet <code>Source</code>. This can be used in a pipeline to
   * handle the transformation of a stream of SAX events. See {@link
   * org.apache.cocoon.transformation.TraxTransformer#setConsumer(org.apache.cocoon.xml.XMLConsumer)}
   * for an example of how to use this method.
   *
   * <p>The additional <code>filter</code> argument, if it's not
   * <code>null</code>, is inserted in the chain SAX events as an XML
   * filter during the parsing or the source document.
   *
   * <p>This method caches the Source object and performs a reparsing
   * only if this changes.
   *
   * @param stylesheet a <code>Source</code> value
   * @param filter a <code>XMLFilter</code> value
   * @return a <code>TransformerHandler</code> value
   * @exception ProcessingException if an error occurs
   */
  TransformerHandler getTransformerHandler(Source stylesheet,
                                           XMLFilter filter)
    throws ProcessingException;

  /**
   * Same as {@link #getTransformerHandler(Source,XMLFilter)}, with
   * <code>filter</code> set to <code>null</code> and <code>factory</code>
   * set to <code>null</code>.
   *
   * @param stylesheet a <code>Source</code> value
   * @return a <code>TransformerHandler</code> value
   * @exception ProcessingException if an error occurs
   * @see org.apache.cocoon.transformation.TraxTransformer#setConsumer(org.apache.cocoon.xml.XMLConsumer)
   */
  TransformerHandler getTransformerHandler(Source stylesheet)
    throws ProcessingException;

  /**
   * Applies an XSLT stylesheet to an XML document. The source and
   * stylesheet documents are specified as <code>Source</code>
   * objects. The result of the transformation is placed in
   * <code>result</code>, which should be properly initialized before
   * invoking this method. Any additional parameters passed in
   * <code>params</code> will become arguments to the stylesheet.
   *
   * @param source a <code>Source</code> value
   * @param stylesheet a <code>Source</code> value
   * @param params a <code>Parameters</code> value
   * @param result a <code>Result</code> value
   * @exception ProcessingException if an error occurs
   */
  void transform(Source source, Source stylesheet, Parameters params,
                 Result result)
    throws ProcessingException;
}
