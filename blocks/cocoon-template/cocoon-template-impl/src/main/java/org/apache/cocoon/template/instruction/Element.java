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
package org.apache.cocoon.template.instruction;

import java.util.Stack;

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

public class Element extends Instruction {
    private Subst name;
    private Subst uri;
    private Subst prefix;
    
    public static  final String XML_ELEM_NAME_BLANK = "parameter: \"name\" is required";
    public static final String XML_ELEM_NAME_INVALID = "parameter: \"name\" is an invalid XML element name";
    public static final String XML_PREFIX_NAME_INVALID = "parameter: \"prefix\" is an Invalid XML prefix";
    public static final String XML_PREFIX_MISSING_NAMESPACE = "parameter: \"uri\" must be specified if \"prefix\" is specified";

    public Element(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack) throws SAXException {

        super(raw);
        this.name = getSubst("name", attrs, parsingContext, true);
        this.uri = getSubst("uri", attrs, parsingContext, false);
        this.prefix = getSubst("prefix", attrs, parsingContext, false); 
    }

    public Event execute(final XMLConsumer consumer, ObjectModel objectModel, ExecutionContext executionContext,
            MacroContext macroContext, NamespacesTable namespaces, Event startEvent, Event endEvent)
            throws SAXException {
	final Attributes EMPTY_ATTRS = new AttributesImpl();
	String nameStr = null;
	String uriStr;
	String prefixStr;
	String qName;
	
        try {
            nameStr = this.name.getStringValue(objectModel);
            uriStr = (this.uri != null) ? this.uri.getStringValue(objectModel) : null;
            prefixStr = (this.prefix != null) ? this.prefix.getStringValue(objectModel) : null;
            
            if (StringUtils.isBlank(nameStr))
        	throw new SAXParseException(XML_ELEM_NAME_BLANK, getLocation());

            if (!nameStr.matches("[A-Za-z][^\\s:]*"))
        	throw new SAXParseException(XML_ELEM_NAME_INVALID, getLocation());  
            
            if (StringUtils.isNotBlank(prefixStr) && !prefixStr.matches("[A-Za-z][^\\s:]*"))
        	throw new SAXParseException(XML_PREFIX_NAME_INVALID, getLocation());           
            
            if (StringUtils.isNotBlank(prefixStr) && StringUtils.isBlank(uriStr))
        	throw new SAXParseException(XML_PREFIX_MISSING_NAMESPACE, getLocation());
        	            
            qName = (StringUtils.isNotBlank(prefixStr)) ? prefixStr + ":" + nameStr : nameStr;
            
            consumer.startElement(uriStr, nameStr, qName, EMPTY_ATTRS);
            Invoker.execute(consumer, objectModel, executionContext, macroContext, namespaces, this.getNext(), this
                            .getEndInstruction());            
            consumer.endElement(uriStr, nameStr, qName);            
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), getLocation(), e);
        }
        return getEndInstruction().getNext();
    }
    
    private Subst getSubst(String attrName, Attributes attrs, ParsingContext parsingContext, boolean isRequired)
    	    throws SAXParseException
    {
	Locator locator = getLocation();
	String value = attrs.getValue(attrName);
        if (isRequired && value == null) {           
            throw new SAXParseException("parameter: \"" + attrName + "\" is required", locator, null);
        }
        else if (!isRequired && value == null) {
            return(null);
        }
        
        return parsingContext.getStringTemplateParser().compileExpr(value, "parameter: \"" + attrName + "\": ", locator);  	
    }
}
