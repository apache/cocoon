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
 *
 * @version CVS $Id: I18nMessage.java,v 1.7 2004/03/09 13:54:24 reinhard Exp $
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
        if (parameters != null) {
            contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TRANSLATE_ELEMENT, "i18n:" + I18nTransformer.I18N_TRANSLATE_ELEMENT, Constants.EMPTY_ATTRS);
        }

        AttributesImpl i18nAttrs = new AttributesImpl();
        if (catalogue != null) {
            i18nAttrs.addCDATAAttribute(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_CATALOGUE_ATTRIBUTE, "i18n:" + I18nTransformer.I18N_CATALOGUE_ATTRIBUTE, catalogue);
        }

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
