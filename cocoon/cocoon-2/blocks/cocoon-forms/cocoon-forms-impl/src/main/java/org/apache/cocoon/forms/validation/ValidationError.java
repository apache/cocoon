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
package org.apache.cocoon.forms.validation;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.util.StringMessage;
import org.apache.commons.lang.ObjectUtils;
import org.apache.excalibur.xml.sax.XMLizable;

/**
 * An object that holds a validation error message. The error message can
 * be a simple string or a piece of XML.
 * 
 * @version $Id$
 */
public class ValidationError {

    /** Holds the error message. */
    private final XMLizable saxFragment;

    /**
     * @param i18n should the errorMessage be interpreted as an i18n key?
     */
    public ValidationError(String errorMessage, boolean i18n) {
        if (i18n) {
            this.saxFragment = new I18nMessage(errorMessage);
        } else {
            this.saxFragment = new StringMessage(errorMessage);
        }
    }

    /**
     * @see I18nMessage#I18nMessage(java.lang.String)
     */
    public ValidationError(String errorMessageKey) {
        this(new I18nMessage(errorMessageKey));
    }

    /**
     * @see I18nMessage#I18nMessage(java.lang.String, java.lang.String[])
     */
    public ValidationError(String errorMessageKey, String[] parameters) {
        this(new I18nMessage(errorMessageKey, parameters));
    }

    /**
     * @see I18nMessage#I18nMessage(java.lang.String, java.lang.String[], boolean[])
     */
    public ValidationError(String errorMessageKey, String[] parameters, boolean[] keys) {
        this(new I18nMessage(errorMessageKey, parameters, keys));
    }

    /**
     * @param errorMessage the errormessages in the form of something that is "XMLizable",
     * i.e. can produce SAX events. It should however not produce start/endDocument calls,
     * only a piece of embeddable, stand-alone SAX events. Helpful implementations are
     * {@link org.apache.cocoon.xml.SaxBuffer SaxBuffer}, {@link I18nMessage} or {@link StringMessage}.
     */
    public ValidationError(XMLizable errorMessage) {
        this.saxFragment = errorMessage;
    }

    /**
     * Generates SAX events for this ValidationError. In case of the constructors where
     * a String error message key was supplied, the necessary I18n tags will be generated.
     */
    public void generateSaxFragment(ContentHandler contentHandler) throws SAXException {
        if (this.saxFragment != null) {
            this.saxFragment.toSAX(contentHandler);
        }
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof ValidationError) {
            return ObjectUtils.equals(this.saxFragment, ((ValidationError)obj).saxFragment);
        }
        return false;
    }
}
