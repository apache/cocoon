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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Generates an XML document representing a calendar for a given month and year.
 * <p>
 * Here is a sample output:
 * <pre>
 * &lt;calendar:calendar xmlns:calendar="http://apache.org/cocoon/calendar/1.0"
 *     year="2004" month="January" prevMonth="12" prevYear="2003"
 *     nextMonth="02" nextYear="2004"&gt;
 *   &lt;calendar:week number="1"&gt;
 *     &lt;calendar:day number="1" date="January 1, 2004"/&gt;
 *     &lt;calendar:day number="2" date="January 2, 2004"/&gt;
 *     &lt;calendar:day number="3" date="January 3, 2004"/&gt;
 *     &lt;calendar:day number="4" date="January 4, 2004"/&gt;
 *   &lt;/calendar:week&gt;
 *   ...
 * &lt;/calendar:calendar&gt;
 * </pre>
 * <p>
 * The <i>src</i> parameter is ignored.
 * </p>
 * <p>
 *  <b>Configuration options:</b>
 *  <dl>
 *   <dt> <i>month</i> (optional)
 *   <dd> Sets the month for the calendar (January is 1). Default is the current month.
 *   <dt> <i>year</i> (optional)
 *   <dd> Sets the year for the calendar. Default is the current year.
 *   <dt> <i>dateFormat</i> (optional)
 *   <dd> Sets the format for the date attribute of each node, as
 *        described in java.text.SimpleDateFormat. If unset, the default
 *        format for the current locale will be used.
 *   <dt> <i>lang</i> (optional)
 *   <dd> Sets the ISO language code for determining the locale.
 *   <dt> <i>country</i> (optional)
 *   <dd> Sets the ISO country code for determining the locale.
 *  </dl>
 * </p>
 * 
 * @version CVS $Id: CalendarGenerator.java,v 1.7 2004/04/15 16:15:50 ugo Exp $
 */
public class CalendarGenerator extends ServiceableGenerator implements CacheableProcessingComponent {
    
    /** The URI of the namespace of this generator. */
    protected static final String URI = "http://apache.org/cocoon/calendar/1.0";
    
    /** The namespace prefix for this namespace. */
    protected static final String PREFIX = "calendar";
    
    /** Node and attribute names */
    protected static final String CALENDAR_NODE_NAME   = "calendar";
    protected static final String WEEK_NODE_NAME       = "week";
    protected static final String DAY_NODE_NAME        = "day";
    protected static final String MONTH_ATTR_NAME      = "month";
    protected static final String YEAR_ATTR_NAME       = "year";
    protected static final String DATE_ATTR_NAME       = "date";
    protected static final String NUMBER_ATTR_NAME     = "number";
    protected static final String PREV_MONTH_ATTR_NAME = "prevMonth";
    protected static final String PREV_YEAR_ATTR_NAME  = "prevYear";
    protected static final String NEXT_MONTH_ATTR_NAME = "nextMonth";
    protected static final String NEXT_YEAR_ATTR_NAME  = "nextYear";
    
    /** Formatter for month number */
    protected static final DecimalFormat monthNumberFormatter = new DecimalFormat("00");
    
    /** Convenience object, so we don't need to create an AttributesImpl for every element. */
    protected AttributesImpl attributes;
    
    /**
     * The cache key needs to be generated for the configuration of this
     * generator, so storing the parameters for generateKey().
     */
    protected List cacheKeyParList;
    
    /** The year to generate the calendar for */
    protected int year;
    
    /** The month to generate the calendar for */
    protected int month;
    
    /** The format for dates */
    protected DateFormat dateFormatter;
    
    /** The format for month names */
    protected DateFormat monthFormatter;
    
    /** The current locale */
    protected Locale locale;
    
    /**
     * Set the request parameters. Must be called before the generate method.
     *
     * @param resolver     the SourceResolver object
     * @param objectModel  a <code>Map</code> containing model object
     * @param src          the source URI (ignored)
     * @param par          configuration parameters
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        
        this.cacheKeyParList = new ArrayList();
        this.cacheKeyParList.add(src);

        // Determine the locale
        String langString = par.getParameter("lang", null);
        locale = Locale.getDefault();
        if (langString != null) {
            this.cacheKeyParList.add(langString);
            String countryString = par.getParameter("country", null);
            if (countryString != null) {
                this.cacheKeyParList.add(countryString);
                locale = new Locale(langString, countryString);
            } else {
                locale = new Locale(langString);
            }
            
        }
        
        // Determine year and month. Default is current year and month.
        Calendar now = Calendar.getInstance(locale);
        this.year = par.getParameterAsInteger("year", now.get(Calendar.YEAR));
        this.cacheKeyParList.add(String.valueOf(this.year));
        this.month = par.getParameterAsInteger("month", now.get(Calendar.MONTH) + 1) - 1;
        this.cacheKeyParList.add(String.valueOf(this.month));
        
        String dateFormatString = par.getParameter("dateFormat", null);
        this.cacheKeyParList.add(dateFormatString);
        if (dateFormatString != null) {
            this.dateFormatter = new SimpleDateFormat(dateFormatString, locale);
        } else {
            this.dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, locale);
        }
        this.monthFormatter = new SimpleDateFormat("MMMM", locale);
        
        this.attributes = new AttributesImpl();
    }
    
    /**
     * Generate XML data.
     *
     * @throws  SAXException if an error occurs while outputting the document
     */
    public void generate() throws SAXException, ProcessingException {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"), locale);
        start.clear();
        start.set(Calendar.YEAR, this.year);
        start.set(Calendar.MONTH, this.month);
        start.set(Calendar.DAY_OF_MONTH, 1);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);

        // Determine previous and next months
        Calendar prevMonth = (Calendar) start.clone();
        prevMonth.add(Calendar.MONTH, -1);
        
        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping(PREFIX, URI);
        attributes.clear();
        attributes.addAttribute("", YEAR_ATTR_NAME, YEAR_ATTR_NAME, "CDATA", String.valueOf(year));
        attributes.addAttribute("", MONTH_ATTR_NAME, MONTH_ATTR_NAME, "CDATA", 
                monthFormatter.format(start.getTime()));
        
        // Add previous and next month
        attributes.addAttribute("", PREV_YEAR_ATTR_NAME, PREV_YEAR_ATTR_NAME, "CDATA", 
                String.valueOf(prevMonth.get(Calendar.YEAR)));
        attributes.addAttribute("", PREV_MONTH_ATTR_NAME, PREV_MONTH_ATTR_NAME, "CDATA", 
                monthNumberFormatter.format(prevMonth.get(Calendar.MONTH) + 1));
        attributes.addAttribute("", NEXT_YEAR_ATTR_NAME, NEXT_YEAR_ATTR_NAME, "CDATA", 
                String.valueOf(end.get(Calendar.YEAR)));
        attributes.addAttribute("", NEXT_MONTH_ATTR_NAME, NEXT_MONTH_ATTR_NAME, "CDATA", 
                monthNumberFormatter.format(end.get(Calendar.MONTH) + 1));

        this.contentHandler.startElement(URI, CALENDAR_NODE_NAME,
                PREFIX + ':' + CALENDAR_NODE_NAME, attributes);
        int weekNo = start.get(Calendar.WEEK_OF_MONTH);
        if (start.get(Calendar.DAY_OF_WEEK) != start.getFirstDayOfWeek()) {
            attributes.clear();
            attributes.addAttribute("", NUMBER_ATTR_NAME, NUMBER_ATTR_NAME, "CDATA", String.valueOf(weekNo));
            this.contentHandler.startElement(URI, WEEK_NODE_NAME,
                    PREFIX + ':' + WEEK_NODE_NAME, attributes);
        }
        while (start.before(end)) {
            if (start.get(Calendar.DAY_OF_WEEK) == start.getFirstDayOfWeek()) {
                weekNo = start.get(Calendar.WEEK_OF_MONTH);
                attributes.clear();
                attributes.addAttribute("", NUMBER_ATTR_NAME, NUMBER_ATTR_NAME, "CDATA", String.valueOf(weekNo));
                this.contentHandler.startElement(URI, WEEK_NODE_NAME,
                        PREFIX + ':' + WEEK_NODE_NAME, attributes);
            }
            attributes.clear();
            attributes.addAttribute("", NUMBER_ATTR_NAME, NUMBER_ATTR_NAME, "CDATA",
                    String.valueOf(start.get(Calendar.DAY_OF_MONTH)));
            attributes.addAttribute("", DATE_ATTR_NAME, DATE_ATTR_NAME, "CDATA",
                    dateFormatter.format(start.getTime()));
            this.contentHandler.startElement(URI, DAY_NODE_NAME,
                    PREFIX + ':' + DAY_NODE_NAME, attributes);
            addContent(start, locale);
            this.contentHandler.endElement(URI, DAY_NODE_NAME,
                    PREFIX + ':' + DAY_NODE_NAME);
            start.add(Calendar.DAY_OF_MONTH, 1);
            if (start.get(Calendar.DAY_OF_WEEK) == start.getFirstDayOfWeek()
                    || ! start.before(end)) {
                this.contentHandler.endElement(URI, WEEK_NODE_NAME,
                        PREFIX + ':' + WEEK_NODE_NAME);
            }
        }
        
        this.contentHandler.endElement(URI, CALENDAR_NODE_NAME,
                PREFIX + ':' + CALENDAR_NODE_NAME);
        this.contentHandler.endPrefixMapping(PREFIX);
        this.contentHandler.endDocument();
    }
    
    /**
     * Add content to a &lt;day&gt; element. This method is intended to be overridden
     * by subclasses that want to add content to one or more days of the calendar.
     * 
     * @param date   The date corresponding to the current element.
     * @param locale The current locale.
     * @throws SAXException if an error occurs while outputting the document
     */
    protected void addContent(Calendar date, Locale locale) throws SAXException {}

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey() {
        StringBuffer buffer = new StringBuffer();
        int len = this.cacheKeyParList.size();
        for (int i = 0; i < len; i++) {
            buffer.append((String)this.cacheKeyParList.get(i) + ":");
        }
        return buffer.toString();
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }
    
    /**
     * Recycle resources
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.cacheKeyParList = null;
        this.attributes = null;
        this.dateFormatter = null;
        this.monthFormatter = null;
        this.locale = null;
        super.recycle();
    }

}
