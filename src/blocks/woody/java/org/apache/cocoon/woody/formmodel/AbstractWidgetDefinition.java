/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.formmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.validation.WidgetValidator;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Provides functionality that is common across many WidgetDefinition implementations.
 * 
 * @version $Id: AbstractWidgetDefinition.java,v 1.8 2004/02/11 10:43:30 antonio Exp $
 */
public abstract class AbstractWidgetDefinition implements WidgetDefinition {
    private FormDefinition formDefinition;
    protected WidgetDefinition parent;
    private String location = null;
    private String id;
    private Map displayData;
    private List validators;

    public FormDefinition getFormDefinition() {
        if (this.formDefinition == null) {
            if (this instanceof FormDefinition) {
                this.formDefinition = (FormDefinition)this;
            } else {
                this.formDefinition = this.parent.getFormDefinition();
            }
        }
        return this.formDefinition;
    }

    /**
     * Sets the parent of this definition
     */
    public void setParent(WidgetDefinition definition) {
        this.parent = definition;
    }
    
    /**
     * Gets the parent of this definition.
     * This method returns null for the root definition.
     */
    public WidgetDefinition getParent() {
        return this.parent;
    }

    protected void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        generateDisplayData("label", contentHandler);
    }

    /**
     * Sets the various display data for this widget. This includes the label, hint and help.
     * They must all be objects implementing the XMLizable interface. This approach
     * allows to have mixed content in these data.
     * 
     * @param displayData an association of {name, sax fragment}
     */
    public void setDisplayData(Map displayData) {
        this.displayData = displayData;
    }
    
    public void addValidator(WidgetValidator validator) {
        if (this.validators == null) {
            this.validators = new ArrayList();
        }
        
        this.validators.add(validator);
    }
    
    public void generateDisplayData(String name, ContentHandler contentHandler) throws SAXException {
        Object data = this.displayData.get(name);
        if (data != null) {
            ((XMLizable)data).toSAX(contentHandler);
        } else if (!this.displayData.containsKey(name)) {
            throw new IllegalArgumentException("Unknown display data name '" + name + "'");
        }
    }
    
    public void generateDisplayData(ContentHandler contentHandler) throws SAXException {
        // Output all non-null display data
        Iterator iter = this.displayData.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (entry.getValue() != null) {
                String name = (String)entry.getKey();
                
                // Enclose the data into a "wi:{name}" element
                contentHandler.startElement(Constants.WI_NS, name, Constants.WI_PREFIX_COLON + name, Constants.EMPTY_ATTRS);

                ((XMLizable)entry.getValue()).toSAX(contentHandler);

                contentHandler.endElement(Constants.WI_NS, name, Constants.WI_PREFIX_COLON + name);
            }
        }   
    }
    
    /**
     * Validate a widget using the validators that were defined in its definition. If validation
     * fails, the validator has set a validation error on the widget or one of its children.
     * 
     * @param widget the widget
     * @param context the form context
     * @return <code>true</code> if validation was successful.
     */
    public boolean validate(Widget widget, FormContext context) {
        if (this.validators == null) {
            // No validators
            return true;
            
        } else {
            Iterator iter = this.validators.iterator();
            while(iter.hasNext()) {
                WidgetValidator validator = (WidgetValidator)iter.next();
                if (! validator.validate(widget, context)) {
                    // Stop at the first validator that fails
                    return false;
                }
            }
            // All validators were sucessful
            return true;
        }
    }
}
