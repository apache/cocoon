/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
 Gopinath M.R. <gopi@aztec.soft.net>. For more information on the Apache
 Software Foundation, please see <http://www.apache.org/>.
 */

package org.apache.tools.doclet.xml;

import java.io.*;
import java.util.*;
import com.sun.javadoc.*;
import org.apache.xml.serialize.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Main Doclet class to generate JavaDocXML.  This doclet generates the
 * document conforming to DTD specified in javadoc-v04draft.dtd.
 * @author <a href="mailto:gopi@aztecsoft.com">Gopinath M.R.</a>
 */

public class XMLDoclet extends Doclet {

    private String xmlns = "jvx";
    private String encodingFormat="UTF-8";
    private String localName = "javadoc";
    private ContentHandler cm = null;
    private String targetFileName="simple.xml";
    private Attributes emptyAtts = new AttributesImpl();

    public XMLDoclet (RootDoc root) throws Exception {
        FileWriter writer = new FileWriter(targetFileName);
        try {
            OutputFormat format = new OutputFormat();
            format.setEncoding(encodingFormat);
            format.setIndenting(true);
            format.setIndent(4);
            format.setLineWidth(4);
            format.setDoctype("-//APACHE//DTD JavaDoc V0.4//EN", "javadoc-v04draft.dtd");
            XMLSerializer serializer = new XMLSerializer(writer, format);
            cm = serializer.asContentHandler();
            javadocXML(root);
            writer.close();
        } catch (IOException e) {
            writer.close();
            throw e;
        }
    }

    /**
     * Generates the xml data for the top element.
     * <xmp><!ELEMENT javadoc (package*, class*, interface*)></xmp>
     */
    private void javadocXML(RootDoc root) throws SAXException {
        cm.startElement(xmlns, localName, "javadoc", emptyAtts);
        PackageDoc[] packageArray = root.specifiedPackages();

        // Generate for packages.
        for (int i = 0; i < packageArray.length; ++i) {
            packageXML(packageArray[i]);
        }

        // Generate for classes.
        ClassDoc[] classArray = root.specifiedClasses();
        Vector interfaceVector = new Vector();
        for (int i = 0; i < classArray.length; ++i) {
            if (classArray[i].isInterface()) {
                interfaceVector.addElement(classArray[i]);
            } else {
                classXML(classArray[i]);
            }
        }

        // Generate for interfaces.
        Enumeration interfaceEnum = interfaceVector.elements();
        if (interfaceEnum.hasMoreElements()) {
            ClassDoc interfaceDoc = (ClassDoc)interfaceEnum.nextElement();
            interfaceXML(interfaceDoc);
        }
        cm.endElement(xmlns, localName, "javadoc");
    }

    /**
     * Generates doc for "package".
     * <xmp><!ELEMENT package (doc?, package*, class*, interface*)>
     *<!ATTLIST package
     *              name CDATA #REQUIRED></xmp>
     */
    private void packageXML(PackageDoc packageDoc) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(xmlns, localName, "name", "String", packageDoc.name());
        cm.startElement(xmlns, localName, "package", atts);

        // generate Doc element.
        docXML(packageDoc);

        // TODO:generate Package elements.
        // doubt: How package can exist inside a package?

        /* Generate for classes. */
        // for ordinary classes.
        ClassDoc[] classArray = packageDoc.ordinaryClasses();
        for (int i = 0; i < classArray.length; ++i) {
            classXML(classArray[i]);
        }
        // for Exception classes.
        classArray = packageDoc.exceptions();
        for (int i = 0; i < classArray.length; ++i) {
            classXML(classArray[i]);
        }
        // for Error classes
        classArray = packageDoc.errors();
        for (int i = 0; i < classArray.length; ++i) {
            classXML(classArray[i]);
        }

        /* Generate for interfaces. */
        ClassDoc[] interfaceArray = packageDoc.interfaces();
        for (int i = 0; i < interfaceArray.length; ++i) {
            interfaceXML(interfaceArray[i]);
        }

        cm.endElement(xmlns, localName, "package");
    }

    /**
     * Generates doc for element "class".
     * <xmp> <!ELEMENT class (doc?,
     *                  extends_class?,
     *                  implements?,
     *                  constructor*,
     *                  method*,
     *                  innerclass*)>
     * <!ATTLIST class
     *     %name;
     *     %extensibility;
     *     %class.access;></xmp>
     */
    private void classXML(ClassDoc classDoc) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(xmlns, localName, "name", "String", classDoc.name());

        String extensibility = "default";
        if (classDoc.isAbstract()) {
            extensibility = "abstract";
        } else if (classDoc.isFinal()) {
            extensibility = "final";
        }
        atts.addAttribute(xmlns, localName, "extensiblity", "String", extensibility);
        String access = "package";
        if (classDoc.isPublic()) {
            access = "public";
        }
        atts.addAttribute(xmlns, localName, "access", "String", access);
        cm.startElement(xmlns, localName, "class", atts);

        // generate "doc" sub-element
        docXML(classDoc);

        // generate "extends_class" sub-element
        extendsXML(classDoc);

        // generate "implements" sub-element
        implementsXML(classDoc);

        // generate "field" sub-elements
        FieldDoc[] fieldArray = classDoc.fields();
        for (int i = 0; i < fieldArray.length; ++i) {
            fieldXML(fieldArray[i]);
        }

        // generate "constructor" sub-elements
        ConstructorDoc[] constructorArray = classDoc.constructors();
        for (int i = 0; i < constructorArray.length; ++i) {
            constructorXML(constructorArray[i]);
        }

        // generate "method" sub-elements
        MethodDoc[] methodArray = classDoc.methods();
        for (int i = 0; i < methodArray.length; ++i) {
            methodXML(methodArray[i]);
        }

        // generate "innerclass" sub-elements
        ClassDoc[] innerClassArray = classDoc.innerClasses();
        for (int i = 0; i < innerClassArray.length; ++i) {
            innerClassXML(innerClassArray[i]);
        }

        cm.endElement(xmlns, localName, "class");
    }

    /**
     * Generates doc for element "extends_class"
     * <xmp><!ELEMENT extends_class (classref+)></xmp>
     */
    private void extendsXML(ClassDoc classDoc) throws SAXException {
        if (classDoc.superclass() != null) {
            cm.startElement(xmlns, localName, "extends_class", emptyAtts);
            createRefXML("classref", classDoc.superclass().qualifiedName());
            cm.endElement(xmlns, localName, "extends_class");
        }
    }

    /**
     * Generates doc for element "innerclass"
     * <xmp> <!ELEMENT innerclass (doc?,
     *                 extends?,
     *                 implements?,
     *                 field*,
     *                 constructor*,
     *                 method*)>
     * <!ATTLIST innerclass
     *    %name;
     *    %access;
     *    %abstract;
     *    %anonymous;
     *    %final;
     *    %static;></xmp>
     */
    private void innerClassXML(ClassDoc classDoc) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(xmlns, localName, "name", "String", classDoc.name());
        String access = "package";
        if (classDoc.isPublic()) {
            access = "public";
        }
        atts.addAttribute(xmlns, localName, "access", "String", access);
        atts.addAttribute(xmlns, localName, "abstract", "String", ""+ classDoc.isAbstract());
        String anonymous = "false";
        if (classDoc.name().equals("")) {
            anonymous = "true";
        }
        atts.addAttribute(xmlns, localName, "anonymous", "String", ""+ anonymous);
        atts.addAttribute(xmlns, localName, "final", "String", ""+ "" + classDoc.isFinal());
        atts.addAttribute(xmlns, localName, "static", "String", ""+ "" + classDoc.isStatic());
        cm.startElement(xmlns, localName, "innerclass", atts);

        // generate "doc" sub-element
        docXML(classDoc);

        // generate "extends" sub-element
        extendsXML(classDoc);

        // generate "implements" sub-element
        implementsXML(classDoc);

        // generate "field" sub-elements
        FieldDoc[] fieldArray = classDoc.fields();
        for (int i = 0; i < fieldArray.length; ++i) {
            fieldXML(fieldArray[i]);
        }

        // generate "constructor" sub-elements
        ConstructorDoc[] constructorArray = classDoc.constructors();
        for (int i = 0; i < constructorArray.length; ++i) {
            constructorXML(constructorArray[i]);
        }

        // generate "method" sub-elements
        MethodDoc[] methodArray = classDoc.methods();
        for (int i = 0; i < methodArray.length; ++i) {
            methodXML(methodArray[i]);
        }

        cm.endElement(xmlns, localName,"innerclass");
    }

    /**
     * Generates doc for element "interface"
     * <xmp><!ELEMENT interface (doc?,
     *               extends_interface?,
     *               field*,
     *               method*)>
     * <!ATTLIST interface
     *             %name;
     *             %access;></xmp>
     */
    private void interfaceXML(ClassDoc interfaceDoc) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(xmlns, localName, "name", "String", interfaceDoc.name());
        String access = "package";
        if (interfaceDoc.isPublic()) {
            access = "public";
        }
        atts.addAttribute(xmlns, localName, "access", "String", access);
        cm.startElement(xmlns, localName, "interface", atts);

        // generate "doc" sub-element
        docXML(interfaceDoc);

        // generate "extends_interface"
        extends_interfaceXML(interfaceDoc);

        // generate "field" sub-elements
        FieldDoc[] fieldArray = interfaceDoc.fields();
        for (int i = 0; i < fieldArray.length; ++i) {
            fieldXML(fieldArray[i]);
        }

        // generate "method" sub-elements
        MethodDoc[] methodArray = interfaceDoc.methods();
        for (int i = 0; i < methodArray.length; ++i) {
            methodXML(methodArray[i]);
        }
        cm.endElement(xmlns, localName, "interface");
    }

    /**
     * Generates doc for element "extends_interface"
     * <xmp><!ELEMENT extends_interface (interfaceref+)></xmp>
     */
    private void extends_interfaceXML(ClassDoc interfaceDoc) throws SAXException {
        ClassDoc[] interfaceArray = interfaceDoc.interfaces();
        if (interfaceArray.length > 0) {
            cm.startElement(xmlns, localName, "extends_interface", emptyAtts);
            for (int i = 0; i < interfaceArray.length; ++i) {
                createRefXML("interfaceref", interfaceArray[i].qualifiedName());
            }
            cm.endElement(xmlns, localName, "extends_interface");
        }
    }

    /**
     * Generates doc for element "implements"
     * <xmp><!ELEMENT implements (interfaceref+)></xmp>
     */
    private void implementsXML(ClassDoc classDoc) throws SAXException {
        ClassDoc[] interfaceArray = classDoc.interfaces();
        if (interfaceArray.length > 0) {
            cm.startElement(xmlns, localName, "implements", emptyAtts);
            for (int i = 0; i < interfaceArray.length; ++i) {
                createRefXML("interfaceref", interfaceArray[i].qualifiedName());
            }
            cm.endElement(xmlns, localName, "implements");
        }
    }

    /**
     * Generates doc for element "throws"
     * <xmp><!ELEMENT throws (classref)+></xmp>
     */
    private void throwsXML(ExecutableMemberDoc member) throws SAXException {
        ThrowsTag[] tagArray = member.throwsTags();
        if(tagArray.length > 0) {
            cm.startElement(xmlns, localName, "throws", emptyAtts);
            for (int i = 0; i < tagArray.length; ++i) {
                ClassDoc exceptionClass = tagArray[i].exception();
                String name = null;
                if (exceptionClass == null) {
                    name = tagArray[i].exceptionName();
                } else {
                    name = tagArray[i].exception().qualifiedName();
                }
                createRefXML("classref", name);
            }
            cm.endElement(xmlns, localName, "throws");
        }
    }

    /**
     * Generates doc for following elements
     * <xmp> <!ELEMENT classref EMPTY>
     * <!ATTLIST classref %name;>
     * <!ELEMENT interfaceref EMPTY>
     * <!ATTLIST interfaceref %name;>
     * <!ELEMENT methodref EMPTY>
     * <!ATTLIST methodref %name;>
     * <!ELEMENT packageref EMPTY>
     * <!ATTLIST packageref %name;></xmp>
     */
    private void createRefXML(String elementName, String nameValue) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(xmlns, localName, "name", "String", nameValue);
        cm.startElement(xmlns, localName, elementName, atts);
        cm.endElement(xmlns, localName, elementName);
    }

    /**
     * Generates doc for "(classref|interfaceref|primitive)" sub-element
     */
    private void createTypeRef(Type type) throws SAXException {
        String qualifiedName = type.qualifiedTypeName();
        ClassDoc fieldType = type.asClassDoc();
        if (fieldType == null) {
            // primitive data type
            AttributesImpl subElmAtts = new AttributesImpl();
            subElmAtts.addAttribute(xmlns, localName, "type", "String", qualifiedName);
            cm.startElement(xmlns, localName, "primitive", subElmAtts);
            cm.endElement(xmlns, localName, "primitive");
        } else if (fieldType.isInterface()) {
            // interface
            createRefXML("interfaceref", qualifiedName);
        } else {
            // class
            createRefXML("classref", qualifiedName);
        }
    }

    /**
     * Generates doc for element "field"
     * <xmp> <!ELEMENT field (doc?, (classref | interfaceref | primitive))>
     * <!ATTLIST field
     *    %name;
     *    %access;
     *    %dimension;
     *    %synthetic;
     *    %static;
     *    %final;
     *    %transient;
     *    %volatile;></xmp>
     */
    private void fieldXML(FieldDoc field) throws SAXException {
        AttributesImpl atts = new AttributesImpl();

        atts.addAttribute(xmlns, localName, "name", "String", field.name());

        String access = "package";
        if (field.isPrivate()) {
            access = "private";
        } else if (field.isProtected()) {
            access = "protected";
        } else if (field.isPublic()) {
            access = "public";
        }
        atts.addAttribute(xmlns, localName, "access", "String", access);

        atts.addAttribute(xmlns, localName, "dimension", "String", field.type().dimension());
        atts.addAttribute(xmlns, localName, "synthetic", "String", "" + field.isSynthetic());
        atts.addAttribute(xmlns, localName, "static", "String", "" + field.isStatic());
        atts.addAttribute(xmlns, localName, "final", "String", "" + field.isFinal());
        atts.addAttribute(xmlns, localName, "transient", "String", "" + field.isTransient());
        atts.addAttribute(xmlns, localName, "volatile", "String", "" + field.isVolatile());
        cm.startElement(xmlns, localName, "field", atts);

        // generate "doc" sub-element
        docXML(field);

        // generate "(classref|interfaceref|primitive)" sub-element
        createTypeRef(field.type()); // foo , field.qualifiedName());

        cm.endElement(xmlns, localName, "field");
    }

    /**
     * Generates doc for element "constructor"
     * <xmp><!ELEMENT constructor (doc?, parameter*, throws*)>
     * <!ATTLIST constructor
     *     %name;
     *     %access;
     *     %synthetic;></xmp>
     */
    private void constructorXML(ConstructorDoc constrDoc) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(xmlns, localName, "name", "String", constrDoc.qualifiedName());
        String access = "package";
        if (constrDoc.isPrivate()) {
            access = "private";
        } else if (constrDoc.isProtected()) {
            access = "protected";
        } else if (constrDoc.isPublic()) {
            access = "public";
        }
        atts.addAttribute(xmlns, localName, "access", "String", access);
        atts.addAttribute(xmlns, localName, "synthetic", "String", "" + constrDoc.isSynthetic());
        cm.startElement(xmlns, localName, "constructor", atts);

        // generate "doc" sub-element
        docXML(constrDoc);

        // generate "parameter" sub-elements
        Parameter[] parameterArray = constrDoc.parameters();
        for (int i = 0; i < parameterArray.length; ++i) {
            parameterXML(parameterArray[i]);
        }

        // generate "throws" sub-element
        throwsXML(constrDoc);

        cm.endElement(xmlns, localName, "constructor");
    }

    /**
     * Generates doc for element "method"
     * <xmp> <!ELEMENT method (doc?, returns, parameter*, throws*)>
     * <!ATTLIST method
     *         %name;
     *         %access;
     *         %extensibility;
     *         %native;
     *         %synthetic;
     *         %static;
     *         %synchronized;></xmp>
     */
    private void methodXML(MethodDoc methodDoc) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        //atts.addAttribute(xmlns, localName, "", String, );
        atts.addAttribute(xmlns, localName, "name", "String", methodDoc.name());

        String access = "package";
        if (methodDoc.isPrivate()) {
            access = "private";
        } else if (methodDoc.isProtected()) {
            access = "protected";
        } else if (methodDoc.isPublic()) {
            access = "public";
        }
        atts.addAttribute(xmlns, localName, "access", "String", access);

        String extensibility = "default";
        if (methodDoc.isAbstract()) {
            extensibility = "abstract";
        } else if (methodDoc.isFinal()) {
            extensibility = "final";
        }
        atts.addAttribute(xmlns, localName, "extensiblity", "String", extensibility);

        atts.addAttribute(xmlns, localName, "native", "String", ""+ methodDoc.isNative());
        atts.addAttribute(xmlns, localName, "synthetic", "String", "" + methodDoc.isSynthetic());
        atts.addAttribute(xmlns, localName, "static", "String", "" + methodDoc.isStatic());
        atts.addAttribute(xmlns, localName, "synchronized", "String", ""+ methodDoc.isSynchronized());
        cm.startElement(xmlns, localName, "method", atts);

        // generate "doc" sub-element
        docXML(methodDoc);

        // generate "returns" sub-element
        returnsXML(methodDoc.returnType());

        // generate "parameter" sub-elements
        Parameter[] parameterArray = methodDoc.parameters();
        for (int i = 0; i < parameterArray.length; ++i) {
            parameterXML(parameterArray[i]);
        }

        // generate "throws" sub-element
        throwsXML(methodDoc);

        cm.endElement(xmlns, localName, "field");
    }

    /**
     * Generates doc for element "returns"
     * <xmp> <!ELEMENT returns (classref | interfaceref | primitive)>
     * <!ATTLIST returns %dimension;></xmp>
     */
    private void returnsXML(Type type) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(xmlns, localName, "dimension", "String", type.dimension());
        cm.startElement(xmlns, localName, "returns", atts);

        // generate "(classref|interfaceref|primitive)" sub-element
        createTypeRef(type);

        cm.endElement(xmlns, localName, "returns");
    }

    /**
     * Generates doc for element "parameter"
     * <xmp> <!ELEMENT parameter (classref | interfaceref | primitive)>
     * <!ATTLIST parameter
     *         %name;
     *         %final;
     *         %dimension;></xmp>
     */
    private void parameterXML(Parameter parameter) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(xmlns, localName, "name", "String", parameter.name());
        boolean isFinal = false;
        Type type = parameter.type();
        if (type.asClassDoc() == null) {
            isFinal = true;
        }
        atts.addAttribute(xmlns, localName, "final", "String", ""+ "" + isFinal);
        atts.addAttribute(xmlns, localName, "dimension", "String", parameter.type().dimension());
        cm.startElement(xmlns, localName, "parameter", atts);

        // generate "(classref|interfaceref|primitive)" sub-element
        createTypeRef(parameter.type());

        cm.endElement(xmlns, localName,"parameter");
    }

    /**
     * Generates doc for element "doc"
     * <xmp><!ELEMENT doc (#PCDATA |
     *              linktag |
     *              authortag |
     *              versiontag |
     *              paramtag |
     *              returntag |
     *              exceptiontag |
     *              throwstag |
     *              seetag |
     *              sincetag |
     *              deprecatedtag |
     *              serialtag |
     *              serialfieldtag |
     *              serialdatatag)*></xmp>
     */
    private void docXML(Doc doc) throws SAXException {
        String commentText = "";
        boolean createDoc = false;
        commentText = doc.commentText();
        if (! commentText.equals("")) {
            createDoc = true;
        }
        Tag[] tags = doc.tags();
        if (tags.length > 0) {
            createDoc = true;
        }
        if (createDoc) {
            cm.startElement(xmlns, localName, "doc", emptyAtts);
            if (! commentText.equals("")) {
                cm.characters(commentText.toCharArray(), 0, commentText.length());
            }
            for (int i = 0; i < tags.length; ++i) {
                tagXML(tags[i]);
            }
            cm.endElement(xmlns, localName, "doc");
        }
    }

    /**
     * Generates doc for all tag elements.
     */
    private void tagXML(Tag tag) throws SAXException {
        String name = tag.name().substring(1) + "tag";
        if (! tag.text().equals("")) {
            cm.startElement(xmlns, localName, name, emptyAtts);
            cm.characters(tag.text().toCharArray(), 0, tag.text().length());
            cm.endElement(xmlns, localName, name);
        }
    }

    public static boolean start(RootDoc root) {
        try {
            new XMLDoclet(root);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return false;
        }
    }
}
