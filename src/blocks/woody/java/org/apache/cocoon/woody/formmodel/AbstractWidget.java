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
import java.util.Locale;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.apache.cocoon.woody.validation.WidgetValidator;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstract base class for Widget implementations. Provides functionality
 * common to many widgets.
 * 
 * @version $Id: AbstractWidget.java,v 1.10 2004/02/11 10:43:30 antonio Exp $
 */
public abstract class AbstractWidget implements Widget {
    private String location;
    private Widget parent;
    private Form form;
    protected AbstractWidgetDefinition definition;
    
    private List validators;

    /**
     * Sets the definition of this widget.
     */
    protected void setDefinition(AbstractWidgetDefinition definition) {
        this.definition = definition;
    }

    /**
     * Gets the id of this widget.
     */
    public String getId() {
        return definition.getId();
    }

    /**
     * Sets the source location of this widget.
     */
    protected void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the source location of this widget.
     */
    public String getLocation() {
        return this.location;
    }

    public Widget getParent() {
        return parent;
    }

    public void setParent(Widget widget) {
        this.parent = widget;
    }

    public Form getForm() {
        if (this.form == null) {
            if (parent == null) {
                this.form = (Form)this;
            } else {
                this.form = parent.getForm();
            }
        }
        return this.form;
    }

    public String getNamespace() {
        if (getParent() != null && getParent().getNamespace().length() > 0) {
            return getParent().getNamespace() + "." + getId();
        } else {
            return getId();
        }
    }

    public String getFullyQualifiedId() {
        if (parent != null) {
            String namespace = parent.getNamespace();
            if (namespace.length() > 0) {
                return namespace + "." + getId();
            }
        }
        return getId();
    }

    public Object getValue() {
        return null;
    }

    public void setValue(Object object) {
        throw new RuntimeException("Cannot set the value of widget " + getFullyQualifiedId());
    }

    public boolean isRequired() {
        return false;
    }

    public Widget getWidget(String id) {
        return null;
    }
    
    public void broadcastEvent(WidgetEvent event) {
        throw new UnsupportedOperationException("Widget " + this.getFullyQualifiedId() + " doesn't handle events.");
    }
    
    /**
     * Add a validator to this widget instance.
     * 
     * @param validator
     */
    public void addValidator(WidgetValidator validator) {
        if (this.validators == null) {
            this.validators = new ArrayList();
        }
        
        this.validators.add(validator);
    }
    
    /**
     * Remove a validator from this widget instance
     * 
     * @param validator
     * @return <code>true</code> if the validator was found.
     */
    public boolean removeValidator(WidgetValidator validator) {
        return (this.validators == null)? false : this.validators.remove(validator);
    }
    
    public boolean validate(FormContext context) {
        // Test validators from the widget definition
        if (!this.definition.validate(this, context)) {
            // Failed
            return false;
        } else {
            // Definition sussessful, test local validators
            if (this.validators == null) {
                // No local validators
                return true;
            } else {
                // Iterate on local validators
                Iterator iter = this.validators.iterator();
                while(iter.hasNext()) {
                    WidgetValidator validator = (WidgetValidator)iter.next();
                    if (!validator.validate(this, context)) {
                        return false;
                    }
                }
                // All local iterators successful
                return true;
            }
        }
    }
    
    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        if (definition != null) {
            definition.generateDisplayData("label", contentHandler);
        }
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // Do nothing
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale, String element, WidgetDefinition definition)
    throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addCDATAAttribute("id", getFullyQualifiedId());
        contentHandler.startElement(Constants.WI_NS, element, Constants.WI_PREFIX_COLON + element, attrs);
        generateItemSaxFragment(contentHandler, locale);
        contentHandler.endElement(Constants.WI_NS, element, Constants.WI_PREFIX_COLON + element);
    }
}
