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

package org.apache.cocoon.servletservice.components;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.reading.AbstractReader;
import org.apache.cocoon.util.location.LocationImpl;

import org.xml.sax.SAXException;

/**
 * <p>
 * <code>LinkRewriterReader<code> implements <code>servlet:</code> link rewriting in
 * text resources. It should be used especially for serving JavaScript files that have
 * paths refering to other blocks (<code>servlet:</code> links).
 * </p>
 *
 * <p><b>Configuration</b><br>
 * <code>encoding</code> - see {@link #setEncoding}<br>
 * <code>expires</code> - see {@link #setExpires}.
 *
 * @cocoon.sitemap.component.documentation
 * <code>LinkRewriterReader<code> implements <code>servlet:</code> link rewriting in
 * text resources. It should be used especially for serving JavaScript files that have
 * paths refering to other blocks (<code>servlet:</code> links).
 * @cocoon.sitemap.component.documentation.caching Yes
 *
 * @version $Id$
 * @since 1.0.0
 */
public class LinkRewriterReader extends AbstractReader
                                implements CacheableProcessingComponent {

    protected long configuredExpires = -1;
    protected String configuredEncoding = "UTF-8";

    protected Response response;
    protected Request request;
    protected Source inputSource;

    protected InputModule inputModule;

    protected long expires;
    protected String encoding;

    /**
     * This parameter is optional. When specified it determines how long
     * in miliseconds the resources can be cached by any proxy or browser
     * between Cocoon and the requesting visitor. Defaults to -1.
     *
     * @param expires
     */
    public void setExpires(long expires) {
        this.configuredExpires = expires;
    }

    /**
     * This parameter is optional. When specified it determines charset encoding
     * of <b>input</b> files. This is needed for parsing working properly.
     * Defaults to "UTF-8".
     * @param encoding
     */
    public void setEncoding(String encoding) {
        this.configuredEncoding = encoding;
    }

    public void setInputModule(InputModule inputModule) {
        this.inputModule = inputModule;
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);

        this.expires = par.getParameterAsLong("expires", this.configuredExpires);
        this.encoding = par.getParameter("encoding", this.configuredEncoding);

        try {
            this.inputSource = resolver.resolveURI(src);
        } catch (SourceException e) {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", e);
        }

        setupHeaders();
    }

    protected void setupHeaders() {
        response.setHeader("Accept-Ranges", "none");
        if (expires > 0) {
            response.setDateHeader("Expires", System.currentTimeMillis() + expires);
        } else if (expires == 0) {
            response.setDateHeader("Expires", 0);
        }

        long lastModified = inputSource.getLastModified();
        if (lastModified > 0)
            response.setDateHeader("Last-Modified", lastModified);
    }

    public void generate() throws IOException, ProcessingException {
        InputStream inputStream = this.inputSource.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, encoding);
        BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(out, encoding));

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line = bufferedReader.readLine();
        int lineNumber = 1;
        while (line != null) {
            line = proccessLine(line, lineNumber);
            outputWriter.write(line, 0, line.length());
            outputWriter.newLine();
            line = bufferedReader.readLine();
            lineNumber++;
        }
        bufferedReader.close();
        outputWriter.close();
    }

    /**
     * This class is just container for regexp URI pattern. This regexp bases on RFC2396.
     * Actually it's stripped version of original definition. It suits well for the task - servlet: links rewriting.
     */
    protected static class URIregexp {
        static final String scheme = "(?:[a-zA-Z](?:[a-zA-Z0-9]|\\+|-|\\.)*)";
        static final String mark = "-|_|\\.|!|~|\\*|'|\\(|\\)";
        static final String unreserved = "[a-zA-Z0-9]" + "|" + mark;
        static final String escaped = "%[a-fA-F0-9]{2}";
        static final String pChar = unreserved+"|"+escaped+"|:|@|&|=|\\+|\\$|,";
        static final String pathSegment = "(?:"+pChar+")*(?:;(?:"+pChar+")*)*";
        static final String absPath = "(?:/"+pathSegment+")+";
        static final String reserved = ";|/|\\?|:|@|&|=|\\+|\\$|,";
        static final String uric = reserved+"|"+unreserved+"|"+escaped;
        static final String hierPath = absPath+"(?:\\?(?:"+uric+")*)*";
        static final String URIpattern = "servlet:((?:"+scheme+":)??"+hierPath+"(?:#(?:"+uric+")*)*)";
        static final Pattern compiledURIpattern = Pattern.compile(URIpattern);
    }

    protected String proccessLine(String line, int lineNumber) throws ProcessingException {
        if (getLogger().isDebugEnabled())
            getLogger().debug("Processing line: " + line);

        Matcher matcher = URIregexp.compiledURIpattern.matcher(line);
        StringBuffer sb = new StringBuffer(line.length());
        while (matcher.find()) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Processing link: " + matcher.group(0));
            String link = matcher.group(1);

            Object replacement;
            try {
                replacement = inputModule.getAttribute(link, null, objectModel);
            } catch (ConfigurationException e) {
                throw ProcessingException.throwLocated("Failed to obtain attribute from input module", e,
                        new LocationImpl(null, inputSource.getURI(), lineNumber, matcher.start()));
            }

            if (!(replacement instanceof String))
                throw new ProcessingException("Attribute named '" + link + "' obtained from 'servlet' input module has to be String object.",
                        new LocationImpl(null, inputSource.getURI(), lineNumber, matcher.start()));

            matcher.appendReplacement(sb, (String)replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Returns the mime-type of the resource in process.
     */
    public String getMimeType() {
        Context ctx = ObjectModelHelper.getContext(objectModel);
        if (ctx != null) {
            final String mimeType = ctx.getMimeType(source);
            if (mimeType != null) {
                return mimeType;
            }
        }

        return inputSource.getMimeType();
    }

	public Serializable getKey() {
		return inputSource.getURI();
	}

	public SourceValidity getValidity() {
		return inputSource.getValidity();
	}

}
