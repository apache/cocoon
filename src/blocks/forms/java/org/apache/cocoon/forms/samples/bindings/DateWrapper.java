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
package org.apache.cocoon.forms.samples.bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * DateWrapper is a specific sample test-class to demo the aggregate-binding.
 * This class must loook quite awkward, but is specially designed to look 
 * similar to the XML structure used in the same sample.
 * 
 * That is also why all field types here are simply kept to String
 * This will cause the binding-conversion to be applied.
 */
public class DateWrapper {

    private Map split = new HashMap();
    
    public DateWrapper(String day, String month, String year) {
        setDay(day);
        setMonth(month);
        setYear(year);        
    }
    
    public String getCombined() {
        return "" + getDay() +"/" + getMonth() + "/" + getYear();
    }


    public void setCombined(String fullDate) {
        StringTokenizer st = new StringTokenizer(fullDate, "/");
        setDay(st.nextToken());
        setMonth(st.nextToken());
        setYear(st.nextToken());
    }

    public Map getSplit() {
        return this.split;
    }

    /**
     * @return Returns the day.
     */
    public String getDay() {
        return split.get("day").toString();
    }
    /**
     * @param day The day to set.
     */
    public void setDay(String day) {
        split.put("day", day);
    }
    /**
     * @return Returns the month.
     */
    public String getMonth() {
        return split.get("month").toString();
    }
    /**
     * @param month The month to set.
     */
    public void setMonth(String month) {
        split.put("month", month);
    }
    /**
     * @return Returns the year.
     */
    public String getYear() {
        return split.get("year").toString();
    }
    /**
     * @param year The year to set.
     */
    public void setYear(String year) {
        split.put("year", year);
    }

    public String toString() {
        return "Wrapped Date as combined='" + getCombined() + "' as split=[" 
                + getDay() + ", " + getMonth() + ", " + getYear() + "]";
    }
}
