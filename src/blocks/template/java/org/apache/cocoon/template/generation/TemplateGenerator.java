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
package org.apache.cocoon.template.generation;

import java.io.IOException;
import java.util.HashMap;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.el.ExpressionCompiler;
import org.apache.cocoon.el.GenericExpressionCompiler;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.template.script.Script;
import org.apache.cocoon.template.script.ScriptCompiler;
import org.apache.cocoon.template.script.ScriptContext;
import org.apache.cocoon.template.script.ScriptInvoker;
import org.apache.cocoon.template.script.TagRepository;
import org.apache.cocoon.util.IncludingConfiguration;
import org.apache.excalibur.source.Source;
import org.xml.sax.SAXException;

public class TemplateGenerator extends ServiceableGenerator implements
        Configurable {
    static HashMap cache = new HashMap();

    TagRepository tagRepository = new TagRepository();

    ExpressionCompiler expressionCompiler = GenericExpressionCompiler
            .getInstance();

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        tagRepository.enableLogging(logger);
    }

    public void configure(Configuration conf) throws ConfigurationException {
        IncludingConfiguration newConf = new IncludingConfiguration(conf,
                manager);
        tagRepository.configure(newConf);
    }

    public TagRepository getTagRepository() {
        return tagRepository;
    }

    public void generate() throws IOException, SAXException,
            ProcessingException {
        try {
            Source inputSource = resolver.resolveURI(source);

            Script script = null;
            synchronized (cache) {
                script = (Script) cache.get(inputSource.getURI());
                if (script == null) {
                    script = compileScript(inputSource);
                    cache.put(inputSource.getURI(), script);
                }
            }

            ScriptContext context = new ScriptContext();
            context.setConsumer(xmlConsumer);

            // TODO: populate context

            ScriptInvoker scriptInvoker = new ScriptInvoker(script, context);
            scriptInvoker.invoke();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Script compileScript(Source inputSource) throws Exception {
        ScriptCompiler compiler = new ScriptCompiler(tagRepository,
                expressionCompiler);
        SourceUtil.parse(manager, inputSource, compiler);
        return compiler.getScript();
    }
}