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
 * @version CVS $Id: MonthsTag.java,v 1.3 2004/03/05 13:02:25 bdelacretaz Exp $
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
