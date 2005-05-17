/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.forms.formmodel;

import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * A {@link Field} for CAPTCHA validation. Upon generation, a secret random string is stored
 * in a session attribute having the same name as the field's ID, for use by a 
 * {@link org.apache.cocoon.forms.validation.impl.CaptchaValidator}.
 * <br>
 * Usage sample:
 * <pre>
    &lt;fd:captcha id="f1" required="true">
      &lt;fd:label>Copy the number shown into the input field&lt;/fd:label>
      &lt;fd:datatype base="string"/>
      &lt;fd:validation>
        &lt;fd:captcha/>
      &lt;/fd:validation>
    &lt;/fd:captcha>
 * </pre>
 * 
 * @see http://www.captcha.net/
 * @version CVS $Id$
 */
public class CaptchaField extends Field {

    private static final String IMAGE_EL = "captcha-image";
    public static final String SESSION_ATTR_PREFIX = "captcha-";
    private static final String SECRET_CHARS = "abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
    private static final int SECRET_LENGTH = 7;
    private Context avalonContext;
    
    private String generateSecret() {
        StringBuffer secret = new StringBuffer(SECRET_LENGTH);
        for (int n = 0 ; n < SECRET_LENGTH ; n++) {
            int randomnumber = (int) Math.floor(SECRET_CHARS.length() * Math.random());
            secret.append(SECRET_CHARS.charAt(randomnumber)); 
        }
        return secret.toString();
    }

    public CaptchaField(FieldDefinition fieldDefinition, Context avalonContext) {
        super(fieldDefinition);
        this.avalonContext = avalonContext;
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        super.generateItemSaxFragment(contentHandler, locale);
        contentHandler.startElement(Constants.INSTANCE_NS, IMAGE_EL, Constants.INSTANCE_PREFIX_COLON + IMAGE_EL, XMLUtils.EMPTY_ATTRIBUTES);
        contentHandler.endElement(Constants.INSTANCE_NS, IMAGE_EL, Constants.INSTANCE_PREFIX_COLON + IMAGE_EL);
        Map objectModel = ContextHelper.getObjectModel(this.avalonContext);
        Session session = ObjectModelHelper.getRequest(objectModel).getSession(true);
        session.setAttribute(SESSION_ATTR_PREFIX + getId(), generateSecret());
    }
    
}
