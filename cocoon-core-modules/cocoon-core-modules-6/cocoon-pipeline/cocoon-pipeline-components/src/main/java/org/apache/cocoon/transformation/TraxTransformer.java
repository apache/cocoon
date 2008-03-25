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

package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.apache.excalibur.xml.xslt.XSLTProcessorException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.components.xslt.TraxErrorListener;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;
import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.SAXException;

/**
 * The XSLT stylesheet processor.
 *
 * <p>This Transformer is used to transform the incoming SAX stream using
 * a TrAXProcessor. Use the following sitemap declarations to define, configure
 * and parameterize it:
 *
 * <p><b>In the map:sitemap/map:components/map:transformers:</b>
 * <pre>
 * &lt;map:transformer name="xslt" src="org.apache.cocoon.transformation.TraxTransformer"&gt;<br>
 *   &lt;use-request-parameters&gt;false&lt;/use-request-parameters&gt;
 *   &lt;use-browser-capabilities-db&gt;false&lt;/use-browser-capabilities-db&gt;
 *   &lt;use-session-info&gt;false&lt;/use-session-info&gt;
 *   &lt;xslt-processor-role&gt;xslt&lt;/xslt-processor-role&gt;
 *   &lt;check-includes&gt;true&lt;/check-includes&gt;
 * &lt;/map:transformer&gt;
 * </pre>
 *
 * The &lt;use-request-parameter&gt; configuration forces the transformer to make all
 * request parameters available in the XSLT stylesheet. Note that this has
 * implications for caching of the generated output of this transformer.<br>
 * This property is false by default.
 * <p>
 * The &lt;use-cookies&gt; configuration forces the transformer to make all
 * cookies from the request available in the XSLT stylesheets.
 * Note that this has implications for caching of the generated output of this
 * transformer.<br>
 * This property is false by default.
 * <p>
 * The &lt;use-session-info&gt; configuration forces the transformer to make all
 * of the session information available in the XSLT stylesheetas.<br>
 * These infos are (boolean values are "true" or "false" strings: session-is-new,
 * session-id-from-cookie, session-id-from-url, session-valid, session-id.<br>
 * This property is false by default.
 *
 * <p>Note that this has implications for caching of the generated output of
 * this transformer.<br>
 *
 *
 * The &lt;xslt-processor-role&gt; configuration allows to specify the TrAX processor (defined in
 * the cocoon.xconf) that will be used to obtain the XSLT processor. This allows to have
 * several XSLT processors in the configuration (e.g. Xalan, XSLTC, Saxon, ...) and choose
 * one or the other depending on the needs of stylesheet specificities.<br>
 * If no processor is specified, this transformer will use the XSLT implementation
 * that Cocoon uses internally.
 *
 * The &lt;check-includes&gt; configuration specifies if the included stylesheets are
 * also checked for changes during caching. If this is set to true (default), the
 * included stylesheets are also checked for changes; if this is set to false, only
 * the main stylesheet is checked. Setting this to false improves the performance,
 * and should be used whenever no includes are in the stylesheet. However, if
 * you have includes, you have to be careful when changing included stylesheets
 * as the changes might not take effect immediately. You should touch the main
 * stylesheet as well.
 *
 * <p>
 * <b>In a map:sitemap/map:pipelines/map:pipeline:</b><br>
 * <pre>
 * &lt;map:transform type="xslt" src="stylesheets/yours.xsl"&gt;<br>
 *   &lt;parameter name="myparam" value="myvalue"/&gt;
 * &lt;/map:transform&gt;
 * </pre>
 * All &lt;parameter&gt; declarations will be made available in the XSLT stylesheet as
 * xsl:variables.
 *
 * @cocoon.sitemap.component.documentation
 * The XSLT stylesheet processor
 * @cocoon.sitemap.component.name   xslt
 * @cocoon.sitemap.component.documentation.caching Yes.
 * Uses the last modification date of the xslt document for validation
 * @cocoon.sitemap.component.pooling.max  32
 *
 * @version SVN $Id$
 */
public class TraxTransformer extends AbstractTransformer
                             implements Serviceable, Configurable, CacheableProcessingComponent,
                                        Disposable {

    /** The service manager instance (protected because used by subclasses) */
    protected ServiceManager manager;

    /** The object model (protected because used by subclasses) */
    protected Map objectModel;

    /** Logicsheet parameters (protected because used by subclasses) */
    protected Map logicSheetParameters;

    /** Should we make the request parameters available in the stylesheet? (default is off) */
    private boolean useParameters = false;
    private boolean _useParameters = false;

    /** Should we make the cookies available in the stylesheet? (default is off) */
    private boolean useCookies = false;
    private boolean _useCookies = false;

    /** Should we info about the session available in the stylesheet? (default is off) */
    private boolean useSessionInfo = false;
    private boolean _useSessionInfo = false;

    /** Do we check included stylesheets for changes? */
    private boolean checkIncludes = true;

    /** The trax TransformerHandler */
    protected TransformerHandler transformerHandler;

    /** The validity of the Transformer */
    protected SourceValidity transformerValidity;

    /** The Source */
    private Source inputSource;
    /** The parameters */
    private Parameters par;
    /** The source resolver */
    private SourceResolver resolver;

    /** Default source, used to create specialized transformers by configuration */
    private String defaultSrc;

    /** The XSLTProcessor */
    private XSLTProcessor xsltProcessor;

    /** Did we finish the processing (is endDocument() called) */
    private boolean finishedDocument = false;

    /** Xalan's DTMManager.getIncremental() method. See recycle() method to see what we need this for. */
    private Method xalanDtmManagerGetIncrementalMethod;

    /** Exception that might occur during setConsumer */
    private SAXException exceptionDuringSetConsumer;

    /** The error listener used by the stylesheet */
    private TraxErrorListener errorListener;

    /**
     * Configure this transformer.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        Configuration child;

        child = conf.getChild("use-request-parameters");
        this.useParameters = child.getValueAsBoolean(false);
        this._useParameters = this.useParameters;

        child = conf.getChild("use-cookies");
        this.useCookies = child.getValueAsBoolean(false);
        this._useCookies = this.useCookies;

        child = conf.getChild("use-session-info");
        this.useSessionInfo = child.getValueAsBoolean(false);
        this._useSessionInfo = this.useSessionInfo;

        child = conf.getChild("xslt-processor-role");
        String xsltProcessorRole = child.getValue(XSLTProcessor.ROLE);
        if (!xsltProcessorRole.startsWith(XSLTProcessor.ROLE)) {
            xsltProcessorRole = XSLTProcessor.ROLE + '/' + xsltProcessorRole;
        }

        child = conf.getChild("check-includes");
        this.checkIncludes = child.getValueAsBoolean(this.checkIncludes);

        child = conf.getChild("default-src",false);
        if(child!=null) {
            this.defaultSrc = child.getValue();
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Use parameters is " + this.useParameters);
            getLogger().debug("Use cookies is " + this.useCookies);
            getLogger().debug("Use session info is " + this.useSessionInfo);
            getLogger().debug("Use TrAX Processor " + xsltProcessorRole);
            getLogger().debug("Check for included stylesheets is " + this.checkIncludes);
            getLogger().debug("Default source = " + this.defaultSrc);
        }

        try {
            this.xsltProcessor = (XSLTProcessor) this.manager.lookup(xsltProcessorRole);
        } catch (ServiceException e) {
            throw new ConfigurationException("Cannot load XSLT processor", e);
        }

        try {
            // see the recyle() method to see what we need this for
            Class dtmManagerClass = Class.forName("org.apache.xml.dtm.DTMManager");
            xalanDtmManagerGetIncrementalMethod = dtmManagerClass.getMethod("getIncremental", null);
        } catch (ClassNotFoundException e) {
            // do nothing -- user does not use xalan, so we don't need the dtm manager
        } catch (NoSuchMethodException e) {
            throw new ConfigurationException("Was not able to get getIncremental method from Xalan's DTMManager.", e);
        }
    }

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Set the <code>SourceResolver</code>, the <code>Map</code> with
     * the object model, the source and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws SAXException, ProcessingException, IOException {

        if(src==null && defaultSrc!=null) {
            if(getLogger().isDebugEnabled()) {
                getLogger().debug("src is null, using default source " + defaultSrc);
            }
            src = defaultSrc;
        }

        if (src == null) {
            throw new ProcessingException("Stylesheet URI can't be null");
        }

        this.par = par;
        this.objectModel = objectModel;
        this.resolver = resolver;
        try {
            this.inputSource = resolver.resolveURI(src);
        } catch (SourceException se) {
            throw SourceUtil.handle("Unable to resolve " + src, se);
        }
        _useParameters = par.getParameterAsBoolean("use-request-parameters", this.useParameters);
        _useCookies = par.getParameterAsBoolean("use-cookies", this.useCookies);
        _useSessionInfo = par.getParameterAsBoolean("use-session-info", this.useSessionInfo);
        final boolean _checkIncludes = par.getParameterAsBoolean("check-includes", this.checkIncludes);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Using stylesheet: '" + this.inputSource.getURI() + "' in " + this);
            getLogger().debug("Use parameters is " + this._useParameters);
            getLogger().debug("Use cookies is " + this._useCookies);
            getLogger().debug("Use session info is " + this._useSessionInfo);
            getLogger().debug("Check for included stylesheets is " + _checkIncludes);
        }

        // Get a Transformer Handler if we check for includes
        // If we don't check the handler is get during setConsumer()
        try {
            if ( _checkIncludes ) {
                XSLTProcessor.TransformerHandlerAndValidity handlerAndValidity =
                        this.xsltProcessor.getTransformerHandlerAndValidity(this.inputSource, null);
                this.transformerHandler = handlerAndValidity.getTransfomerHandler();
                this.transformerValidity = handlerAndValidity.getTransfomerValidity();
            } else {
                this.transformerValidity = this.inputSource.getValidity();
            }
        } catch (XSLTProcessorException se) {
            throw new ProcessingException("Unable to get transformer handler for " + this.inputSource.getURI(), se);
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public Serializable getKey() {
        Map map = getLogicSheetParameters();
        if (map == null) {
            return this.inputSource.getURI();
        }

        StringBuffer sb = new StringBuffer();
        sb.append(this.inputSource.getURI());
        Set entries = map.entrySet();
        for(Iterator i=entries.iterator(); i.hasNext();){
            sb.append(';');
            Map.Entry entry = (Map.Entry)i.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        //
        // VG: Key is generated using parameter/value pairs,
        // so this information does not need to be verified again
        // (if parameter added/removed or value changed, key should
        // change also), only stylesheet's validity is included.
        //
        return this.transformerValidity;
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     */
    public void setConsumer(XMLConsumer consumer) {

        if ( this.transformerHandler == null ) {
            try {
                this.transformerHandler = this.xsltProcessor.getTransformerHandler(this.inputSource);
            } catch (XSLTProcessorException se) {
                // the exception will be thrown during startDocument()
                this.exceptionDuringSetConsumer =
                   new SAXException("Unable to get transformer handler for " + this.inputSource.getURI(), se);
                return;
            }
        }
        final Map map = getLogicSheetParameters();
        if (map != null) {
            final javax.xml.transform.Transformer transformer = this.transformerHandler.getTransformer();
            final Iterator iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry entry = (Entry) iterator.next();
                transformer.setParameter((String)entry.getKey(), entry.getValue());
            }
        }

        super.setContentHandler(this.transformerHandler);
        super.setLexicalHandler(this.transformerHandler);
        // Is there even single implementation of LogEnabled TransformerHandler?
        if (this.transformerHandler instanceof LogEnabled) {
        	((LogEnabled) this.transformerHandler).enableLogging(new CLLoggerWrapper(getLogger()));
        }
        // According to TrAX specs, all TransformerHandlers are LexicalHandlers
        final SAXResult result = new SAXResult(consumer);
        result.setLexicalHandler(consumer);
        this.transformerHandler.setResult(result);

        this.errorListener = new TraxErrorListener(this.inputSource.getURI());
        this.transformerHandler.getTransformer().setErrorListener(this.errorListener);
    }

    /**
     * Get the parameters for the logicsheet
     */
    protected Map getLogicSheetParameters() {
        if (this.logicSheetParameters != null) {
            return this.logicSheetParameters;
        }

        HashMap map = null;
        if (par != null) {
            String[] params = par.getNames();
            if (params != null) {
                for(int i = 0; i < params.length; i++) {
                    String name = params[i];
                    if (isValidXSLTParameterName(name)) {
                        String value = par.getParameter(name,null);
                        if (value != null) {
                            if (map == null) {
                                map = new HashMap(params.length);
                            }
                            map.put(name,value);
                        }
                    }
                }
            }
        }

        if (this._useParameters) {
            Request request = ObjectModelHelper.getRequest(objectModel);

            Enumeration parameters = request.getParameterNames();
            if (parameters != null) {
                while (parameters.hasMoreElements()) {
                    String name = (String) parameters.nextElement();
                    if (isValidXSLTParameterName(name)) {
                        String value = request.getParameter(name);
                        if (map == null) {
                            map = new HashMap();
                        }
                        map.put(name,value);
                    }
                }
            }
        }

        if (this._useSessionInfo) {
            final Request request = ObjectModelHelper.getRequest(objectModel);
            if (map == null) {
                map = new HashMap(6);
            }

            final HttpSession session = request.getSession(false);
            if (session != null) {
                map.put("session-available", "true");
                map.put("session-is-new", BooleanUtils.toStringTrueFalse(session.isNew()));
                map.put("session-id-from-cookie", BooleanUtils.toStringTrueFalse(request.isRequestedSessionIdFromCookie()));
                map.put("session-id-from-url", BooleanUtils.toStringTrueFalse(request.isRequestedSessionIdFromURL()));
                map.put("session-valid", BooleanUtils.toStringTrueFalse(request.isRequestedSessionIdValid()));
                map.put("session-id", session.getId());
            } else {
                map.put("session-available", "false");
            }
        }

        if (this._useCookies) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            Cookie cookies[] = request.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    String name = cookies[i].getName();
                    if (isValidXSLTParameterName(name)) {
                        String value = cookies[i].getValue();
                        if (map == null) {
                            map = new HashMap(cookies.length);
                        }
                        map.put(name,value);
                    }
                }
            }
        }
        this.logicSheetParameters = map;
        return this.logicSheetParameters;
    }

    /**
     * Test if the name is a valid parameter name for XSLT
     */
    static boolean isValidXSLTParameterName(String name) {
        if (name.length() == 0) {
            return false;
        }

        char c = name.charAt(0);
        if (!(Character.isLetter(c) || c == '_')) {
            return false;
        }

        for (int i = name.length()-1; i > 1; i--) {
            c = name.charAt(i);
            if (!(Character.isLetterOrDigit(c) ||
                    c == '-' ||
                    c == '_' ||
                    c == '.')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Disposable
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.xsltProcessor);
            this.xsltProcessor = null;
            this.manager = null;
        }
    }

    /**
     * Recyclable
     */
    public void recycle() {
        this.objectModel = null;
        if (this.inputSource != null) {
            this.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        this.resolver = null;
        this.par = null;
        if (!this.finishedDocument && transformerHandler != null) {
            // This situation will only occur if an exception occured during pipeline execution.
            // If Xalan is used in incremental mode, it is important that endDocument is called, otherwise
            // the thread on which it runs the transformation will keep waiting.
            // However, calling endDocument will cause the pipeline to continue executing, and thus the
            // serializer will write output to the outputstream after what's already there (the error page),
            // see also bug 13186.
            if (xalanDtmManagerGetIncrementalMethod != null
                && transformerHandler.getClass().getName().equals("org.apache.xalan.transformer.TransformerHandlerImpl")) {
                try {
                    final boolean incremental = ((Boolean)xalanDtmManagerGetIncrementalMethod.invoke(null, null)).booleanValue();
                    if (incremental) {
                        super.endDocument();
                    }
                } catch (Exception ignore) {}
            }
        }
        this.finishedDocument = true;
        this.logicSheetParameters = null;
        this.transformerHandler = null;
        this.transformerValidity = null;
        this.exceptionDuringSetConsumer = null;
        this.errorListener = null;
        super.recycle();
    }

    /**
     * Fix for stopping hanging threads of Xalan
     */
    public void endDocument()
    throws SAXException {
        try {
            super.endDocument();
        } catch(Exception e) {

            Throwable realEx = this.errorListener.getThrowable();
            if (realEx == null) realEx = e;

            if (realEx instanceof RuntimeException) {
                throw (RuntimeException)realEx;
            }

            if (realEx instanceof SAXException) {
                throw (SAXException)realEx;
            }

            if (realEx instanceof Error) {
                throw (Error)realEx;
            }

            throw new NestableRuntimeException(realEx);
        }
        this.finishedDocument = true;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        // did an exception occur during setConsumer?
        // if so, throw it here
        if ( this.exceptionDuringSetConsumer != null ) {
            throw this.exceptionDuringSetConsumer;
        }
        this.finishedDocument = false;
        super.startDocument();
    }
}
