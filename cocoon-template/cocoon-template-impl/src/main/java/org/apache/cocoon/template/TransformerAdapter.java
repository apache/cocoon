/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.template;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.StringTemplateParser;
import org.apache.cocoon.template.script.InstructionFactory;
import org.apache.cocoon.template.script.Parser;
import org.apache.cocoon.transformation.ServiceableTransformer;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.SAXException;

/**
 * Adapter that makes this generator usable as a transformer (Note there is a
 * performance penalty for this however: you effectively recompile the template
 * for every instance document)
 * 
 * @version SVN $Id$
 */
public class TransformerAdapter extends ServiceableTransformer {
    static class TemplateConsumer extends Parser implements XMLConsumer, Serviceable, Disposable {
        private ServiceManager manager;

        public TemplateConsumer() {
            this.gen = new JXTemplateGenerator();
        }

        public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
                throws ProcessingException, SAXException, IOException {
            this.gen.setup(resolver, objectModel, null, parameters);
        }

        public void service(ServiceManager manager) throws ServiceException {
            this.manager = manager;
            this.gen.service(manager);
            setParsingContext(new ParsingContext((StringTemplateParser) this.manager.lookup(StringTemplateParser.ROLE),
                    (InstructionFactory) this.manager.lookup(InstructionFactory.ROLE)));
        }

        public void dispose() {
            setParsingContext( null );
            this.manager.release(this.parsingContext.getInstructionFactory());
            this.manager.release(this.parsingContext.getStringTemplateParser());
        }

        public void endDocument() throws SAXException {
            super.endDocument();
            gen.performGeneration(getStartEvent(), null);
        }

        void setConsumer(XMLConsumer consumer) {
            gen.setConsumer(consumer);
        }

        protected void recycle() {
            super.recycle();
            gen.recycle();
        }

        JXTemplateGenerator gen;
    }

    TemplateConsumer templateConsumer = new TemplateConsumer();

    public void recycle() {
        super.recycle();
        templateConsumer.recycle();
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);
        templateConsumer.setup(resolver, objectModel, src, parameters);
    }

    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        templateConsumer.service(manager);
    }

    public void dispose() {
        templateConsumer.dispose();
        super.dispose();
    }

    public void setConsumer(XMLConsumer xmlConsumer) {
        super.setConsumer(templateConsumer);
        templateConsumer.setConsumer(xmlConsumer);
    }
}
