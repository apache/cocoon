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
 *     year="2004" month="January"&gt;
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
 * The <i>src</i> parameter must be in the form "yyyy/mm" where "2004/01" stands
 * for January, 2004.
 * </p>
 * <p>
 *  <b>Configuration options:</b>
 *  <dl>
 *   <dt> <i>dateFormat</i> (optional)
 *   <dd> Sets the format for the date attribute of each node, as
 *        described in java.text.SimpleDateFormat. If unset, the default
 *        format for the current locale will be used.
 *  </dl>
 *   <dt> <i>lang</i> (optional)
 *   <dd> Sets the ISO language code for determining the locale.
 *  </dl>
 *  </dl>
 *   <dt> <i>country</i> (optional)
 *   <dd> Sets the ISO country code for determining the locale.
 *  </dl>
 * </p>
 * 
 * @version CVS $Id: CalendarGenerator.java,v 1.2 2004/04/07 23:29:22 ugo Exp $
 */
public class CalendarGenerator extends ServiceableGenerator implements CacheableProcessingComponent {
    
    /** The URI of the namespace of this generator. */
    protected static final String URI = "http://apache.org/cocoon/calendar/1.0";
    
    /** The namespace prefix for this namespace. */
    protected static final String PREFIX = "calendar";
    
    /** Node and attribute names */
    protected static final String CALENDAR_NODE_NAME = "calendar";
    protected static final String WEEK_NODE_NAME     = "week";
    protected static final String DAY_NODE_NAME      = "day";
    protected static final String MONTH_ATTR_NAME    = "month";
    protected static final String YEAR_ATTR_NAME     = "year";
    protected static final String DATE_ATTR_NAME     = "date";
    protected static final String NUMBER_ATTR_NAME   = "number";
    
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
    DateFormat dateFormatter;
    
    /** The format for month names */
    DateFormat monthFormatter;
    
    /**
     * Set the request parameters. Must be called before the generate method.
     *
     * @param resolver     the SourceResolver object
     * @param objectModel  a <code>Map</code> containing model object
     * @param src          the year and month in the form "yyyy/mm"
     * @param par          configuration parameters
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        if (src == null) {
            throw new ProcessingException("No src attribute specified.");
        }
        super.setup(resolver, objectModel, src, par);
        
        this.cacheKeyParList = new ArrayList();
        this.cacheKeyParList.add(src);
        
        this.year = Integer.parseInt(src.substring(0, src.indexOf('/')));
        this.month = Integer.parseInt(src.substring(src.indexOf('/') + 1)) - 1;
        
        String dateFormatString = par.getParameter("dateFormat", null);
        this.cacheKeyParList.add(dateFormatString);
        String langString = par.getParameter("lang", null);
        Locale locale = Locale.getDefault();
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
    public void generate() throws SAXException {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        start.clear();
        start.set(Calendar.YEAR, this.year);
        start.set(Calendar.MONTH, this.month);
        start.set(Calendar.DAY_OF_MONTH, 1);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        
        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping(PREFIX, URI);
        attributes.clear();
        attributes.addAttribute("", YEAR_ATTR_NAME, YEAR_ATTR_NAME, "CDATA", String.valueOf(year));
        attributes.addAttribute("", MONTH_ATTR_NAME, MONTH_ATTR_NAME, "CDATA", 
                monthFormatter.format(start.getTime()));
        this.contentHandler.startElement(URI, CALENDAR_NODE_NAME,
                PREFIX + ':' + CALENDAR_NODE_NAME, attributes);
        int weekNo = start.get(Calendar.WEEK_OF_MONTH);
        if (start.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            attributes.clear();
            attributes.addAttribute("", NUMBER_ATTR_NAME, NUMBER_ATTR_NAME, "CDATA", String.valueOf(weekNo));
            this.contentHandler.startElement(URI, WEEK_NODE_NAME,
                    PREFIX + ':' + WEEK_NODE_NAME, attributes);
        }
        while (start.before(end)) {
            if (start.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
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
            this.contentHandler.endElement(URI, DAY_NODE_NAME,
                    PREFIX + ':' + DAY_NODE_NAME);
            start.add(Calendar.DAY_OF_MONTH, 1);
            if (start.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
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
}
