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
package org.apache.cocoon.portal.pluto.om.common;

import java.util.Locale;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.util.StringUtils;

/**
 *
 * @version $Id$
 */
public class DescriptionImpl implements Description, java.io.Serializable, Support {

    private String description;
    private Locale locale;      // default locale;
    private String castorLocale;

    public DescriptionImpl() {
        // nothing to do 
    }

    // Description implementation.
    public String getDescription() {
        return description;
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postBuild(java.lang.Object)
     */
    public void postBuild(Object parameter) throws Exception {
        // nothing to do 
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postLoad(java.lang.Object)
     */
    public void postLoad(Object parameter) throws Exception {   
        if (castorLocale == null) {
            locale = Locale.ENGLISH;
        } else {
            locale = new Locale(castorLocale, "");
        }
    }

    /**
     * @see org.apache.cocoon.portal.pluto.om.common.Support#postStore(java.lang.Object)
     */
    public void postStore(Object parameter) throws Exception {
        // nothing to do 
    }

    public void preBuild(Object parameter) throws Exception {
        // nothing to do 
    }

    public void preStore(Object parameter) throws Exception {
        // nothing to do 
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        StringBuffer buffer = new StringBuffer(50);
        StringUtils.newLine(buffer,indent);
        buffer.append(getClass().toString());
        buffer.append(": description='");
        buffer.append(description);
        buffer.append("', locale='");
        buffer.append(locale);
        buffer.append("'");
        return buffer.toString();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    // end castor methods

    /**
     * Returns the castorLocale.
     * @return String
     */
    public String getCastorLocale() {
        return castorLocale;
    }

    /**
     * Sets the castorLocale.
     * @param castorLocale The castorLocale to set
     */
    public void setCastorLocale(String castorLocale) {
        this.castorLocale = castorLocale;
    }
}
