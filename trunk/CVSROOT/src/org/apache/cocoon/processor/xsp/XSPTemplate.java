/*-- $Id: XSPTemplate.java,v 1.4 2000-01-08 13:03:45 stefano Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
 
 package org.apache.cocoon.processor.xsp;

import java.util.*;
import org.w3c.dom.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.transformer.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.processor.xsp.language.*;

/**
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version $Revision: 1.4 $ $Date: 2000-01-08 13:03:45 $
 */
public class XSPTemplate {
  protected String languageName;
  protected Document stylesheet;
  protected XSPPreprocessor preprocessor;
  
  private Transformer transformer;
  private Parser parser;

  public XSPTemplate(Transformer transformer, Parser parser) {
    this.transformer = transformer;
    this.parser = parser;
  }

  public void setLanguageName(String languageName) {
    this.languageName = languageName;
  }

  public void setStylesheet(Document stylesheet) {
    this.stylesheet = stylesheet;
  }

  public void setPreprocessor(XSPPreprocessor preprocessor) {
    this.preprocessor = preprocessor;
  }

  public String getLanguageName() {
    return this.languageName;
  }

  public Document getStylesheet() {
    return this.stylesheet;
  }

  public XSPPreprocessor getPreprocessor() {
    return this.preprocessor;
  }

  public Document apply(Document document, Dictionary parameters)
    throws Exception
  {
    if (this.preprocessor != null) {
      document = this.preprocessor.preprocess(document, parameters);
    }

/*
System.err.println(XSPUtil.toMarkup(this.transformer.transform(document, stylesheet, this.parser.createEmptyDocument())));
*/

    // FIXME: we should change these nulls to something meaningful to allow
    // the transformers to do includes and imports.
    return this.transformer.transform(document, null, stylesheet, null, this.parser.createEmptyDocument());
  }
}
