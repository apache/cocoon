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
package org.apache.cocoon.selection;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * A <code>Selector</code> depending on current date, and time.
 * <p>
 *  This selector matches when a configured date is before, respectivly
 *  after the current date, and time.
 * </p>
 * <p>
 *  You may want to use this selector to make the pipeline behaviour time
 *  dependent.
 * </p>
 * <p>
 *  The configuration of DataSelector configures for a symbolic name
 *  a compare-mode, and a date-time value.
 *  <br/>
 *  The compare mode is specified by the element name before, or after.
 *  The date-time value is specified by the attribute date. The attribute
 *  dateformat specifies the dateformat of the date-time value. Time only values
 *  are relative to the current date. Optionally you specify a country, and
 *  a language attribute for specifying the locale used in the date-value parsing.
 *  Per default the default locale is used.
 * </p>
 * <p>
 *   The following configuration example, sets partition a day into four time
 *   areas, each spanning six hours, giving them symbolic names night, morning,
 *   afternoon, and evening.
 * </p>
 * <pre><code>
 *  &lt;map:components&gt;
 *  ...
 *    &lt;map:selectors default="browser"&gt;
 *    ...
 *      &lt;map:selector type="date" src="org.apache.cocoon.selection.DateSelector"&gt;
 *        &lt;before name="night" date="06:00:00" dateformat="HH:mm:ss"/&gt;
 *        &lt;before name="morning" date="12:00:00" dateformat="HH:mm:ss"/&gt;
 *        &lt;before name="afternoon" date="18:00:00" dateformat="HH:mm:ss"/&gt;
 *        &lt;before name="evening" date="23:59:59" dateformat="HH:mm:ss"/&gt;
 *      &lt;/map:selector&gt;
 *    ...
 *    &lt;/map:selectors&gt;
 *  ...
 *  &lt;/map:components&gt;
 * </code></pre>
 * <p>
 *  The above date selector definition is used to control the behaviour of a
 *  pipeline processing depending on the current time of day.
 * </p>
 * <pre><code>
 *  &lt;map:pipelines&gt;
 *    &lt;map:pipeline&gt;
 *    ...
 *      &lt;map:match pattern="&asterik;&asterik;/resources/*.css"&gt;
 *      &lt;map:select type="date"&gt;
 *        &lt;map:when test="night"&gt;
 *          &lt;!-- do something for night publishing --&gt;
 *          &lt;map:read src="resources/{2}-night.css" mime-type="text/css&gt;
 *        &lt;/map:when&gt;
 *        &lt;map:when test="morning"&gt;
 *          &lt;!-- do something for night publishing --&gt;
 *          &lt;map:read src="resources/{2}-morning.css" mime-type="text/css&gt;
 *        &lt;/map:when&gt;
 *        ...
 *        &lt;map:otherwise&gt;
 *          &lt;!-- define for completness, and if selecting fails due to errors --&gt;
 *        &lt;/map:otherwise&gt;
 *      &lt;/map:select&gt;
 *    &lt;/map:pipeline&gt;
 *  &lt;/map:pipelines&gt;
 * </code></pre>
 *
 * @author <a href="mailto:huber@apache.org">Bernhard Huber</a>
 * @version CVS $Id: DateSelector.java,v 1.5 2004/03/05 10:07:26 bdelacretaz Exp $
 */
public class DateSelector extends AbstractSwitchSelector
implements Configurable, ThreadSafe {
    /** the configuration
     */
    private Configuration config;
    
    final public static String AFTER_ELEMENT = "after";
    final public static String BEFORE_ELEMENT = "before";
    
    final public static String NAME_ATTR = "name";
    final public static String DATE_ATTR = "date";
    final public static String DATEFORMAT_ATTR = "dateformat";
    final public static String LANGUAGE_ATTR = "language";
    final public static String COUNTRY_ATTR = "country";
    
    public void configure(Configuration config) throws ConfigurationException {
        this.config = config;
    }
    
    protected void configure( final Configuration conf, final String confName, final Map configMap ) {
        final Configuration[] confs = conf.getChildren( confName );
        String name = null;
        String date = null;
        String dateformat = null;
        String language = null;
        String country = null;
        
        final Calendar now = Calendar.getInstance();
        for (int i = 0; i < confs.length; i++ ) {
            try {
                name = confs[i].getAttribute( NAME_ATTR );
                date = confs[i].getAttribute( DATE_ATTR );
                dateformat = confs[i].getAttribute( DATEFORMAT_ATTR, null );
                language = confs[i].getAttribute( LANGUAGE_ATTR, null );
                country = confs[i].getAttribute( COUNTRY_ATTR, null);
                Date parsed_date = null;
                
                SimpleDateFormat sdf = null;
                if (dateformat != null && language != null && country != null) {
                    Locale locale = new Locale( language, country );
                    sdf = new SimpleDateFormat( dateformat, locale );
                } else if (dateformat != null) {
                    sdf = new SimpleDateFormat( dateformat );
                } else {
                    sdf = new SimpleDateFormat();
                }
                sdf.parse( date );
                Calendar parsed_calendar = sdf.getCalendar();
                setUnsetFields( parsed_calendar, now );
                parsed_date = parsed_calendar.getTime();
                
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug( "Parsed " + DATE_ATTR + " '" + String.valueOf(date) + "' to " +
                    "date object " + parsed_date.toString() );
                }
                if (parsed_date != null) {
                    // associate the name with associate date, and the compare-mode
                    configMap.put( name,
                    new DateComparator( parsed_date, confName ) );
                }
            } catch (Exception e) {
                if (this.getLogger().isErrorEnabled()) {
                    this.getLogger().error(
                    "Cannot parse date " + String.valueOf(date) + ", using " +
                    NAME_ATTR + " " + String.valueOf( name ) + ", " +
                    DATE_ATTR + " "  + String.valueOf( date ) + ", " +
                    DATEFORMAT_ATTR + " " + String.valueOf( dateformat ) + ", " +
                    LANGUAGE_ATTR + " " + String.valueOf( language ) + ", " +
                    COUNTRY_ATTR + " " + String.valueOf( country )
                    , e );
                }
            }
        }
    }
    
    /**
     * set fields which are not set by parsing the date attribute value
     *
     * @param cal Calendar parsed in
     * @param default_cal Calendar object providing default value for unset
     *   fields of cal
     */
    protected void setUnsetFields( Calendar cal, Calendar default_cal ) {
        // set fields which are not set by parsing date attribute
        if (!cal.isSet( Calendar.YEAR )) {
            cal.set( Calendar.YEAR, default_cal.get( Calendar.YEAR ) );
        }
        if (!cal.isSet( Calendar.MONTH )) {
            cal.set( Calendar.MONTH, default_cal.get( Calendar.MONTH ) );
        }
        if (!cal.isSet( Calendar.DAY_OF_MONTH )) {
            cal.set( Calendar.DAY_OF_MONTH, default_cal.get( Calendar.DAY_OF_MONTH ) );
        }
        if (!cal.isSet( Calendar.HOUR_OF_DAY )) {
            cal.set( Calendar.HOUR_OF_DAY, default_cal.get( Calendar.HOUR_OF_DAY ) );
        }
        if (!cal.isSet( Calendar.MINUTE )) {
            cal.set( Calendar.MINUTE, default_cal.get( Calendar.MINUTE ) );
        }
        if (!cal.isSet( Calendar.SECOND )) {
            cal.set( Calendar.SECOND, default_cal.get( Calendar.SECOND ) );
        }
        //
    }
    
    /**
     * create an object representing a context for multiple select/when
     * invocations
     *
     * @param objectModel the objectModel of the pipeline processing
     * @param parameters unused here
     * @return Object which is an object of class DataSelectorContext
     */
    public Object getSelectorContext(Map objectModel, Parameters parameters) {
        // Inform proxies that response varies over time of request, which
        // ASFAIK not a specific header fields, thus Vary is set to *
        // Seems that caching is not possible for this resource
        ObjectModelHelper.getResponse(objectModel).addHeader("Vary", "*");
        
        // 1 create map : name -> { date, [after|before] } from configuration
        Map map = new HashMap();
        configure( config, BEFORE_ELEMENT, map );
        configure( config, AFTER_ELEMENT, map );
        
        // 2 create SelectorContext
        DateSelectorContext csc = new DateSelectorContext(this.getLogger());
        // 3 precalculate result of comparing current date, and configuration map        
        csc.setup( map );
        
        return csc;
    }
    
    /**
     * Evaluate select for a given expression
     *
     * @param expression the expression to test against, as now expression
     *   should be a name defined as <code>NAME_ATTR</code> in the
     *   configuration section of this selector
     * @param selectorContext is the SelectorContext set up by
     *   the getSelectorContext() method
     * @return true if expression defining a name which yields
     *   true comparing the current date set in getSelectorContext,
     *   and the configured comparison, referenced by the
     *   expression name value.
     */
    public boolean select(String expression, Object selectorContext) {
        
        if (selectorContext == null) {
            getLogger().debug("selectorContext is null!" );
            return false;
        }
        // let SelectorContext do the work
        DateSelectorContext csc = (DateSelectorContext)selectorContext;
        return csc.select(expression);
    }
    
    /**
     * A helper class to store <code>Date, and compare-mode [after|before]</code>.
     * <p>For each configuration entry the compare-mode
     *   <code>[before|after]</code> is stored, and the date value associated with it.
     * </p>
     * <p>
     *  The 'main' method of this class is providing a compareTo method for easily comparing
     *  a given date to the configured compare-mode, and date.
     * </p>
     */
    private static class DateComparator {
        /** the configured date value
         */
        private Date date;
        /** indicator if after comparison should be performed
         */
        private boolean isCompareAfter;
        /** indicator if before comparison shoulde be performed
         */
        private boolean isCompareBefore;
        
        final String AFTER_COMPARATOR_MODE = "after";
        final String BEFORE_COMPARATOR_MODE = "before";
        
        public DateComparator( Date d, String comparator ) {
            this.date = d;
            this.isCompareAfter = AFTER_COMPARATOR_MODE.equalsIgnoreCase( comparator );
            this.isCompareBefore = BEFORE_COMPARATOR_MODE.equalsIgnoreCase( comparator );
        }
        public Date getDate() {
            return this.date;
        }
        public boolean isCompareAfter() {
            return this.isCompareAfter;
        }
        public boolean isCompareBefore() {
            return this.isCompareBefore();
        }
        
        /**
         * Compare when to date value of this object.
         * <p>
         *   Depending on the comparison mode the result of
         *   <code>now.after( date )</code>, or <code>now.before( date )</code>
         *   is returned.
         * </p>
         * @param now the current date and time value
         * @return true iff depending on compare-mode
         *   <code>now.after( date)</code>, or <code>now.before( date )</code>
         *   yield true
         */
        public boolean compareTo( Date now ) {
            if (isCompareAfter) {
                return now.after( date );
            } else if (isCompareBefore) {
                return now.before( date );
            } else {
                return now.compareTo( date) == 0;
            }
        }
    }
    
    /**
     * A SelectorContext for this Selector
     * <p>
     *  This SelectorContext compares the configured date values in this context,
     *  and stores only configure name attributes of comparison yielding true,
     *  reducing the comparsion effort.
     * </p>
     */
    private class DateSelectorContext {
        Date now;
        HashSet set;
        Logger logger;
        
        public DateSelectorContext(Logger logger) {
            now = new Date();
            set = new HashSet();
            this.logger = logger;
        }
        
        public void setup( final Map map ) {
            Iterator i = map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry)i.next();
                final String name = (String)entry.getKey();
                final DateComparator dc = (DateComparator)entry.getValue();
                
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(
                    "Compare name " + name + " having date " + String.valueOf( dc.getDate() ) + " to " +
                    String.valueOf( now ) );
                }
                // only store name in set iff comparison is true
                if (dc.compareTo( now )) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug(
                        "Storing name " + String.valueOf( name ) + " as it yielded true " );
                    }
                    this.set.add( name );
                }
            }
        }
        
        /**
         * Select yields true iff expression is member of the precalculated set.
         * As the set may contain various elements the order of checking elements
         * is important, and should be considered in the &lt;map:select&gt; sequence.
         *
         * @param expression a symbolic name which may match member of set
         * @return true iff expression is member of set
         */
        public boolean select( String expression ) {
            return this.set.contains( expression );
        }
    }
}
