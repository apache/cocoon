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

import java.util.Locale;

import org.apache.pluto.om.common.DisplayName;
import org.apache.pluto.util.StringUtils;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DisplayNameImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
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
