/* ========================================================================== *
 * Copyright (C) 2004-2005 Pier Fumagalli <http://www.betaversion.org/~pier/> *
 *                            All rights reserved.                            *
 * ========================================================================== *
 *                                                                            *
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.       *
 *                                                                            *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software *
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT *
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the *
 * License for the  specific language  governing permissions  and limitations *
 * under the License.                                                         *
 *                                                                            *
 * ========================================================================== */

package org.apache.cocoon.components.validation.jaxp;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.components.validation.impl.AbstractSchemaParser;
import org.apache.excalibur.source.Source;
import org.xml.sax.SAXException;

/**
 * <p>TODO: ...</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class JaxpSchemaParser extends AbstractSchemaParser
implements Configurable, ThreadSafe {

    private String className = null;
    private String[] grammars = null;

    public void configure(Configuration conf)
    throws ConfigurationException {
        this.className = conf.getChild("factory-class").getValue();
        final SchemaFactory fact;
        try {
            fact = (SchemaFactory) Class.forName(this.className).newInstance();
        } catch (Exception exception) {
            String message = "Unable to instantiate factory " + className;
            throw new ConfigurationException(message, conf, exception);
        }

        /* Detect languages or use the supplied ones */
        Configuration languages[] = conf.getChild("grammars").getChildren("grammar");
        Set grammars = new HashSet();
        if (languages.length > 0) {

            /* If the configuration specified (formally) a list of grammars use it */
            for (int x = 0; x < languages.length; x++) {
                grammars.add(languages[x].getValue());
            }

        } else {

            /* Attempt to detect the languages directly using the JAXP factory */
            if (fact.isSchemaLanguageSupported(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                grammars.add(Validator.GRAMMAR_XML_SCHEMA);
            }
            if (fact.isSchemaLanguageSupported(XMLConstants.RELAXNG_NS_URI)) {
                grammars.add(Validator.GRAMMAR_RELAX_NG);
            }
            if (fact.isSchemaLanguageSupported(XMLConstants.XML_DTD_NS_URI)) {
                grammars.add(Validator.GRAMMAR_XML_DTD);
            }
        }
        
        /* Store our grammars */
        this.grammars = (String[]) grammars.toArray(new String[grammars.size()]);
    }

    public Schema parseSchema(Source source, String grammar)
    throws SAXException, IOException {
        throw new UnsupportedOperationException();
    }

    public String[] getSupportedGrammars() {
        return this.grammars;
    }
}
