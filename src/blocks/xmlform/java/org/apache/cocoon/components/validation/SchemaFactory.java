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
package org.apache.cocoon.components.validation;

import org.xml.sax.InputSource;

/**
 * Responsible for creating new instances of Schemas
 * for different Schema languages.
 *
 * @author  ivelin@apache.org
 * @version CVS $Id: SchemaFactory.java,v 1.3 2004/03/05 13:02:37 bdelacretaz Exp $
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
            return new org.apache.cocoon.components.validation.schematron.SchematronFactory();
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
