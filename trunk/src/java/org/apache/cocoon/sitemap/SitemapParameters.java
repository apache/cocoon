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
package org.apache.cocoon.sitemap;

import java.util.HashMap;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;

/**
 * Extension to the Avalon Parameters
 *
 * @version CVS $Id: SitemapParameters.java,v 1.1 2004/03/11 14:48:29 cziegeler Exp $
 */
public class SitemapParameters extends Parameters {
    
    protected String statementLocation;
    
    /**
    public String getParameterLocation(String name) {
        return null;   
    }
    */
    public String getStatementLocation() {
        return this.statementLocation;   
    }
    
    public void setStatementLocation(String value) {
        this.statementLocation = value;   
    }
    
    /**
     * Return the location  - if available
     */
    public static String getStatementLocation(Parameters param) {
        String value = null;
        if ( param instanceof SitemapParameters ) {
            value = ((SitemapParameters)param).getStatementLocation();
        }
        if ( value == null ) {
            value = "[unknown location]";
        }
        return value;
    }
    
    public static class ExtendedHashMap extends HashMap {
        
        protected Configuration configuration;
        
        public ExtendedHashMap(Configuration conf) {
            super();
            this.configuration = conf;
        }
        
        public ExtendedHashMap(Configuration conf, int capacity) {
            super(capacity);
            this.configuration = conf;
        }

        public String getLocation() {
            if ( this.configuration != null ) {
                return this.configuration.getLocation();
            } 
            return null;
        }
        
        public Configuration getConfiguration() {
            return this.configuration;
        }
    }
}
