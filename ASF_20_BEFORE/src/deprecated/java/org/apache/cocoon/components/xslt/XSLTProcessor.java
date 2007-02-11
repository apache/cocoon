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
 * @version CVS $Id: XSLTProcessor.java,v 1.2 2004/02/19 22:13:28 joerg Exp $
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
