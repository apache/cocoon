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
package org.apache.cocoon.woody.datatype.convertor;

import org.w3c.dom.Element;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.i18n.I18nUtils;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Builds {@link FormattingDateConvertor}s.
 *
 * @version CVS $Id: FormattingDateConvertorBuilder.java,v 1.2 2003/12/31 17:15:46 vgritsenko Exp $
 */
public class FormattingDateConvertorBuilder implements ConvertorBuilder {
    public Convertor build(Element configElement) throws Exception {
        FormattingDateConvertor convertor = new FormattingDateConvertor();

        if (configElement == null)
            return convertor;

        String style = configElement.getAttribute("style");
        if (!style.equals("")) {
            if (style.equals("short"))
                convertor.setStyle(DateFormat.SHORT);
            else if (style.equals("medium"))
                convertor.setStyle(DateFormat.MEDIUM);
            else if (style.equals("long"))
                convertor.setStyle(DateFormat.LONG);
            else if (style.equals("full"))
                convertor.setStyle(DateFormat.FULL);
            else
                throw new Exception("Invalid value \"" + style + "\" for style attribute at " + DomHelper.getLocation(configElement));
        }

        String variant = configElement.getAttribute("variant");
        if (!variant.equals("")) {
            if (variant.equals("date"))
                convertor.setVariant(FormattingDateConvertor.DATE);
            else if (variant.equals("time"))
                convertor.setVariant(FormattingDateConvertor.TIME);
            else if (variant.equals("datetime"))
                convertor.setVariant(FormattingDateConvertor.DATE_TIME);
            else
                throw new Exception("Invalid value \"" + variant + "\" for variant attribute at " + DomHelper.getLocation(configElement));
        }

        Element patternsEl = DomHelper.getChildElement(configElement, Constants.WD_NS, "patterns", false);
        if (patternsEl != null) {
            Element patternEl[] = DomHelper.getChildElements(patternsEl, Constants.WD_NS, "pattern");
            for (int i = 0; i < patternEl.length; i++) {
                String locale = patternEl[i].getAttribute("locale");
                String pattern = DomHelper.getElementText(patternEl[i]);
                if (pattern.equals(""))
                    throw new Exception("pattern element does not contain any content at " + DomHelper.getLocation(patternEl[i]));
                if (locale.equals(""))
                    convertor.setNonLocalizedPattern(pattern);
                else {
                    Locale loc = I18nUtils.parseLocale(locale);
                    convertor.addFormattingPattern(loc, pattern);
                }
            }
        }

        return convertor;
    }
}
