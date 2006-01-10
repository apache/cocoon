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
package org.apache.cocoon.template.script;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.instruction.Instruction;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.commons.lang.ClassUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id: DefaultInstructionFactory.java 169615 2005-05-11 09:57:41Z
 *          lgawron $
 */
public class DefaultInstructionFactory extends AbstractLogEnabled implements ThreadSafe, Serviceable, Configurable,
        InstructionFactory {
    private Map instructions = new HashMap();
    private ServiceManager manager;
    private final static Class[] INSTRUCTION_CONSTRUCTOR_PARAMS = new Class[] { ParsingContext.class,
            StartElement.class, Attributes.class, Stack.class };
    private final static String CONFIG_LOCATION = "resource://org/apache/cocoon/template/template-instructions.xml";

    private void registerInstruction(String instructionName, String targetNamespace, String className)
            throws ConfigurationException {
        Class clazz;
        try {
            clazz = Class.forName(className);
            if (!ClassUtils.isAssignable(clazz, Instruction.class))
                throw new ConfigurationException("Class '" + className + "' is not assignable to "
                        + "o.a.c.template.jxtg.script.event.StartInstruction ");
            Constructor constructor = clazz.getConstructor(INSTRUCTION_CONSTRUCTOR_PARAMS);

            String instructionKey = instructionKey(instructionName, targetNamespace);
            this.instructions.put(instructionKey, constructor);
        } catch (Exception e) {
            if (e instanceof ConfigurationException)
                throw (ConfigurationException) e;
            else
                throw new ConfigurationException("unable to register instruction", e);
        }
    }

    private String instructionKey(String instructionName, String targetNamespace) {
        return "{" + targetNamespace + "}" + instructionName;
    }

    private String instructionKey(StartElement element) {
        return instructionKey(element.getLocalName(), element.getNamespaceURI());
    }

    public boolean isInstruction(StartElement element) {
        String instructionKey = instructionKey(element);
        return this.instructions.containsKey(instructionKey);
    }

    public Instruction createInstruction(ParsingContext parsingContext, StartElement startElement, Attributes attrs, Stack stack) throws SAXException {
        String instructionKey = instructionKey(startElement);
        Constructor constructor = (Constructor) this.instructions.get(instructionKey);
        if (constructor == null)
            throw new SAXParseException("unrecognized instruction: " + instructionKey, startElement.getLocation(), null);
        Object[] arguments = new Object[] { parsingContext, startElement, attrs, stack };
        try {
            return (Instruction) constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new SAXParseException("error creating instruction: " + instructionKey, startElement.getLocation(), e);
        }
    }

    public void setupInstructions(Configuration conf) throws ConfigurationException {
        Configuration[] instructionSets = conf.getChildren("instructions");
        for (int i = 0; i < instructionSets.length; i++) {
            Configuration instructionSet = instructionSets[i];
            String namespace = instructionSet.getAttribute("targetNamespace", "");

            Configuration[] instr = instructionSet.getChildren("instruction");
            for (int j = 0; j < instr.length; j++) {
                Configuration currentInstruction = instr[j];
                String name = currentInstruction.getAttribute("name");
                if (name == null)
                    throw new ConfigurationException("@name for instruction required");

                String className = currentInstruction.getAttribute("class");
                if (className == null)
                    throw new ConfigurationException("@class for instruction required");

                registerInstruction(name, namespace, className);
            }
        }
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public void configure(Configuration omitted) throws ConfigurationException {
        SourceResolver resolver = null;
        Source source = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(CONFIG_LOCATION);
            DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder();
            Configuration conf = configurationBuilder.build(source.getInputStream());
            setupInstructions(conf);
        } catch (Exception e) {
            if (e instanceof ConfigurationException)
                throw (ConfigurationException) e;
            else
                throw new ConfigurationException("unable to parse template instructions configuration", e);
        } finally {
            if (source != null)
                resolver.release(source);
            if (resolver != null)
                this.manager.release(resolver);
        }
    }
}
