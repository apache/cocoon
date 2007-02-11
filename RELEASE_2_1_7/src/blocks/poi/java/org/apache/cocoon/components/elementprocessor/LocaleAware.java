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
package org.apache.cocoon.components.elementprocessor;

/**
 * The LocaleAware interface is for element processors whom require the locale
 * configuration string to control their behavior.  For HSSF this is somewhat
 * of a kludge to get past the fact the Gnumeric XML format does not allow
 * numbers formatted according to different locales in the &lt;Cell&gt; tags.
 * However, the ESQL generator for instance will generate them no other way.
 * 
 * @author Andrew C. Oliver (acoliver@apache.org)
 * @version CVS $Id: LocaleAware.java,v 1.3 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public interface LocaleAware
{
    /**
     * Set the locale for a given element processor.
     */
    public void setLocale(String locale);
}   // end public interface LocaleAware
