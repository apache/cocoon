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
package org.apache.cocoon.components.serializers.encoding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: CharsetFactory.java,v 1.3 2004/04/30 22:57:22 joerg Exp $
 */
public final class CharsetFactory {

    /** The lookup class name for the encodings. */
    private static final String CHARSET_LOOKUP_CLASS =
        "org/apache/cocoon/components/serializers/encoding/cs_US_ASCII.class";

    /** Our private instance. */
    private static CharsetFactory instance = new CharsetFactory();

    /** The instance of the JVM default <code>Charset</code>. */
    private Charset defaultCharset = null;

    /** The instance of the JVM unknown <code>Charset</code>. */
    private Charset unknownCharset = null;

    /** All our charsets, mapped by their name and aliases. */
    private HashMap charsets = new HashMap();

    /**
     * Create a new instance of this <code>CharsetFactory</code>.
     */
    private CharsetFactory() {
        super();
        this.unknownCharset = new UnknownCharset();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(CHARSET_LOOKUP_CLASS);

        if ("jar".equals(url.getProtocol())) {
            this.loadCharsetsFromJar(url);
        } else if ("file".equals(url.getProtocol())) {
            this.loadCharsetsFromFile(url);
        } else {
            throw new CharsetFactoryException("Unable to load charsets " +
                        "from protocol \"" + url.getProtocol() + "\"", null);
        }

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
     * Load all <code>Charset</code> available if this class was loaded
     * from a JAR file.
     */
    private void loadCharsetsFromJar(URL url) {
        try {
            String file = url.getFile();
            String mtch = file.substring(file.indexOf('!'));
            file = file.substring(5, file.indexOf('!'));
            mtch = mtch.substring(2, mtch.lastIndexOf('/') + 1) + "cs_";
    
            ZipFile zip = new ZipFile(file);
            Enumeration enum = zip.entries();
            while (enum.hasMoreElements()) {
                ZipEntry entry = (ZipEntry)enum.nextElement();
                String name = entry.getName();
                if ((! name.startsWith(mtch)) ||
                    (! name.endsWith(".class"))) continue;
                name = name.substring(mtch.length());
                name = ".cs_" + name.substring(0, name.length() - 6);
                name = this.getClass().getPackage().getName() + name;
                loadCharset(name);
            }
        } catch (IOException exception) {
            throw new CharsetFactoryException("Unable to access JAR \""
                                          + url.toString() + "\"", exception);
        }
    }

    /**
     * Load all <code>Charset</code> available if this class was loaded
     * from a plain file on disk.
     */
    private void loadCharsetsFromFile(URL url) {
        File file = new File(url.getFile()).getParentFile();
        String children[] = file.list();
        for (int x = 0; x < children.length; x++) {
            String child = children[x];
            if ((! child.startsWith("cs_")) ||
                (! child.endsWith(".class"))) continue;
            child = '.' + child.substring(0, child.length() - 6);
            child = this.getClass().getPackage().getName() + child;
            this.loadCharset(child);
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
