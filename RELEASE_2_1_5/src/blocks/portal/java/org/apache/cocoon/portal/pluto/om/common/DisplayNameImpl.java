/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.om.common;

import java.util.Locale;

import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.util.StringUtils;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DisplayNameImpl.java,v 1.2 2004/03/05 13:02:15 bdelacretaz Exp $
 */
public class DisplayNameImpl implements DisplayName, java.io.Serializable, Support {

    private String displayName;
    private Locale locale;  // default locale
    private String castorLocale;

    public DisplayNameImpl()
    {
    }

    // DisplayName implementation.
    public String getDisplayName()
    {
        return displayName;
    }

    public Locale getLocale()
    {
        return locale;
    }

    // Support implementation.

    public void postLoad(Object parameter) throws Exception
    {
        if (castorLocale == null) {
            locale = Locale.ENGLISH;
        } else {
            locale = new Locale(castorLocale, "");
        }
    }
    public void postStore(Object parameter) throws Exception
    {
    }

    public void preBuild(Object parameter) throws Exception
    {
    }

    public void preStore(Object parameter) throws Exception
    {
    }

    public void postBuild(Object parameter) throws Exception {
    }

    // additional methods.
    public String toString()
    {
        return toString(0);
    }

    public String toString(int indent)
    {
        StringBuffer buffer = new StringBuffer(50);
        StringUtils.newLine(buffer,indent);
        buffer.append(getClass().toString());
        buffer.append(": displayName='");
        buffer.append(displayName);
        buffer.append("', locale='");
        buffer.append(locale);
        buffer.append("'");
        return buffer.toString();
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

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
