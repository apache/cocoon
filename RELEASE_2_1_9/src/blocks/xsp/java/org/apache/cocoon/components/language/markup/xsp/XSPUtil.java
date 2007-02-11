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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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
 * @version CVS $Id: XSPUtil.java,v 1.2 2004/03/28 14:28:04 antonio Exp $
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
        return StringUtils.split(line, " \t\r\f\n");
    }

    public static String[] split(String line, String delimiter) {
        return StringUtils.split(line, delimiter);
    }

    public static String encodeMarkup(String string) {
        return StringEscapeUtils.escapeXml(string);
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
        if (StringUtils.isEmpty(pattern)) {
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
        if (StringUtils.isEmpty(base)) {
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
        if (StringUtils.isEmpty(base)) {
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

    public static void includeString(String string, ComponentManager manager, ContentHandler contentHandler)
            throws RuntimeException {
        XSPUtil.includeInputSource(new InputSource(new StringReader(String.valueOf(string))), manager,
                                   contentHandler);
    }

    public static void includeFile(String name, ComponentManager manager, ContentHandler contentHandler, Map objectModel)
            throws RuntimeException {
        try {
            XSPUtil.includeInputSource(new InputSource(new FileReader(XSPUtil.relativeFilename(name, objectModel))),
                                       manager, contentHandler);
        } catch (IOException e) {
            throw new CascadingRuntimeException("Could not include file " + name, e);
        }
    }

    public static void includeInputSource(InputSource source, ComponentManager manager, ContentHandler contentHandler)
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
                manager.release((Component)parser);
            }
        }
    }
}
