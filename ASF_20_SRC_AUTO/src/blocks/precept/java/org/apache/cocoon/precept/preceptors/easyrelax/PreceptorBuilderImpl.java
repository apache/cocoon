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
package org.apache.cocoon.precept.preceptors.easyrelax;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParserFactory;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.precept.Constraint;
import org.apache.cocoon.precept.Preceptor;
import org.apache.cocoon.precept.preceptors.PreceptorBuilder;
import org.apache.cocoon.xml.AbstractXMLConsumer;
import org.apache.cocoon.xml.EmbeddedXMLPipe;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Feb 23, 2002
 * @version CVS $Id: PreceptorBuilderImpl.java,v 1.6 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public class PreceptorBuilderImpl extends AbstractXMLConsumer
        implements PreceptorBuilder, Serviceable, Disposable, Poolable {
    //public final static String ROLE = "org.apache.cocoon.precept.PreceptorBuilderImpl";

    public final static Attributes NOATTR = new AttributesImpl();

    public final static String NS = "http://www.dff.st/ns/desire/easyrelax/grammar/1.0";

    private ServiceManager manager;

    private SAXConfigurationHandler configurationHandler;
    private PreceptorImpl preceptor;
    private Stack environments;
    private Environment environment;
    private List constraints;
    private StringBuffer text;
    private ElementPreceptorNode root;
    private ElementPreceptorNode currentElement;
    private AttributePreceptorNode currentAttribute;
    private String constraintAliasType;
    private String constraintType;
    private String constraintName;
    private String constraintContext;
//    private String includeUri;
    private StringBuffer currentPath;
    private Map constraintAliases;
    private XMLByteStreamInterpreter xmli;
    private ConstraintFactory constraintFactory = new ConstraintFactory();

//    private boolean define;

    private ContentHandler redirect;
    private int redirectLevel;

    public Preceptor getPreceptor() {
        return (preceptor);
    }

    public Preceptor buildPreceptor(String url) throws Exception {

        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);

            Source source = resolver.resolveURI(url);
            parse(source.getInputStream());
        }
        finally {
            manager.release(resolver);
        }

        return (preceptor);
    }

    public void parse(URL url) throws Exception {
        parse(new InputStreamReader((BufferedInputStream) url.getContent()));
    }

    public void parse(InputStream input) throws Exception {
        parse(new InputStreamReader(input));
    }

    public void parse(String xmlstring) {
        StringReader reader = new StringReader(xmlstring);
        parse(reader);
    }

    public void parse(Reader reader) {
        try {
            //FIXME: use parser component
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            spf.setNamespaceAware(true);
            XMLReader parser = spf.newSAXParser().getXMLReader();
            parser.setContentHandler(this);
            //parser.setErrorHandler(this);
            InputSource source = new InputSource(reader);
            parser.parse(source);
        }
        catch (Exception e) {
            getLogger().error("", e);
        }
    }


    public void startDocument() throws SAXException {
        currentPath = new StringBuffer();
        preceptor = new PreceptorImpl();
        preceptor.enableLogging(getLogger());
        text = new StringBuffer();
        environments = new Stack();
        environment = new Environment();
        constraints = new ArrayList();
        constraintAliases = new HashMap();
        redirect = null;
        redirectLevel = 0;
//        define = false;
        xmli = new XMLByteStreamInterpreter();
        xmli.setContentHandler(new EmbeddedXMLPipe(this));
    }

    public void endDocument() throws SAXException {
        text = null;
        constraints = null;
        environments = null;
        environment = null;
        constraints = null;
        redirect = null;
    }

    public void startElement(String ns, String name, String raw, Attributes attributes) throws SAXException {
        if (redirect != null) {
            redirectLevel++;
            getLogger().debug("saving [start." + String.valueOf(name) + "] into config");
            redirect.startElement(ns, name, raw, attributes);
        }
        else {
            text.setLength(0);

            if (NS.equals(ns)) {
                if ("grammar".equals(name)) {
                }
                else if ("include".equals(name)) {
//                    includeUri = attributes.getValue("uri");
                }
                else if ("define".equals(name)) {
//                    define = true;
                }
                else if ("start".equals(name)) {
                }
                else if ("occurrs".equals(name)) {
                    environments.push(environment);
                    environment = new Environment();

                    try {
                        environment.minOcc = Integer.parseInt(String.valueOf(attributes.getValue("min")));
                        environment.maxOcc = Integer.parseInt(String.valueOf(attributes.getValue("max")));
                    }
                    catch (NumberFormatException e) {
                        throw new SAXException("min/max must be a valid number", e);
                    }
                }
                else if ("optional".equals(name)) {
                    environments.push(environment);
                    environment = new Environment();

                    environment.minOcc = 0;
                    environment.maxOcc = 1;
                }
                else if ("element-alias".equals(name)) {
                }
                else if ("element".equals(name)) {
                    String nameAttr = attributes.getValue("name");
                    if (root != null) {
                        currentElement = currentElement.addElement(nameAttr, environment.minOcc, environment.maxOcc, null);
                        currentPath.append("/");
                        environment.len = nameAttr.length() + 1;
                    }
                    else {
                        root = new ElementPreceptorNode(preceptor, null, nameAttr, 1, 1);
                        currentElement = root;
                        environment.len = nameAttr.length();
                    }
                    currentPath.append(nameAttr);
                    preceptor.index.put(currentPath.toString(), currentElement);
                    getLogger().debug("creating index [" + String.valueOf(currentPath) + "]");

                    environments.push(environment);
                    environment = new Environment();
                }
                else if ("attribute".equals(name)) {
                    String nameAttr = attributes.getValue("name");
                    currentAttribute = currentElement.addAttribute(nameAttr, environment.minOcc > 0, null);

                    String path = currentPath.toString() + "/@" + nameAttr;
                    preceptor.index.put(path, currentElement);

                    getLogger().debug("creating index [" + String.valueOf(path) + "]");
                }
                else if ("constraint-alias".equals(name)) {
                    constraintAliasType = attributes.getValue("type");
                }
                else if ("constraint".equals(name)) {
                    constraintType = attributes.getValue("type");
                    constraintName = attributes.getValue("name");
                    constraintContext = attributes.getValue("context");

                    configurationHandler = new SAXConfigurationHandler();


                    configurationHandler.startElement("", "constraint", "constraint", new AttributesImpl(attributes));
                    redirect = configurationHandler;
                }
                else if ("value".equals(name)) {
                }
                else if ("value-of".equals(name)) {
                }
                else {
                    throw new SAXException("unknown element " + String.valueOf(name));
                }
            }
            else {
                throw new SAXException("only elements in namespace " + NS + " are supported");
            }
        }
    }

    public void endElement(String ns, String name, String raw) throws SAXException {
        if (redirect != null) {
            if (--redirectLevel < 0) {
                redirect = null;
                redirectLevel = 0;
            }
            else {
                getLogger().debug("saving [end." + String.valueOf(name) + "] into config");
                redirect.endElement(ns, name, raw);
            }
        }

        if (redirect == null) {
            if (NS.equals(ns)) {
                if ("grammar".equals(name)) {
                }
                else if ("include".equals(name)) {
                }
                else if ("define".equals(name)) {
//                    define = false;
                }
                else if ("start".equals(name)) {
                }
                else if ("occurrs".equals(name)) {
                    environment = (Environment) environments.pop();
                }
                else if ("optional".equals(name)) {
                    environment = (Environment) environments.pop();
                }
                else if ("element-alias".equals(name)) {
                }
                else if ("element".equals(name)) {
                    if (!constraints.isEmpty()) {
                        getLogger().debug("adding " + constraints.size() + " constrain(s) to element [" + currentElement.getName() + "]");
                        currentElement.addConstraints(constraints);
                        constraints.clear();
                    }

                    currentElement = currentElement.getParent();
                    environment = (Environment) environments.pop();
                    currentPath.setLength(currentPath.length() - environment.len);
                }
                else if ("attribute".equals(name)) {
                    if (!constraints.isEmpty()) {
                        getLogger().debug("adding " + constraints.size() + " constrain(s) to attribute [" + currentAttribute.getName() + "]");
                        currentAttribute.addConstraints(constraints);
                        constraints.clear();
                    }
                }
                else if ("constraint-alias".equals(name)) {
                    if (!constraints.isEmpty()) {
                        getLogger().debug("registering local constraint alias [" + String.valueOf(constraintAliasType) + "] with " + constraints.size() + " constraint(s)");
                        constraintAliases.put(constraintAliasType, new ArrayList(constraints));
                        constraints.clear();
                    }
                }
                else if ("constraint".equals(name)) {
                    configurationHandler.endElement("", "constraint", "constraint");

                    if (constraintAliases.containsKey(constraintType)) {
                        List aliasConstraints = (List) constraintAliases.get(constraintType);
                        int i = 1;
                        for (Iterator it = aliasConstraints.iterator(); it.hasNext(); i++) {
                            Constraint constraint = (Constraint) it.next();
                            getLogger().debug("new alias constraint " + (constraints.size() + i) + ". " + String.valueOf(constraint.getType()) + "[" + String.valueOf(constraint) + "]");
                        }
                        constraints.addAll(aliasConstraints);
                    }
                    else {
                        Constraint constraint = constraintFactory.createConstraintInstance(constraintType, constraintName, constraintContext, configurationHandler.getConfiguration());

                        if (constraint instanceof LogEnabled) {
                            ((LogEnabled) constraint).enableLogging(getLogger());
                        }

                        if (constraint instanceof Configurable) {
                            try {
                                ((Configurable) constraint).configure(configurationHandler.getConfiguration());
                            }
                            catch (Throwable t) {

                                getLogger().error("", t);

                            }
                        }

                        if (constraint != null) {
                            getLogger().debug("new simple constraint " + (constraints.size() + 1) + ". " + String.valueOf(constraint.getType()) + "[" + String.valueOf(constraint) + "]");
                            constraints.add(constraint);
                        }
                        else {
                            throw new SAXException("could not create constraint " + String.valueOf(constraintType));
                        }
                    }
                    configurationHandler = null;
                }
                else if ("value".equals(name)) {
                }
                else if ("value-of".equals(name)) {
                }
            }
            else {
                throw new SAXException("only elements in namespace " + NS + " are supported");
            }
        }
    }

    public void characters(char[] chars, int start, int len) throws SAXException {
        if (redirect != null) {
            getLogger().debug("saving [" + new String(chars, start, len) + "] into config");
            redirect.characters(chars, start, len);
        }
        else {
            text.append(chars, start, len);
        }
    }

    private class Environment {
        int minOcc = 1;
        int maxOcc = 1;
        int len = 0;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        //this.constraintFactory = (ConstraintFactory) manager.lookup(ConstraintFactory.ROLE);
        //this.preceptorRepository = (PreceptorRepository) manager.lookup(PreceptorRepository.ROLE);
    }

    public void dispose() {
        //this.manager.release(preceptorRepository);
        //this.manager.release(constraintFactory);
    }
}
