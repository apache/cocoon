/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Response;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The XSP <code>Response</code> object helper
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: XSPResponseHelper.java,v 1.1 2003/03/09 00:08:55 pier Exp $
 */
public class XSPResponseHelper {
    /**
     * Assign values to the object's namespace uri and prefix
     */
    private static final String URI = Constants.XSP_RESPONSE_URI;
    private static final String PREFIX = Constants.XSP_RESPONSE_PREFIX;

    public static void getLocale(Response response, ContentHandler handler)
            throws SAXException {
        Locale locale = response.getLocale();
        XSPObjectHelper.start(URI, PREFIX, handler, "locale");

        XSPObjectHelper.elementData(URI, PREFIX, handler, "language",
            locale.getLanguage());
        XSPObjectHelper.elementData(URI, PREFIX, handler, "country",
            locale.getCountry());
        XSPObjectHelper.elementData(URI, PREFIX, handler, "variant",
            locale.getVariant());

        XSPObjectHelper.end(URI, PREFIX, handler, "locale");
    }

    public static void addDateHeader(Response response, String name, long date) {
        response.addDateHeader(name, date);
    }

    public static void addDateHeader(Response response, String name, Date date) {
        response.addDateHeader(name, date.getTime());
    }

    public static void addDateHeader(Response response, String name, String date) throws ParseException {
        addDateHeader(response, name, date, DateFormat.getDateInstance());
    }

    public static void addDateHeader(Response response, String name, String date, String format) throws ParseException {
        addDateHeader(response, name, date, new SimpleDateFormat(format));
    }

    public static void addDateHeader(Response response, String name, String date, DateFormat format) throws ParseException {
        response.addDateHeader(name, format.parse(date).getTime());
    }

    public static void setDateHeader(Response response, String name, long date) {
      response.setDateHeader(name, date);
    }

    public static void setDateHeader(Response response, String name, Date date) {
      response.setDateHeader(name, date.getTime());
    }

    public static void setDateHeader(Response response, String name, String date) throws ParseException {
      setDateHeader(response, name, date, DateFormat.getDateInstance());
    }

    public static void setDateHeader(Response response, String name, String date, String format) throws ParseException {
      setDateHeader(response, name, date, new SimpleDateFormat(format));
    }

    public static void setDateHeader(Response response, String name, String date, DateFormat format) throws ParseException {
      response.setDateHeader(name, format.parse(date).getTime());
    }
}
