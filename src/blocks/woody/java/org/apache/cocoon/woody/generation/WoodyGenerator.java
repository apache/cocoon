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
package org.apache.cocoon.woody.generation;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.cocoon.woody.transformation.WoodyPipeLineConfig;
import org.xml.sax.SAXException;

/**
 * A generator that streams an XML representation of a Woody {@link Form}. This will
 * recursively contain the XML for all widgets on the form. This can then be styled
 * using an XSLT.
 *
 * <p>An alternative approach that requires less (or even none) XSLT work is offered by
 * the {@link org.apache.cocoon.woody.transformation.WoodyTemplateTransformer WoodyTemplateTransformer}.
 *
 * <p>The Form whose XML should be produced should reside either 
 * <ol><li> In a request attribute, whose name should be provided to this 
 * generator as a sitemap parameter called "attribute-name".</li>
 * <li> Or else at its default-location in the flow context-object.</li>
 * </ol>
 */
public class WoodyGenerator extends AbstractGenerator {
    
    protected Form form;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        
        WoodyPipeLineConfig config = WoodyPipeLineConfig.createConfig(objectModel, parameters);
        form = config.findForm();     
    }

    public void recycle() {
        super.recycle();
        form = null;
    }

    public void generate() throws IOException, SAXException, ProcessingException {
        contentHandler.startDocument();
        contentHandler.startPrefixMapping(Constants.WI_PREFIX, Constants.WI_NS);
        form.generateSaxFragment(contentHandler, Locale.US);
        contentHandler.endPrefixMapping(Constants.WI_PREFIX);
        contentHandler.endDocument();
    }
}
