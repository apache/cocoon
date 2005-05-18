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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * A {@link Field} for CAPTCHA validation. Upon generation, a secret random string is stored
 * in a session attribute having a randomly generated name, for use by a 
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

    public static final String SESSION_ATTR_PREFIX = "captcha-";

    private static final String IMAGE_EL = "captcha-image";
    private static final String SECRET_CHARS = "abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
    private static final int SECRET_LENGTH = 7;
    private static final int SESSION_ATTR_NAME_LENGTH = 6;
    
    private Context avalonContext;

    /**
     * Random number generator used to create session attribute name.
     */
    protected static SecureRandom random;

    static {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch(java.security.NoSuchAlgorithmException nsae) {
            // Maybe we are on IBM's SDK
            try {
                random = SecureRandom.getInstance("IBMSecureRandom");
            } catch (NoSuchAlgorithmException e) {
                throw new CascadingRuntimeException("No random number generator available", e);
            }
        }
        random.setSeed(System.currentTimeMillis());
    }

    public CaptchaField(FieldDefinition fieldDefinition, Context avalonContext) {
        super(fieldDefinition);
        this.avalonContext = avalonContext;
    }
    
    private String generateSecret() {
        StringBuffer secret = new StringBuffer(SECRET_LENGTH);
        for (int n = 0 ; n < SECRET_LENGTH ; n++) {
            int randomnumber = random.nextInt(SECRET_CHARS.length());
            secret.append(SECRET_CHARS.charAt(randomnumber)); 
        }
        return secret.toString();
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        super.generateItemSaxFragment(contentHandler, locale);
        byte[] bytes = new byte[SESSION_ATTR_NAME_LENGTH];
        char[] result = new char[bytes.length * 2];
        random.nextBytes(bytes);
        for (int i = 0; i < SESSION_ATTR_NAME_LENGTH; i++) {
            byte ch = bytes[i];
            result[2 * i] = Character.forDigit(Math.abs(ch >> 4), 16);
            result[2 * i + 1] = Character.forDigit(Math.abs(ch & 0x0f), 16);
        }
        String id = new String(result);
        Map objectModel = ContextHelper.getObjectModel(this.avalonContext);
        Session session = ObjectModelHelper.getRequest(objectModel).getSession(true);
        String secret = generateSecret();
        session.setAttribute(SESSION_ATTR_PREFIX + id, secret);
        this.setAttribute("secret", secret);
        AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute("", "id", "id", "PCDATA", id);
        contentHandler.startElement(Constants.INSTANCE_NS, IMAGE_EL, Constants.INSTANCE_PREFIX_COLON + IMAGE_EL, attrs);
        contentHandler.endElement(Constants.INSTANCE_NS, IMAGE_EL, Constants.INSTANCE_PREFIX_COLON + IMAGE_EL);
    }
    
}
