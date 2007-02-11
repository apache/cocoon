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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.transformation.I18nTransformer;
import org.apache.cocoon.woody.Constants;

/**
 * An object that holds a validation error message. The error message can
 * be a simple string (which should be a message bundle key) or a piece of XML.
 */
public class ValidationError {
    /** Holds the error message compiled using the {@link org.apache.cocoon.components.sax.XMLByteStreamCompiler}. */
    private Object saxFragment;
    /** Holds a simple string error message. */
    private String errorMessage;
    /** Should the errorMessage be interpreted as a resource bundle key? */
    private boolean i18n;
    private String[] errorMessageParameters;
    private boolean[] keys;

    /**
     * @param i18n should the errorMessage be interpreted as an i18n key?
     */
    public ValidationError(String errorMessage, boolean i18n) {
        this.errorMessage = errorMessage;
        this.i18n = i18n;
    }

    /**
     * @param errorMessageKey a message key, to be translated by the I18nTransformer
     */
    public ValidationError(String errorMessageKey) {
        this.errorMessage = errorMessageKey;
        this.i18n = true;
    }

    /**
     * @param parameters parameters to be substituted in the errorMessage (will be
     * done by the I18nTransformer)
     */
    public ValidationError(String errorMessageKey, String[] parameters) {
        this.errorMessage = errorMessageKey;
        this.errorMessageParameters = parameters;
        this.keys = null;
        this.i18n = true;
    }

    /**
     * @param keys Each element in the keys array corresponds to a string in the parameters array
     * and indicates whether that parameter is in itself again a key.
     */
    public ValidationError(String errorMessageKey, String[] parameters, boolean[] keys) {
        this.errorMessage = errorMessageKey;
        this.errorMessageParameters = parameters;
        this.keys = keys;
        this.i18n = true;
    }

    public ValidationError(Object errorMessage) {
        this.saxFragment = errorMessage;
    }

    /**
     * Generates SAX events for this ValidationError. In case of the constructors where
     * a String error message key was supplied, the necessary I18n tags will be generated.
     */
    public void generateSaxFragment(ContentHandler contentHandler) throws SAXException {
        if (saxFragment != null) {
            XMLByteStreamInterpreter byteStreamInterpreter = new XMLByteStreamInterpreter();
            byteStreamInterpreter.setContentHandler(contentHandler);
            byteStreamInterpreter.deserialize(saxFragment);
        } else if (errorMessageParameters != null) {
            contentHandler.startPrefixMapping("i18n", I18nTransformer.I18N_NAMESPACE_URI);

            contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TRANSLATE_ELEMENT, "i18n:" + I18nTransformer.I18N_TRANSLATE_ELEMENT, Constants.EMPTY_ATTRS);

            // the i18n:text element
            AttributesImpl i18nAttrs = new AttributesImpl();
            i18nAttrs.addAttribute(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_CATALOGUE_ATTRIBUTE, "i18n:" + I18nTransformer.I18N_CATALOGUE_ATTRIBUTE, "CDATA", "woody");

            contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT, i18nAttrs);
            contentHandler.characters(errorMessage.toCharArray(), 0, errorMessage.length());
            contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT);

            // the parameters
            for (int i = 0; i < errorMessageParameters.length; i++) {
                contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_PARAM_ELEMENT, "i18n:" + I18nTransformer.I18N_PARAM_ELEMENT, Constants.EMPTY_ATTRS);
                if (keys != null && keys[i])
                    contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT, i18nAttrs);
                contentHandler.characters(errorMessageParameters[i].toCharArray(), 0, errorMessageParameters[i].length());
                if (keys != null && keys[i])
                    contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT);
                contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_PARAM_ELEMENT, "i18n:" + I18nTransformer.I18N_PARAM_ELEMENT);
            }

            contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TRANSLATE_ELEMENT, "i18n:" + I18nTransformer.I18N_TRANSLATE_ELEMENT);

            contentHandler.endPrefixMapping("i18n");
        } else if (i18n) {
            contentHandler.startPrefixMapping("i18n", I18nTransformer.I18N_NAMESPACE_URI);

            AttributesImpl i18nAttrs = new AttributesImpl();
            i18nAttrs.addAttribute(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_CATALOGUE_ATTRIBUTE, "i18n:" + I18nTransformer.I18N_CATALOGUE_ATTRIBUTE, "CDATA", "woody");

            contentHandler.startElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT, i18nAttrs);
            contentHandler.characters(errorMessage.toCharArray(), 0, errorMessage.length());
            contentHandler.endElement(I18nTransformer.I18N_NAMESPACE_URI, I18nTransformer.I18N_TEXT_ELEMENT, "i18n:" + I18nTransformer.I18N_TEXT_ELEMENT);

            contentHandler.endPrefixMapping("i18n");
        } else {
            contentHandler.characters(errorMessage.toCharArray(), 0, errorMessage.length());
        }
    }
}
