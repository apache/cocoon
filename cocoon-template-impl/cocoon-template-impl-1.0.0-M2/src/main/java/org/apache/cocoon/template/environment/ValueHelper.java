/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.template.environment;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

/**
 * @version SVN $Id$
 */
public class ValueHelper {

    public static Locale parseLocale(String locale, String variant) {
        Locale ret = null;
        String language = locale;
        String country = null;
        int index = StringUtils.indexOfAny(locale, "-_");
    
        if (index > -1) {
            language = locale.substring(0, index);
            country = locale.substring(index + 1);
        }
        if (StringUtils.isEmpty(language)) {
            throw new IllegalArgumentException("No language in locale");
        }
        if (country == null) {
            ret = variant != null ? new Locale(language, "", variant) : new Locale(language, ""); 
        } else if (country.length() > 0) {
            ret = variant != null ? new Locale(language, country, variant) : new Locale(language, country); 
        } else {
            throw new IllegalArgumentException("Empty country in locale");
        }
        return ret;
    }

}


