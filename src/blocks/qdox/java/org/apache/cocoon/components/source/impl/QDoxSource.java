/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.source.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.AbstractJavaEntity;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.Type;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.serialization.XMLSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.AbstractSource;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Source implementation for XML Javadoc.
 *
 * @author <a href="mailto:b.guijt1@chello.nl">Bart Guijt</a>
 * @version CVS $Id: QDoxSource.java,v 1.8 2004/04/03 00:46:33 antonio Exp $ $Date: 2004/04/03 00:46:33 $
 */
public final class QDoxSource
    extends AbstractSource
    implements XMLizable, Recyclable {

    protected final static String ROOT_CLASSNAME = "java.lang.Object";

    protected final static String EMPTY = "";
    protected final static String PROTOCOL = "qdox:";
    protected final static String NS_URI = "http://apache.org/cocoon/javadoc/1.0";
    protected final static String NS_PREFIX = "jd";
    protected final static String ATTR_TYPE = "NMTOKEN";
    protected final static Attributes EMPTY_ATTRS = new AttributesImpl();

    protected final static String CLASS_ELEMENT = "class";
    protected final static String CLASSNAME_ATTRIBUTE = "name";
    protected final static String PACKAGE_ATTRIBUTE = "package";
    protected final static String QNAME_ATTRIBUTE = "qname";
    protected final static String INHERIT_ELEMENT = "inherit";
    protected final static String INNERCLASSES_ELEMENT = "innerclasses";
    protected final static String NESTED_IN_ELEMENT = "nested-in";
    protected final static String IMPORTS_ELEMENT = "imports";
    protected final static String IMPORT_ELEMENT = "import";
    protected final static String IMPORT_ATTRIBUTE = "type";
    protected final static String IMPLEMENTS_ELEMENT = "implements";
    protected final static String INTERFACE_ELEMENT = "interface";
    protected final static String MODIFIERS_ELEMENT = "modifiers";
    protected final static String COMMENT_ELEMENT = "comment";
    protected final static String LINK_ELEMENT = "link";
    protected final static String LINK_CLASS_ATTRIBUTE = "class";
    protected final static String LINK_MEMBER_ATTRIBUTE = "member";
    protected final static String HREF_ATTRIBUTE = "uri";
    protected final static String TAGS_ELEMENT = "tags";
    protected final static String FIELDS_ELEMENT = "fields";
    protected final static String FIELD_ELEMENT = "field";
    protected final static String CONSTRUCTORS_ELEMENT = "constructors";
    protected final static String CONSTRUCTOR_ELEMENT = "constructor";
    protected final static String METHODS_ELEMENT = "methods";
    protected final static String METHOD_ELEMENT = "method";
    protected final static String NAME_ATTRIBUTE = "name";
    protected final static String TYPE_ATTRIBUTE = "type";
    protected final static String DIMENSIONS_ATTRIBUTE = "dimensions";
    protected final static String SIGNATURE_ATTRIBUTE = "signature";
    protected final static String PARAMETERS_ELEMENT = "parameters";
    protected final static String PARAMETER_ELEMENT = "parameter";
    protected final static String THROWS_ELEMENT = "throws";
    protected final static String EXCEPTION_ELEMENT = "exception";

    protected final static int CONSTRUCTOR_MODE = 1;
    protected final static int METHOD_MODE = 2;

    protected final static int CLASS_INHERITANCE = 1;
    protected final static int INTERFACE_INHERITANCE = 2;
    protected final static int INNERCLASS_INHERITANCE = 3;
    protected final static int FIELD_INHERITANCE = 4;
    protected final static int CONSTRUCTOR_INHERITANCE = 5;
    protected final static int METHOD_INHERITANCE = 6;

    protected ServiceManager manager;
    protected Logger logger;

    protected Source javaSource;
    protected String javadocUri;
    protected String javadocClassName;
    protected JavaClass javadocClass;
    protected JavaClass containingJavadocClass;  // in case javadocClass is an inner class
    protected Map classMap;

    /**
     * This RegExp matches the <code>{</code><code>@link &hellip;}</code> occurrances in
     * Javadoc comments.
     */
    protected RE reLink;

    /**
     * Contains a regular expression to match the <code>{</code><code>@link &hellip;}</code> occurrances.
     *
     * <p>The following <code>{</code><code>@link &hellip;}</code> literals are recognized:</p>
     *
     * <ul>
     * <li><code>{</code><code>@link HashMap}</code> - returns 'Hashmap' with <code>reLink.getParen(6)</code>;</li>
     * <li><code>{</code><code>@link #equals(java.lang.Object) equals(&hellip;)}</code> - returns '#equals(java.lang.Object)' with <code>reLink.getParen(2)</code>
     *   and 'equals(&hellip;)' with <code>reLink.getParen(5)</code>;</li>
     * <li><code>{</code><code>@link #indexOf(char, int) indexOf(&hellip;)}</code> - returns '#indexOf(char, int)' with <code>reLink.getParen(2)</code>
     *   and 'indexOf(&hellip;)' with <code>reLink.getParen(5)</code>.</li>
     * </ul>
     * <p>The regexp is as follows:</p>
     * <code>\{@link\s+((([\w.#,$&amp;;\s]+)|([\w.#,$&amp;;(\s]+[\w.#,$&amp;;)\s]+))\s+([\w()#.,$&amp;;\s]+)|([\w.#,$&amp;;\s()]+))\s*\}</code>
     *
     * @see #reLink
     */
    protected final static String RE_LINK = "\\{@link\\s+((([\\w.#,$&;\\s]+)|([\\w.#,$&;(\\s]+[\\w.#,$&;)\\s]+))\\s+([\\w()#.,$&;\\s]+)|([\\w.#,$&;\\s()]+))\\s*\\}";

    /**
     * Constructor for QDoxSource.
     *
     * @param location
     * @param javaSource
     * @param logger
     * @param manager
     */
    public QDoxSource(String location, Source javaSource, Logger logger, ServiceManager manager) {
        this.javadocUri = location;
        this.javaSource = javaSource;
        this.logger = logger;
        this.manager = manager;

        this.javadocClassName = javadocUri.substring(javadocUri.indexOf(':') + 1);

        try {
            createJavadocXml();
        } catch (SourceException se) {
            logger.error("Error reading source!", se);
        } catch (IOException ioe) {
            logger.error("Error reading source!", ioe);
        }

        // Initialize regular expression:
        try {
            reLink = new RE(RE_LINK);
        } catch (RESyntaxException rse) {
            logger.error("Regular Expression syntax error!", rse);
        }
    }

    /**
     * Returns the parsed Java class.
     *
     * @return JavaClass
     */
    public JavaClass getJavadocClass() {
        return javadocClass;
    }

    /**
     * @see XMLizable#toSAX(org.xml.sax.ContentHandler)
     * @throws SAXException if any error occurs during SAX outputting.
     */
    public void toSAX(ContentHandler handler) throws SAXException {
        if (javadocClass == null) {
            logger.error("No classfile loaded! Cannot output SAX events.");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Outputting SAX events for class " + javadocClass.getFullyQualifiedName());
            logger.debug("  #fields: " + javadocClass.getFields().length);
            logger.debug("  #methods and constructors: " + javadocClass.getMethods().length);
        }

        // Output SAX 'header':
        handler.startDocument();
        handler.startPrefixMapping(NS_PREFIX, NS_URI);

        // Output class-level element:
        outputClassStartElement(handler, javadocClass);

        // Modifiers:
        outputModifiers(handler, javadocClass);

        // Imports:
        JavaSource parent = javadocClass.getParentSource();
        // Add two implicit imports:
        parent.addImport("java.lang.*");
        if (parent.getPackage().length() > 0) {
            parent.addImport(parent.getPackage() + ".*");
        } else {
            parent.addImport("*");
        }
        String[] imports = parent.getImports();

        saxStartElement(handler, IMPORTS_ELEMENT);
        for (int i = 0; i < imports.length; i++) {
            if (imports[i].endsWith("*")) {
                // package import:
                saxStartElement(handler, IMPORT_ELEMENT, new String[][] {{IMPORT_ATTRIBUTE, "package"}});
                String imp = imports[i];
                while (imp.endsWith("*") || imp.endsWith(".")) {
                    imp = StringUtils.chop(imp);
                }
                saxCharacters(handler, imp);
            } else {
                saxStartElement(handler, IMPORT_ELEMENT, new String[][] {{IMPORT_ATTRIBUTE, "class"}});
                saxCharacters(handler, imports[i]);
            }
            saxEndElement(handler, IMPORT_ELEMENT);
        }
        saxEndElement(handler, IMPORTS_ELEMENT);

        // Superclass:
        if (!javadocClass.isInterface()) {
            outputSuperClassInheritance(handler, javadocClass, CLASS_INHERITANCE);
        }

        // Implements:
        outputImplements(handler, javadocClass, true);

        // Containing class in case this is an inner class:
        if (containingJavadocClass != null) {
            saxStartElement(handler, NESTED_IN_ELEMENT);
            outputClassStartElement(handler, containingJavadocClass);
            outputModifiers(handler, containingJavadocClass);
            outputComment(handler, containingJavadocClass.getComment());
            outputTags(handler, containingJavadocClass);
            outputClassEndElement(handler, containingJavadocClass);
            saxEndElement(handler, NESTED_IN_ELEMENT);
        }

        // Comment:
        outputComment(handler, javadocClass.getComment());

        // Tags:
        outputTags(handler, javadocClass);

        // Inner classes:
        outputInnerClasses(handler, javadocClass, true);

        // Fields:
        outputFields(handler, javadocClass, true);

        // Constructors:
        outputMethods(handler, javadocClass, CONSTRUCTOR_MODE);

        // Methods:
        outputMethods(handler, javadocClass, METHOD_MODE);

        // Close class-level element:
        outputClassEndElement(handler, javadocClass);

        // Output SAX 'footer':
        handler.endPrefixMapping(NS_PREFIX);
        handler.endDocument();
    }

    /**
     * @see Recyclable#recycle()
     */
    public void recycle() {
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug("Recycling QDoxSource '" + javadocClassName + "'...");
        }

        manager = null;
        javaSource = null;
        javadocUri = null;
        javadocClassName = null;
        javadocClass = null;
        containingJavadocClass = null;
        classMap = null;
        logger = null;
    }

    /**
     * Get the content length of the source or -1 if it
     * is not possible to determine the length.
     */
    public long getContentLength() {
        return -1L;
    }

    /**
     * @see org.apache.excalibur.source.Source#getLastModified()
     */
    public long getLastModified() {
        return javaSource.getLastModified();
    }

    /**
     * @see org.apache.excalibur.source.Source#getMimeType()
     */
    public String getMimeType() {
        return "text/xml";
    }

    /**
     * Return the unique identifer for this source
     */
    public String getURI() {
        return javadocUri;
    }

    /**
     * @see org.apache.excalibur.source.Source#getValidity()
     */
    public SourceValidity getValidity() {
        return javaSource.getValidity();
    }

    /**
     * @see org.apache.excalibur.source.Source#getInputStream()
     */
    public InputStream getInputStream() throws IOException, SourceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting InputStream for class " + javadocClass.getFullyQualifiedName());
        }

        // Serialize the SAX events to the XMLSerializer:

        XMLSerializer serializer = new XMLSerializer();
        //ComponentSelector serializerSelector = null;
        ByteArrayInputStream inputStream = null;

        try {
            //serializerSelector = (ComponentSelector) manager.lookup(Serializer.ROLE + "Selector");
            //logger.debug("serializer selector: " + serializerSelector.toString());
            //serializer = (XMLSerializer) serializerSelector.select(XMLSerializer.class);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
            serializer.setOutputStream(outputStream);
            toSAX(serializer);
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        //} catch (ComponentException ce) {
        //    logger.error("Component not found: " + XMLSerializer.ROLE, ce);
        //    throw new SourceException("Component lookup of XMLSerializer failed!", ce);
        } catch (SAXException se) {
            logger.error("SAX exception!", se);
            throw new SourceException("Serializing SAX to a ByteArray failed!", se);
        //} finally {
            //serializerSelector.release(serializer);
            //manager.release(serializerSelector);
        }

        return inputStream;
    }

    protected void createJavadocXml() throws SourceException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Reading Java source " + javaSource.getURI());
        }

        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSource(new BufferedReader(new InputStreamReader(javaSource.getInputStream())));

        javadocClass = builder.getClassByName(javadocClassName);
        if (javadocClass == null) {
            // An inner class is specified - let's find it:
            int index = javadocUri.lastIndexOf('.');
            String containingClassName = javadocUri.substring(javadocUri.indexOf(':') + 1, index);
            String innerClassName = javadocUri.substring(index + 1);
            containingJavadocClass = builder.getClassByName(containingClassName);
            javadocClass = getJavadocInnerClass(containingJavadocClass, innerClassName);
        }
    }

    /**
     * Method resolveMemberNameFromLink.
     *
     * @param ref
     * @return String
     */
    private String resolveMemberNameFromLink(String ref) {
        return StringUtils.substringAfter(ref, "#");
    }

    /**
     * Method resolveClassNameFromLink.
     *
     * @param ref
     * @return String
     */
    private String resolveClassNameFromLink(String ref) {
        String classPart = null;
        int hashIndex = ref.indexOf('#');
        if (hashIndex < 0) {
            classPart = ref;
        } else {
            classPart = ref.substring(0, hashIndex);
        }
        return getQualifiedClassName(classPart);
    }

    private String getQualifiedClassName(String classPart) {
        if (classPart.length() == 0) {
            // No classname specified:
            classPart = javadocClass.getFullyQualifiedName();
        } else if (classPart.equals("Object")) {
            // Fastest way to identify the root object - otherwise the next, *expensive* 'if' block is executed!
            classPart = ROOT_CLASSNAME;
        } else if (classPart.indexOf('.') < 0) {
            // No qualified name specified:
            String[] imports = javadocClass.getParentSource().getImports();
            List classImports = new ArrayList();
            List packageImports = new ArrayList();
            packageImports.add(javadocClass.getPackage());  // Most likely to find sources here, I guess...
            packageImports.add("java.lang");  // 2nd most likely place to find sources?
            for (int i=0; i<imports.length; i++) {
                if (imports[i].endsWith(".*")) {
                    packageImports.add(imports[i].substring(0, imports[i].length() - 2));
                } else if (imports[i].endsWith("*")) {
                    packageImports.add(imports[i].substring(0, imports[i].length() - 1));
                } else {
                    classImports.add(imports[i]);
                }
            }

            boolean found = false;
            for (int i = 0; !found && i < classImports.size(); i++) {
                String name = (String) classImports.get(i);
                if (name.endsWith(classPart)) {
                    classPart = name;
                    found = true;
                }
            }

            if (!found) {
                SourceResolver resolver = null;
                try {
                    resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
                    for (int i=0; !found && i<packageImports.size(); i++) {
                        String name = (String) packageImports.get(i);
                        if (name.length() == 0) {
                            name = classPart;
                        } else {
                            name += '.' + classPart;
                        }

                        // Test whether the classname 'name' is valid:
                        Source source = resolver.resolveURI(PROTOCOL + name);
                        found = source != null && source instanceof QDoxSource;
                        if (found) {
                            classPart = name;
                        }

                        resolver.release(source);
                    }
                } catch (ServiceException se) {
                    logger.error("Could not find a SourceResolver!", se);
                } catch (MalformedURLException e) {
                    // ignore - invalid URI (is subject of test)
                } catch (SourceException e) {
                    // ignore
                } catch (IOException e) {
                    // ignore
                } finally {
                    if (resolver != null) {
                        manager.release(resolver);
                    }
                }
            }
        }
        return classPart;
    }

    /**
     * Method outputClassInheritance.
     *
     * @param handler
     * @param jClass
     */
    private void outputSuperClassInheritance(ContentHandler handler, JavaClass jClass, int mode) throws SAXException {
        JavaClass superClass = getJavadocSuperClass(jClass);
        if (superClass != null && hasInheritance(jClass, mode)) {
            outputClassInheritance(handler, superClass, mode);
        }
    }

    private void outputClassInheritance(ContentHandler handler, JavaClass jClass, int mode) throws SAXException {
        outputInheritStartElement(handler, jClass);

        switch (mode) {
            case CLASS_INHERITANCE :
                // Already there!
                outputSuperClassInheritance(handler, jClass, mode);
                break;

            case INTERFACE_INHERITANCE :
                // Output interface inheritance summary:
                outputImplements(handler, jClass, false);
                break;

            case INNERCLASS_INHERITANCE :
                // Output nested inheritance summary:
                outputInnerClasses(handler, jClass, false);
                break;

            case FIELD_INHERITANCE :
                // Output field inheritance summary:
                outputFields(handler, jClass, false);
                break;

            case METHOD_INHERITANCE :
                // Output method inheritance summary from implemented interfaces:
                Type[] interfaces = jClass.getImplements();
                for (int i=0; i<interfaces.length; i++) {
                    logger.debug("inherit from " + interfaces[i].getValue());
                    outputClassInheritance(handler, getJavaClass(interfaces[i].getValue()), mode);
                }

            case CONSTRUCTOR_INHERITANCE :
                // Output method/constructor inheritance summary from superclass:
                if (!(mode == METHOD_INHERITANCE && jClass.isInterface())) {
                    outputSuperClassInheritance(handler, jClass, mode);
                }
                JavaMethod[] methods = jClass.getMethods();
                for (int i = 0; i < methods.length; i++) {
                    if ((mode == METHOD_INHERITANCE && methods[i].getReturns() != null) ||
                        (mode == CONSTRUCTOR_INHERITANCE && methods[i].getReturns() == null)) {
                        outputMethodStartElement(handler, methods[i]);
                        outputMethodEndElement(handler, methods[i]);
                    }
                }
                break;
            default :
                break;
        }
        saxEndElement(handler, INHERIT_ELEMENT);
    }

    private boolean hasInheritance(JavaClass jClass, int mode) {
        JavaClass superClass = getJavadocSuperClass(jClass);
        boolean result = false;

        if (superClass != null) {
            switch (mode) {
                case CLASS_INHERITANCE :
                    // Already there!
                    result = true;
                    break;

                case INTERFACE_INHERITANCE :
                    result = superClass.getImplements().length > 0;
                    break;

                case INNERCLASS_INHERITANCE :
                    result = superClass.getClasses().length > 0;
                    break;

                case FIELD_INHERITANCE :
                    result = superClass.getFields().length > 0;
                    break;

                case METHOD_INHERITANCE :
                    Type[] interfaces = jClass.getImplements();
                    for (int i=0; i<interfaces.length && !result; i++) {
                        JavaClass iface = getJavaClass(interfaces[i].getValue());
                        result = iface != null && iface.getMethods().length > 0;
                    }

                case CONSTRUCTOR_INHERITANCE :
                    JavaMethod[] methods = superClass.getMethods();
                    for (int i=0; i<methods.length && !result; i++) {
                        result = ((mode == METHOD_INHERITANCE && methods[i].getReturns() != null) ||
                            (mode == CONSTRUCTOR_INHERITANCE && methods[i].getReturns() == null));
                    }
                    break;

                default :
                    break;
            }

            if (!result) {
                result = hasInheritance(superClass, mode);
            }
        }

        return result;
    }

    /**
     * Method getJavadocSuperClass.
     *
     * @param jClass
     * @return JavaClass
     */
    private JavaClass getJavadocSuperClass(JavaClass jClass) {
        if (jClass == null) {
            // May not happen, of course ;-)
            throw new IllegalArgumentException("Argument 'jClass' must not be <null>!");
        }

        if (jClass.getFullyQualifiedName().equals(ROOT_CLASSNAME)) {
            // jClass is root class:
            return null;
        }

        JavaClass superClass = null;

        if (!jClass.isInterface()) {
            try {
                // Use QDocx operation to retrieve class:
                superClass = jClass.getSuperJavaClass();
            } catch (UnsupportedOperationException uoe) {
                // No Cache built (yet)... ignore!
            }
        }

        if (superClass == null) {
            String superJavadocClassName = null;

            if (jClass.isInterface()) {
                Type[] interfaces = jClass.getImplements();
                if (interfaces.length == 1) {
                    superJavadocClassName = interfaces[0].getValue();
                }
            } else {
                superJavadocClassName = jClass.getSuperClass().getValue();

                // Is the superClass itself an inner class?
                if (superJavadocClassName.indexOf('.') == -1 && getJavadocInnerClass(containingJavadocClass, superJavadocClassName) != null) {
                    superJavadocClassName = containingJavadocClass.getFullyQualifiedName() + '.' + superJavadocClassName;
                }
            }

            if (superJavadocClassName != null) {
                superClass = getJavaClass(superJavadocClassName);
            }
        }

        return superClass;
    }

    /**
     * Method getInnerClass.
     *
     * @param jClass
     * @param className
     * @return JavaClass
     */
    private JavaClass getJavadocInnerClass(JavaClass jClass, String className) {
        if (jClass != null) {
            JavaClass[] classes = jClass.getClasses();

            for (int i=0; i<classes.length; i++) {
                if (classes[i].getName().equals(className)) {
                    return classes[i];
                }
            }
        }
        return null;
    }

    /**
     * Get the meta class for the specified classname. The result is cached internally.
     *
     * @param className
     * @return JavaClass
     */
    private JavaClass getJavaClass(String className) {
        if (classMap != null && classMap.containsKey(className)) {
            return (JavaClass) classMap.get(className);
        }

        JavaClass jClass = null;
        SourceResolver resolver = null;

        try {
            resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
            Source source = resolver.resolveURI(PROTOCOL + className);
            if (source instanceof QDoxSource) {
                QDoxSource javadocSource = (QDoxSource) source;
                jClass = javadocSource.getJavadocClass();
                if (classMap == null) {
                    classMap = new HashMap();
                }
                classMap.put(className, jClass);
            }
            resolver.release(source);
        } catch (ServiceException se) {
            logger.error("Couldn't return JavaClass!", se);
        } catch (MalformedURLException mue) {
            logger.error("Couldn't return JavaClass!", mue);
        } catch (SourceException se) {
            logger.error("Couldn't return JavaClass!", se);
        } catch (IOException ioe) {
            logger.error("Couldn't return JavaClass!", ioe);
        } finally {
            if (resolver != null) {
                manager.release(resolver);
            }
        }

        return jClass;
    }

    /**
     * Method outputModifiers.
     *
     * @param handler
     * @param entity
     */
    private void outputModifiers(ContentHandler handler, AbstractJavaEntity entity) throws SAXException {
        String[] modifiers = entity.getModifiers();
        if (modifiers.length > 0) {
            saxStartElement(handler, MODIFIERS_ELEMENT);
            for (int i=0; i<modifiers.length; i++) {
                saxStartElement(handler, modifiers[i]);
                saxEndElement(handler, modifiers[i]);
            }
            saxEndElement(handler, MODIFIERS_ELEMENT);
        }
    }

    /**
     * Method outputCommentAndTags.
     *
     * @param handler
     * @param entity
     */
    private void outputTags(ContentHandler handler, AbstractJavaEntity entity) throws SAXException {
        DocletTag[] tags = entity.getTags();

        boolean tagElementPassed = false;
        for (int i=0; i<tags.length; i++) {
            String tagName = tags[i].getName();
            String value = tags[i].getValue();
            if (!tagElementPassed && !tagName.equals("throws") && !tagName.equals("param")) {
                saxStartElement(handler, TAGS_ELEMENT);
                tagElementPassed = true;
            }

            if (tagName.equals("see")) {
                saxStartElement(handler, tagName);
                outputLink(handler, value, null);
                saxEndElement(handler, tagName);
            } else if (!tagName.equals("throws") && !tagName.equals("param")) {
                // the 'throws' and 'param' tags are handled at method exception and method parameter level:
                saxStartElement(handler, tagName);
                outputComment(handler, value);
                saxEndElement(handler, tagName);
            }
        }

        if (tagElementPassed) {
            saxEndElement(handler, TAGS_ELEMENT);
        }
    }

    /**
     * Outputs a Javadoc comment.
     *
     * @param handler SAX ContentHandler
     * @param comment The Javadoc comment
     * @throws SAXException if something goes wrong
     */
    private void outputComment(ContentHandler handler, String comment) throws SAXException {
        if (comment != null && comment.length() > 0) {
            // When newlines are recognized in QDox, uncomment these lines:
            //if (comment.indexOf("<pre>") >= 0) {
                //saxStartElement(handler, COMMENT_ELEMENT, new String[][] {{"http://www.w3.org/XML/1998/namespace",
                //                "space", "xml:space", "NMTOKEN", "preserve"}});
                //saxStartElement(handler, COMMENT_ELEMENT, new String[][] {{"xml:space", "preserve"}});
            //} else {
                saxStartElement(handler, COMMENT_ELEMENT);
            //}

            while (reLink.match(comment)) {
                String ref = null;
                String display = null;
                if (reLink.getParen(6) == null) {
                    // {@link xxx yyy}
                    ref = reLink.getParen(2);
                    display = reLink.getParen(5);
                } else {
                    // {@link xxx}
                    ref = reLink.getParen(6);
                    display = EMPTY;
                }

                // Output SAX:
                saxCharacters(handler, comment.substring(0, reLink.getParenStart(0)));

                outputLink(handler, ref, display);

                // Cut from doc:
                comment = comment.substring(reLink.getParenEnd(0));
            }

            saxCharacters(handler, comment);

            saxEndElement(handler, COMMENT_ELEMENT);
        }
    }

    /**
     * Method outputLink.
     *
     * @param handler
     * @param ref
     * @param display
     */
    private void outputLink(ContentHandler handler, String ref, String display) throws SAXException {
        String classPart = resolveClassNameFromLink(ref);
        String memberPart = resolveMemberNameFromLink(ref);
        String displayPart = display;

        List attrs = new ArrayList();

        if (StringUtils.isNotEmpty(classPart)) {
            attrs.add(new String[] {LINK_CLASS_ATTRIBUTE, classPart});
        }

        if (StringUtils.isNotEmpty(memberPart)) {
            attrs.add(new String[] {LINK_MEMBER_ATTRIBUTE, memberPart});
        }

        if (StringUtils.isEmpty(display) && !ref.equals(classPart + "#" + memberPart)) {
            displayPart = ref.replace('#', '.');
        }

        saxStartElement(handler, LINK_ELEMENT, (String[][]) attrs.toArray(new String[][]{{}}));
        saxCharacters(handler, displayPart);
        saxEndElement(handler, LINK_ELEMENT);
    }

    /**
     * Method outputInnerClasses.
     *
     * @param handler
     * @param jClass
     * @param detailed
     */
    private void outputInnerClasses(ContentHandler handler, JavaClass jClass, boolean detailed) throws SAXException {
        JavaClass[] innerClasses = jClass.getClasses();
        if (innerClasses.length > 0 || hasInheritance(jClass, INNERCLASS_INHERITANCE)) {
            if (detailed) {
                saxStartElement(handler, INNERCLASSES_ELEMENT);
            }

            // Output inheritance:
            outputSuperClassInheritance(handler, jClass, INNERCLASS_INHERITANCE);

            for (int i=0; i<innerClasses.length; i++) {
                outputClassStartElement(handler, innerClasses[i]);
                if (detailed) {
                    outputModifiers(handler, innerClasses[i]);
                    outputComment(handler, innerClasses[i].getComment());
                    outputTags(handler, innerClasses[i]);
                }
                outputClassEndElement(handler, innerClasses[i]);
            }

            if (detailed) {
                saxEndElement(handler, INNERCLASSES_ELEMENT);
            }
        }
    }

    /**
     * Method outputImplements.
     *
     * @param handler
     * @param jClass
     */
    private void outputImplements(ContentHandler handler, JavaClass jClass, boolean detailed) throws SAXException {
        Type[] interfaces = jClass.getImplements();
        if (interfaces.length > 0 || hasInheritance(jClass, INTERFACE_INHERITANCE)) {
            if (detailed) {
                saxStartElement(handler, IMPLEMENTS_ELEMENT);
            }

            // Output inheritance:
            outputSuperClassInheritance(handler, jClass, INTERFACE_INHERITANCE);

            for (int i=0; i<interfaces.length; i++) {
                String name = interfaces[i].getValue().toString();
                String pckg = name.substring(0, name.lastIndexOf('.'));
                name = name.substring(pckg.length() + 1);

                saxStartElement(handler, INTERFACE_ELEMENT,
                    new String[][] {{CLASSNAME_ATTRIBUTE, name},
                                    {PACKAGE_ATTRIBUTE, pckg},
                                    {QNAME_ATTRIBUTE, pckg + '.' + name}});
                saxEndElement(handler, INTERFACE_ELEMENT);
            }

            if (detailed) {
                saxEndElement(handler, IMPLEMENTS_ELEMENT);
            }
        }
    }

    /**
     * Method outputFields.
     *
     * @param handler
     * @param jClass
     * @param detailed
     */
    private void outputFields(ContentHandler handler, JavaClass jClass, boolean detailed) throws SAXException {
        JavaField[] fields = jClass.getFields();

        if (fields.length > 0 || hasInheritance(jClass, FIELD_INHERITANCE)) {
            if (detailed) {
                saxStartElement(handler, FIELDS_ELEMENT);
            }

            // Output inheritance:
            outputSuperClassInheritance(handler, jClass, FIELD_INHERITANCE);

            for (int i=0; i<fields.length; i++) {
                saxStartElement(handler, FIELD_ELEMENT,
                    new String[][] {{NAME_ATTRIBUTE, fields[i].getName()},
                                    {TYPE_ATTRIBUTE, fields[i].getType().getValue()},
                                    {DIMENSIONS_ATTRIBUTE, Integer.toString(fields[i].getType().getDimensions())}});
                if (detailed) {
                    outputModifiers(handler, fields[i]);
                    outputComment(handler, fields[i].getComment());
                    outputTags(handler, fields[i]);
                }
                saxEndElement(handler, FIELD_ELEMENT);
            }

            if (detailed) {
                saxEndElement(handler, FIELDS_ELEMENT);
            }
        }
    }

    /**
     * Method outputClassStartElement.
     *
     * @param handler
     * @param jClass
     */
    private void outputInheritStartElement(ContentHandler handler, JavaClass jClass) throws SAXException {
        saxStartElement(handler, INHERIT_ELEMENT,
            new String[][] {{TYPE_ATTRIBUTE, jClass.isInterface() ? INTERFACE_ELEMENT : CLASS_ELEMENT},
                            {CLASSNAME_ATTRIBUTE, jClass.getName()},
                            {PACKAGE_ATTRIBUTE, jClass.getPackage()},
                            {QNAME_ATTRIBUTE, jClass.getFullyQualifiedName()}});
    }

    /**
     * Method outputClassStartElement.
     *
     * @param handler
     * @param jClass
     */
    private void outputClassStartElement(ContentHandler handler, JavaClass jClass) throws SAXException {
        saxStartElement(handler, jClass.isInterface() ? INTERFACE_ELEMENT : CLASS_ELEMENT,
            new String[][] {{CLASSNAME_ATTRIBUTE, jClass.getName()},
                            {PACKAGE_ATTRIBUTE, jClass.getPackage()},
                            {QNAME_ATTRIBUTE, jClass.getFullyQualifiedName()}});
    }

    /**
     * Method outputClassEndElement.
     *
     * @param handler
     * @param jClass
     */
    private void outputClassEndElement(ContentHandler handler, JavaClass jClass) throws SAXException {
        saxEndElement(handler, jClass.isInterface() ? INTERFACE_ELEMENT : CLASS_ELEMENT);
    }

    /**
     * Method outputMethods.
     *
     * @param handler
     * @param jClass
     * @param mode
     */
    private void outputMethods(ContentHandler handler, JavaClass jClass, int mode) throws SAXException {
        // Are there any methods in <mode>?
        int size = 0;
        String elementGroup, element;
        JavaMethod[] methods = jClass.getMethods();

        if (mode == CONSTRUCTOR_MODE) {
            elementGroup = CONSTRUCTORS_ELEMENT;
            element = CONSTRUCTOR_ELEMENT;
            for (int i=0; i<methods.length; i++) {
                if (methods[i].getReturns() == null) {
                    size++;
                }
            }
        } else {
            elementGroup = METHODS_ELEMENT;
            element = METHOD_ELEMENT;
            for (int i=0; i<methods.length; i++) {
                if (methods[i].getReturns() != null) {
                    size++;
                }
            }
        }

        if (size > 0 || (mode == METHOD_MODE && hasInheritance(jClass, METHOD_INHERITANCE)) ||
            (mode == CONSTRUCTOR_MODE && hasInheritance(jClass, CONSTRUCTOR_INHERITANCE))) {
            saxStartElement(handler, elementGroup);

            // Output inheritance:
            if (mode == METHOD_MODE) {
                outputSuperClassInheritance(handler, jClass, METHOD_INHERITANCE);
            } else {
                outputSuperClassInheritance(handler, jClass, CONSTRUCTOR_INHERITANCE);
            }

            for (int i=0; i<methods.length; i++) {
                if (mode == METHOD_MODE && methods[i].getReturns() != null) {
                    outputMethodStartElement(handler, methods[i]);
                } else if (mode == CONSTRUCTOR_MODE && methods[i].getReturns() == null) {
                    saxStartElement(handler, CONSTRUCTOR_ELEMENT,
                        new String[][] {{NAME_ATTRIBUTE, methods[i].getName()},
                                        {SIGNATURE_ATTRIBUTE, getSignature(methods[i])}});
                } else {
                    // Do not process this method or constructor:
                    continue;
                }

                JavaParameter[] params = methods[i].getParameters();
                DocletTag[] paramTags = methods[i].getTagsByName("param");
                DocletTag[] throwsTags = methods[i].getTagsByName("throws");

                // Modifiers, comment, tags:
                outputModifiers(handler, methods[i]);
                outputComment(handler, methods[i].getComment());
                outputTags(handler, methods[i]);

                // Parameters:
                if (params.length > 0) {
                    saxStartElement(handler, PARAMETERS_ELEMENT);
                    for (int j=0; j<params.length; j++) {
                        String paramName = params[j].getName();
                        saxStartElement(handler, PARAMETER_ELEMENT,
                            new String[][] {{NAME_ATTRIBUTE, paramName},
                                            {TYPE_ATTRIBUTE, params[j].getType().getValue()},
                                            {DIMENSIONS_ATTRIBUTE, Integer.toString(params[j].getType().getDimensions())}});

                        // Is there any doc for this parameter?
                        for (int k=0; k<paramTags.length; k++) {
                            String paramValue = paramTags[k].getValue();
                            int splitIndex = paramValue.indexOf(' ');
                            String paramValueName = splitIndex > 0 ? paramValue.substring(0, splitIndex) : paramValue;
                            if (paramName.equals(paramValueName)) {
                                outputComment(handler, splitIndex > 0 ? paramValue.substring(splitIndex + 1) : "");
                            }
                        }

                        saxEndElement(handler, PARAMETER_ELEMENT);
                    }
                    saxEndElement(handler, PARAMETERS_ELEMENT);
                }

                // Exceptions:
                Type[] exceptions = methods[i].getExceptions();
                if (exceptions.length + throwsTags.length > 0) {
                    saxStartElement(handler, THROWS_ELEMENT);
                    for (int j=0; j<exceptions.length; j++) {
                        // Iterate each exception which is declared in the throws clause:
                        String exceptionName = exceptions[j].getValue();
                        saxStartElement(handler, EXCEPTION_ELEMENT, new String[][] {{NAME_ATTRIBUTE, exceptionName}});

                        // Is there any doc for this exception?
                        if (throwsTags.length > 0) {
                            String exceptionClassName = exceptionName.substring(exceptionName.lastIndexOf('.'));
                            for (int k=0; k<throwsTags.length; k++) {
                                String excValue = throwsTags[k].getValue();
                                int splitIndex = excValue.indexOf(' ');
                                String excValueName = splitIndex > 0 ? excValue.substring(0, splitIndex) : excValue;
                                if (exceptionClassName.equals(excValueName)) {
                                    outputComment(handler, splitIndex > 0 ? excValue.substring(splitIndex + 1) : "");
                                }
                            }
                        }

                        saxEndElement(handler, EXCEPTION_ELEMENT);
                    }

                    for (int j=0; j<throwsTags.length; j++) {
                        // Iterate each exception which is not declared in the throws clause but documented in javadoc:
                        String content = throwsTags[j].getValue();
                        int splitIndex = content.indexOf(' ');
                        String exceptionName = content.substring(0, splitIndex);
                        String qualifiedExceptionName = getQualifiedClassName(exceptionName);

                        // Does the exception *not* exist in the throws clause?
                        boolean found = false;
                        for (int k=0; !found && k<exceptions.length; k++) {
                            found = qualifiedExceptionName.equals(exceptions[k].getValue());
                        }

                        if (!found) {
                            saxStartElement(handler, EXCEPTION_ELEMENT, new String[][] {{NAME_ATTRIBUTE, qualifiedExceptionName}});
                            outputComment(handler, splitIndex > 0 ? content.substring(splitIndex + 1) : "");
                            saxEndElement(handler, EXCEPTION_ELEMENT);
                        }
                    }

                    saxEndElement(handler, THROWS_ELEMENT);
                }

                saxEndElement(handler, element);
            }

            saxEndElement(handler, elementGroup);
        }
    }

    /**
     * Method getSignature.
     *
     * @param javaMethod
     * @return String
     */
    private String getSignature(JavaMethod javaMethod) {
        StringBuffer sb = new StringBuffer(javaMethod.getName());
        sb.append('(');
        JavaParameter[] params = javaMethod.getParameters();
        for (int j=0; j<params.length; j++) {
            if (j > 0) {
                sb.append(", ");
            }
            sb.append(params[j].getType().getValue());
            int dims = params[j].getType().getDimensions();
            for (int k=0; k<dims; k++) {
                sb.append("[]");
            }
        }
        sb.append(')');

        return sb.toString();
    }

    /**
     * Method outputMethodStartElement.
     *
     * @param handler
     * @param javaMethod
     */
    private void outputMethodStartElement(ContentHandler handler, JavaMethod javaMethod) throws SAXException {
        if (javaMethod.getReturns() != null) {
            saxStartElement(handler, METHOD_ELEMENT,
                new String[][] {{NAME_ATTRIBUTE, javaMethod.getName()},
                                {TYPE_ATTRIBUTE, javaMethod.getReturns().getValue()},
                                {DIMENSIONS_ATTRIBUTE, Integer.toString(javaMethod.getReturns().getDimensions())},
                                {SIGNATURE_ATTRIBUTE, getSignature(javaMethod)}});
        } else {
            saxStartElement(handler, CONSTRUCTOR_ELEMENT,
                new String[][] {{NAME_ATTRIBUTE, javaMethod.getName()},
                                {SIGNATURE_ATTRIBUTE, getSignature(javaMethod)}});
        }
    }

    /**
     * Method outputMethodEndElement.
     *
     * @param handler
     */
    private void outputMethodEndElement(ContentHandler handler, JavaMethod javaMethod) throws SAXException {
        if (javaMethod.getReturns() != null) {
            saxEndElement(handler, METHOD_ELEMENT);
        } else {
            saxEndElement(handler, CONSTRUCTOR_ELEMENT);
        }
    }

    /**
     * Method saxStartElement.
     *
     * @param handler
     * @param localName
     */
    private void saxStartElement(ContentHandler handler, String localName) throws SAXException {
        handler.startElement(NS_URI, localName, NS_PREFIX + ':' + localName, EMPTY_ATTRS);
    }

    /**
     * Method saxStartElement.
     *
     * @param handler
     * @param localName
     * @param attrs
     */
    private void saxStartElement(ContentHandler handler, String localName, String[][] attrs) throws SAXException {
        AttributesImpl saxAttrs = new AttributesImpl();
        for (int i=0; i<attrs.length; i++) {
            if (attrs[i].length == 2) {
                saxAttrs.addAttribute(EMPTY, attrs[i][0], attrs[i][0], ATTR_TYPE, attrs[i][1]);
            } else if (attrs[i].length == 5) {
                saxAttrs.addAttribute(attrs[i][0], attrs[i][1], attrs[i][2], attrs[i][3], attrs[i][4]);
            }
        }

        handler.startElement(NS_URI, localName, NS_PREFIX + ':' + localName, saxAttrs);
    }

    /**
     * Method saxEndElement.
     *
     * @param handler
     * @param localName
     */
    private void saxEndElement(ContentHandler handler, String localName) throws SAXException {
        handler.endElement(NS_URI, localName, NS_PREFIX + ':' + localName);
    }

    /**
     * Method saxCharacters.
     *
     * @param handler
     * @param text
     */
    private void saxCharacters(ContentHandler handler, String text) throws SAXException {
        if (text != null && text.length() > 0) {
            handler.characters(text.toCharArray(), 0, text.length());
        }
    }

    /**
      * @see org.apache.excalibur.source.Source#exists()
      */
     public boolean exists() {
         return true;
     }
}
