/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
package org.apache.garbage.serializer.encoding;

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
 * @version CVS $Id: CharsetFactory.java,v 1.1 2003/06/21 21:11:48 pier Exp $
 */
public final class CharsetFactory {

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

        ClassLoader loader = this.getClass().getClassLoader();
        String file = this.getClass().getName().replace('.','/') + ".class";
        URL url = loader.getResource(file);

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
     * @throws UnsupportedEncodingException If the default is invalid or
     *                                      cannot be accessed.
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
