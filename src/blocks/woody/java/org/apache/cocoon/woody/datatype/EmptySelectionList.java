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
package org.apache.cocoon.woody.datatype;

import java.util.Locale;

import org.apache.cocoon.transformation.I18nTransformer;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: EmptySelectionList.java,v 1.2 2003/10/03 13:40:41 sylvain Exp $
 */
public class EmptySelectionList implements SelectionList {
    private String text;
    private boolean i18n;
    
    public EmptySelectionList(String text) {
        this.text = text;
        this.i18n = false;
    }
    
    public EmptySelectionList(String text, boolean i18n) {
        this.text = text;
        this.i18n = i18n;
    }

    public Datatype getDatatype() {
        // Cannot return anything meaningful
        return null;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // Start wi:selection list
        contentHandler.startElement(Constants.WI_NS, SELECTION_LIST_EL, Constants.WI_PREFIX_COLON + SELECTION_LIST_EL, Constants.EMPTY_ATTRS);

        // Start wi:item
        AttributesImpl itemAttrs = new AttributesImpl();
        itemAttrs.addCDATAAttribute("value", "");
        contentHandler.startElement(Constants.WI_NS, ITEM_EL, Constants.WI_PREFIX_COLON + ITEM_EL, itemAttrs);

        // Start wi:label
        contentHandler.startElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL, Constants.EMPTY_ATTRS);
        if (this.text != null) {

            if (i18n) {
                contentHandler.startPrefixMapping("i18n", I18nTransformer.I18N_NAMESPACE_URI);
        
                contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT, Constants.EMPTY_ATTRS);
                contentHandler.characters(this.text.toCharArray(), 0, this.text.length());
                contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT);
    
                contentHandler.endPrefixMapping("i18n");
            } else {
                contentHandler.characters(this.text.toCharArray(), 0, this.text.length());
            }
        }
        
        // End wi:label
        contentHandler.endElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL);
        
        // End wi:item
        contentHandler.endElement(Constants.WI_NS, ITEM_EL, Constants.WI_PREFIX_COLON + ITEM_EL);
        
        // End wi:selection-list
        contentHandler.endElement(Constants.WI_NS, SELECTION_LIST_EL, Constants.WI_PREFIX_COLON + SELECTION_LIST_EL);
    }
}
