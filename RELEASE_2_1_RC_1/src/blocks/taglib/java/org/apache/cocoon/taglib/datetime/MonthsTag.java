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

package org.apache.cocoon.taglib.datetime;

import java.text.DateFormatSymbols;
import java.util.Locale;

import org.apache.cocoon.taglib.IterationTag;
import org.apache.cocoon.taglib.TagSupport;
import org.apache.cocoon.taglib.VarTagSupport;
import org.apache.cocoon.taglib.i18n.LocaleTag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Tag <b>months</b>, used to loop through all the months of the year.
 * <p>
 * The script variable of name <b>var</b> is availble only within the
 * body of the <b>months</b> tag.
 *
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: MonthsTag.java,v 1.2 2003/03/16 17:49:08 vgritsenko Exp $
 */
public class MonthsTag extends VarTagSupport implements IterationTag {
    private String[] short_months = null;
    private String[] long_months = null;
    private int month;
    private int month_num;

    /**
     * Initializes tag so it can loop through the months of the year.
     *
     * @return EVAL_BODY
     */
    public final int doStartTag(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        // Initialize variables
        month = 0;
        month_num = 1;

        Locale locale = null;
        LocaleTag localeTag = (LocaleTag) TagSupport.findAncestorWithClass(this, LocaleTag.class);
        if (localeTag != null) {
            locale = localeTag.getLocale();
        } else {
            locale = Locale.getDefault();
        }

        DateFormatSymbols dfs = new DateFormatSymbols(locale);
        short_months = dfs.getShortMonths();
        long_months = dfs.getMonths();

        // Make sure we skip any blank array elements
        while (month < long_months.length && (long_months[month] == null || long_months[month].length() == 0)) {
            month++;
        }

        if (month >= short_months.length)
            return SKIP_BODY;

        setVariable(var, this);
        return EVAL_BODY;
    }

    /*
     * @see Tag#doEndTag(String, String, String)
     */
    public int doEndTag(String namespaceURI, String localName, String qName) throws SAXException {
        removeVariable(var);
        return EVAL_PAGE;
    }

    /**
     * Method called at end of each months tag.
     *
     * @return EVAL_BODY_TAG if there is another month, or SKIP_BODY if there are no more months
     */
    public final int doAfterBody() throws SAXException {
        // See if we are done looping through months
        month++;
        month_num++;
        if (month >= short_months.length)
            return SKIP_BODY;

        // Make sure we skip any blank array elements
        while (month < long_months.length && (long_months[month] == null || long_months[month].length() == 0)) {
            month++;
        }

        if (month >= short_months.length)
            return SKIP_BODY;

        // There is another month, so loop again
        return EVAL_BODY_AGAIN;
    }

    /**
     * Returns the short name of the month.
     *
     * @return String - short name of the month
     */
    public final String getShortMonth() {
        return short_months[month];
    }

    /**
     * Returns the long name of the month.
     *
     * @return String - long name of the month
     */
    public final String getMonth() {
        return long_months[month];
    }

    /**
     * Returns the number of the month.
     *             
     * @return String - number of the month
     */
    public final String getMonthOfYear() {
        if (month_num < 10)
            return "0" + month_num;
        return "" + month_num;
    }

    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        this.short_months = null;
        this.long_months = null;
        super.recycle();
    }

}
