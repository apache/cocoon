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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.pluto.om.common.Language;
import org.apache.pluto.util.Enumerator;
import org.apache.pluto.util.StringUtils;

/**
 *
 * @version $Id$
 */
public class LanguageImpl implements Language, java.io.Serializable {
    // ResourceBundle creation part

    /*private static class Resources extends ListResourceBundle {
        private Object [][] resources = null;
        private Language source = null;

        public Resources(Language source)
        {                        
            this.source = source;
            Vector v  = new Vector();
            Iterator it = source.getKeywords();
            while(it != null && it.hasNext()) 
                v.add(it.next());

            resources = new Object [][] {            
                    { "javax.portlet.title", source.getTitle()==null? "": source.getTitle()},
                    { "javax.portlet.short-title", source.getShortTitle()==null? "": source.getShortTitle()},
                    { "javax.portlet.keywords", v.size() > 0 ? v.toArray().toString() : ""}};
        }

        public Object[][] getContents()
        {
            return resources;
        }        
    }*/

    private static class DefaultsResourceBundle extends ListResourceBundle {
        private Object[][] resources;

        public DefaultsResourceBundle(String defaultTitle, String defaultShortTitle, String defaultKeyWords) {
            resources = new Object[][] {
                {"javax.portlet.title"      , defaultTitle},
                {"javax.portlet.short-title", defaultShortTitle},
                {"javax.portlet.keywords"   , defaultKeyWords}
            };
        }

        protected Object[][] getContents() {
            return resources;
        }
    }

    private static class ResourceBundleImpl extends ResourceBundle {
        private HashMap data;

        public ResourceBundleImpl(ResourceBundle bundle, ResourceBundle defaults) {
            data = new HashMap();

            importData(defaults);
            importData(bundle);
        }

        private void importData(ResourceBundle bundle) {
            if (bundle != null) {
                for (Enumeration enumeration = bundle.getKeys(); enumeration.hasMoreElements();) {
                    String key   = (String)enumeration.nextElement();
                    Object value = bundle.getObject(key);

                    data.put(key, value);
                }
            }
        }

        protected Object handleGetObject(String key) {
            return data.get(key);
        }

        public Enumeration getKeys() {
            return new Enumerator(data.keySet());
        }
    }

    private Locale         locale;
    private String         title;
    private String         shortTitle;
    private ResourceBundle bundle;
    private ArrayList      keywords;

    public LanguageImpl(Locale locale, ResourceBundle bundle, String defaultTitle, String defaultShortTitle, String defaultKeyWords) {
        this.bundle = new ResourceBundleImpl(bundle, new DefaultsResourceBundle(defaultTitle, defaultShortTitle, defaultKeyWords));

        this.locale = locale;
        title       = this.bundle.getString("javax.portlet.title");
        shortTitle  = this.bundle.getString("javax.portlet.short-title");
        keywords    = toList(this.bundle.getString("javax.portlet.keywords"));
    }

    // Language implementation.

    public Locale getLocale() {
        return locale;
    }

    public String getTitle() {
        return title;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public Iterator getKeywords() {
        return keywords.iterator();
    }

    public ResourceBundle getResourceBundle() {                            
        return bundle;
    }

    // internal methods.
    private ArrayList toList(String value) {
        ArrayList elements = new ArrayList();

        for (StringTokenizer st = new StringTokenizer(value, ","); st.hasMoreTokens();) {
            elements.add(st.nextToken().trim());
        }

        return elements;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(0);
    }

    public String toString(final int indent) {
        StringBuffer buffer = new StringBuffer(50);
        StringUtils.newLine(buffer,indent);
        buffer.append(getClass().toString()); buffer.append(":");
        StringUtils.newLine(buffer,indent);
        buffer.append("{");
        StringUtils.newLine(buffer,indent);
        buffer.append("locale='"); buffer.append(locale); buffer.append("'");
        StringUtils.newLine(buffer,indent);
        buffer.append("title='"); buffer.append(title); buffer.append("'");
        StringUtils.newLine(buffer,indent);
        buffer.append("shortTitle='"); buffer.append(shortTitle); buffer.append("'");
        Iterator iterator = keywords.iterator();
        if (iterator.hasNext()) {
            StringUtils.newLine(buffer,indent);
            buffer.append("Keywords:");
        }
        while (iterator.hasNext()) {
            buffer.append(iterator.next());
            buffer.append(',');
        }
        StringUtils.newLine(buffer,indent);
        buffer.append("}");
        return buffer.toString();
    }

    // additional methods.

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     * used for element equality according collection implementations
     */
    public boolean equals(Object o) {
        return o == null ? false
                         : ((LanguageImpl)o).getLocale().equals(this.locale);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return locale.hashCode();
    }

    public void setKeywords(Collection keywords) {
        this.keywords.clear();
        this.keywords.addAll(keywords);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
