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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.util.Tokenizer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * The XSP <code>Utility</code> object helper
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: XSPUtil.java,v 1.8 2004/02/06 23:34:32 joerg Exp $
 */
public class XSPUtil {

    public static String pathComponent(String filename) {
        int i = filename.lastIndexOf(File.separator);
        return (i >= 0) ? filename.substring(0, i) : filename;
    }

    public static String fileComponent(String filename) {
        int i = filename.lastIndexOf(File.separator);
        return (i >= 0) ? filename.substring(i + 1) : filename;
    }

    public static String baseName(String filename) {
        return baseName(filename, ".");
    }

    public static String baseName(String filename, String suffix) {
        int lastDot = filename.lastIndexOf(suffix);
        if (lastDot >= 0) {
            filename = filename.substring(0, lastDot);
        }
        return filename;
    }

    public static String normalizedBaseName(String filename) {
        filename = baseName(filename);
        return normalizedName(filename);
    }

    public static String normalizedName(String filename) {
        String[] path = split(filename, File.separator);
        int start = (path[0].length() == 0) ? 1 : 0;
        StringBuffer buffer = new StringBuffer();
        for (int i = start; i < path.length; i++) {
            if (i > start) {
                buffer.append(File.separator);
            }
            buffer.append('_');
            char[] chars = path[i].toCharArray();
            for (int j = 0; j < chars.length; j++) {
                if (isAlphaNumeric(chars[j])) {
                    buffer.append(chars[j]);
                } else {
                    buffer.append('_');
                }
            }
        }
        return buffer.toString();
    }

    public static String relativeFilename(String filename, Map objectModel) throws IOException {
        File file = new File(filename);
        if (file.isAbsolute() && file.exists()) {
            return filename;
        }
        Context context = ObjectModelHelper.getContext(objectModel);
        URL resource = context.getResource(filename);
        if (resource == null) {
            throw new FileNotFoundException("The file " + filename + " does not exist!");
        }
        return NetUtils.getPath(resource.toExternalForm());
    }

    public static boolean isAlphaNumeric(char c) {
        return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

    public static String[] split(String line) {
        return split(line, " \t\r\f\n");
    }

    public static String[] split(String line, String delimiter) {
        Tokenizer tokenizer = new Tokenizer(line, delimiter);
        int tokenCount = tokenizer.countTokens();
        String[] result = new String[tokenCount];
        for (int i = 0; i < tokenCount; i++) {
            result[i] = tokenizer.nextToken();
        }
        return result;
    }

    public static String encodeMarkup(String string) {
        char[] array = string.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            switch (array[i]) {
                case '<':
                    buffer.append("&lt;");
                    break;
                case '>':
                    buffer.append("&gt;");
                    break;
                case '&':
                    buffer.append("&amp;");
                    break;
                default:
                    buffer.append(array[i]);
                    break;
            }
        }
        return buffer.toString();
    }

    public static String formEncode(String text) throws Exception {
        return URLEncoder.encode(text);
    }

    // Shameless, ain't it?
    public static String formDecode(String s) throws Exception {
        return URLDecoder.decode(s);
    }

    /* Logicsheet Utility Methods */

    // Date
    public static String formatDate(Date date, String pattern) {
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy/MM/dd hh:mm:ss aa";
        }
        try {
            return (new SimpleDateFormat(pattern)).format(date);
        } catch (Exception e) {
            return date.toString();
        }
    }

    // Counters
    private static volatile int count = 0;

    public static int getCount() {
        return ++count;
    }

    public static int getSessionCount(Session session) {
        synchronized (session) {
            Integer integer = (Integer)session.getAttribute("util.counter");
            if (integer == null) {
                integer = new Integer(0);
            }
            int cnt = integer.intValue() + 1;
            session.setAttribute("util.counter", new Integer(cnt));
            return cnt;
        }
    }

    public static Object getContextAttribute(Map objectModel, String name) {
        Context context = ObjectModelHelper.getContext(objectModel);
        return context.getAttribute(name);
    }

    // Inclusion
    public static String getSourceContents(String url, SourceResolver resolver) throws IOException {
        Source source = resolver.resolveURI(url);
        try {
            return getContents(source.getInputStream());
        } finally {
            resolver.release(source);
        }
    }

    public static String getSourceContents(String uri, String base, SourceResolver resolver) throws IOException {
        if (base != null && base.length() == 0) {
            base = null;
        }
        Source source = resolver.resolveURI(uri, base, null);
        try {
            return getContents(source.getInputStream());
        } finally {
            resolver.release(source);
        }
    }

    public static String getFileContents(String filename) throws IOException {
        return getContents(new FileReader(filename));
    }

    public static String getFileContents(String filename, String encoding) throws IOException {
        return getContents(new FileInputStream(filename), encoding);
    }

    public static String getContents(InputStream in, String encoding) throws IOException {
        return getContents(new InputStreamReader(in, encoding));
    }

    public static String getContents(InputStream in) throws IOException {
        return getContents(new InputStreamReader(in));
    }

    public static String getContents(Reader reader) throws IOException {
        int len;
        char[] chr = new char[4096];
        StringBuffer buffer = new StringBuffer();
        try {
            while ((len = reader.read(chr)) > 0) {
                buffer.append(chr, 0, len);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }

    public static void includeSource(String uri, String base, SourceResolver resolver, ContentHandler contentHandler)
            throws RuntimeException {
        if (base != null && base.length() == 0) {
            base = null;
        }
        Source source = null;
        try {
            source = resolver.resolveURI(uri, base, null);
            SourceUtil.toSAX(source, new IncludeXMLConsumer(contentHandler));
        } catch (Exception e) {
            throw new CascadingRuntimeException("Error including source " + base + " " + uri, e);
        } finally {
            if (source != null) {
                resolver.release(source);
            }
        }
    }

    public static void includeString(String string, ServiceManager manager, ContentHandler contentHandler)
            throws RuntimeException {
        XSPUtil.includeInputSource(new InputSource(new StringReader(String.valueOf(string))), manager,
                                   contentHandler);
    }

    public static void includeFile(String name, ServiceManager manager, ContentHandler contentHandler, Map objectModel)
            throws RuntimeException {
        try {
            XSPUtil.includeInputSource(new InputSource(new FileReader(XSPUtil.relativeFilename(name, objectModel))),
                                       manager, contentHandler);
        } catch (IOException e) {
            throw new CascadingRuntimeException("Could not include file " + name, e);
        }
    }

    public static void includeInputSource(InputSource source, ServiceManager manager, ContentHandler contentHandler)
            throws RuntimeException {
        SAXParser parser = null;
        try {
            parser = (SAXParser)manager.lookup(SAXParser.ROLE);
            IncludeXMLConsumer consumer = new IncludeXMLConsumer(contentHandler);
            parser.parse(source, consumer, consumer);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not include page", e);
        } finally {
            if (parser != null) {
                manager.release(parser);
            }
        }
    }
}
