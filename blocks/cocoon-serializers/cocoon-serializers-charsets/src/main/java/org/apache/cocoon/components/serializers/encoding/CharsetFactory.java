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
package org.apache.cocoon.components.serializers.encoding;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * A factory for the charsets.
 * The list of charsets is hard-coded, whenever the list changes this
 * hard-coded list must be updated manually.
 *
 */
public final class CharsetFactory {

    /** Our private instance. */
    private static CharsetFactory instance = new CharsetFactory();

    /** The instance of the JVM default <code>Charset</code>. */
    private Charset defaultCharset;

    /** The instance of the JVM unknown <code>Charset</code>. */
    private Charset unknownCharset;

    /** All our charsets, mapped by their name and aliases. */
    private HashMap charsets = new HashMap();

    private static String[] getCharsets() {
        return new String[] {
        "BIG5_HKSCS",
        "BIG5",
        "EUC_CN",
        "EUC_JP_LINUX",
        "EUC_JP",
        "EUC_KR",
        "EUC_TW",
        "GB18030",
        "GB2312",
        "GBK",
        "IBM_THAI",
        "IBM00858",
        "IBM01140",
        "IBM01141",
        "IBM01142",
        "IBM01143",
        "IBM01144",
        "IBM01145",
        "IBM01146",
        "IBM01147",
        "IBM01148",
        "IBM01149",
        "IBM037",
        "IBM1026",
        "IBM1047",
        "IBM273",
        "IBM277",
        "IBM278",
        "IBM280",
        "IBM284",
        "IBM285",
        "IBM297",
        "IBM420",
        "IBM424",
        "IBM437",
        "IBM500",
        "IBM775",
        "IBM850",
        "IBM852",
        "IBM855",
        "IBM857",
        "IBM860",
        "IBM861",
        "IBM862",
        "IBM863",
        "IBM864",
        "IBM865",
        "IBM866",
        "IBM868",
        "IBM869",
        "IBM870",
        "IBM871",
        "IBM918",
        "ISO_2022_CN_CNS",
        "ISO_2022_CN_GB",
        "ISO_2022_JP",
        "ISO_2022_KR",
        "ISO_8859_1",
        "ISO_8859_13",
        "ISO_8859_15",
        "ISO_8859_2",
        "ISO_8859_3",
        "ISO_8859_4",
        "ISO_8859_5",
        "ISO_8859_6",
        "ISO_8859_7",
        "ISO_8859_8",
        "ISO_8859_9",
        "JIS_X0201",
        "JIS_X0212_1990",
        "JIS0201",
        "JIS0208",
        "JIS0212",
        "JOHAB",
        "KOI8_R",
        "MACARABIC",
        "MACCENTRALEUROPE",
        "MACCROATIAN",
        "MACCYRILLIC",
        "MACDINGBAT",
        "MACGREEK",
        "MACHEBREW",
        "MACICELAND",
        "MACROMAN",
        "MACROMANIA",
        "MACSYMBOL",
        "MACTHAI",
        "MACTURKISH",
        "MACUKRAINE",
        "SHIFT_JIS",
        "TIS_620",
        "US_ASCII",
        "UTF_16",
        "UTF_16BE",
        "UTF_16LE",
        "UTF_8",
        "WINDOWS_1250",
        "WINDOWS_1251",
        "WINDOWS_1252",
        "WINDOWS_1253",
        "WINDOWS_1254",
        "WINDOWS_1255",
        "WINDOWS_1256",
        "WINDOWS_1257",
        "WINDOWS_1258",
        "WINDOWS_31J",
        "WINDOWS_936",
        "WINDOWS_949",
        "WINDOWS_950",
        "X_BIG5_SOLARIS",
        "X_EUC_CN",
        "X_EUC_JP_LINUX",
        "X_EUC_TW",
        "X_EUCJP_OPEN",
        "X_IBM1006",
        "X_IBM1025",
        "X_IBM1046",
        "X_IBM1097",
        "X_IBM1098",
        "X_IBM1112",
        "X_IBM1122",
        "X_IBM1123",
        "X_IBM1124",
        "X_IBM1381",
        "X_IBM33722",
        "X_IBM737",
        "X_IBM856",
        "X_IBM874",
        "X_IBM875",
        "X_IBM921",
        "X_IBM922",
        "X_IBM930",
        "X_IBM933",
        "X_IBM935",
        "X_IBM937",
        "X_IBM939",
        "X_IBM942",
        "X_IBM942C",
        "X_IBM942",
        "X_IBM942C",
        "X_IBM943",
        "X_IBM943C",
        "X_IBM948",
        "X_IBM949",
        "X_IBM949C",
        "X_IBM950",
        "X_IBM964",
        "X_IBM970",
        "X_ISCII91",
        "X_ISO_2022_CN_CNS",
        "X_ISO_2022_CN_GB",
        "X_ISO_8859_11",
        "X_JIS0208",
        "X_JOHAB",
        "X_MACARABIC",
        "X_MACCENTRALEUROPE",
        "X_MACCROATIAN",
        "X_MACCYRILLIC",
        "X_MACDINGBAT",
        "X_MACGREEK",
        "X_MACHEBREW",
        "X_MACICELAND",
        "X_MACROMANIA",
        "X_MACSYMBOL",
        "X_MACTHAI",
        "X_MACTURKISH",
        "X_MACUKRAINE",
        "X_MS950_HKSCS",
        "X_MSWIN_936",
        "X_PCK",
        "X_WINDOWS_874",
        "X_WINDOWS_949",
        "X_WINDOWS_950"
        };
    }

    /**
     * Create a new instance of this <code>CharsetFactory</code>.
     */
    private CharsetFactory() {
        super();
        this.unknownCharset = new UnknownCharset();

        // load charset
        final String[] sets = getCharsets();
        for(int i=0; i<sets.length; i++) {
            final String name = this.getClass().getPackage().getName() + ".cs_" + sets[i];
            loadCharset(name);
        }
        // detect default encoding
        ByteArrayOutputStream otmp = new ByteArrayOutputStream();
        OutputStreamWriter wtmp = new OutputStreamWriter(otmp);
        String etmp = wtmp.getEncoding();
        try {
            defaultCharset = this.getCharset(etmp);
        } catch (UnsupportedEncodingException exception) {
            throw new CharsetFactoryException("The default encoding of this "
                + "JVM \"" + etmp + "\" is not supported", exception);
        }

    }

    /**
     * Load a <code>Charset</code> into this factory.
     */
    private void loadCharset(Charset charset) {
        this.charsets.put(charset.getName().toLowerCase(), charset);
        String aliases[] = charset.getAliases();
        for (int x = 0; x < aliases.length; x++) {
            this.charsets.put(aliases[x].toLowerCase(), charset);
        }
    }

    /**
     * Instantiate and load a <code>Charset</code> into this factory.
     */
    private void loadCharset(String clazz) {
        try {
            Class c = Class.forName(clazz);
            Object o = c.newInstance();
            if (o instanceof Charset) {
                loadCharset((Charset)o);
            }
        } catch (Exception exception) {
            throw new CharsetFactoryException("Unable to instantiate class \""
                                              + clazz + "\"", exception);
        }
    }

    /**
     * Return an instance of this <code>CharsetFactory</code>.
     */
    public static CharsetFactory newInstance() {
        if (instance != null) return (instance);
        synchronized (CharsetFactory.class) {
            if (instance != null) return (instance);
            instance = new CharsetFactory();
        }
        return(instance);
    }

    /**
     * Return the <code>Charset</code> instance for the unknown charset.
     * <br />
     * All calls to the <code>allows(...)</code> method of the returned
     * <code>Charset</code> will return <b>true</b>.
     */
    public Charset getCharset() {
        return(unknownCharset);
    }

    /**
     * Return the <code>Charset</code> instance for the default charset.
     *
     */
    public Charset getDefaultCharset() {
        return(defaultCharset);
    }

    /**
     * Return the <code>Charset</code> instance for a specifc charset.
     *
     * @throws UnsupportedEncodingException If the specified is invalid or
     *                                      cannot be accessed.
     */
    public Charset getCharset(String name)
    throws UnsupportedEncodingException {
        if (name == null) return(this.getDefaultCharset());
        Charset charset = (Charset)this.charsets.get(name.toLowerCase());
        if (charset != null) return(charset);
        throw new UnsupportedEncodingException("Unsupported charset \""
                                               + name + "\"");
    }

    /**
     * An <code>RuntimeException</code> thrown if something bad happens
     * while initializing our factory.
     */
    private static class CharsetFactoryException extends RuntimeException {

        /** The root cause of this exception. */
        private Exception exception = null;

        /**
         * Create a new <code>CharsetFactoryException</code> instance.
         */
        private CharsetFactoryException(String message, Exception exception) {
            super(message == null? exception.getMessage(): message);
            this.exception = exception;
        }

        /**
         * Return the <code>Exception</code> cause of this exception.
         */
        public Exception getException() {
            return(this.exception);
        }

        /**
         * Print this <code>Exception</code> stacktrace to a specified
         * <code>PrintWriter</code>.
         */
        public void printStackTrace(PrintWriter out) {
            super.printStackTrace(out);
            if (this.exception != null) {
                out.print("Root cause: ");
                this.exception.printStackTrace(out);
            }
        }

        /**
         * Print this <code>Exception</code> stacktrace to a specified
         * <code>PrintStream</code>.
         */
        public void printStackTrace(PrintStream out) {
            super.printStackTrace(out);
            if (this.exception != null) {
                out.print("Root cause: ");
                this.exception.printStackTrace(out);
            }
        }
    }
}
