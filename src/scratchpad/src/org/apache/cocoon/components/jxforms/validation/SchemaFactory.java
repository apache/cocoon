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
package org.apache.cocoon.components.jxforms.validation;

import org.xml.sax.InputSource;

/**
 * Responsible for creating new instances of Schemas
 * for different Schema languages.
 *
 * @author  ivelin@apache.org
 * @version CVS $Id: SchemaFactory.java,v 1.1 2003/04/27 08:28:51 coliver Exp $
 */
public abstract class SchemaFactory {

    public static String NAMESPACE_SCHEMATRON = "http://www.ascc.net/xml/schematron";

    /** Creates a new instance of ValidatorFactory */
    public SchemaFactory() {
    }

    /**
     * This method creates an instance of a ValidatorFactory
     * using the JDK 1.3 META-INF/services mechanism.
     * The idea is borrowed from JARV
     * http://iso-relax.sourceforge.net/apiDoc/org/iso_relax/verifier/VerifierFactory.html
     *
     * @param ns the namespace of the schema language
     * @return ValidatorFactory
     * @throws InstantiationException when a factory could not be created
     */
    public static SchemaFactory lookup(String ns)
      throws InstantiationException {
        // currently hardcoded implementation for Schematron
        // until another schema validator is implemented

        /* TODO: create SchematronValidatorFactory */

        if (ns.equals(NAMESPACE_SCHEMATRON)) {
            return new org.apache.cocoon.components.jxforms.validation.schematron.SchematronFactory();
        }
        return null;
    }

    /**
     * Loads and compiles a Schema instance
     *
     * @param is         
     * @return Schema the compiled schema instance
     * @throws InstantiationException when the Schema could not be loaded or compiled
     */
    public abstract Schema compileSchema(InputSource is)
      throws InstantiationException;
}
