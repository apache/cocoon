/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package org.apache.cocoon.portal.pluto.om.common;

import org.apache.pluto.om.common.*;
import org.apache.pluto.util.Enumerator;
import org.apache.pluto.util.StringUtils;
import java.util.*;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: LanguageImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
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
                for (Enumeration enum = bundle.getKeys(); enum.hasMoreElements();) {
                    String key   = (String)enum.nextElement();
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
        ArrayList keywords = new ArrayList();

        for (StringTokenizer st = new StringTokenizer(value, ","); st.hasMoreTokens();) {
            keywords.add(st.nextToken().trim());
        }

        return keywords;
    }
    
    public String toString()
    {
        return toString(0);
    }

    public String toString(final int indent)
    {
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     * used for element equality according collection implementations
     */
    public boolean equals(Object o) {
        return o == null ? false
                         : ((LanguageImpl)o).getLocale().equals(this.locale);
    }

    /* (non-Javadoc)
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
