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
package doclets;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.thoughtworks.qdox.model.AbstractJavaEntity;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Type;

/**
 * Makes constants and utility methods available to subclasses 
 * 
 * @version CVS $Revision: 1.1 $ $Date: 2004/05/25 12:53:43 $
 */
public class AbstractXMLWriter {

    protected final static String ROOT_CLASSNAME = "java.lang.Object";

    protected final static String EMPTY = "";
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

    protected final static String PACKAGE_ELEMENT = "package";
    protected final static String PACKAGENAME_ATTRIBUTE = "packagename";
    protected final static String PACKAGEPATH_ATTRIBUTE = "packagepath";

    protected final static String CLASSES_ELEMENT = "classes";
    protected final static String INTERFACES_ELEMENT = "interfaces";
    protected final static String PACKAGES_ELEMENT = "packages";
    
    protected final static int CONSTRUCTOR_MODE = 1;
    protected final static int METHOD_MODE = 2;

    protected final static int CLASS_INHERITANCE = 1;
    protected final static int INTERFACE_INHERITANCE = 2;
    protected final static int INNERCLASS_INHERITANCE = 3;
    protected final static int FIELD_INHERITANCE = 4;
    protected final static int CONSTRUCTOR_INHERITANCE = 5;
    protected final static int METHOD_INHERITANCE = 6;

    protected JavaClass javadocClass;
    protected Resolver resolver;
    
    public AbstractXMLWriter(Resolver resolver) {
        this.resolver = resolver;
    }

    protected TransformerHandler getHandler(OutputStream output) throws TransformerConfigurationException {
        SAXTransformerFactory factory = null;
        TransformerHandler handler = null;
        factory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
        handler = factory.newTransformerHandler();
        StreamResult result = new StreamResult(output);
        handler.setResult(result);
        return handler;
    }
    
    /**
     * Method outputClassStartElement.
     *
     * @param handler
     * @param jClass
     */
    protected void outputClassStartElement(ContentHandler handler, JavaClass jClass) throws SAXException {
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
    protected void outputClassEndElement(ContentHandler handler, JavaClass jClass) throws SAXException {
        saxEndElement(handler, jClass.isInterface() ? INTERFACE_ELEMENT : CLASS_ELEMENT);
    }

    /**
     * Method outputClassesStartElement.
     *
     * @param handler
     */
    protected void outputClassesStartElement(ContentHandler handler) throws SAXException {
        saxStartElement(handler, CLASSES_ELEMENT, new String[][] {{}});
    }

    /**
     * Method outputClassesEndElement.
     *
     * @param handler
     */
    protected void outputClassesEndElement(ContentHandler handler) throws SAXException {
        saxEndElement(handler, CLASSES_ELEMENT);
    }

    /**
     * Method outputPackageStartElement.
     *
     * @param handler
     * @param javaPackage
     */
    protected void outputPackageStartElement(ContentHandler handler, String javaPackage) throws SAXException {
        saxStartElement(handler, PACKAGE_ELEMENT, new String[][] {{PACKAGENAME_ATTRIBUTE, javaPackage}});
    }

    /**
     * Method outputPackageEndElement.
     *
     * @param handler
     */
    protected void outputPackageEndElement(ContentHandler handler) throws SAXException {
        saxEndElement(handler, PACKAGE_ELEMENT);
    }

    /**
     * Method outputPackagesStartElement.
     *
     * @param handler
     */
    protected void outputPackagesStartElement(ContentHandler handler) throws SAXException {
        saxStartElement(handler, PACKAGES_ELEMENT, new String[][] {{}});
    }

    /**
     * Method outputPackagesEndElement.
     *
     * @param handler
     */
    protected void outputPackagesEndElement(ContentHandler handler) throws SAXException {
        saxEndElement(handler, PACKAGES_ELEMENT);
    }

    /**
     * Method saxStartElement.
     *
     * @param handler
     * @param localName
     */
    protected void saxStartElement(ContentHandler handler, String localName) throws SAXException {
        handler.startElement(NS_URI, localName, NS_PREFIX + ':' + localName, EMPTY_ATTRS);
    }

    /**
     * Method saxStartElement.
     *
     * @param handler
     * @param localName
     * @param attrs
     */
    protected void saxStartElement(ContentHandler handler, String localName, String[][] attrs) throws SAXException {
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
    protected void saxEndElement(ContentHandler handler, String localName) throws SAXException {
        handler.endElement(NS_URI, localName, NS_PREFIX + ':' + localName);
    }

    /**
     * Method saxCharacters.
     *
     * @param handler
     * @param text
     */
    protected void saxCharacters(ContentHandler handler, String text) throws SAXException {
        if (text != null && text.length() > 0) {
            handler.characters(text.toCharArray(), 0, text.length());
        }
    }

    /**
     * Method outputModifiers.
     *
     * @param handler
     * @param entity
     */
    protected void outputModifiers(ContentHandler handler, AbstractJavaEntity entity) throws SAXException {
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
     * Outputs a Javadoc comment.
     *
     * @param handler SAX ContentHandler
     * @param comment The Javadoc comment
     * @throws SAXException if something goes wrong
     */
    protected void outputComment(ContentHandler handler, String comment) throws SAXException {
        if (comment != null && comment.length() > 0) {

            //TODO treatment of inline tags
            saxStartElement(handler, COMMENT_ELEMENT);
            saxCharacters(handler, comment);
            saxEndElement(handler, COMMENT_ELEMENT);
        }
    }

    /**
     * Method outputSuperClassInheritance.
     *
     * @param handler
     * @param jClass
     * @param mode
     */
    protected void outputSuperClassInheritance(ContentHandler handler, JavaClass jClass, int mode) throws SAXException {
        JavaClass superClass = getJavadocSuperClass(jClass);
        if (superClass != null && hasInheritance(jClass, mode)) {
            outputClassInheritance(handler, superClass, mode);
        }
    }

    /**
     * Method outputClassInheritance.
     *
     * @param handler
     * @param jClass
     * @param mode
     */
    protected void outputClassInheritance(ContentHandler handler, JavaClass jClass, int mode) throws SAXException {
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
//                    outputClassInheritance(handler, interfaces[i], mode);
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

    /**
     * Method hasInheritance.
     *
     * @param jClass
     * @param mode
     * @return String
     */
    protected boolean hasInheritance(JavaClass jClass, int mode) {
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
                    result = superClass.getInnerClasses().length > 0;
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
     * Method outputImplements.
     *
     * @param handler
     * @param jClass
     * @param detailed
     */
    protected void outputImplements(ContentHandler handler, JavaClass jClass, boolean detailed) throws SAXException {
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
    protected void outputFields(ContentHandler handler, JavaClass jClass, boolean detailed) throws SAXException {
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
     * Method outputTags.
     *
     * @param handler
     * @param entity
     */
    protected void outputTags(ContentHandler handler, AbstractJavaEntity entity) throws SAXException {
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
     * Method outputMethods.
     *
     * @param handler
     * @param jClass
     * @param mode
     */
    protected void outputMethods(ContentHandler handler, JavaClass jClass, int mode) throws SAXException {
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
                        String exceptionName = null;
                        String qualifiedExceptionName = null;
                        if (splitIndex != -1) {
                            exceptionName = content.substring(0, splitIndex);
                            qualifiedExceptionName = getQualifiedClassName(exceptionName);
                        } else {
                            qualifiedExceptionName = content;
                        }

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
    protected String getSignature(JavaMethod javaMethod) {
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
    protected void outputMethodStartElement(ContentHandler handler, JavaMethod javaMethod) throws SAXException {
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
     * @param javaMethod
     */
    protected void outputMethodEndElement(ContentHandler handler, JavaMethod javaMethod) throws SAXException {
        if (javaMethod.getReturns() != null) {
            saxEndElement(handler, METHOD_ELEMENT);
        } else {
            saxEndElement(handler, CONSTRUCTOR_ELEMENT);
        }
    }

    /**
     * Method resolveClassNameFromLink.
     *
     * @param ref
     * @return String
     */
    protected String resolveClassNameFromLink(String ref) {
        String classPart = null;
        int hashIndex = ref.indexOf('#');
        if (hashIndex < 0) {
            classPart = ref;
        } else {
            classPart = ref.substring(0, hashIndex);
        }
        return getQualifiedClassName(classPart);
    }

    /**
     * Method outputInnerClasses.
     *
     * @param handler
     * @param jClass
     * @param detailed
     */
    protected void outputInnerClasses(ContentHandler handler, JavaClass jClass, boolean detailed) throws SAXException {
        JavaClass[] innerClasses = jClass.getInnerClasses();
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
     * Method getJavadocSuperClass.
     *
     * @param jClass
     * @return JavaClass
     */
    protected JavaClass getJavadocSuperClass(JavaClass jClass) {
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

            }

            if (superJavadocClassName != null) {
                superClass = getJavaClass(superJavadocClassName);
            }
        }

        return superClass;
    }
    /**
     * Method outputInheritStartElement.
     *
     * @param handler
     * @param jClass
     */
    protected void outputInheritStartElement(ContentHandler handler, JavaClass jClass) throws SAXException {
        saxStartElement(handler, INHERIT_ELEMENT,
            new String[][] {{TYPE_ATTRIBUTE, jClass.isInterface() ? INTERFACE_ELEMENT : CLASS_ELEMENT},
                            {CLASSNAME_ATTRIBUTE, jClass.getName()},
                            {PACKAGE_ATTRIBUTE, jClass.getPackage()},
                            {QNAME_ATTRIBUTE, jClass.getFullyQualifiedName()}});
    }

    /**
     * Method outputLink.
     *
     * @param handler
     * @param ref
     * @param display
     */
    protected void outputLink(ContentHandler handler, String ref, String display) throws SAXException {
        String classPart = resolveClassNameFromLink(ref);
        String memberPart = resolveMemberNameFromLink(ref);
        String displayPart = display;

        List attrs = new ArrayList();

        if (classPart != null && !"".equals(classPart)) {
            attrs.add(new String[] {LINK_CLASS_ATTRIBUTE, classPart});
        }

        if (memberPart != null && !"".equals(memberPart)) {
            attrs.add(new String[] {LINK_MEMBER_ATTRIBUTE, memberPart});
        }

        if ((display == null || "".equals(display)) && !ref.equals(classPart + "#" + memberPart)) {
            displayPart = ref.replace('#', '.');
        }

        saxStartElement(handler, LINK_ELEMENT, (String[][]) attrs.toArray(new String[][]{{}}));
        saxCharacters(handler, displayPart);
        saxEndElement(handler, LINK_ELEMENT);
    }

    /**
     * Method resolveMemberNameFromLink.
     *
     * @param ref
     * @return String
     */
    protected String resolveMemberNameFromLink(String ref) {
        return ref.substring(ref.indexOf("#") + 1);
    }

    protected String getQualifiedClassName(String classPart) {
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
                for (int i=0; !found && i<packageImports.size(); i++) {
                    String name = (String) packageImports.get(i);
                    if (name.length() == 0) {
                        name = classPart;
                    } else {
                        name += '.' + classPart;
                    }
                    classPart = name;
                    found = (this.resolver.getJavaClasses().get(classPart) != null);
                }
            }
        }
        return classPart;
    }

    /**
     * Method getJavadocInnerClass.
     *
     * @param jClass
     * @param className
     * @return JavaClass
     */
    protected JavaClass getJavadocInnerClass(JavaClass jClass, String className) {
        if (jClass != null) {
            JavaClass[] classes = jClass.getInnerClasses();

            for (int i=0; i<classes.length; i++) {
                if (classes[i].getName().equals(className)) {
                    return classes[i];
                }
            }
        }
        return null;
    }
    
    /**
     * Method getJavaClass.
     *
     * @param className
     * @return JavaClass
     */
    protected JavaClass getJavaClass(String className) {
        if (this.resolver.getJavaClasses().get(className) != null) {
            return (JavaClass)this.resolver.getJavaClasses().get(className);
        }
        return null;
    }

}
