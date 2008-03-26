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
package org.apache.cocoon.template;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.SAXException;

/**
 * Adapter that makes this generator usable as a transformer (Note there is a performance penalty
 * for this however: you effectively recompile the template for every instance document)
 *
 * @cocoon.sitemap.component.documentation
 * Adapter that makes this generator usable as a transformer (Note there is a performance penalty
 * for this however: you effectively recompile the template for every instance document)
 *
 * @version $Id$
 */
public class TransformerAdapter extends AbstractTransformer {
    private TemplateConsumer templateConsumer;

    public TemplateConsumer getTemplateConsumer() {
        return templateConsumer;
    }

    public void setTemplateConsumer(TemplateConsumer templateConsumer) {
        this.templateConsumer = templateConsumer;
    }

    /**
     * @see org.apache.cocoon.transformation.ServiceableTransformer#setup(org.apache.cocoon.environment.SourceResolver,
     *      java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        templateConsumer.setup(resolver, objectModel, src, parameters);
    }

    /**
     * @see org.apache.cocoon.xml.AbstractXMLProducer#setConsumer(org.apache.cocoon.xml.XMLConsumer)
     */
    public void setConsumer(XMLConsumer xmlConsumer) {
        super.setConsumer(templateConsumer);
        templateConsumer.setConsumer(xmlConsumer);
    }

}
