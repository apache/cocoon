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
package org.apache.cocoon.woody.util;

import org.apache.cocoon.transformation.I18nTransformer;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A XMLizable implementation that will produce SAX events for the
 * I18nTransformer in its toSAX method, based on the information
 * given in the constructor.
 *
 * <p>This generates an autonomous SAX-blurb, i.e. all necessary namespace
 * declarations will be made, and no start/endDocument events will be generated.
 */
public class I18nMessage implements XMLizable {
    private String key;
    private String catalogue;
    private String[] parameters;
    private boolean[] keys;

    /**
     * @param key a message key, to be translated by the I18nTransformer
     */
    public I18nMessage(String key) {
        this(key, (String) null);
    }

    /**
     * @param key a message key, to be translated by the I18nTransformer
     * @param catalogue a named I18nTransformer catalogue to use
     */
    public I18nMessage(String key, String catalogue) {
        this.key = key;
        this.catalogue = catalogue;
    }

    /**
     * @param key a message key, to be translated by the I18nTransformer
     * @param parameters parameters to be substituted in the errorMessage (will be
     * done by the I18nTransformer)
     */
    public I18nMessage(String key, String[] parameters) {
        this(key, parameters, (String)null);
    }

    /**
     * @param key a message key, to be translated by the I18nTransformer
     * @param parameters parameters to be substituted in the errorMessage (will be
     * done by the I18nTransformer)
     * @param catalogue a named I18nTransformer catalogue to use
     */
    public I18nMessage(String key, String[] parameters, String catalogue) {
        this.key = key;
        this.parameters = parameters;
        this.catalogue = catalogue;
    }

    /**
     * @param key a message key, to be translated by the I18nTransformer
     * @param parameters parameters to be substituted in the errorMessage (will be
     * done by the I18nTransformer)
     * @param keys Each element in the keys array corresponds to a string in the parameters array
     * and indicates whether that parameter is in itself again a key.
     */
    public I18nMessage(String key, String[] parameters, boolean[] keys) {
        this(key, parameters, keys, null);
    }

    /**
     * @param key a message key, to be translated by the I18nTransformer
     * @param parameters parameters to be substituted in the errorMessage (will be
     * done by the I18nTransformer)
     * @param keys Each element in the keys array corresponds to a string in the parameters array
     * and indicates whether that parameter is in itself again a key.
     * @param catalogue a named I18nTransformer catalogue to use
     */
    public I18nMessage(String key, String[] parameters, boolean[] keys, String catalogue) {
        this.key = key;
        this.parameters = parameters;
        this.keys = keys;
        this.catalogue = catalogue;
    }

    public void toSAX(ContentHandler contentHandler) throws SAXException {
        contentHandler.startPrefixMapping("i18n", I18nTransformer.I18N_NAMESPACE_URI);
        AttributesImpl i18nAttrs = new AttributesImpl();
        if (parameters != null) {
            contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TRANSLATE_ELEMENT, "i18n:" + I18nTransformer.I18N_TRANSLATE_ELEMENT, Constants.EMPTY_ATTRS);
        }
        if (catalogue != null)
            i18nAttrs.addCDATAAttribute(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_CATALOGUE_ATTRIBUTE, "i18n:" + I18nTransformer.I18N_CATALOGUE_ATTRIBUTE, catalogue);
        contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT, i18nAttrs);
        contentHandler.characters(key.toCharArray(), 0, key.length());
        contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT);
        // the parameters
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_PARAM_ELEMENT, "i18n:" + I18nTransformer.I18N_PARAM_ELEMENT, Constants.EMPTY_ATTRS);
                if (keys != null && keys[i])
                    contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT, i18nAttrs);
                contentHandler.characters(parameters[i].toCharArray(), 0, parameters[i].length());
                if (keys != null && keys[i])
                    contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT);
                contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_PARAM_ELEMENT, "i18n:" + I18nTransformer.I18N_PARAM_ELEMENT);
            }
            contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TRANSLATE_ELEMENT, "i18n:" + I18nTransformer.I18N_TRANSLATE_ELEMENT);
        }
        contentHandler.endPrefixMapping("i18n");
    }
}
