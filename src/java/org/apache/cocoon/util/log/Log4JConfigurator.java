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
package org.apache.cocoon.util.log;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * This is a configurator for log4j that supports variable substitution
 * 
 * @version CVS $Id: Log4JConfigurator.java,v 1.2 2004/06/14 14:07:09 cziegeler Exp $
 */
public class Log4JConfigurator extends DOMConfigurator {

    protected Context context;
    
    public Log4JConfigurator(Context context) {
        this.context = context;
    }
    
    protected String subst(String value) {
        try {
          return this.substVars(value);
        } catch (IllegalArgumentException e) {
          LogLog.warn("Could not perform variable substitution.", e);

          return value;
        }
    }  
    
    static String DELIM_START = "${";
    static char   DELIM_STOP  = '}';
    static int DELIM_START_LEN = 2;
    static int DELIM_STOP_LEN  = 1;

    /**
     * This is directly copied from log4j's OptionConverter class.
     * The only difference is the getting of a property.
     */
    public String substVars(String val) 
    throws IllegalArgumentException {

        StringBuffer sbuf = new StringBuffer();

        int i = 0;
        int j, k;

        while(true) {
            j=val.indexOf(DELIM_START, i);
            if (j == -1) {
                // no more variables
                if(i==0) { // this is a simple string
                    return val;
                } else { // add the tail string which contails no variables and return the result.
                    sbuf.append(val.substring(i, val.length()));
                    return sbuf.toString();
                }
            } else {
                sbuf.append(val.substring(i, j));
                k = val.indexOf(DELIM_STOP, j);
                if(k == -1) {
                    throw new IllegalArgumentException('"'+val+
                             "\" has no closing brace. Opening brace at position " + j
                             + '.');
                } else {
                    j += DELIM_START_LEN;
                    String key = val.substring(j, k);
                    // first try in System properties
                    String replacement = this.getSystemProperty(key);
                    // then try props parameter
                    if (replacement == null && this.context != null) {
                        try {
                            Object o = this.context.get(key);
                            if ( o != null ) {
                                replacement = o.toString();
                            }
                        } catch (ContextException ce) {
                            LogLog.debug("Was not allowed to read context property \""+key+"\".");                            
                        }
                    }
    
                    if (replacement != null) {
                        // Do variable substitution on the replacement string
                        // such that we can solve "Hello ${x2}" as "Hello p1" 
                        // the where the properties are
                        // x1=p1
                        // x2=${x1}
                        String recursiveReplacement = substVars(replacement);
                        sbuf.append(recursiveReplacement);
                    }
                    i = k + DELIM_STOP_LEN;
                }
            }
        }
    }
    
    /**
     * This is directly copied from log4j's OptionConverter class.
     * The only difference is the getting of a property.
     */
    public String getSystemProperty(String key) {
        try {
            return System.getProperty(key, null);
        } catch(Throwable e) { // MS-Java throws com.ms.security.SecurityExceptionEx
            LogLog.debug("Was not allowed to read system property \""+key+"\".");
            return null;
        }
    }
}
