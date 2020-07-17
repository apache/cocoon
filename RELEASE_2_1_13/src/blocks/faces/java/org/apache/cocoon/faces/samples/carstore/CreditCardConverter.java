/*
 * $Id$
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package org.apache.cocoon.faces.samples.carstore;


import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * CreditCardConverter Class accepts a Credit Card Number of type String
 * and strips blanks and <oode>"-"</code> if any from it. It also formats the
 * CreditCardNumber such a blank space separates every four characters.
 * Blanks and <oode>"-"</code> characters are the expected demiliters
 * that could be used as part of a CreditCardNumber.
 */
public class CreditCardConverter implements Converter {

    /**
     * <p>The message identifier of the Message to be created if
     * the conversion fails.  The message format string for this
     * message may optionally include a <code>{0}</code> and
     * <code>{1}</code> placeholders, which
     * will be replaced by the object and value.</p>
     */
    public static final String CONVERSION_ERROR_MESSAGE_ID =
        "carstore.Conversion_Error";


    /**
     * Parses the CreditCardNumber and strips any blanks or <oode>"-"</code>
     * characters from it.
     */
    public Object getAsObject(FacesContext context, UIComponent component,
                              String newValue) throws ConverterException {

        String convertedValue = null;
        if (newValue == null) {
            return newValue;
        }
        // Since this is only a String to String conversion, this conversion
        // does not throw ConverterException.
        convertedValue = newValue.trim();
        if (((convertedValue.indexOf("-")) != -1) ||
            ((convertedValue.indexOf(" ")) != -1)) {
            char[] input = convertedValue.toCharArray();
            StringBuffer buffer = new StringBuffer(50);
            for (int i = 0; i < input.length; ++i) {
                if (input[i] == '-' || input[i] == ' ') {
                    continue;
                } else {
                    buffer.append(input[i]);
                }
            }
            convertedValue = buffer.toString();
        }
        // System.out.println("Converted value " + convertedValue);
        return convertedValue;
    }


    /**
     * Formats the value by inserting space after every four characters
     * for better readability if they don't already exist. In the process
     * converts any <oode>"-"</code> characters into blanks for consistency.
     */
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) throws ConverterException {

        String inputVal = null;
        if (value == null) {
            return null;
        }
        // value must be of the type that can be cast to a String.
        try {
            inputVal = (String) value;
        } catch (ClassCastException ce) {
            FacesMessage errMsg = MessageFactory.getMessage(
                CONVERSION_ERROR_MESSAGE_ID,
                (new Object[]{value, inputVal}));
            throw new ConverterException(errMsg.getSummary());
        }

        // insert spaces after every four characters for better
        // readability if it doesn't already exist.
        char[] input = inputVal.toCharArray();
        StringBuffer buffer = new StringBuffer(50);
        for (int i = 0; i < input.length; ++i) {
            if ((i % 4) == 0 && i != 0) {
                if (input[i] != ' ' || input[i] != '-') {
                    buffer.append(" ");
                    // if there any "-"'s convert them to blanks.
                } else if (input[i] == '-') {
                    buffer.append(" ");
                }
            }
            buffer.append(input[i]);
        }
        String convertedValue = buffer.toString();
        // System.out.println("Formatted value " + convertedValue);
        return convertedValue;
    }
}
