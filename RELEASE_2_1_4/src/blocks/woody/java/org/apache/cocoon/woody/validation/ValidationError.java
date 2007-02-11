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
package org.apache.cocoon.woody.validation;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.cocoon.woody.util.I18nMessage;
import org.apache.cocoon.woody.util.StringMessage;
import org.apache.excalibur.xml.sax.XMLizable;

/**
 * An object that holds a validation error message. The error message can
 * be a simple string or a piece of XML.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: ValidationError.java,v 1.1 2004/02/04 17:25:58 sylvain Exp $
 */
public class ValidationError {

    /** Holds the error message. */
    private XMLizable saxFragment;

    /**
     * @param i18n should the errorMessage be interpreted as an i18n key?
     */
    public ValidationError(String errorMessage, boolean i18n) {
        if (i18n) {
            saxFragment = new I18nMessage(errorMessage);
        } else {
            saxFragment = new StringMessage(errorMessage);
        }
    }

    /**
     * @see I18nMessage#I18nMessage(java.lang.String)
     */
    public ValidationError(String errorMessageKey) {
        this.saxFragment = new I18nMessage(errorMessageKey);
    }

    /**
     * @see I18nMessage#I18nMessage(java.lang.String, java.lang.String[])
     */
    public ValidationError(String errorMessageKey, String[] parameters) {
        this.saxFragment = new I18nMessage(errorMessageKey, parameters);
    }

    /**
     * @see I18nMessage#I18nMessage(java.lang.String, java.lang.String[], boolean[])
     */
    public ValidationError(String errorMessageKey, String[] parameters, boolean[] keys) {
        this.saxFragment = new I18nMessage(errorMessageKey, parameters, keys);
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
        if (saxFragment != null) {
            saxFragment.toSAX(contentHandler);
        }
    }
}
