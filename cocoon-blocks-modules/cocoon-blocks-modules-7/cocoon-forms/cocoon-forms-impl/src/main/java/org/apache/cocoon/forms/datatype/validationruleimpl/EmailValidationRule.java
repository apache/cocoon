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
package org.apache.cocoon.forms.datatype.validationruleimpl;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.oro.text.perl.Perl5Util;
import org.outerj.expression.ExpressionContext;

/**
 * <p>ValidationRule that checks that a string is an email address.</p>
 * <p>Based on code copied from org.apache.commons.validator.EmailValidator.</p>
 * 
 * @version $Id$
 */
public class EmailValidationRule extends AbstractValidationRule {

    private static final String SPECIAL_CHARS = "\\(\\)<>@,;:\\\\\\\"\\.\\[\\]";
    private static final String VALID_CHARS = "[^\\s" + SPECIAL_CHARS + "]";
    private static final String QUOTED_USER = "(\"[^\"]*\")";
    private static final String ATOM = VALID_CHARS + '+';
    private static final String WORD = "(" + ATOM + "|" + QUOTED_USER + ")";

    // Each pattern must be surrounded by /
    private static final String LEGAL_ASCII_PATTERN = "/^[\\000-\\177]+$/";
    private static final String EMAIL_PATTERN = "/^(.+)@(.+)$/";
    private static final String IP_DOMAIN_PATTERN =
            "/^(\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})$/";

    private static final String USER_PATTERN = "/^" + WORD + "(\\." + WORD + ")*$/";
    private static final String DOMAIN_PATTERN = "/^" + ATOM + "(\\." + ATOM + ")*$/";
    private static final String ATOM_PATTERN = "/(" + ATOM + ")/";

    public ValidationError validate(Object value, ExpressionContext expressionContext) {
        String email = (String)value;

        if (isEmail(email))
            return null;
        else
            return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.string.invalidemail", FormsConstants.I18N_CATALOGUE));
    }

    public boolean supportsType(Class clazz, boolean arrayType) {
        return clazz.isAssignableFrom(String.class) && !arrayType;
    }

    boolean isEmail(String email) {
        if (email == null) {
            return false;
        }

        Perl5Util matchAsciiPat = new Perl5Util();
        if (!matchAsciiPat.match(LEGAL_ASCII_PATTERN, email)) {
            return false;
        }

        // Check the whole email address structure
        Perl5Util emailMatcher = new Perl5Util();
        if (!emailMatcher.match(EMAIL_PATTERN, email)) {
            return false;
        }

        if (email.endsWith(".")) {
            return false;
        }

        if (!isValidUser(emailMatcher.group(1))) {
            return false;
        }

        if (!isValidDomain(emailMatcher.group(2))) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the domain component of an email address is valid.
     * @param domain being validatied.
     */
    protected boolean isValidDomain(String domain) {
        boolean symbolic = false;
        Perl5Util ipAddressMatcher = new Perl5Util();

        if (ipAddressMatcher.match(IP_DOMAIN_PATTERN, domain)) {
            if (!isValidIpAddress(ipAddressMatcher)) {
                return false;
            }
        } else {
            // Domain is symbolic name
            Perl5Util domainMatcher = new Perl5Util();
            symbolic = domainMatcher.match(DOMAIN_PATTERN, domain);
        }

        if (symbolic) {
            if (!isValidSymbolicDomain(domain)) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the user component of an email address is valid.
     * @param user being validated
     */
    protected boolean isValidUser(String user) {
        Perl5Util userMatcher = new Perl5Util();
        return userMatcher.match(USER_PATTERN, user);
    }

    /**
     * Validates an IP address. Returns true if valid.
     * @param ipAddressMatcher Pattren matcher
     */
    protected boolean isValidIpAddress(Perl5Util ipAddressMatcher) {
        for (int i = 1; i <= 4; i++) {
            String ipSegment = ipAddressMatcher.group(i);
            if (ipSegment == null || ipSegment.length() <= 0) {
                return false;
            }

            int iIpSegment = 0;

            try {
                iIpSegment = Integer.parseInt(ipSegment);
            } catch(NumberFormatException e) {
                return false;
            }

            if (iIpSegment > 255) {
                return false;
            }

        }
        return true;
    }

    /**
     * Validates a symbolic domain name.  Returns true if it's valid.
     * @param domain symbolic domain name
     */
    protected boolean isValidSymbolicDomain(String domain) {
        String[] domainSegment = new String[10];
        boolean match = true;
        int i = 0;
        Perl5Util atomMatcher = new Perl5Util();

        while (match) {
            match = atomMatcher.match(ATOM_PATTERN, domain);
            if (match) {
                domainSegment[i] = atomMatcher.group(1);
                int l = domainSegment[i].length() + 1;
                domain =
                        (l >= domain.length())
                        ? ""
                        : domain.substring(l);

                i++;
            }
        }

        int len = i;
        if (domainSegment[len - 1].length() < 2
                || domainSegment[len - 1].length() > 4) {

            return false;
        }

        // Make sure there's a host name preceding the domain.
        if (len < 2) {
            return false;
        }

        return true;
    }

}
