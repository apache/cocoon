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

import java.util.Locale;
import java.util.Iterator;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A general-purpose abstract Widget which can hold zero or more widgets.
 *
 * CVS $Id: AbstractContainerWidget.java,v 1.4 2004/02/04 17:25:57 sylvain Exp $
 * @author Timothy Larson
 */
public abstract class AbstractContainerWidget extends AbstractWidget implements ContainerWidget {
    protected ContainerDelegate widgets;

    public AbstractContainerWidget(AbstractWidgetDefinition definition) {
        setDefinition(definition);
        setLocation(definition.getLocation());
        widgets = new ContainerDelegate(definition);
    }

    public void addWidget(Widget widget) {
        widget.setParent(this);
        widgets.addWidget(widget);
    }

    public boolean hasWidget(String id) {
        return widgets.hasWidget(id);
    }

    public Widget getWidget(String id) {
    	return widgets.getWidget(id);
    }

    public Iterator getChildren() {
        return widgets.iterator();
    }

    public void readFromRequest(FormContext formContext) {
        widgets.readFromRequest(formContext);
    }

    public boolean validate(FormContext formContext) {
        // Validate self only if child widgets are valid
        if (widgets.validate(formContext)) {
            return super.validate(formContext);
        } else {
            return false;
        }
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale, String element) throws SAXException {
        if (getId() == null || getId().equals("")) {
            contentHandler.startElement(Constants.WI_NS, element, Constants.WI_PREFIX_COLON + element, Constants.EMPTY_ATTRS);
        } else {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", getFullyQualifiedId());
            contentHandler.startElement(Constants.WI_NS, element, Constants.WI_PREFIX_COLON + element, attrs);
        }
        if (definition != null)
            definition.generateDisplayData(contentHandler);
        // The child widgets
        widgets.generateSaxFragment(contentHandler, locale);
        contentHandler.endElement(Constants.WI_NS, element, Constants.WI_PREFIX_COLON + element);
    }
}
