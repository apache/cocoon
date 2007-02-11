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
 * @version CVS $Id: XSPResponseHelper.java,v 1.2 2004/03/05 13:02:47 bdelacretaz Exp $
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
